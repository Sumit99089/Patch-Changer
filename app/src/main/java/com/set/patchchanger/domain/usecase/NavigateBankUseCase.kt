package com.set.patchchanger.domain.usecase

import com.set.patchchanger.domain.repository.MidiRepository
import com.set.patchchanger.domain.repository.SettingsRepository
import javax.inject.Inject

class NavigateBankUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val midiRepository: MidiRepository
) {
    suspend operator fun invoke(direction: Int) {
        val settings = settingsRepository.getSettings()
        val totalBanks = 8

        val newBankIndex = (settings.currentBankIndex + direction + totalBanks) % totalBanks

        settingsRepository.updateBankIndex(newBankIndex)
        settingsRepository.updatePageIndex(0)

        midiRepository.sendLiveSetBankChange(newBankIndex)
    }
}