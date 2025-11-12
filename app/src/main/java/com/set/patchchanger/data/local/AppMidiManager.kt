package com.set.patchchanger.data.local

import android.content.Context
import android.media.midi.*
import com.set.patchchanger.domain.model.MidiConnectionState
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages MIDI device connections and communication.
 *
 * Renamed to AppMidiManager to avoid conflict with android.media.midi.MidiManager
 */
@Singleton
class AppMidiManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    // Explicitly use the system MidiManager type
    private val systemMidiManager: MidiManager? =
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
        systemMidiManager?.registerDeviceCallback(deviceCallback, null)
    }

    /**
     * Gets list of available MIDI devices.
     */
    fun getAvailableDevices(): List<Pair<String, MidiDeviceInfo>> {
        val devices = systemMidiManager?.devices ?: return emptyList()
        return devices.mapNotNull { info ->
            // Filter out "Through" and virtual ports
            val name = info.properties.getString(MidiDeviceInfo.PROPERTY_NAME) ?: return@mapNotNull null
            if (name.contains("through", ignoreCase = true)) return@mapNotNull null

            // CHANGED: Check for inputPortCount because we want to send data TO the device
            if (info.inputPortCount == 0) return@mapNotNull null

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
        systemMidiManager?.openDevice(targetDevice, { device ->
            if (device == null) {
                _connectionState.value = MidiConnectionState.Error("Failed to open device")
                return@openDevice
            }

            // CHANGED: openInputPort instead of openOutputPort
            // In Android MIDI API, we open an InputPort on the device to write data TO it.
            val port = device.openInputPort(0)

            if (port == null) {
                device.close()
                _connectionState.value = MidiConnectionState.Error("No input ports available")
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
        systemMidiManager?.unregisterDeviceCallback(deviceCallback)
        disconnect()
    }
}