package com.set.patchcahnger.domain.usecase

import com.set.patchcahnger.domain.model.*
import com.set.patchcahnger.domain.repository.*
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use Case: Select and activate a patch slot.
 *
 * This encapsulates the business logic for:
 * 1. Deselecting all other slots
 * 2. Selecting the target slot
 * 3. Sending MIDI program change
 * 4. Triggering assigned sample (if any)
 *
 * Use cases allow complex operations to be reused and tested easily.
 */
class SelectPatchUseCase @Inject constructor(
    private val patchRepository: PatchRepository,
    private val midiRepository: MidiRepository,
    private val settingsRepository: SettingsRepository
) {
    /**
     * Executes the patch selection.
     * @param slotId ID of slot to select
     * @return The selected slot, or null if not found
     */
    suspend operator fun invoke(slotId: Int): PatchSlot? {
        val patchData = patchRepository.getPatchData()
        val settings = settingsRepository.getSettings()

        // Find all slots and update selection state
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

        // Update all slots in database
        patchRepository.updateSlots(updatedSlots)

        // Send MIDI program change if we found the slot
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

/**
 * Use Case: Swap two patch slots.
 *
 * Swaps all properties except ID and selection state.
 */
class SwapSlotsUseCase @Inject constructor(
    private val patchRepository: PatchRepository
) {
    suspend operator fun invoke(slot1Id: Int, slot2Id: Int) {
        val patchData = patchRepository.getPatchData()

        var slot1: PatchSlot? = null
        var slot2: PatchSlot? = null

        // Find both slots
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

        // Swap if both found
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

/**
 * Use Case: Update transpose and send MIDI message.
 */
class UpdateTransposeUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val midiRepository: MidiRepository
) {
    companion object {
        const val MIN_TRANSPOSE = -11
        const val MAX_TRANSPOSE = 11
    }

    /**
     * Updates transpose value.
     * @param delta Amount to change (+1 or -1)
     */
    suspend operator fun invoke(delta: Int) {
        val settings = settingsRepository.getSettings()
        val newTranspose = (settings.currentTranspose + delta)
            .coerceIn(MIN_TRANSPOSE, MAX_TRANSPOSE)

        if (newTranspose != settings.currentTranspose) {
            settingsRepository.updateTranspose(newTranspose)
            midiRepository.sendTranspose(settings.currentMidiChannel, newTranspose)
        }
    }

    /**
     * Resets transpose to 0.
     */
    suspend fun reset() {
        val settings = settingsRepository.getSettings()
        if (settings.currentTranspose != 0) {
            settingsRepository.updateTranspose(0)
            midiRepository.sendTranspose(settings.currentMidiChannel, 0)
        }
    }
}

/**
 * Use Case: Navigate between banks.
 */
class NavigateBankUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val midiRepository: MidiRepository
) {
    /**
     * Navigates to next/previous bank.
     * @param direction +1 for next, -1 for previous
     */
    suspend operator fun invoke(direction: Int) {
        val settings = settingsRepository.getSettings()
        val totalBanks = 8

        val newBankIndex = (settings.currentBankIndex + direction + totalBanks) % totalBanks

        settingsRepository.updateBankIndex(newBankIndex)
        settingsRepository.updatePageIndex(0) // Reset to first page

        // Send Live Set bank change
        midiRepository.sendLiveSetBankChange(newBankIndex)
    }
}

/**
 * Use Case: Navigate between pages.
 */
class NavigatePageUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository
) {
    /**
     * Navigates to next/previous page.
     * @param direction +1 for next, -1 for previous
     */
    suspend operator fun invoke(direction: Int) {
        val settings = settingsRepository.getSettings()
        val totalPages = 16

        val newPageIndex = (settings.currentPageIndex + direction + totalPages) % totalPages

        settingsRepository.updatePageIndex(newPageIndex)
    }
}

/**
 * Use Case: Get performances for a specific category and bank.
 *
 * This generates performance data on-demand rather than storing it all.
 */
class GetPerformancesUseCase @Inject constructor() {

    /**
     * Performance data structure matching the JavaScript version
     */
    private val performanceData = mapOf(
        "For Montage (Single)" to PerformanceCategory(
            msb = 63,
            banks = (0..15).map { PerformanceBank("Preset ${it + 1}", it, 128) } +
                    (16..20).map { PerformanceBank("User ${it - 15}", it, 128) } +
                    (24..63).map { PerformanceBank("Library ${it - 23}", it, 128) }
        ),
        "For Montage (Multi)" to PerformanceCategory(
            msb = 63,
            banks = (64..79).map { PerformanceBank("Preset ${it - 63}", it, 128) } +
                    (80..84).map { PerformanceBank("User ${it - 79}", it, 128) } +
                    (88..127).map { PerformanceBank("Library ${it - 87}", it, 128) }
        ),
        "For ModX / ModX+ (Single)" to PerformanceCategory(
            msb = 63,
            banks = (0..31).map { PerformanceBank("Preset ${it + 1}", it, 128) } +
                    (32..36).map { PerformanceBank("User ${it - 31}", it, 128) } +
                    (40..79).map { PerformanceBank("Library ${it - 39}", it, 128) }
        ),
        "For ModX / ModX+ (Multi)" to PerformanceCategory(
            msb = 64,
            banks = (0..31).map { PerformanceBank("Preset ${it + 1}", it, 128) } +
                    (32..36).map { PerformanceBank("User ${it - 31}", it, 128) } +
                    (40..79).map { PerformanceBank("Library ${it - 39}", it, 128) }
        ),
        "For MODX M / Montage M (Single) - Presets" to PerformanceCategory(
            msb = 63,
            banks = (0..39).map { PerformanceBank("Preset ${it + 1}", it, 128) }
        ),
        "For MODX M / Montage M (Single) - User/Lib" to PerformanceCategory(
            msb = 64,
            banks = (0..4).map { PerformanceBank("User ${it + 1}", it, 128) } +
                    (8..87).map { PerformanceBank("Library ${it - 7}", it, 128) }
        ),
        "For MODX M / Montage M (Multi) - Presets" to PerformanceCategory(
            msb = 65,
            banks = (0..39).map { PerformanceBank("Preset ${it + 1}", it, 128) }
        ),
        "For MODX M / Montage M (Multi) - User/Lib" to PerformanceCategory(
            msb = 66,
            banks = (0..4).map { PerformanceBank("User ${it + 1}", it, 128) } +
                    (8..87).map { PerformanceBank("Library ${it - 7}", it, 128) }
        ),
        "For GM Voice" to PerformanceCategory(
            msb = 0,
            banks = listOf(PerformanceBank("GM Voice", 0, 128))
        ),
        "For GM Drum Voice" to PerformanceCategory(
            msb = 127,
            banks = listOf(PerformanceBank("GM Drum Voice", 0, 128))
        )
    )

    /**
     * Gets all category names.
     */
    fun getCategories(): List<String> = performanceData.keys.toList()

    /**
     * Gets banks for a category.
     */
    fun getBanks(category: String): List<PerformanceBank> {
        return performanceData[category]?.banks ?: emptyList()
    }

    /**
     * Gets performances for a specific category and bank.
     */
    operator fun invoke(category: String, bankIndex: Int): List<Performance> {
        val categoryData = performanceData[category] ?: return emptyList()
        val bank = categoryData.banks.getOrNull(bankIndex) ?: return emptyList()

        return (0 until bank.pcCount).map { pc ->
            Performance(
                category = category,
                bankName = bank.name,
                msb = categoryData.msb,
                lsb = bank.lsb,
                pc = pc,
                name = "${bank.name} - ${(pc + 1).toString().padStart(3, '0')}"
            )
        }
    }

    /**
     * Searches all performances.
     */
    fun search(query: String): List<Performance> {
        val results = mutableListOf<Performance>()
        val lowerQuery = query.lowercase()

        performanceData.forEach { (category, categoryData) ->
            categoryData.banks.forEach { bank ->
                (0 until bank.pcCount).forEach { pc ->
                    val name = "${bank.name} - ${(pc + 1).toString().padStart(3, '0')}"
                    if (name.lowercase().contains(lowerQuery) ||
                        (pc + 1).toString().contains(lowerQuery)) {
                        results.add(
                            Performance(
                                category = category,
                                bankName = bank.name,
                                msb = categoryData.msb,
                                lsb = bank.lsb,
                                pc = pc,
                                name = name
                            )
                        )
                    }
                }
            }
        }

        return results
    }

    data class PerformanceCategory(
        val msb: Int,
        val banks: List<PerformanceBank>
    )

    data class PerformanceBank(
        val name: String,
        val lsb: Int,
        val pcCount: Int
    )
}

/**
 * Use Case: Export all data to JSON.
 */
class ExportDataUseCase @Inject constructor(
    private val patchRepository: PatchRepository,
    private val sampleRepository: SampleRepository,
    private val settingsRepository: SettingsRepository
) {
    /**
     * Exports all app data to JSON string.
     */
    suspend operator fun invoke(): String {
        return patchRepository.exportToJson()
    }
}

/**
 * Use Case: Import data from JSON.
 */
class ImportDataUseCase @Inject constructor(
    private val patchRepository: PatchRepository
) {
    /**
     * Imports data from JSON string.
     * @return true if successful
     */
    suspend operator fun invoke(jsonData: String): Boolean {
        return patchRepository.importFromJson(jsonData)
    }
}