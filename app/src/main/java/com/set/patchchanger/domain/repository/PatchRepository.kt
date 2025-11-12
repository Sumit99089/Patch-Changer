package com.set.patchchanger.domain.repository

import com.set.patchchanger.domain.model.AppSettings
import com.set.patchchanger.domain.model.AppTheme
import com.set.patchchanger.domain.model.AudioLibraryItem
import com.set.patchchanger.domain.model.MidiConnectionState
import com.set.patchchanger.domain.model.PatchData
import com.set.patchchanger.domain.model.PatchSlot
import com.set.patchchanger.domain.model.SamplePad
import com.set.patchchanger.domain.model.SearchResult
import kotlinx.coroutines.flow.Flow
import java.io.File

/**
 * Repository interface for patch data operations.
 *
 * This interface defines the contract for data operations without
 * specifying implementation details. This allows:
 * - Easy testing with mock implementations
 * - Swapping data sources (Room, Firebase, etc.)
 * - Keeping domain layer independent of frameworks
 */
interface PatchRepository {
    /**
     * Observes the complete patch data structure.
     * Returns a Flow for reactive updates in the UI.
     */
    fun observePatchData(): Flow<PatchData>

    /**
     * Gets the current patch data synchronously.
     * Used when Flow observation isn't needed.
     */
    suspend fun getPatchData(): PatchData

    /**
     * Updates a single patch slot.
     * @param slot The updated slot data
     */
    suspend fun updateSlot(slot: PatchSlot)

    /**
     * Updates multiple slots atomically (for swap operations).
     * @param slots List of slots to update
     */
    suspend fun updateSlots(slots: List<PatchSlot>)

    /**
     * Updates a bank name.
     * @param index Bank index (0-7)
     * @param newName New bank name
     */
    suspend fun updateBankName(index: Int, newName: String)

    /**
     * Updates a page name.
     * @param index Page index (0-15)
     * @param newName New page name
     */
    suspend fun updatePageName(index: Int, newName: String)

    /**
     * Resets all patch data to factory defaults.
     */
    suspend fun resetToDefaults()

    /**
     * Imports patch data from JSON string.
     * @param jsonData JSON string containing patch data
     * @return true if successful, false otherwise
     */
    suspend fun importFromJson(jsonData: String): Boolean

    /**
     * Exports all patch data to JSON string.
     * @return JSON string representation
     */
    suspend fun exportToJson(): String

    /**
     * Searches all slots for matching text.
     * @param query Search query (name or performance name)
     * @return List of matching search results
     */
    suspend fun searchSlots(query: String): List<SearchResult>
}

/**
 * Repository interface for sample pad operations.
 */
interface SampleRepository {
    /**
     * Observes all sample pad configurations.
     */
    fun observeSamples(): Flow<List<SamplePad>>

    /**
     * Gets current sample pads.
     */
    suspend fun getSamples(): List<SamplePad>

    /**
     * Updates a single sample pad configuration.
     * @param sample Updated sample data
     */
    suspend fun updateSample(sample: SamplePad)

    /**
     * Clears audio from a sample pad.
     * @param sampleId Sample ID (0-3)
     */
    suspend fun clearSampleAudio(sampleId: Int)

    /**
     * Saves audio file for a sample pad.
     * @param sampleId Sample ID
     * @param sourceFile Source audio file
     * @return File path of saved audio
     */
    suspend fun saveSampleAudio(sampleId: Int, sourceFile: File): String

    /**
     * Resets all samples to defaults.
     */
    suspend fun resetSamples()
}

/**
 * Repository interface for audio library management.
 */
interface AudioLibraryRepository {
    /**
     * Observes all audio library items.
     */
    fun observeLibrary(): Flow<List<AudioLibraryItem>>

    /**
     * Gets all library items.
     */
    suspend fun getLibraryItems(): List<AudioLibraryItem>

    /**
     * Adds an audio file to the library.
     * @param sourceFile Source audio file
     * @param originalName Original file name
     * @return AudioLibraryItem representing the added file
     */
    suspend fun addAudioFile(sourceFile: File, originalName: String): AudioLibraryItem

    /**
     * Deletes an audio file from the library.
     * @param item Item to delete
     */
    suspend fun deleteAudioFile(item: AudioLibraryItem)

    /**
     * Gets the actual file for an audio library item.
     * @param item Library item
     * @return File object
     */
    fun getAudioFile(item: AudioLibraryItem): File

    /**
     * Searches library items by name.
     * @param query Search query
     * @return Filtered list of items
     */
    suspend fun searchLibrary(query: String): List<AudioLibraryItem>
}

/**
 * Repository interface for app settings persistence.
 */
interface SettingsRepository {
    /**
     * Observes app settings changes.
     */
    fun observeSettings(): Flow<AppSettings>

    /**
     * Gets current settings.
     */
    suspend fun getSettings(): AppSettings

    /**
     * Updates app settings.
     * @param settings New settings
     */
    suspend fun updateSettings(settings: AppSettings)

    /**
     * Updates only the current bank index.
     */
    suspend fun updateBankIndex(index: Int)

    /**
     * Updates only the current page index.
     */
    suspend fun updatePageIndex(index: Int)

    /**
     * Updates only the MIDI channel.
     */
    suspend fun updateMidiChannel(channel: Int)

    /**
     * Updates only the transpose value.
     */
    suspend fun updateTranspose(transpose: Int)

    /**
     * Updates only the theme.
     */
    suspend fun updateTheme(theme: AppTheme)
}

/**
 * Repository interface for MIDI communication.
 *
 * Note: This is still in the domain layer because MIDI is core
 * business logic for this app. The implementation details
 * (Android MIDI API) are in the data layer.
 */
interface MidiRepository {
    /**
     * Observes MIDI connection state.
     */
    fun observeConnectionState(): Flow<MidiConnectionState>

    /**
     * Connects to a MIDI device.
     * @param deviceId Device identifier (null for auto-connect)
     */
    suspend fun connect(deviceId: String? = null)

    /**
     * Disconnects from the current MIDI device.
     */
    suspend fun disconnect()

    /**
     * Sends a program change message.
     * @param channel MIDI channel (1-16)
     * @param msb Bank MSB (0-127)
     * @param lsb Bank LSB (0-127)
     * @param pc Program number (0-127)
     */
    suspend fun sendProgramChange(channel: Int, msb: Int, lsb: Int, pc: Int)

    /**
     * Sends a Live Set bank change SysEx message.
     * @param bankIndex Bank index (0-7)
     */
    suspend fun sendLiveSetBankChange(bankIndex: Int)

    /**
     * Sends a transpose SysEx message.
     * @param channel MIDI channel (1-16)
     * @param transpose Transpose amount (-11 to +11)
     */
    suspend fun sendTranspose(channel: Int, transpose: Int)

    /**
     * Gets list of available MIDI devices.
     * @return List of device names
     */
    suspend fun getAvailableDevices(): List<String>
}