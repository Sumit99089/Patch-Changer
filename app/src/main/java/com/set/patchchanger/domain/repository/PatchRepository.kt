package com.set.patchchanger.domain.repository

import com.set.patchchanger.domain.model.PatchData
import com.set.patchchanger.domain.model.PatchSlot
import com.set.patchchanger.domain.model.SearchResult
import kotlinx.coroutines.flow.Flow

interface PatchRepository {
    fun observePatchData(): Flow<PatchData>
    suspend fun getPatchData(): PatchData
    suspend fun updateSlot(slot: PatchSlot)
    suspend fun updateSlots(slots: List<PatchSlot>)
    suspend fun updateBankName(index: Int, newName: String)
    suspend fun updatePageName(index: Int, newName: String)
    suspend fun resetToDefaults()
    suspend fun importFromJson(jsonData: String): Boolean
    suspend fun exportToJson(): String
    suspend fun searchSlots(query: String): List<SearchResult>
}