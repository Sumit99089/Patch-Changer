package com.set.patchchanger.domain.util

/**
 * Central configuration for the application.
 * Defines the structure of the Patch system and limits.
 */
object Constants {
    // Structure
    const val BANK_COUNT = 8
    const val PAGE_COUNT = 16
    const val ROW_COUNT = 4
    const val COL_COUNT = 4
    const val SLOTS_PER_PAGE = ROW_COUNT * COL_COUNT // 16
    const val SLOTS_PER_BANK = PAGE_COUNT * SLOTS_PER_PAGE // 256

    // MIDI Limits
    const val MIN_TRANSPOSE = -11
    const val MAX_TRANSPOSE = 11
    const val MIN_MIDI_CHANNEL = 1
    const val MAX_MIDI_CHANNEL = 16

    // Defaults
    const val DEFAULT_BANK_INDEX = 0
    const val DEFAULT_PAGE_INDEX = 0
    const val DEFAULT_MIDI_CHANNEL = 1
    const val DEFAULT_TRANSPOSE = 0
}