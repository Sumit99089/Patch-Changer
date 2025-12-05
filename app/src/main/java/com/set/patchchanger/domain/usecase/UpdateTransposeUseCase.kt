package com.set.patchchanger.domain.usecase

import com.set.patchchanger.domain.repository.MidiRepository
import com.set.patchchanger.domain.repository.SettingsRepository
import com.set.patchchanger.domain.util.Constants
import javax.inject.Inject

class UpdateTransposeUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val midiRepository: MidiRepository
) {
    suspend operator fun invoke(delta: Int) {
        val settings = settingsRepository.getSettings()
        val newTranspose = (settings.currentTranspose + delta)
            .coerceIn(Constants.MIN_TRANSPOSE, Constants.MAX_TRANSPOSE)

        if (newTranspose != settings.currentTranspose) {
            settingsRepository.updateTranspose(newTranspose)
            midiRepository.sendTranspose(settings.currentMidiChannel, newTranspose)
        }
    }

    suspend fun reset() {
        val settings = settingsRepository.getSettings()
        if (settings.currentTranspose != Constants.DEFAULT_TRANSPOSE) {
            settingsRepository.updateTranspose(Constants.DEFAULT_TRANSPOSE)
            midiRepository.sendTranspose(settings.currentMidiChannel, Constants.DEFAULT_TRANSPOSE)
        }
    }
}