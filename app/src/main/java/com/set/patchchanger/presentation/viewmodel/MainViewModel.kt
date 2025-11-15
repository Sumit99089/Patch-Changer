package com.set.patchchanger.presentation.viewmodel

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.set.patchchanger.domain.model.AppSettings
import com.set.patchchanger.domain.model.AudioLibraryItem
import com.set.patchchanger.domain.model.MidiConnectionState
import com.set.patchchanger.domain.model.PatchData
import com.set.patchchanger.domain.model.PatchSlot
import com.set.patchchanger.domain.model.SamplePad
import com.set.patchchanger.domain.model.SearchResult
import com.set.patchchanger.domain.repository.AudioLibraryRepository
import com.set.patchchanger.domain.repository.MidiRepository
import com.set.patchchanger.domain.repository.PatchRepository
import com.set.patchchanger.domain.repository.SampleRepository
import com.set.patchchanger.domain.repository.SettingsRepository
import com.set.patchchanger.domain.usecase.ExportDataUseCase
import com.set.patchchanger.domain.usecase.GetPerformancesUseCase
import com.set.patchchanger.domain.usecase.ImportDataUseCase
import com.set.patchchanger.domain.usecase.NavigateBankUseCase
import com.set.patchchanger.domain.usecase.NavigatePageUseCase
import com.set.patchchanger.domain.usecase.SelectPatchUseCase
import com.set.patchchanger.domain.usecase.SwapSlotsUseCase
import com.set.patchchanger.domain.usecase.UpdateTransposeUseCase
import com.set.patchchanger.presentation.viewmodel.event.MainEvent
import com.set.patchchanger.presentation.viewmodel.event.UiEvent
import com.set.patchchanger.presentation.viewmodel.state.InternalState
import com.set.patchchanger.presentation.viewmodel.state.MainUiState
import com.set.patchchanger.ui.theme.getDefaultColors
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the main screen.
 *
 * ViewModel survives configuration changes (like rotation).
 * It holds UI state and exposes business logic to the UI.
 *
 * @HiltViewModel enables dependency injection in ViewModel
 */
