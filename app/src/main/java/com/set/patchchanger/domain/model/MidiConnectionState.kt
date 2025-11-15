package com.set.patchchanger.domain.model

sealed class MidiConnectionState {
    object Disconnected : MidiConnectionState()
    data class Connected(val deviceName: String) : MidiConnectionState()
    data class Error(val message: String) : MidiConnectionState()
}