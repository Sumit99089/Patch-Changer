package com.set.patchchanger.data.repository


import com.set.patchchanger.data.local.AppMidiManager
import com.set.patchchanger.domain.model.MidiConnectionState
import com.set.patchchanger.domain.repository.MidiRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * MIDI Repository Implementation
 */
@Singleton
class MidiRepositoryImpl @Inject constructor(
    private val midiManager: AppMidiManager
) : MidiRepository {

    override fun observeConnectionState(): Flow<MidiConnectionState> {
        return midiManager.connectionState
    }

    override suspend fun connect(deviceId: String?) {
        midiManager.connect()
    }

    override suspend fun disconnect() {
        midiManager.disconnect()
    }

    override suspend fun sendProgramChange(channel: Int, msb: Int, lsb: Int, pc: Int) {
        midiManager.sendProgramChange(channel, msb, lsb, pc)
    }

    override suspend fun sendLiveSetBankChange(bankIndex: Int) {
        midiManager.sendLiveSetBankChange(bankIndex)
    }

    override suspend fun sendTranspose(channel: Int, transpose: Int) {
        midiManager.sendTranspose(channel, transpose)
    }

    override suspend fun getAvailableDevices(): List<String> {
        return midiManager.getAvailableDevices().map { it.first }
    }

    override fun cleanup() {
        midiManager.cleanup()
    }
}