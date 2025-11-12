package com.set.patchchanger.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

/**
 * Room Database Entity for patch slots.
 *
 * Room is Android's SQL database abstraction layer.
 * It provides compile-time verification of SQL queries and
 * automatic conversion between Kotlin objects and database rows.
 *
 * @Entity annotation marks this as a database table
 * @PrimaryKey marks the primary key column
 */
@Entity(tableName = "patch_slots")
data class PatchSlotEntity(
    @PrimaryKey val id: Int,
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "description") val description: String,
    @ColumnInfo(name = "selected") val selected: Boolean,
    @ColumnInfo(name = "color") val color: String,
    @ColumnInfo(name = "msb") val msb: Int,
    @ColumnInfo(name = "lsb") val lsb: Int,
    @ColumnInfo(name = "pc") val pc: Int,
    @ColumnInfo(name = "volume") val volume: Int,
    @ColumnInfo(name = "performance_name") val performanceName: String,
    @ColumnInfo(name = "display_name_type") val displayNameType: String,
    @ColumnInfo(name = "assigned_sample") val assignedSample: Int
)

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
    @Query("""
        SELECT * FROM patch_slots 
        WHERE LOWER(name) LIKE '%' || LOWER(:query) || '%'
        OR LOWER(performance_name) LIKE '%' || LOWER(:query) || '%'
        ORDER BY id ASC
    """)
    suspend fun searchSlots(query: String): List<PatchSlotEntity>
}

/**
 * Entity for bank names.
 */
@Entity(tableName = "banks")
data class BankEntity(
    @PrimaryKey val index: Int,
    @ColumnInfo(name = "name") val name: String
)

@Dao
interface BankDao {
    @Query("SELECT * FROM banks ORDER BY `index` ASC")
    fun observeAllBanks(): Flow<List<BankEntity>>

    @Query("SELECT * FROM banks ORDER BY `index` ASC")
    suspend fun getAllBanks(): List<BankEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBanks(banks: List<BankEntity>)

    @Update
    suspend fun updateBank(bank: BankEntity)

    @Query("DELETE FROM banks")
    suspend fun deleteAll()
}

/**
 * Entity for page names.
 */
@Entity(tableName = "pages")
data class PageEntity(
    @PrimaryKey val index: Int,
    @ColumnInfo(name = "name") val name: String
)

@Dao
interface PageDao {
    @Query("SELECT * FROM pages ORDER BY `index` ASC")
    fun observeAllPages(): Flow<List<PageEntity>>

    @Query("SELECT * FROM pages ORDER BY `index` ASC")
    suspend fun getAllPages(): List<PageEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPages(pages: List<PageEntity>)

    @Update
    suspend fun updatePage(page: PageEntity)

    @Query("DELETE FROM pages")
    suspend fun deleteAll()
}

/**
 * Entity for sample pad configurations.
 */
@Entity(tableName = "samples")
data class SampleEntity(
    @PrimaryKey val id: Int,
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "volume") val volume: Int,
    @ColumnInfo(name = "loop") val loop: Boolean,
    @ColumnInfo(name = "color") val color: String,
    @ColumnInfo(name = "audio_file_name") val audioFileName: String?,
    @ColumnInfo(name = "source_name") val sourceName: String?
)

@Dao
interface SampleDao {
    @Query("SELECT * FROM samples ORDER BY id ASC")
    fun observeAllSamples(): Flow<List<SampleEntity>>

    @Query("SELECT * FROM samples ORDER BY id ASC")
    suspend fun getAllSamples(): List<SampleEntity>

    @Query("SELECT * FROM samples WHERE id = :sampleId")
    suspend fun getSampleById(sampleId: Int): SampleEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSamples(samples: List<SampleEntity>)

    @Update
    suspend fun updateSample(sample: SampleEntity)

    @Query("DELETE FROM samples")
    suspend fun deleteAll()
}

/**
 * Entity for audio library items.
 */
@Entity(tableName = "audio_library")
data class AudioLibraryEntity(
    @PrimaryKey val name: String,
    @ColumnInfo(name = "file_path") val filePath: String,
    @ColumnInfo(name = "size_bytes") val sizeBytes: Long,
    @ColumnInfo(name = "duration_ms") val durationMs: Long,
    @ColumnInfo(name = "added_timestamp") val addedTimestamp: Long
)

@Dao
interface AudioLibraryDao {
    @Query("SELECT * FROM audio_library ORDER BY added_timestamp DESC")
    fun observeAllAudio(): Flow<List<AudioLibraryEntity>>

    @Query("SELECT * FROM audio_library ORDER BY added_timestamp DESC")
    suspend fun getAllAudio(): List<AudioLibraryEntity>

    @Query("SELECT * FROM audio_library WHERE name = :name")
    suspend fun getAudioByName(name: String): AudioLibraryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAudio(audio: AudioLibraryEntity)

    @Delete
    suspend fun deleteAudio(audio: AudioLibraryEntity)

    @Query("DELETE FROM audio_library")
    suspend fun deleteAll()

    @Query("""
        SELECT * FROM audio_library 
        WHERE LOWER(name) LIKE '%' || LOWER(:query) || '%'
        ORDER BY added_timestamp DESC
    """)
    suspend fun searchAudio(query: String): List<AudioLibraryEntity>
}

/**
 * The main Room Database class.
 *
 * @Database annotation specifies:
 * - entities: List of entity classes
 * - version: Database version (increment when schema changes)
 * - exportSchema: Whether to export schema for version control
 */
@Database(
    entities = [
        PatchSlotEntity::class,
        BankEntity::class,
        PageEntity::class,
        SampleEntity::class,
        AudioLibraryEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    /**
     * Room automatically implements these abstract methods.
     */
    abstract fun patchSlotDao(): PatchSlotDao
    abstract fun bankDao(): BankDao
    abstract fun pageDao(): PageDao
    abstract fun sampleDao(): SampleDao
    abstract fun audioLibraryDao(): AudioLibraryDao
}