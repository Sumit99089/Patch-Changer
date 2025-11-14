package com.set.patchchanger.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.set.patchchanger.data.local.entities.SampleEntity
import kotlinx.coroutines.flow.Flow

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