package com.set.patchchanger.domain.usecase

import com.set.patchchanger.domain.model.PatchSlot
import com.set.patchchanger.domain.repository.PatchRepository
import javax.inject.Inject

class SwapSlotsUseCase @Inject constructor(
    private val patchRepository: PatchRepository
) {
    suspend operator fun invoke(slot1Id: Int, slot2Id: Int) {
        val patchData = patchRepository.getPatchData()

        var slot1: PatchSlot? = null
        var slot2: PatchSlot? = null

        patchData.banks.forEach { bank ->
            bank.pages.forEach { page ->
                page.slots.forEach { slot ->
                    when (slot.id) {
                        slot1Id -> slot1 = slot
                        slot2Id -> slot2 = slot
                    }
                }
            }
        }

        if (slot1 != null && slot2 != null) {
            val s1 = slot1!!
            val s2 = slot2!!

            val updatedSlot1 = s1.copy(
                name = s2.name,
                description = s2.description,
                color = s2.color,
                msb = s2.msb,
                lsb = s2.lsb,
                pc = s2.pc,
                volume = s2.volume,
                performanceName = s2.performanceName,
                displayNameType = s2.displayNameType,
                assignedSample = s2.assignedSample
            )

            val updatedSlot2 = s2.copy(
                name = s1.name,
                description = s1.description,
                color = s1.color,
                msb = s1.msb,
                lsb = s1.lsb,
                pc = s1.pc,
                volume = s1.volume,
                performanceName = s1.performanceName,
                displayNameType = s1.displayNameType,
                assignedSample = s1.assignedSample
            )

            patchRepository.updateSlots(listOf(updatedSlot1, updatedSlot2))
        }
    }
}