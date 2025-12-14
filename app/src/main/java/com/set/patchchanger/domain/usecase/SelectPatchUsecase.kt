package com.set.patchchanger.domain.usecase

import com.set.patchchanger.domain.model.PatchSlot
import com.set.patchchanger.domain.repository.MidiRepository
import com.set.patchchanger.domain.repository.PatchRepository
import com.set.patchchanger.domain.repository.SettingsRepository
import javax.inject.Inject

class SelectPatchUseCase @Inject constructor(
    private val patchRepository: PatchRepository,
    private val midiRepository: MidiRepository,
    private val settingsRepository: SettingsRepository
) {
    suspend operator fun invoke(slotId: Int): PatchSlot? {
        // 1. Fast Fetch: Get only the target slot info needed for MIDI
        val slot = patchRepository.getSlotById(slotId) ?: return null
        val settings = settingsRepository.getSettings()

        // 2. Immediate Action: Send MIDI before doing any heavy DB work
        midiRepository.sendProgramChange(
            channel = settings.currentMidiChannel,
            msb = slot.msb,
            lsb = slot.lsb,
            pc = slot.pc
        )

        // 3. Background: Efficiently update the 'selected' state in DB
        // This replaces the previous logic that read the whole DB, iterated 2048 items, and wrote them back.
        patchRepository.setSelectedSlot(slotId)

        return slot.copy(selected = true)
    }
}