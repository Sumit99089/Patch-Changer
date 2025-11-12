package com.set.patchchanger.data.local

import android.content.Context
import android.media.midi.*
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.set.patchchanger.domain.model.AppSettings
import com.set.patchchanger.domain.model.AppTheme
import com.set.patchchanger.domain.model.MidiConnectionState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * DataStore extension for Context.
 *
 * DataStore is a data storage solution that uses Kotlin coroutines
 * and Flow to store data asynchronously. It's a replacement for
 * SharedPreferences with better type safety and error handling.
 */
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
    name = "app_settings"
)

/**
 * Manages app settings persistence using DataStore.
 *
 * @Singleton ensures only one instance exists in the app.
 * @Inject tells Hilt to inject dependencies automatically.
 */

@Singleton
class SettingsDataStore @Inject constructor(
    private val context: Context
) {
    /**
     * Preference keys for type-safe access.
     */
    private object PreferencesKeys {
        val BANK_INDEX = intPreferencesKey("bank_index")
        val PAGE_INDEX = intPreferencesKey("page_index")
        val MIDI_CHANNEL = intPreferencesKey("midi_channel")
        val TRANSPOSE = intPreferencesKey("transpose")
        val THEME = stringPreferencesKey("theme")
    }

    /**
     * Observes settings as a Flow.
     * Map operator transforms Preferences to AppSettings.
     */
    val settingsFlow: Flow<AppSettings> = context.dataStore.data
        .map { preferences ->
            AppSettings(
                currentBankIndex = preferences[PreferencesKeys.BANK_INDEX] ?: 0,
                currentPageIndex = preferences[PreferencesKeys.PAGE_INDEX] ?: 0,
                currentMidiChannel = preferences[PreferencesKeys.MIDI_CHANNEL] ?: 1,
                currentTranspose = preferences[PreferencesKeys.TRANSPOSE] ?: 0,
                theme = AppTheme.fromId(preferences[PreferencesKeys.THEME] ?: "black")
            )
        }

    /**
     * Updates settings atomically.
     * edit { } provides transactional updates.
     */
    suspend fun updateSettings(settings: AppSettings) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.BANK_INDEX] = settings.currentBankIndex
            preferences[PreferencesKeys.PAGE_INDEX] = settings.currentPageIndex
            preferences[PreferencesKeys.MIDI_CHANNEL] = settings.currentMidiChannel
            preferences[PreferencesKeys.TRANSPOSE] = settings.currentTranspose
            preferences[PreferencesKeys.THEME] = settings.theme.id
        }
    }

    suspend fun updateBankIndex(index: Int) {
        context.dataStore.edit { it[PreferencesKeys.BANK_INDEX] = index }
    }

    suspend fun updatePageIndex(index: Int) {
        context.dataStore.edit { it[PreferencesKeys.PAGE_INDEX] = index }
    }

    suspend fun updateMidiChannel(channel: Int) {
        context.dataStore.edit { it[PreferencesKeys.MIDI_CHANNEL] = channel }
    }

    suspend fun updateTranspose(transpose: Int) {
        context.dataStore.edit { it[PreferencesKeys.TRANSPOSE] = transpose }
    }

    suspend fun updateTheme(theme: AppTheme) {
        context.dataStore.edit { it[PreferencesKeys.THEME] = theme.id }
    }
}

/**
 * Manages MIDI device connections and communication.
 *
 * Android MIDI API differences from Web MIDI:
 * - Asynchronous device opening with callbacks
 * - Requires MIDI permission (not needed on most Android versions)
 * - Device lifecycle management (open/close)
 * - Buffer management for sending messages
 */
