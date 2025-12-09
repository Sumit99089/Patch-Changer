package com.set.patchchanger.presentation.viewmodel.event

import android.net.Uri
import com.set.patchchanger.domain.model.AppTheme
import com.set.patchchanger.domain.model.AudioLibraryItem
import com.set.patchchanger.domain.model.PatchSlot
import com.set.patchchanger.domain.model.Performance
import com.set.patchchanger.domain.model.SamplePad
import com.set.patchchanger.domain.model.SearchResult
import java.io.File

sealed class MainEvent {
    // Navigation & Selection
    data class SelectSlot(val slotId: Int) : MainEvent()
    data class UpdateSlot(val slot: PatchSlot) : MainEvent()
    data class SwapSlots(val slot1Id: Int, val slot2Id: Int) : MainEvent()
    data class NavigateBank(val direction: Int) : MainEvent()
    data class NavigatePage(val direction: Int) : MainEvent()

    // Global Settings
    data class UpdateTranspose(val delta: Int) : MainEvent()
    object ResetTranspose : MainEvent()
    data class UpdateMidiChannel(val channel: Int) : MainEvent()

    // Theme
    data class UpdateTheme(val theme: AppTheme) : MainEvent()
    object CycleTheme : MainEvent() // <--- NEW: Logic to cycle themes like HTML

    // Naming
    data class UpdateBankName(val index: Int, val name: String) : MainEvent()
    data class UpdatePageName(val index: Int, val name: String) : MainEvent()

    // MIDI
    object ConnectMidi : MainEvent()
    object DisconnectMidi : MainEvent()

    // Search
    data class UpdateSearchQuery(val query: String) : MainEvent()
    data class GoToSearchResult(val result: SearchResult) : MainEvent()

    // Dialog Visibility
    data class ShowResetDialog(val show: Boolean) : MainEvent()
    data class ShowBankPageNameDialog(val show: Boolean) : MainEvent()
    data class ShowEditSampleDialog(val sample: SamplePad?) : MainEvent()
    data class ShowPasteConfirmDialog(val slot: PatchSlot?) : MainEvent()
    data class ShowClearConfirmDialog(val slot: PatchSlot?) : MainEvent()
    data class ShowSwapDialog(val slot: PatchSlot?) : MainEvent()
    data class ShowSlotColorDialog(val slot: PatchSlot?) : MainEvent()
    data class ShowSampleColorDialog(val sample: SamplePad?) : MainEvent()
    data class ShowAudioLibrary(val show: Boolean, val sampleId: Int = -1) : MainEvent()

    // File IO
    data class ShowLoadFileDialog(val show: Boolean) : MainEvent()
    object ResetData : MainEvent()
    object RequestExportData : MainEvent()
    object RequestImportData : MainEvent()
    data class PerformExport(val uri: Uri) : MainEvent()
    data class PerformImport(val uri: Uri) : MainEvent()
    data class LoadSelectedFile(val file: File) : MainEvent()
    data class ImportData(val jsonData: String) : MainEvent()

    // Actions
    data class CopySlot(val slot: PatchSlot) : MainEvent()
    data class PasteSlot(val targetSlot: PatchSlot) : MainEvent()
    data class ClearSlot(val slot: PatchSlot) : MainEvent()

    // Sample Management
    data class UpdateSample(val sample: SamplePad) : MainEvent()
    data class ClearSampleAudio(val sampleId: Int) : MainEvent()
    object LoadSampleFile : MainEvent()
    data class SetSampleFile(val uri: Uri, val name: String) : MainEvent()
    data class AddFileToLibrary(val uri: Uri, val name: String) : MainEvent()
    data class DeleteFromAudioLibrary(val item: AudioLibraryItem) : MainEvent()
    data class SelectSampleFromLibrary(val item: AudioLibraryItem) : MainEvent()
    data class TriggerSample(val sampleId: Int) : MainEvent()

    // Performance Browser
    data class ShowPerformanceBrowser(val slot: PatchSlot) : MainEvent()
    object HidePerformanceBrowser : MainEvent()
    data class SelectPerformanceCategory(val category: String) : MainEvent()
    data class SelectPerformanceBank(val bankIndex: Int) : MainEvent()
    data class SelectPerformance(val performance: Performance) : MainEvent()
    data class UpdatePerformanceSearch(val query: String) : MainEvent()
}