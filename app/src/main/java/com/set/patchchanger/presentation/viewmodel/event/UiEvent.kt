package com.set.patchchanger.presentation.viewmodel.event

/**
 * One-time UI events (like showing snackbars or playing audio).
 */
sealed class UiEvent {
    data class ShowMessage(val message: String) : UiEvent()
    object RequestFilePicker : UiEvent()
}