@Singleton
class MidiManager @Inject constructor(
    private val context: Context
) {
    private val midiManager: MidiManager? =
        context.getSystemService(Context.MIDI_SERVICE) as? MidiManager

    private var midiDevice: MidiDevice? = null
    private var inputPort: MidiInputPort? = null

    /**
     * StateFlow for connection state.
     * MutableStateFlow allows internal updates, exposed as read-only StateFlow.
     */
    private val _connectionState = MutableStateFlow<MidiConnectionState>(
        MidiConnectionState.Disconnected
    )
    val connectionState: StateFlow<MidiConnectionState> = _connectionState.asStateFlow()

    /**
     * Callback for device connection events.
     */
    private val deviceCallback = object : MidiManager.DeviceCallback() {
        override fun onDeviceAdded(device: MidiDeviceInfo?) {
            // Could auto-connect here
        }

        override fun onDeviceRemoved(device: MidiDeviceInfo?) {
            if (device?.id == midiDevice?.info?.id) {
                disconnect()
            }
        }
    }

    init {
        // Register for device hotplug events
        midiManager?.registerDeviceCallback(deviceCallback, null)
    }

    /**
     * Gets list of available MIDI devices.
     */
    fun getAvailableDevices(): List<Pair<String, MidiDeviceInfo>> {
        val devices = midiManager?.devices ?: return emptyList()
        return devices.mapNotNull { info ->
            // Filter out "Through" and virtual ports
            val name = info.properties.getString(MidiDeviceInfo.PROPERTY_NAME) ?: return@mapNotNull null
            if (name.contains("through", ignoreCase = true)) return@mapNotNull null
            if (info.outputPortCount == 0) return@mapNotNull null // Need output port to send to

            name to info
        }
    }

    /**
     * Connects to a MIDI device.
     *
     * @param deviceInfo Device to connect to, or null for auto-connect
     */
    fun connect(deviceInfo: MidiDeviceInfo? = null) {
        val targetDevice = deviceInfo ?: getAvailableDevices().firstOrNull()?.second

        if (targetDevice == null) {
            _connectionState.value = MidiConnectionState.Error("No MIDI devices found")
            return
        }

        // Open device asynchronously
        midiManager?.openDevice(targetDevice, { device ->
            if (device == null) {
                _connectionState.value = MidiConnectionState.Error("Failed to open device")
                return@openDevice
            }

            // Get first output port (our input for sending)
            val port = device.openOutputPort(0)
            if (port == null) {
                device.close()
                _connectionState.value = MidiConnectionState.Error("No output ports available")
                return@openDevice
            }

            // Successfully connected
            midiDevice = device
            inputPort = port

            val deviceName = targetDevice.properties
                .getString(MidiDeviceInfo.PROPERTY_NAME) ?: "Unknown Device"
            _connectionState.value = MidiConnectionState.Connected(deviceName)

        }, null) // null handler = use main thread
    }

    /**
     * Disconnects from current device.
     */
    fun disconnect() {
        inputPort?.close()
        midiDevice?.close()
        inputPort = null
        midiDevice = null
        _connectionState.value = MidiConnectionState.Disconnected
    }

    /**
     * Sends a MIDI message.
     *
     * @param bytes MIDI message bytes
     */
    private fun sendMessage(bytes: ByteArray) {
        inputPort?.send(bytes, 0, bytes.size) ?: run {
            _connectionState.value = MidiConnectionState.Error("Not connected")
        }
    }

    /**
     * Sends a Program Change with Bank Select.
     *
     * MIDI Protocol:
     * - Bank Select MSB: 0xBn 0x00 [msb]
     * - Bank Select LSB: 0xBn 0x20 [lsb]
     * - Program Change: 0xCn [pc]
     * where n = channel (0-15)
     */
    fun sendProgramChange(channel: Int, msb: Int, lsb: Int, pc: Int) {
        val ch = (channel - 1).coerceIn(0, 15)

        // Bank Select MSB
        sendMessage(byteArrayOf(
            (0xB0 + ch).toByte(),
            0x00.toByte(),
            msb.toByte()
        ))

        // Bank Select LSB
        sendMessage(byteArrayOf(
            (0xB0 + ch).toByte(),
            0x20.toByte(),
            lsb.toByte()
        ))

        // Program Change
        sendMessage(byteArrayOf(
            (0xC0 + ch).toByte(),
            pc.toByte()
        ))
    }

    /**
     * Sends Live Set Bank Change SysEx.
     *
     * SysEx format: F0 43 10 7F 1C 07 09 00 00 [bank] F7
     */
    fun sendLiveSetBankChange(bankIndex: Int) {
        val bankNumber = (bankIndex + 1).toByte()
        sendMessage(byteArrayOf(
            0xF0.toByte(), // SysEx start
            0x43.toByte(), // Yamaha ID
            0x10.toByte(),
            0x7F.toByte(),
            0x1C.toByte(),
            0x07.toByte(),
            0x09.toByte(),
            0x00.toByte(),
            0x00.toByte(),
            bankNumber,
            0xF7.toByte()  // SysEx end
        ))
    }

    /**
     * Sends Transpose SysEx.
     *
     * SysEx format: F0 43 1n 7F 1C 07 00 00 07 [value] F7
     * where n = channel (0-15)
     * value = 64 + transpose (-11 to +11)
     */
    fun sendTranspose(channel: Int, transpose: Int) {
        val ch = (0x10 + (channel - 1).coerceIn(0, 15)).toByte()
        val value = (64 + transpose.coerceIn(-11, 11)).toByte()

        sendMessage(byteArrayOf(
            0xF0.toByte(),
            0x43.toByte(),
            ch,
            0x7F.toByte(),
            0x1C.toByte(),
            0x07.toByte(),
            0x00.toByte(),
            0x00.toByte(),
            0x07.toByte(),
            value,
            0xF7.toByte()
        ))
    }

    /**
     * Cleanup when manager is destroyed.
     */
    fun cleanup() {
        midiManager?.unregisterDeviceCallback(deviceCallback)
        disconnect()
    }
}