package com.set.patchchanger.domain.usecase

import com.set.patchchanger.domain.repository.SettingsRepository
import javax.inject.Inject

class NavigatePageUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository
) {
    suspend operator fun invoke(direction: Int) {
        val settings = settingsRepository.getSettings()
        val totalPages = 16

        val newPageIndex = (settings.currentPageIndex + direction + totalPages) % totalPages

        settingsRepository.updatePageIndex(newPageIndex)
    }
}