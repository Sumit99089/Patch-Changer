package com.set.patchchanger.domain.usecase

import com.set.patchchanger.domain.repository.MidiRepository
import com.set.patchchanger.domain.repository.SettingsRepository
import com.set.patchchanger.domain.util.Constants
import javax.inject.Inject

class NavigateBankUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val midiRepository: MidiRepository
) {
    suspend operator fun invoke(direction: Int) {
        val settings = settingsRepository.getSettings()

        // Use Constants for scalability
        val newBankIndex = (settings.currentBankIndex + direction + Constants.BANK_COUNT) % Constants.BANK_COUNT

        settingsRepository.updateBankIndex(newBankIndex)
        // Reset page to 0 when switching banks (optional UX choice, but keeps navigation clean)
        settingsRepository.updatePageIndex(Constants.DEFAULT_PAGE_INDEX)

        midiRepository.sendLiveSetBankChange(newBankIndex)
    }
}