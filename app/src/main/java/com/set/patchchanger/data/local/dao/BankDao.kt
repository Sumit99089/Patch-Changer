package com.set.patchchanger.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.set.patchchanger.data.local.entities.BankEntity
import kotlinx.coroutines.flow.Flow

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