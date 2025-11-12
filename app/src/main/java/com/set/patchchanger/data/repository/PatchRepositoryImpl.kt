package com.set.patchchanger.data.repository

import android.content.Context
import com.example.livepatchcontroller.data.local.MidiManager
import com.example.livepatchcontroller.data.local.SettingsDataStore
import com.example.livepatchcontroller.data.local.database.*
import com.example.livepatchcontroller.domain.model.*
import com.example.livepatchcontroller.domain.repository.*
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.set.patchchanger.data.local.BankDao
import com.set.patchchanger.data.local.PageDao
import com.set.patchchanger.data.local.PatchSlotDao
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of PatchRepository using Room Database.
 *
 * This class bridges the domain layer (business logic) with
 * the data layer (Room database). It:
 * - Converts between domain models and database entities
 * - Handles database operations
 * - Provides Flow-based reactive data
 */
@Singleton
class PatchRepositoryImpl @Inject constructor(
    private val patchSlotDao: PatchSlotDao,
    private val bankDao: BankDao,
    private val pageDao: PageDao,
    @ApplicationContext private val context: Context
) : PatchRepository {

    /**
     * Observes complete patch data structure.
     *
     * combine() operator merges three Flows into one.
     * When any source Flow emits, the combined Flow emits.
     */
    override fun observePatchData(): Flow<PatchData> = combine(
        patchSlotDao.observeAllSlots(),
        bankDao.observeAllBanks(),
        pageDao.observeAllPages()
    ) { slots, banks, pages ->
        buildPatchData(slots, banks, pages)
    }

    override suspend fun getPatchData(): PatchData {
        val slots = patchSlotDao.getAllSlots()
        val banks = bankDao.getAllBanks()
        val pages = pageDao.getAllPages()
        return buildPatchData(slots, banks, pages)
    }

    /**
     * Builds PatchData from database entities.
     * Groups slots by bank and page.
     */
    private fun buildPatchData(
        slots: List<PatchSlotEntity>,
        banks: List<BankEntity>,
        pages: List<PageEntity>
    ): PatchData {
        val bankList = mutableListOf<Bank>()

        // Group slots by bank (0-7)
        for (bankIndex in 0..7) {
            val pageList = mutableListOf<Page>()

            // Group by page (0-15)
            for (pageIndex in 0..15) {
                val pageSlots = slots.filter { slot ->
                    (slot.id / 256) == bankIndex &&
                            ((slot.id / 16) % 16) == pageIndex
                }.sortedBy { it.id % 16 }
                    .map { it.toDomainModel() }

                pageList.add(Page(pageSlots))
            }

            val bankName = banks.getOrNull(bankIndex)?.name ?: "User ${bankIndex + 1}"
            bankList.add(Bank(bankName, pageList))
        }

        return PatchData(
            banks = bankList,
            bankNames = banks.map { it.name },
            pageNames = pages.map { it.name }
        )
    }

    override suspend fun updateSlot(slot: PatchSlot) {
        patchSlotDao.updateSlot(slot.toEntity())
    }

    override suspend fun updateSlots(slots: List<PatchSlot>) {
        patchSlotDao.updateSlots(slots.map { it.toEntity() })
    }

    override suspend fun updateBankName(index: Int, newName: String) {
        bankDao.updateBank(BankEntity(index, newName))
    }

    override suspend fun updatePageName(index: Int, newName: String) {
        pageDao.updatePage(PageEntity(index, newName))
    }

    override suspend fun resetToDefaults() {
        // Clear existing data
        patchSlotDao.deleteAll()
        bankDao.deleteAll()
        pageDao.deleteAll()

        // Generate default data
        val defaultSlots = generateDefaultSlots()
        val defaultBanks = (0..7).map { BankEntity(it, "User ${it + 1}") }
        val defaultPages = (0..15).map { PageEntity(it, "Page ${it + 1}") }

        // Insert defaults
        patchSlotDao.insertSlots(defaultSlots)
        bankDao.insertBanks(defaultBanks)
        pageDao.insertPages(defaultPages)
    }

    /**
     * Generates 2048 default slots (8 banks × 16 pages × 16 slots).
     */
    private fun generateDefaultSlots(): List<PatchSlotEntity> {
        val colors = getDefaultColors()
        val slots = mutableListOf<PatchSlotEntity>()

        for (bank in 0..7) {
            for (page in 0..15) {
                for (slot in 0..15) {
                    val id = bank * 256 + page * 16 + slot
                    slots.add(
                        PatchSlotEntity(
                            id = id,
                            name = "${slot + 1}",
                            description = "Slot ${slot + 1}",
                            selected = (id == 0), // First slot selected
                            color = colors[slot % colors.size],
                            msb = 62,
                            lsb = page,
                            pc = slot,
                            volume = 100,
                            performanceName = "Slot ${slot + 1}",
                            displayNameType = "custom",
                            assignedSample = -1
                        )
                    )
                }
            }
        }

        return slots
    }

    override suspend fun importFromJson(jsonData: String): Boolean {
        return try {
            val gson = Gson()
            val type = object : TypeToken<Map<String, Any>>() {}.type
            val data: Map<String, Any> = gson.fromJson(jsonData, type)

            // Extract and convert data
            @Suppress("UNCHECKED_CAST")
            val patchDataMap = data["patchData"] as? Map<String, Any>
            val banksData = data["banks"] as? List<String>
            val pagesData = data["pages"] as? List<String>

            if (patchDataMap != null && banksData != null && pagesData != null) {
                // Clear existing
                patchSlotDao.deleteAll()
                bankDao.deleteAll()
                pageDao.deleteAll()

                // Convert and insert
                val slots = mutableListOf<PatchSlotEntity>()
                patchDataMap.forEach { (_, bankData) ->
                    @Suppress("UNCHECKED_CAST")
                    val bd = bankData as Map<String, Any>
                    val pages = bd["pages"] as List<List<Map<String, Any>>>
                    pages.forEach { page ->
                        page.forEach { slotMap ->
                            slots.add(slotMapToEntity(slotMap))
                        }
                    }
                }

                patchSlotDao.insertSlots(slots)
                bankDao.insertBanks(banksData.mapIndexed { i, name -> BankEntity(i, name) })
                pageDao.insertPages(pagesData.mapIndexed { i, name -> PageEntity(i, name) })

                true
            } else {
                false
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    override suspend fun exportToJson(): String {
        val patchData = getPatchData()
        val gson = Gson()

        val exportData = mapOf(
            "patchData" to buildExportMap(patchData),
            "banks" to patchData.bankNames,
            "pages" to patchData.pageNames,
            "currentMidiChannel" to 1,
            "currentBankIndex" to 0,
            "currentPageIndex" to 0,
            "currentTranspose" to 0,
            "theme" to "black"
        )

        return gson.toJson(exportData)
    }

    private fun buildExportMap(patchData: PatchData): Map<String, Map<String, Any>> {
        val result = mutableMapOf<String, Map<String, Any>>()

        patchData.banks.forEachIndexed { index, bank ->
            result[patchData.bankNames[index]] = mapOf(
                "name" to patchData.bankNames[index],
                "pages" to bank.pages.map { page ->
                    page.slots.map { slot -> slotToMap(slot) }
                }
            )
        }

        return result
    }

    private fun slotToMap(slot: PatchSlot): Map<String, Any> = mapOf(
        "id" to slot.id,
        "name" to slot.name,
        "description" to slot.description,
        "selected" to slot.selected,
        "color" to slot.color,
        "msb" to slot.msb,
        "lsb" to slot.lsb,
        "pc" to slot.pc,
        "volume" to slot.volume,
        "performanceName" to slot.performanceName,
        "displayNameType" to slot.displayNameType.name.lowercase(),
        "assignedSample" to slot.assignedSample
    )

    private fun slotMapToEntity(map: Map<String, Any>): PatchSlotEntity {
        return PatchSlotEntity(
            id = (map["id"] as Double).toInt(),
            name = map["name"] as String,
            description = map["description"] as String,
            selected = map["selected"] as Boolean,
            color = map["color"] as String,
            msb = (map["msb"] as Double).toInt(),
            lsb = (map["lsb"] as Double).toInt(),
            pc = (map["pc"] as Double).toInt(),
            volume = (map["volume"] as Double).toInt(),
            performanceName = map["performanceName"] as String,
            displayNameType = map["displayNameType"] as String,
            assignedSample = (map["assignedSample"] as Double).toInt()
        )
    }

    override suspend fun searchSlots(query: String): List<SearchResult> {
        val slots = patchSlotDao.searchSlots(query)
        val banks = bankDao.getAllBanks()
        val pages = pageDao.getAllPages()

        return slots.map { slot ->
            val bankIndex = slot.id / 256
            val pageIndex = (slot.id / 16) % 16

            SearchResult(
                slot = slot.toDomainModel(),
                bankIndex = bankIndex,
                pageIndex = pageIndex,
                bankName = banks.getOrNull(bankIndex)?.name ?: "User ${bankIndex + 1}",
                pageName = pages.getOrNull(pageIndex)?.name ?: "Page ${pageIndex + 1}"
            )
        }
    }

    private fun getDefaultColors(): List<String> = listOf(
        "#333333", "#F44336", "#FFEB3B", "#4CAF50",
        "#2196F3", "#00BCD4", "#E91E63", "#FF9800",
        "#9C27B0", "#F8BBD0", "#FFECB3", "#CDDC39",
        "#B2EBF2", "#D7CCC8", "#B2DFDB", "#D1C4E9"
    )
}

/**
 * Extension functions to convert between domain models and database entities.
 *
 * These are in a separate section for clarity and reusability.
 */
private fun PatchSlotEntity.toDomainModel() = PatchSlot(
    id = id,
    name = name,
    description = description,
    selected = selected,
    color = color,
    msb = msb,
    lsb = lsb,
    pc = pc,
    volume = volume,
    performanceName = performanceName,
    displayNameType = when (displayNameType) {
        "performance" -> DisplayNameType.PERFORMANCE
        else -> DisplayNameType.CUSTOM
    },
    assignedSample = assignedSample
)

private fun PatchSlot.toEntity() = PatchSlotEntity(
    id = id,
    name = name,
    description = description,
    selected = selected,
    color = color,
    msb = msb,
    lsb = lsb,
    pc = pc,
    volume = volume,
    performanceName = performanceName,
    displayNameType = when (displayNameType) {
        DisplayNameType.PERFORMANCE -> "performance"
        DisplayNameType.CUSTOM -> "custom"
    },
    assignedSample = assignedSample
)

/**
 * Settings Repository Implementation
 */
@Singleton
class SettingsRepositoryImpl @Inject constructor(
    private val settingsDataStore: SettingsDataStore
) : SettingsRepository {

    override fun observeSettings(): Flow<AppSettings> = settingsDataStore.settingsFlow

    override suspend fun getSettings(): AppSettings {
        return settingsDataStore.settingsFlow.first()
    }

    override suspend fun updateSettings(settings: AppSettings) {
        settingsDataStore.updateSettings(settings)
    }

    override suspend fun updateBankIndex(index: Int) {
        settingsDataStore.updateBankIndex(index)
    }

    override suspend fun updatePageIndex(index: Int) {
        settingsDataStore.updatePageIndex(index)
    }

    override suspend fun updateMidiChannel(channel: Int) {
        settingsDataStore.updateMidiChannel(channel)
    }

    override suspend fun updateTranspose(transpose: Int) {
        settingsDataStore.updateTranspose(transpose)
    }

    override suspend fun updateTheme(theme: AppTheme) {
        settingsDataStore.updateTheme(theme)
    }
}

/**
 * MIDI Repository Implementation
 */
@Singleton
class MidiRepositoryImpl @Inject constructor(
    private val midiManager: MidiManager
) : MidiRepository {

    override fun observeConnectionState(): Flow<MidiConnectionState> {
        return midiManager.connectionState
    }

    override suspend fun connect(deviceId: String?) {
        // In Android, we use MidiDeviceInfo instead of string ID
        // For simplicity, auto-connect to first available device
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
}

/**
 * Sample Repository Implementation with audio file management
 */
@Singleton
class SampleRepositoryImpl @Inject constructor(
    private val sampleDao: SampleDao,
    @ApplicationContext private val context: Context
) : SampleRepository {

    private val audioDir = File(context.filesDir, "sample_audio").apply {
        if (!exists()) mkdirs()
    }

    override fun observeSamples(): Flow<List<SamplePad>> {
        return sampleDao.observeAllSamples().map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    override suspend fun getSamples(): List<SamplePad> {
        return sampleDao.getAllSamples().map { it.toDomainModel() }
    }

    override suspend fun updateSample(sample: SamplePad) {
        sampleDao.updateSample(sample.toEntity())
    }

    override suspend fun clearSampleAudio(sampleId: Int) {
        val sample = sampleDao.getSampleById(sampleId)
        sample?.audioFileName?.let { fileName ->
            File(audioDir, fileName).delete()
        }
        sample?.let {
            sampleDao.updateSample(
                it.copy(audioFileName = null, sourceName = null)
            )
        }
    }

    override suspend fun saveSampleAudio(sampleId: Int, sourceFile: File): String {
        val fileName = "sample_${sampleId}_${System.currentTimeMillis()}.audio"
        val destFile = File(audioDir, fileName)
        sourceFile.copyTo(destFile, overwrite = true)
        return fileName
    }

    override suspend fun resetSamples() {
        sampleDao.deleteAll()
        val defaultSamples = listOf(
            SampleEntity(0, "S1", 80, false, "#008B8B", null, null),
            SampleEntity(1, "S2", 80, false, "#F50057", null, null),
            SampleEntity(2, "S3", 80, false, "#00C853", null, null),
            SampleEntity(3, "S4", 80, false, "#D500F9", null, null)
        )
        sampleDao.insertSamples(defaultSamples)
    }
}

private fun SampleEntity.toDomainModel() = SamplePad(
    id = id,
    name = name,
    volume = volume,
    loop = loop,
    color = color,
    audioFileName = audioFileName,
    sourceName = sourceName
)

private fun SamplePad.toEntity() = SampleEntity(
    id = id,
    name = name,
    volume = volume,
    loop = loop,
    color = color,
    audioFileName = audioFileName,
    sourceName = sourceName
)