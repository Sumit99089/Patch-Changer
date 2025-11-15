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
        val patchData = patchRepository.getPatchData()
        val settings = settingsRepository.getSettings()

        val updatedSlots = mutableListOf<PatchSlot>()
        var selectedSlot: PatchSlot? = null

        patchData.banks.forEach { bank ->
            bank.pages.forEach { page ->
                page.slots.forEach { slot ->
                    val updated = slot.copy(selected = slot.id == slotId)
                    updatedSlots.add(updated)
                    if (updated.selected) {
                        selectedSlot = updated
                    }
                }
            }
        }

        patchRepository.updateSlots(updatedSlots)

        selectedSlot?.let { slot ->
            midiRepository.sendProgramChange(
                channel = settings.currentMidiChannel,
                msb = slot.msb,
                lsb = slot.lsb,
                pc = slot.pc
            )
        }

        return selectedSlot
    }
}