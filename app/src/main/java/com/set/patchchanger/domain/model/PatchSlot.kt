package com.set.patchchanger.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * Core domain model representing a single patch/performance slot.
 * This is framework-independent and contains only business logic.
 *
 * @property id Unique identifier for the slot (0-2047 for 8 banks × 16 pages × 16 slots)
 * @property name Custom name displayed on the grid
 * @property description Additional description (currently unused in UI)
 * @property selected Whether this slot is currently active
 * @property color Hex color code for the slot background
 * @property msb MIDI Bank Select MSB (Controller 0)
 * @property lsb MIDI Bank Select LSB (Controller 32)
 * @property pc MIDI Program Change number (0-127)
 * @property volume MIDI volume (0-127, currently unused)
 * @property performanceName The actual performance name from the synth
 * @property displayNameType Which name to show: 'custom' or 'performance'
 * @property assignedSample ID of sample pad to trigger (-1 = none)
 */
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
    /**
     * Returns the display name based on the displayNameType setting
     */
    fun getDisplayName(): String {
        return when (displayNameType) {
            DisplayNameType.PERFORMANCE -> performanceName
            DisplayNameType.CUSTOM -> name
        }
    }

    /**
     * Returns the slot number (1-16) within its page
     */
    fun getSlotNumber(): Int = (id % 16) + 1

    /**
     * Returns the page index (0-15) within its bank
     */
    fun getPageIndex(): Int = (id / 16) % 16

    /**
     * Returns the bank index (0-7)
     */
    fun getBankIndex(): Int = id / 256
}

/**
 * Enum defining how slot names are displayed
 */
enum class DisplayNameType {
    PERFORMANCE,
    CUSTOM
}

/**
 * Represents a bank of 16 pages, each containing 16 slots
 */
@Parcelize
data class Bank(
    val name: String,
    val pages: List<Page>
) : Parcelable

/**
 * Represents a page of 16 patch slots
 */
@Parcelize
data class Page(
    val slots: List<PatchSlot>
) : Parcelable

/**
 * Represents the complete patch data structure
 */
data class PatchData(
    val banks: List<Bank>,
    val bankNames: List<String>,
    val pageNames: List<String>
)

/**
 * Sample pad configuration for audio playback
 *
 * @property id Sample ID (0-3 for S1-S4)
 * @property name Display name on the button
 * @property volume Playback volume (0-100)
 * @property loop Whether to loop playback
 * @property color Button background color (hex)
 * @property audioFileName Name of the audio file in internal storage
 * @property sourceName Original file name when imported
 */
@Parcelize
data class SamplePad(
    val id: Int,
    val name: String,
    val volume: Int,
    val loop: Boolean,
    val color: String,
    val audioFileName: String?,
    val sourceName: String?
) : Parcelable {
    /**
     * Checks if this sample has audio assigned
     */
    fun hasAudio(): Boolean = !audioFileName.isNullOrEmpty()
}

/**
 * Represents a performance from the MODX/Montage library
 * Used for browsing and assigning performances to slots
 */
data class Performance(
    val category: String,
    val bankName: String,
    val msb: Int,
    val lsb: Int,
    val pc: Int,
    val name: String
) {
    /**
     * Generates a unique key for this performance
     */
    fun getKey(): String = "$msb:$lsb:$pc"
}

/**
 * MIDI connection state
 */
sealed class MidiConnectionState {
    object Disconnected : MidiConnectionState()
    data class Connected(val deviceName: String) : MidiConnectionState()
    data class Error(val message: String) : MidiConnectionState()
}

/**
 * Theme configuration
 */
enum class AppTheme(val id: String, val displayName: String) {
    BLACK("black", "Black"),
    WHITE("white", "White"),
    BLUE("blue", "Blue"),
    ORANGE("orange", "Orange"),
    YELLOW("yellow", "Yellow"),
    RED("red", "Red"),
    GREEN("green", "Green"),
    PURPLE("purple", "Purple"),
    TEAL("teal", "Teal");

    companion object {
        fun fromId(id: String): AppTheme {
            return values().find { it.id == id } ?: BLACK
        }
    }
}

/**
 * App-wide settings and state
 */
data class AppSettings(
    val currentBankIndex: Int = 0,
    val currentPageIndex: Int = 0,
    val currentMidiChannel: Int = 1,
    val currentTranspose: Int = 0,
    val theme: AppTheme = AppTheme.BLACK
)

/**
 * Audio library item stored in the database
 */
data class AudioLibraryItem(
    val name: String,
    val filePath: String,
    val sizeBytes: Long,
    val durationMs: Long,
    val addedTimestamp: Long
)

/**
 * MODX color preset
 */
data class ModxColor(
    val name: String,
    val hex: String
)

/**
 * Search result for global patch search
 */
data class SearchResult(
    val slot: PatchSlot,
    val bankIndex: Int,
    val pageIndex: Int,
    val bankName: String,
    val pageName: String
)