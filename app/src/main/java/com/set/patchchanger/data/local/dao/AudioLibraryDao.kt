package com.set.patchchanger.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.set.patchchanger.data.local.entities.AudioLibraryEntity
import kotlinx.coroutines.flow.Flow

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

    @Query(
        """
        SELECT * FROM audio_library 
        WHERE LOWER(name) LIKE '%' || LOWER(:query) || '%'
        ORDER BY added_timestamp DESC
    """
    )
    suspend fun searchAudio(query: String): List<AudioLibraryEntity>
}