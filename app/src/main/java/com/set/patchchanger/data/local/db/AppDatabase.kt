package com.set.patchchanger.data.local.db

import androidx.room.RoomDatabase
import com.set.patchchanger.data.local.dao.AudioLibraryDao
import com.set.patchchanger.data.local.dao.BankDao
import com.set.patchchanger.data.local.dao.PageDao
import com.set.patchchanger.data.local.dao.PatchSlotDao
import com.set.patchchanger.data.local.dao.SampleDao

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