@HiltViewModel
class MainViewModel @Inject constructor(
    private val patchRepository: PatchRepository,
    private val settingsRepository: SettingsRepository,
    private val midiRepository: MidiRepository,
    private val sampleRepository: SampleRepository,
    private val audioLibraryRepository: AudioLibraryRepository,
    private val selectPatchUseCase: SelectPatchUseCase,
    private val swapSlotsUseCase: SwapSlotsUseCase,
    private val updateTransposeUseCase: UpdateTransposeUseCase,
    private val navigateBankUseCase: NavigateBankUseCase,
    private val navigatePageUseCase: NavigatePageUseCase,
    private val exportDataUseCase: ExportDataUseCase,
    private val importDataUseCase: ImportDataUseCase,
    private val getPerformancesUseCase: GetPerformancesUseCase,
    @ApplicationContext private val context: Context
) : ViewModel() {

    // Internal state for combining
    private val _internalState = MutableStateFlow(InternalState())
    val internalState = _internalState.asStateFlow()

    private var copiedSlot: PatchSlot? = null

    /**
     * Helper data class to combine the first 5 flows, as `combine` only supports up to 5 arguments.
     */
    private data class CombinedData(
        val patchData: PatchData,
        val settings: AppSettings,
        val samples: List<SamplePad>,
        val midiState: MidiConnectionState,
        val library: List<AudioLibraryItem>
    )

    /**
     * StateFlow for UI state.
     *
     * We combine the first 5 flows into `combinedDataFlow`,
     * then combine that result with `_internalState`.
     */
    private val combinedDataFlow = combine(
        patchRepository.observePatchData(),
        settingsRepository.observeSettings(),
        sampleRepository.observeSamples(),
        midiRepository.observeConnectionState(),
        audioLibraryRepository.observeLibrary()
    ) { patchData, settings, samples, midiState, library ->
        CombinedData(patchData, settings, samples, midiState, library)
    }

    val uiState: StateFlow<MainUiState> = combine(
        combinedDataFlow,
        _internalState
    ) { data, internal ->
        MainUiState.Success(
            patchData = data.patchData,
            settings = data.settings,
            samples = data.samples,
            midiState = data.midiState,
            audioLibrary = data.library,
            searchQuery = internal.searchQuery,
            searchResults = internal.searchResults,
            showResetDialog = internal.showResetDialog,
            showBankPageNameDialog = internal.showBankPageNameDialog,
            editingSample = internal.editingSample,
            showAudioLibrary = internal.showAudioLibrary,
            audioLibrarySampleIdTarget = internal.audioLibrarySampleIdTarget,
            slotToPaste = internal.slotToPaste,
            slotToClear = internal.slotToClear,
            slotToSwap = internal.slotToSwap,
            slotToEditColor = internal.slotToEditColor,
            sampleToEditColor = internal.sampleToEditColor,
            // Performance Browser State
            showPerformanceBrowser = internal.showPerformanceBrowser,
            slotToEditPerformance = internal.slotToEditPerformance,
            performanceCategories = internal.performanceCategories,
            performanceSelectedCategory = internal.performanceSelectedCategory,
            performanceBanks = internal.performanceBanks,
            performanceSelectedBankIndex = internal.performanceSelectedBankIndex,
            performances = internal.performances,
            performanceSearchQuery = internal.performanceSearchQuery
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = MainUiState.Loading
    )

    /**
     * SharedFlow for one-time events (like showing snackbars).
     *
     * SharedFlow doesn't hold state, just emits events.
     */
    private val _events = MutableSharedFlow<UiEvent>()
    val events: SharedFlow<UiEvent> = _events.asSharedFlow()

    init {
        // **** CHANGE IS HERE ****
        // Try to auto-connect on startup
        onEvent(MainEvent.ConnectMidi)

        // Observe changes to the search query
        _internalState
            .onEach {
                // Don't search if query is too short
                if (it.searchQuery.length < 2) {
                    _internalState.update { state -> state.copy(searchResults = emptyList()) }
                } else {
                    val results = patchRepository.searchSlots(it.searchQuery)
                    _internalState.update { state -> state.copy(searchResults = results) }
                }
            }
            .launchIn(viewModelScope)
    }

    /**
     * Handles UI events.
     */
    fun onEvent(event: MainEvent) {
        viewModelScope.launch {
            when (event) {
                is MainEvent.SelectSlot -> {
                    val slot = selectPatchUseCase(event.slotId)
                    slot?.assignedSample?.let { sampleId ->
                        if (sampleId >= 0) {
                            sampleRepository.triggerSampleAudio(sampleId)
                        }
                    }
                }

                is MainEvent.TriggerSample -> {
                    sampleRepository.triggerSampleAudio(event.sampleId)
                }

                is MainEvent.UpdateSlot -> {
                    patchRepository.updateSlot(event.slot)
                }

                is MainEvent.SwapSlots -> {
                    swapSlotsUseCase(event.slot1Id, event.slot2Id)
                    _internalState.update { it.copy(slotToSwap = null) }
                }

                is MainEvent.NavigateBank -> {
                    navigateBankUseCase(event.direction)
                }

                is MainEvent.NavigatePage -> {
                    navigatePageUseCase(event.direction)
                }

                is MainEvent.UpdateTranspose -> {
                    updateTransposeUseCase(event.delta)
                }

                is MainEvent.ResetTranspose -> {
                    updateTransposeUseCase.reset()
                }

                is MainEvent.UpdateMidiChannel -> {
                    settingsRepository.updateMidiChannel(event.channel)
                }

                is MainEvent.UpdateTheme -> {
                    settingsRepository.updateTheme(event.theme)
                }

                is MainEvent.UpdateBankName -> {
                    patchRepository.updateBankName(event.index, event.name)
                }

                is MainEvent.UpdatePageName -> {
                    patchRepository.updatePageName(event.index, event.name)
                }

                is MainEvent.ConnectMidi -> {
                    midiRepository.connect()
                }

                is MainEvent.DisconnectMidi -> {
                    midiRepository.disconnect()
                }

                is MainEvent.ResetData -> {
                    patchRepository.resetToDefaults()
                    sampleRepository.resetSamples()
                    _internalState.update { it.copy(showResetDialog = false) }
                    _events.emit(UiEvent.ShowMessage("Data reset to defaults"))
                }

                is MainEvent.ImportData -> {
                    val success = importDataUseCase(event.jsonData)
                    _events.emit(
                        if (success) UiEvent.ShowMessage("Data imported successfully")
                        else UiEvent.ShowMessage("Failed to import data")
                    )
                }

                is MainEvent.UpdateSample -> {
                    sampleRepository.updateSample(event.sample)
                }

                is MainEvent.ClearSampleAudio -> {
                    sampleRepository.clearSampleAudio(event.sampleId)
                    // Update editingSample state if it was the one being edited
                    _internalState.update {
                        if (it.editingSample?.id == event.sampleId) {
                            it.copy(
                                editingSample = it.editingSample.copy(
                                    audioFileName = null,
                                    sourceName = null,
                                    name = "S${event.sampleId + 1}"
                                )
                            )
                        } else {
                            it
                        }
                    }
                }

                // --- Search Events ---
                is MainEvent.UpdateSearchQuery -> {
                    _internalState.update { it.copy(searchQuery = event.query) }
                }

                is MainEvent.GoToSearchResult -> {
                    // Navigate to the correct page
                    settingsRepository.updateBankIndex(event.result.bankIndex)
                    settingsRepository.updatePageIndex(event.result.pageIndex)
                    // Select the slot
                    selectPatchUseCase(event.result.slot.id)
                    // Clear search
                    _internalState.update { it.copy(searchQuery = "", searchResults = emptyList()) }
                }

                // Dialog Controls
                is MainEvent.ShowResetDialog -> _internalState.update { it.copy(showResetDialog = event.show) }
                is MainEvent.ShowBankPageNameDialog -> _internalState.update {
                    it.copy(
                        showBankPageNameDialog = event.show
                    )
                }
                is MainEvent.ShowEditSampleDialog -> _internalState.update {
                    it.copy(
                        editingSample = event.sample
                    )
                }

                is MainEvent.ShowPasteConfirmDialog -> _internalState.update {
                    it.copy(
                        slotToPaste = event.slot
                    )
                }

                is MainEvent.ShowClearConfirmDialog -> _internalState.update {
                    it.copy(
                        slotToClear = event.slot
                    )
                }

                is MainEvent.ShowSwapDialog -> _internalState.update { it.copy(slotToSwap = event.slot) }
                is MainEvent.ShowSlotColorDialog -> _internalState.update {
                    it.copy(
                        slotToEditColor = event.slot
                    )
                }

                is MainEvent.ShowSampleColorDialog -> _internalState.update {
                    it.copy(
                        sampleToEditColor = event.sample
                    )
                }

                is MainEvent.ShowAudioLibrary -> _internalState.update {
                    it.copy(
                        showAudioLibrary = event.show,
                        audioLibrarySampleIdTarget = if (event.show) event.sampleId else -1
                    )
                }

                // Slot Actions
                is MainEvent.CopySlot -> {
                    copiedSlot = event.slot
                    _events.emit(UiEvent.ShowMessage("Slot '${event.slot.getDisplayName()}' copied"))
                }

                is MainEvent.PasteSlot -> {
                    val source = copiedSlot
                    if (source == null) {
                        _events.emit(UiEvent.ShowMessage("Nothing to paste"))
                    } else {
                        patchRepository.updateSlot(event.targetSlot.copyDataFrom(source))
                        _events.emit(UiEvent.ShowMessage("Pasted over '${event.targetSlot.getDisplayName()}'"))
                    }
                    _internalState.update { it.copy(slotToPaste = null) }
                }

                is MainEvent.ClearSlot -> {
                    val defaultColors = getDefaultColors()
                    val defaultSlot =
                        PatchSlot.createDefault(event.slot.id, defaultColors[event.slot.id % 16])
                    patchRepository.updateSlot(defaultSlot)
                    _internalState.update { it.copy(slotToClear = null) }
                }

                // Sample File Events
                is MainEvent.LoadSampleFile -> {
                    _events.emit(UiEvent.RequestFilePicker)
                }

                is MainEvent.SetSampleFile -> {
                    val sampleId = _internalState.value.editingSample?.id ?: return@launch
                    val fileName =
                        sampleRepository.saveSampleAudioFromUri(sampleId, event.uri, event.name)
                    _internalState.update {
                        it.copy(
                            editingSample = it.editingSample?.copy(
                                audioFileName = fileName,
                                sourceName = event.name,
                                name = event.name.substringBeforeLast('.')
                            )
                        )
                    }
                }

                is MainEvent.AddFileToLibrary -> {
                    val item = audioLibraryRepository.addAudioFile(event.uri, event.name)
                    _events.emit(UiEvent.ShowMessage("Added '${item.name}' to library"))
                }

                is MainEvent.DeleteFromAudioLibrary -> {
                    audioLibraryRepository.deleteAudioFile(event.item)
                }

                is MainEvent.SelectSampleFromLibrary -> {
                    val sampleId = _internalState.value.audioLibrarySampleIdTarget
                    if (sampleId != -1) {
                        val fileName =
                            sampleRepository.saveSampleAudioFromLibrary(sampleId, event.item)
                        _internalState.update {
                            it.copy(
                                editingSample = it.editingSample?.copy(
                                    audioFileName = fileName,
                                    sourceName = event.item.name,
                                    name = event.item.name.substringBeforeLast('.')
                                ),
                                showAudioLibrary = false,
                                audioLibrarySampleIdTarget = -1
                            )
                        }
                    }
                }

                // --- Performance Browser Events ---
                is MainEvent.ShowPerformanceBrowser -> {
                    val categories = getPerformancesUseCase.getCategories()
                    _internalState.update {
                        it.copy(
                            showPerformanceBrowser = true,
                            slotToEditPerformance = event.slot,
                            performanceCategories = categories,
                            performanceSelectedCategory = null,
                            performanceBanks = emptyList(),
                            performanceSelectedBankIndex = -1,
                            performances = emptyList(),
                            performanceSearchQuery = ""
                        )
                    }
                }

                is MainEvent.HidePerformanceBrowser -> {
                    _internalState.update {
                        it.copy(
                            showPerformanceBrowser = false,
                            slotToEditPerformance = null
                        )
                    }
                }

                is MainEvent.SelectPerformanceCategory -> {
                    val banks = getPerformancesUseCase.getBanks(event.category)
                    _internalState.update {
                        it.copy(
                            performanceSelectedCategory = event.category,
                            performanceBanks = banks,
                            performanceSelectedBankIndex = -1,
                            performances = emptyList()
                        )
                    }
                }

                is MainEvent.SelectPerformanceBank -> {
                    val category = _internalState.value.performanceSelectedCategory ?: return@launch
                    val perfs = getPerformancesUseCase(category, event.bankIndex)
                    _internalState.update {
                        it.copy(
                            performanceSelectedBankIndex = event.bankIndex,
                            performances = perfs
                        )
                    }
                }

                is MainEvent.SelectPerformance -> {
                    val slot = _internalState.value.slotToEditPerformance ?: return@launch
                    val updatedSlot = slot.copy(
                        msb = event.performance.msb,
                        lsb = event.performance.lsb,
                        pc = event.performance.pc,
                        performanceName = event.performance.name
                    )
                    patchRepository.updateSlot(updatedSlot)
                    _internalState.update {
                        it.copy(
                            showPerformanceBrowser = false,
                            slotToEditPerformance = null,
                            // Update the slot in the dialog as well
                            slotToEditColor = updatedSlot
                        )
                    }
                }

                is MainEvent.UpdatePerformanceSearch -> {
                    _internalState.update { it.copy(performanceSearchQuery = event.query) }
                }
            }
        }
    }

    /**
     * Exports data to JSON.
     */
    fun exportData(): Flow<String> = flow {
        emit(exportDataUseCase())
    }

    /**
     * Searches patches globally.
     */
    fun searchPatches(query: String): Flow<List<SearchResult>> = flow {
        if (query.length < 2) {
            emit(emptyList())
        } else {
            emit(patchRepository.searchSlots(query))
        }
    }

    fun getFileName(uri: Uri): String {
        var name = "unknown"
        val cursor = context.contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (nameIndex != -1) {
                    name = it.getString(nameIndex)
                }
            }
        }
        return name
    }

    fun cleanup() {
        midiRepository.cleanup()
        sampleRepository.cleanup()
    }
}