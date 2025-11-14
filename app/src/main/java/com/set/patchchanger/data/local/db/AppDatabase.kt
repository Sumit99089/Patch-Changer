package com.set.patchchanger.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.set.patchchanger.data.local.dao.AudioLibraryDao
import com.set.patchchanger.data.local.dao.BankDao
import com.set.patchchanger.data.local.dao.PageDao
import com.set.patchchanger.data.local.dao.PatchSlotDao
import com.set.patchchanger.data.local.dao.SampleDao
import com.set.patchchanger.data.local.entities.AudioLibraryEntity
import com.set.patchchanger.data.local.entities.BankEntity
import com.set.patchchanger.data.local.entities.PageEntity
import com.set.patchchanger.data.local.entities.PatchSlotEntity
import com.set.patchchanger.data.local.entities.SampleEntity

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
    abstract fun patchSlotDao(): PatchSlotDao
    abstract fun bankDao(): BankDao
    abstract fun pageDao(): PageDao
    abstract fun sampleDao(): SampleDao
    abstract fun audioLibraryDao(): AudioLibraryDao
}