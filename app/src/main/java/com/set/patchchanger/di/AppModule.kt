package com.set.patchchanger.di

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.set.patchchanger.data.local.db.AppDatabase
import com.set.patchchanger.data.local.dao.BankDao
import com.set.patchchanger.data.local.entities.BankEntity
import com.set.patchchanger.data.local.dao.PageDao
import com.set.patchchanger.data.local.entities.PageEntity
import com.set.patchchanger.data.local.dao.PatchSlotDao
import com.set.patchchanger.data.local.entities.PatchSlotEntity
import com.set.patchchanger.data.repository.MidiRepositoryImpl
import com.set.patchchanger.data.repository.PatchRepositoryImpl
import com.set.patchchanger.data.repository.SampleRepositoryImpl
import com.set.patchchanger.data.repository.SettingsRepositoryImpl
import com.set.patchchanger.domain.model.DisplayNameType
import com.set.patchchanger.domain.model.PatchSlot
import com.set.patchchanger.domain.repository.AudioLibraryRepository
import com.set.patchchanger.domain.repository.MidiRepository
import com.set.patchchanger.domain.repository.PatchRepository
import com.set.patchchanger.domain.repository.SampleRepository
import com.set.patchchanger.domain.repository.SettingsRepository
import com.set.patchchanger.ui.theme.getDefaultColors
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Provider
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    // CHANGED: Removed @Inject constructor. The class is now created manually.
    internal class AppDatabaseCallback(
        private val patchSlotDao: Provider<PatchSlotDao>,
        private val bankDao: Provider<BankDao>,
        private val pageDao: Provider<PageDao>,
        private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {

        /**
         * This is called ONLY when the database is created for the first time.
         */
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            scope.launch(Dispatchers.IO) {
                populateDatabase()
            }
        }

        suspend fun populateDatabase() {
            val defaultSlots = generateDefaultSlots().map { it.toEntity() }
            val defaultBanks = (0..7).map { BankEntity(it, "User ${it + 1}") }
            val defaultPages = (0..15).map { PageEntity(it, "Page ${it + 1}") }

            bankDao.get().insertBanks(defaultBanks)
            pageDao.get().insertPages(defaultPages)
            patchSlotDao.get().insertSlots(defaultSlots)
        }

        private fun generateDefaultSlots(): List<PatchSlot> {
            val colors = getDefaultColors()
            val slots = mutableListOf<PatchSlot>()

            for (id in 0 until (8 * 16 * 16)) {
                val color = colors[id % 16]
                slots.add(PatchSlot.createDefault(id, color))
            }
            return slots
        }

        private fun PatchSlot.toEntity() = PatchSlotEntity(
            id = id,
            name = name,
            description = description,
            selected = selected,
            color = color,
            msb = msb,
            lsb = lsb,
            pc = pc,
            volume = volume,
            performanceName = performanceName,
            displayNameType = when (displayNameType) {
                DisplayNameType.PERFORMANCE -> "performance"
                DisplayNameType.CUSTOM -> "custom"
            },
            assignedSample = assignedSample
        )
    }

    @Provides
    @Singleton
    fun provideApplicationScope(): CoroutineScope {
        return CoroutineScope(SupervisorJob() + Dispatchers.Default)
    }

    @Provides
    @Singleton
    // CHANGED: Reverted the function signature.
    // It now takes the dependencies for the callback, not the callback itself.
    fun provideAppDatabase(
        @ApplicationContext context: Context,
        patchSlotDao: Provider<PatchSlotDao>,
        bankDao: Provider<BankDao>,
        pageDao: Provider<PageDao>,
        scope: CoroutineScope
    ): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "live_patch_controller_db"
        )
            .fallbackToDestructiveMigration()
            // CHANGED: We now create the callback instance manually right here.
            // This avoids the public/internal visibility conflict.
            .addCallback(AppDatabaseCallback(patchSlotDao, bankDao, pageDao, scope))
            .build()
    }

    @Provides
    @Singleton
    fun providePatchSlotDao(db: AppDatabase) = db.patchSlotDao()

    @Provides
    @Singleton
    fun provideBankDao(db: AppDatabase) = db.bankDao()

    @Provides
    @Singleton
    fun providePageDao(db: AppDatabase) = db.pageDao()

    @Provides
    @Singleton
    fun provideSampleDao(db: AppDatabase) = db.sampleDao()

    @Provides
    @Singleton
    fun provideAudioLibraryDao(db: AppDatabase) = db.audioLibraryDao()

    // ... (rest of the file is unchanged) ...

    @Provides
    @Singleton
    fun providePatchRepository(impl: PatchRepositoryImpl): PatchRepository = impl

    @Provides
    @Singleton
    fun provideSettingsRepository(impl: SettingsRepositoryImpl): SettingsRepository = impl

    @Provides
    @Singleton
    fun provideMidiRepository(impl: MidiRepositoryImpl): MidiRepository = impl

    @Provides
    @Singleton
    fun provideSampleRepository(impl: SampleRepositoryImpl): SampleRepository = impl

    @Provides
    @Singleton
    fun provideAudioLibraryRepository(impl: SampleRepositoryImpl): AudioLibraryRepository = impl
}