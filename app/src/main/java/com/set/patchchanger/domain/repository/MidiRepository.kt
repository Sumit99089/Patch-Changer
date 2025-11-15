package com.set.patchchanger.domain.repository

import com.set.patchchanger.domain.model.MidiConnectionState
import kotlinx.coroutines.flow.Flow

interface MidiRepository {
    fun observeConnectionState(): Flow<MidiConnectionState>
    suspend fun connect(deviceId: String? = null)
    suspend fun disconnect()
    suspend fun sendProgramChange(channel: Int, msb: Int, lsb: Int, pc: Int)
    suspend fun sendLiveSetBankChange(bankIndex: Int)
    suspend fun sendTranspose(channel: Int, transpose: Int)
    suspend fun getAvailableDevices(): List<String>
    fun cleanup()
}