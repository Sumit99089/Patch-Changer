package com.set.patchchanger.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class PatchSlot(
    val id: Int,
    val name: String,
    val description: String,
    val selected: Boolean,
    val color: String,
    val msb: Int,
    val lsb: Int,
    val pc: Int,
    val volume: Int,
    val performanceName: String,
    val displayNameType: DisplayNameType,
    val assignedSample: Int
) : Parcelable {
    fun getDisplayName(): String {
        return when (displayNameType) {
            DisplayNameType.PERFORMANCE -> performanceName
            DisplayNameType.CUSTOM -> name
        }
    }

    fun getSlotNumber(): Int = (id % 16) + 1
    fun getPageIndex(): Int = (id / 16) % 16
    fun getBankIndex(): Int = id / 256

    fun copyDataFrom(source: PatchSlot): PatchSlot {
        return this.copy(
            name = source.name,
            description = source.description,
            color = source.color,
            msb = source.msb,
            lsb = source.lsb,
            pc = source.pc,
            volume = source.volume,
            performanceName = source.performanceName,
            displayNameType = source.displayNameType,
            assignedSample = source.assignedSample
        )
    }

    companion object {
        fun createDefault(id: Int, defaultColor: String): PatchSlot {
            val slotNum = (id % 16) + 1
            val pageNum = ((id / 16) % 16)
            val perfName = "Slot $slotNum"
            return PatchSlot(
                id = id,
                name = "$slotNum",
                description = "Slot $slotNum",
                selected = false,
                color = defaultColor,
                msb = 62,
                lsb = pageNum,
                pc = slotNum - 1,
                volume = 100,
                performanceName = perfName,
                displayNameType = DisplayNameType.CUSTOM,
                assignedSample = -1
            )
        }
    }
}

enum class DisplayNameType {
    PERFORMANCE,
    CUSTOM
}