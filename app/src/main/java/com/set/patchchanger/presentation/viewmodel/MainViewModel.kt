package com.set.patchchanger.presentation.viewmodel

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.set.patchchanger.data.local.FileManager
import com.set.patchchanger.domain.model.AppSettings
import com.set.patchchanger.domain.model.AudioLibraryItem
import com.set.patchchanger.domain.model.MidiConnectionState
import com.set.patchchanger.domain.model.PatchData
import com.set.patchchanger.domain.model.PatchSlot
import com.set.patchchanger.domain.model.SamplePad
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

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
    private val fileManager: FileManager,
    @param:ApplicationContext private val context: Context
) : ViewModel() {

    private val _internalState = MutableStateFlow(InternalState())
    val internalState = _internalState.asStateFlow()

    private var copiedSlot: PatchSlot? = null

    private data class CombinedData(
        val patchData: PatchData,
        val settings: AppSettings,
        val samples: List<SamplePad>,
        val midiState: MidiConnectionState,
        val library: List<AudioLibraryItem>
    )

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

    private val _events = MutableSharedFlow<UiEvent>()
    val events: SharedFlow<UiEvent> = _events.asSharedFlow()

    init {
        onEvent(MainEvent.ConnectMidi)

        _internalState
            .onEach {
                if (it.searchQuery.length < 2) {
                    _internalState.update { state -> state.copy(searchResults = emptyList()) }
                } else {
                    val results = patchRepository.searchSlots(it.searchQuery)
                    _internalState.update { state -> state.copy(searchResults = results) }
                }
            }
            .launchIn(viewModelScope)
    }

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

                // --- FIXED: Import/Export Logic for Android 11+ ---
                is MainEvent.RequestExportData -> {
                    // We attempt to save to Documents first, but we also trigger the system saver
                    // This is handled by the FileManager for Q+, but fallback to Share/System Picker is safer.
                    val timestamp =
                        SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
                    val filename = "modx_backup_$timestamp.json"
                    val jsonData = exportDataUseCase()
                    val success = fileManager.saveJsonToDocuments(filename, jsonData)
                    if (success) {
                        _events.emit(UiEvent.ShowMessage("Saved to Documents/PatchChanger/$filename"))
                    } else {
                        // Fallback to system picker if auto-save fails
                        _events.emit(UiEvent.RequestSaveFile)
                    }
                }

                is MainEvent.RequestImportData -> {
                    // DIRECTLY Open System Picker.
                    // Reading directory listing is restricted on Android 11+.
                    _events.emit(UiEvent.RequestLoadFile)
                }

                is MainEvent.ShowLoadFileDialog -> {
                    // Deprecated / Unused in new flow
                }

                is MainEvent.LoadSelectedFile -> {
                    // Deprecated / Unused in new flow
                }

                is MainEvent.PerformExport -> {
                    val jsonData = exportDataUseCase()
                    try {
                        withContext(Dispatchers.IO) {
                            context.contentResolver.openOutputStream(event.uri)?.use { output ->
                                output.write(jsonData.toByteArray())
                            }
                        }
                        _events.emit(UiEvent.ShowMessage("Data saved successfully"))
                    } catch (e: Exception) {
                        _events.emit(UiEvent.ShowMessage("Failed to save data: ${e.message}"))
                    }
                }

                is MainEvent.PerformImport -> {
                    try {
                        val jsonData = withContext(Dispatchers.IO) {
                            context.contentResolver.openInputStream(event.uri)?.use { input ->
                                BufferedReader(InputStreamReader(input)).readText()
                            }
                        }
                        if (jsonData != null) {
                            val success = importDataUseCase(jsonData)
                            _events.emit(
                                if (success) UiEvent.ShowMessage("Data imported successfully")
                                else UiEvent.ShowMessage("Failed to parse data")
                            )
                        }
                    } catch (e: Exception) {
                        _events.emit(UiEvent.ShowMessage("Failed to read file: ${e.message}"))
                    }
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

                is MainEvent.UpdateSearchQuery -> {
                    _internalState.update { it.copy(searchQuery = event.query) }
                }

                is MainEvent.GoToSearchResult -> {
                    settingsRepository.updateBankIndex(event.result.bankIndex)
                    settingsRepository.updatePageIndex(event.result.pageIndex)
                    selectPatchUseCase(event.result.slot.id)
                    _internalState.update { it.copy(searchQuery = "", searchResults = emptyList()) }
                }

                is MainEvent.ShowResetDialog -> _internalState.update { it.copy(showResetDialog = event.show) }
                is MainEvent.ShowBankPageNameDialog -> _internalState.update {
                    it.copy(showBankPageNameDialog = event.show)
                }

                is MainEvent.ShowEditSampleDialog -> _internalState.update {
                    it.copy(editingSample = event.sample)
                }

                is MainEvent.ShowPasteConfirmDialog -> _internalState.update {
                    it.copy(slotToPaste = event.slot)
                }

                is MainEvent.ShowClearConfirmDialog -> _internalState.update {
                    it.copy(slotToClear = event.slot)
                }

                is MainEvent.ShowSwapDialog -> _internalState.update { it.copy(slotToSwap = event.slot) }
                is MainEvent.ShowSlotColorDialog -> _internalState.update {
                    it.copy(slotToEditColor = event.slot)
                }

                is MainEvent.ShowSampleColorDialog -> _internalState.update {
                    it.copy(sampleToEditColor = event.sample)
                }

                is MainEvent.ShowAudioLibrary -> _internalState.update {
                    it.copy(
                        showAudioLibrary = event.show,
                        audioLibrarySampleIdTarget = if (event.show) event.sampleId else -1
                    )
                }

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