package com.set.patchchanger.domain.usecase

import javax.inject.Inject

/**
 * Wrapper class to consolidate patch-related use cases.
 * Reduces constructor clutter in ViewModels.
 */
data class PatchUseCases @Inject constructor(
    val selectPatch: SelectPatchUseCase,
    val swapSlots: SwapSlotsUseCase,
    // Future: Add Copy/Paste/Clear logic here
)

data class NavigationUseCases @Inject constructor(
    val navigateBank: NavigateBankUseCase,
    val navigatePage: NavigatePageUseCase
)

data class DataTransferUseCases @Inject constructor(
    val exportData: ExportDataUseCase,
    val importData: ImportDataUseCase
)