package com.set.patchchanger.presentation.viewmodel.state


import com.set.patchchanger.domain.model.AppSettings
import com.set.patchchanger.domain.model.AudioLibraryItem
import com.set.patchchanger.domain.model.MidiConnectionState
import com.set.patchchanger.domain.model.PatchData
import com.set.patchchanger.domain.model.PatchSlot
import com.set.patchchanger.domain.model.Performance
import com.set.patchchanger.domain.model.SamplePad
import com.set.patchchanger.domain.model.SearchResult
import com.set.patchchanger.domain.usecase.GetPerformancesUseCase

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
        // Search State
        val searchQuery: String = "",
        val searchResults: List<SearchResult> = emptyList(),
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
        val sampleToEditColor: SamplePad? = null,
        // Performance Browser State
        val showPerformanceBrowser: Boolean = false,
        val slotToEditPerformance: PatchSlot? = null,
        val performanceCategories: List<String> = emptyList(),
        val performanceSelectedCategory: String? = null,
        val performanceBanks: List<GetPerformancesUseCase.PerformanceBank> = emptyList(),
        val performanceSelectedBankIndex: Int = -1,
        val performances: List<Performance> = emptyList(),
        val performanceSearchQuery: String = ""
    ) : MainUiState()

    data class Error(val message: String) : MainUiState()
}