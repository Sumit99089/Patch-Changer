package com.set.patchchanger.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.set.patchchanger.data.local.entities.PatchSlotEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object (DAO) for patch slots.
 *
 * DAOs define the methods for database access.
 * Room generates the implementation automatically.
 */
@Dao
interface PatchSlotDao {
    /**
     * Flow-based query for reactive UI updates.
     * When data changes, the Flow emits new values automatically.
     */
    @Query("SELECT * FROM patch_slots ORDER BY id ASC")
    fun observeAllSlots(): Flow<List<PatchSlotEntity>>

    /**
     * Synchronous query to get all slots.
     */
    @Query("SELECT * FROM patch_slots ORDER BY id ASC")
    suspend fun getAllSlots(): List<PatchSlotEntity>

    /**
     * Get a single slot by ID.
     */
    @Query("SELECT * FROM patch_slots WHERE id = :slotId")
    suspend fun getSlotById(slotId: Int): PatchSlotEntity?

    /**
     * Insert or replace a single slot.
     * OnConflictStrategy.REPLACE updates if ID exists.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSlot(slot: PatchSlotEntity)

    /**
     * Insert multiple slots efficiently.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSlots(slots: List<PatchSlotEntity>)

    /**
     * Update a slot.
     */
    @Update
    suspend fun updateSlot(slot: PatchSlotEntity)

    /**
     * Update multiple slots in a transaction.
     */
    @Update
    suspend fun updateSlots(slots: List<PatchSlotEntity>)

    /**
     * Delete all slots (for reset).
     */
    @Query("DELETE FROM patch_slots")
    suspend fun deleteAll()

    /**
     * Search slots by name or performance name.
     */
    @Query(
        """
        SELECT * FROM patch_slots 
        WHERE LOWER(name) LIKE '%' || LOWER(:query) || '%'
        OR LOWER(performance_name) LIKE '%' || LOWER(:query) || '%'
        ORDER BY id ASC
    """
    )
    suspend fun searchSlots(query: String): List<PatchSlotEntity>
}
