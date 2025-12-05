package com.set.patchchanger.domain.usecase

import com.set.patchchanger.domain.repository.SettingsRepository
import com.set.patchchanger.domain.util.Constants
import javax.inject.Inject

class NavigatePageUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository
) {
    suspend operator fun invoke(direction: Int) {
        val settings = settingsRepository.getSettings()
        val newPageIndex = (settings.currentPageIndex + direction + Constants.PAGE_COUNT) % Constants.PAGE_COUNT
        settingsRepository.updatePageIndex(newPageIndex)
    }
}