package com.set.patchchanger.domain.usecase

import com.set.patchchanger.domain.repository.MidiRepository
import com.set.patchchanger.domain.repository.SettingsRepository
import javax.inject.Inject

class UpdateTransposeUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val midiRepository: MidiRepository
) {
    companion object {
        const val MIN_TRANSPOSE = -11
        const val MAX_TRANSPOSE = 11
    }

    suspend operator fun invoke(delta: Int) {
        val settings = settingsRepository.getSettings()
        val newTranspose = (settings.currentTranspose + delta)
            .coerceIn(MIN_TRANSPOSE, MAX_TRANSPOSE)

        if (newTranspose != settings.currentTranspose) {
            settingsRepository.updateTranspose(newTranspose)
            midiRepository.sendTranspose(settings.currentMidiChannel, newTranspose)
        }
    }

    suspend fun reset() {
        val settings = settingsRepository.getSettings()
        if (settings.currentTranspose != 0) {
            settingsRepository.updateTranspose(0)
            midiRepository.sendTranspose(settings.currentMidiChannel, 0)
        }
    }
}