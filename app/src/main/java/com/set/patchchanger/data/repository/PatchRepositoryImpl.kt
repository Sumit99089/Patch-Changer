package com.set.patchchanger.data.repository

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.set.patchchanger.data.local.BankDao
import com.set.patchchanger.data.local.BankEntity
import com.set.patchchanger.data.local.PageDao
import com.set.patchchanger.data.local.PageEntity
import com.set.patchchanger.data.local.PatchSlotDao
import com.set.patchchanger.data.local.PatchSlotEntity
import com.set.patchchanger.domain.model.Bank
import com.set.patchchanger.domain.model.DisplayNameType
import com.set.patchchanger.domain.model.Page
import com.set.patchchanger.domain.model.PatchData
import com.set.patchchanger.domain.model.PatchSlot
import com.set.patchchanger.domain.model.SearchResult
import com.set.patchchanger.domain.repository.PatchRepository
import com.set.patchchanger.ui.theme.getDefaultColors
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of PatchRepository using Room Database.
 */
@Singleton
class PatchRepositoryImpl @Inject constructor(
    private val patchSlotDao: PatchSlotDao,
    private val bankDao: BankDao,
    private val pageDao: PageDao,
    @ApplicationContext private val context: Context
) : PatchRepository {

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

    private fun buildPatchData(
        slots: List<PatchSlotEntity>,
        banks: List<BankEntity>,
        pages: List<PageEntity>
    ): PatchData {
        val bankList = mutableListOf<Bank>()
        val bankNameMap = banks.associateBy { it.index }
        val pageNameMap = pages.associateBy { it.index }

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

            val bankName = bankNameMap[bankIndex]?.name ?: "User ${bankIndex + 1}"
            bankList.add(Bank(bankName, pageList))
        }

        return PatchData(
            banks = bankList,
            bankNames = (0..7).map { bankNameMap[it]?.name ?: "User ${it + 1}" },
            pageNames = (0..15).map { pageNameMap[it]?.name ?: "Page ${it + 1}" }
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
        patchSlotDao.deleteAll()
        bankDao.deleteAll()
        pageDao.deleteAll()

        val defaultSlots = generateDefaultSlots()
        val defaultBanks = (0..7).map { BankEntity(it, "User ${it + 1}") }
        val defaultPages = (0..15).map { PageEntity(it, "Page ${it + 1}") }

        patchSlotDao.insertSlots(defaultSlots.map { it.toEntity() })
        bankDao.insertBanks(defaultBanks)
        pageDao.insertPages(defaultPages)
    }

    private fun generateDefaultSlots(): List<PatchSlot> {
        val colors = getDefaultColors()
        val slots = mutableListOf<PatchSlot>()

        for (id in 0 until (8 * 16 * 16)) {
            val color = colors[id % 16]
            slots.add(PatchSlot.createDefault(id, color))
        }
        return slots
    }

    override suspend fun importFromJson(jsonData: String): Boolean {
        return try {
            val gson = Gson()
            val type = object : TypeToken<Map<String, Any>>() {}.type
            val data: Map<String, Any> = gson.fromJson(jsonData, type)

            @Suppress("UNCHECKED_CAST")
            val patchDataMap = data["patchData"] as? Map<String, Any>
            val banksData = data["banks"] as? List<String>
            val pagesData = data["pages"] as? List<String>

            if (patchDataMap != null && banksData != null && pagesData != null) {
                patchSlotDao.deleteAll()
                bankDao.deleteAll()
                pageDao.deleteAll()

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

        val bankNameMap = banks.associateBy { it.index }
        val pageNameMap = pages.associateBy { it.index }

        return slots.map { slot ->
            val bankIndex = slot.id / 256
            val pageIndex = (slot.id / 16) % 16

            SearchResult(
                slot = slot.toDomainModel(),
                bankIndex = bankIndex,
                pageIndex = pageIndex,
                bankName = bankNameMap[bankIndex]?.name ?: "User ${bankIndex + 1}",
                pageName = pageNameMap[pageIndex]?.name ?: "Page ${pageIndex + 1}"
            )
        }
    }
}

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