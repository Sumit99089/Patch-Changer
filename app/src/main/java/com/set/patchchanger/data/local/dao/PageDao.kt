package com.set.patchchanger.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.set.patchchanger.data.local.entities.PageEntity
import kotlinx.coroutines.flow.Flow

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
