package com.set.patchchanger.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.set.patchchanger.domain.model.AppSettings
import com.set.patchchanger.domain.model.AppTheme
import com.set.patchchanger.domain.model.MidiConnectionState
import com.set.patchchanger.domain.model.PatchData
import com.set.patchchanger.domain.model.PatchSlot
import com.set.patchchanger.domain.model.SamplePad
import com.set.patchchanger.domain.model.SearchResult
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
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
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
        val midiState: MidiConnectionState
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
    object ResetData : MainEvent()
    data class ImportData(val jsonData: String) : MainEvent()
    data class UpdateSample(val sample: SamplePad) : MainEvent()
    data class ClearSampleAudio(val sampleId: Int) : MainEvent()
}

@HiltViewModel
class MainViewModel @Inject constructor(
    private val patchRepository: PatchRepository,
    private val settingsRepository: SettingsRepository,
    private val midiRepository: MidiRepository,
    private val sampleRepository: SampleRepository,
    private val selectPatchUseCase: SelectPatchUseCase,
    private val swapSlotsUseCase: SwapSlotsUseCase,
    private val updateTransposeUseCase: UpdateTransposeUseCase,
    private val navigateBankUseCase: NavigateBankUseCase,
    private val navigatePageUseCase: NavigatePageUseCase,
    private val exportDataUseCase: ExportDataUseCase,
    private val importDataUseCase: ImportDataUseCase
) : ViewModel() {

    /**
     * StateFlow for UI state.
     *
     * StateFlow is a hot Flow that always has a value.
     * UI collects this to get updates.
     *
     * combine() merges multiple Flows into one.
     */
    val uiState: StateFlow<MainUiState> = combine(
        patchRepository.observePatchData(),
        settingsRepository.observeSettings(),
        sampleRepository.observeSamples(),
        midiRepository.observeConnectionState()
    ) { patchData, settings, samples, midiState ->
        MainUiState.Success(
            patchData = patchData,
            settings = settings,
            samples = samples,
            midiState = midiState
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
                            _events.emit(UiEvent.PlaySample(sampleId))
                        }
                    }
                }

                is MainEvent.UpdateSlot -> {
                    patchRepository.updateSlot(event.slot)
                }

                is MainEvent.SwapSlots -> {
                    swapSlotsUseCase(event.slot1Id, event.slot2Id)
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
}

/**
 * One-time UI events (like showing snackbars or playing audio).
 */
sealed class UiEvent {
    data class ShowMessage(val message: String) : UiEvent()
    data class PlaySample(val sampleId: Int) : UiEvent()
}