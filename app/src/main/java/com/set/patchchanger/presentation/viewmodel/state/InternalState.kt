package com.set.patchchanger.presentation.viewmodel.state

import com.set.patchchanger.domain.model.PatchSlot
import com.set.patchchanger.domain.model.Performance
import com.set.patchchanger.domain.model.SamplePad
import com.set.patchchanger.domain.model.SearchResult
import com.set.patchchanger.domain.usecase.GetPerformancesUseCase

/**
 * Internal mutable state for the ViewModel
 */
data class InternalState(
    val searchQuery: String = "",
    val searchResults: List<SearchResult> = emptyList(),
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
)