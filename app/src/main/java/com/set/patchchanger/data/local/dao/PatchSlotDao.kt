package com.set.patchchanger.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.set.patchchanger.data.local.entities.PatchSlotEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PatchSlotDao {
    @Query("SELECT * FROM patch_slots ORDER BY id ASC")
    fun observeAllSlots(): Flow<List<PatchSlotEntity>>

    @Query("SELECT * FROM patch_slots ORDER BY id ASC")
    suspend fun getAllSlots(): List<PatchSlotEntity>

    @Query("SELECT * FROM patch_slots WHERE id = :slotId")
    suspend fun getSlotById(slotId: Int): PatchSlotEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSlot(slot: PatchSlotEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSlots(slots: List<PatchSlotEntity>)

    @Update
    suspend fun updateSlot(slot: PatchSlotEntity)

    @Update
    suspend fun updateSlots(slots: List<PatchSlotEntity>)

    @Query("DELETE FROM patch_slots")
    suspend fun deleteAll()

    @Query(
        """
        SELECT * FROM patch_slots 
        WHERE LOWER(name) LIKE '%' || LOWER(:query) || '%'
        OR LOWER(performance_name) LIKE '%' || LOWER(:query) || '%'
        ORDER BY id ASC
    """
    )
    suspend fun searchSlots(query: String): List<PatchSlotEntity>

    /**
     * Efficiently updates the selected state.
     * Sets 'selected' to true for the given slotId and false for all others.
     */
    @Query("UPDATE patch_slots SET selected = CASE WHEN id = :slotId THEN 1 ELSE 0 END")
    suspend fun setSelectedSlot(slotId: Int)
}