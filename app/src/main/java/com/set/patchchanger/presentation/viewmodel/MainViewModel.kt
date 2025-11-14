package com.set.patchchanger.presentation.viewmodel

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.set.patchchanger.domain.model.AppSettings
import com.set.patchchanger.domain.model.AppTheme
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
import com.set.patchchanger.domain.usecase.ImportDataUseCase
import com.set.patchchanger.domain.usecase.NavigateBankUseCase
import com.set.patchchanger.domain.usecase.NavigatePageUseCase
import com.set.patchchanger.domain.usecase.SelectPatchUseCase
import com.set.patchchanger.domain.usecase.SwapSlotsUseCase
import com.set.patchchanger.domain.usecase.UpdateTransposeUseCase
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

/**
 * UI State for the main screen.
 *
 * Sealed class represents mutually exclusive states.
 * This prevents impossible states (e.g., loading + error simultaneously).
 */
sealed class MainUiState {
    object Loading : MainUiState()
    data class Success(
        val patchData: PatchData,
        val settings: AppSettings,
        val samples: List<SamplePad>,
        val midiState: MidiConnectionState,
        val audioLibrary: List<AudioLibraryItem>,
        // Dialog visibility states
        val showResetDialog: Boolean = false,
        val showBankPageNameDialog: Boolean = false,
        val editingSample: SamplePad? = null,
        val showAudioLibrary: Boolean = false,
        val audioLibrarySampleIdTarget: Int = -1,
        val slotToPaste: PatchSlot? = null,
        val slotToClear: PatchSlot? = null,
        val slotToSwap: PatchSlot? = null,
        val slotToEditColor: PatchSlot? = null,
        val sampleToEditColor: SamplePad? = null
    ) : MainUiState()

    data class Error(val message: String) : MainUiState()
}

/**
 * Events that the UI can trigger.
 *
 * Using sealed class for events ensures exhaustive when() statements.
 */
sealed class MainEvent {
    data class SelectSlot(val slotId: Int) : MainEvent()
    data class UpdateSlot(val slot: PatchSlot) : MainEvent()
    data class SwapSlots(val slot1Id: Int, val slot2Id: Int) : MainEvent()
    data class NavigateBank(val direction: Int) : MainEvent()
    data class NavigatePage(val direction: Int) : MainEvent()
    data class UpdateTranspose(val delta: Int) : MainEvent()
    object ResetTranspose : MainEvent()
    data class UpdateMidiChannel(val channel: Int) : MainEvent()
    data class UpdateTheme(val theme: AppTheme) : MainEvent()
    data class UpdateBankName(val index: Int, val name: String) : MainEvent()
    data class UpdatePageName(val index: Int, val name: String) : MainEvent()
    object ConnectMidi : MainEvent()
    object DisconnectMidi : MainEvent()

    // Dialog Control
    data class ShowResetDialog(val show: Boolean) : MainEvent()
    data class ShowBankPageNameDialog(val show: Boolean) : MainEvent()
    data class ShowEditSampleDialog(val sample: SamplePad?) : MainEvent()
    data class ShowPasteConfirmDialog(val slot: PatchSlot?) : MainEvent()
    data class ShowClearConfirmDialog(val slot: PatchSlot?) : MainEvent()
    data class ShowSwapDialog(val slot: PatchSlot?) : MainEvent()
    data class ShowSlotColorDialog(val slot: PatchSlot?) : MainEvent()
    data class ShowSampleColorDialog(val sample: SamplePad?) : MainEvent()
    data class ShowAudioLibrary(val show: Boolean, val sampleId: Int = -1) : MainEvent()

    // Actions
    object ResetData : MainEvent()
    data class ImportData(val jsonData: String) : MainEvent()
    data class CopySlot(val slot: PatchSlot) : MainEvent()
    data class PasteSlot(val targetSlot: PatchSlot) : MainEvent()
    data class ClearSlot(val slot: PatchSlot) : MainEvent()

    // Sample Events
    data class UpdateSample(val sample: SamplePad) : MainEvent()
    data class ClearSampleAudio(val sampleId: Int) : MainEvent()
    object LoadSampleFile : MainEvent() // Triggers picker
    data class SetSampleFile(val uri: Uri, val name: String) : MainEvent() // From picker result
    data class AddFileToLibrary(val uri: Uri, val name: String) : MainEvent()
    data class DeleteFromAudioLibrary(val item: AudioLibraryItem) : MainEvent()
    data class SelectSampleFromLibrary(val item: AudioLibraryItem) : MainEvent()
    data class TriggerSample(val sampleId: Int) : MainEvent()
}

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
            showResetDialog = internal.showResetDialog,
            showBankPageNameDialog = internal.showBankPageNameDialog,
            editingSample = internal.editingSample,
            showAudioLibrary = internal.showAudioLibrary,
            audioLibrarySampleIdTarget = internal.audioLibrarySampleIdTarget,
            slotToPaste = internal.slotToPaste,
            slotToClear = internal.slotToClear,
            slotToSwap = internal.slotToSwap,
            slotToEditColor = internal.slotToEditColor,
            sampleToEditColor = internal.sampleToEditColor
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

    /**
     * Handles UI events.
     *
     * viewModelScope is a CoroutineScope tied to ViewModel lifecycle.
     * When ViewModel is destroyed, all jobs are cancelled.
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

/**
 * One-time UI events (like showing snackbars or playing audio).
 */
sealed class UiEvent {
    data class ShowMessage(val message: String) : UiEvent()
    object RequestFilePicker : UiEvent()
}

/**
 * Internal mutable state for the ViewModel
 */
data class InternalState(
    val showResetDialog: Boolean = false,
    val showBankPageNameDialog: Boolean = false,
    val editingSample: SamplePad? = null,
    val showAudioLibrary: Boolean = false,
    val audioLibrarySampleIdTarget: Int = -1,
    val slotToPaste: PatchSlot? = null,
    val slotToClear: PatchSlot? = null,
    val slotToSwap: PatchSlot? = null,
    val slotToEditColor: PatchSlot? = null,
    val sampleToEditColor: SamplePad? = null
)