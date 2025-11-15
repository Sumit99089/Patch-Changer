package com.set.patchchanger.presentation.viewmodel.event

import android.net.Uri
import com.set.patchchanger.domain.model.AppTheme
import com.set.patchchanger.domain.model.AudioLibraryItem
import com.set.patchchanger.domain.model.PatchSlot
import com.set.patchchanger.domain.model.Performance
import com.set.patchchanger.domain.model.SamplePad
import com.set.patchchanger.domain.model.SearchResult

/**
 * Events that the UI can trigger.
 *
 * Using sealed class for events ensures exhaustive when() statements.
 */
sealed class MainEvent {
    data class SelectSlot(val slotId: Int) : MainEvent()
    data class UpdateSlot(val slot: PatchSlot) : MainEvent()
    data class SwapSlots(val slot1Id: Int, val slot2Id: Int) : MainEvent()
    data class NavigateBank(val direction: Int) : MainEvent()
    data class NavigatePage(val direction: Int) : MainEvent()
    data class UpdateTranspose(val delta: Int) : MainEvent()
    object ResetTranspose : MainEvent()
    data class UpdateMidiChannel(val channel: Int) : MainEvent()
    data class UpdateTheme(val theme: AppTheme) : MainEvent()
    data class UpdateBankName(val index: Int, val name: String) : MainEvent()
    data class UpdatePageName(val index: Int, val name: String) : MainEvent()
    object ConnectMidi : MainEvent()
    object DisconnectMidi : MainEvent()

    // Search Events
    data class UpdateSearchQuery(val query: String) : MainEvent()
    data class GoToSearchResult(val result: SearchResult) : MainEvent()

    // Dialog Control
    data class ShowResetDialog(val show: Boolean) : MainEvent()
    data class ShowBankPageNameDialog(val show: Boolean) : MainEvent()
    data class ShowEditSampleDialog(val sample: SamplePad?) : MainEvent()
    data class ShowPasteConfirmDialog(val slot: PatchSlot?) : MainEvent()
    data class ShowClearConfirmDialog(val slot: PatchSlot?) : MainEvent()
    data class ShowSwapDialog(val slot: PatchSlot?) : MainEvent()
    data class ShowSlotColorDialog(val slot: PatchSlot?) : MainEvent()
    data class ShowSampleColorDialog(val sample: SamplePad?) : MainEvent()
    data class ShowAudioLibrary(val show: Boolean, val sampleId: Int = -1) : MainEvent()

    // Actions
    object ResetData : MainEvent()
    data class ImportData(val jsonData: String) : MainEvent()
    data class CopySlot(val slot: PatchSlot) : MainEvent()
    data class PasteSlot(val targetSlot: PatchSlot) : MainEvent()
    data class ClearSlot(val slot: PatchSlot) : MainEvent()

    // Sample Events
    data class UpdateSample(val sample: SamplePad) : MainEvent()
    data class ClearSampleAudio(val sampleId: Int) : MainEvent()
    object LoadSampleFile : MainEvent() // Triggers picker
    data class SetSampleFile(val uri: Uri, val name: String) : MainEvent() // From picker result
    data class AddFileToLibrary(val uri: Uri, val name: String) : MainEvent()
    data class DeleteFromAudioLibrary(val item: AudioLibraryItem) : MainEvent()
    data class SelectSampleFromLibrary(val item: AudioLibraryItem) : MainEvent()
    data class TriggerSample(val sampleId: Int) : MainEvent()

    // Performance Browser Events
    data class ShowPerformanceBrowser(val slot: PatchSlot) : MainEvent()
    object HidePerformanceBrowser : MainEvent()
    data class SelectPerformanceCategory(val category: String) : MainEvent()
    data class SelectPerformanceBank(val bankIndex: Int) : MainEvent()
    data class SelectPerformance(val performance: Performance) : MainEvent()
    data class UpdatePerformanceSearch(val query: String) : MainEvent()
}