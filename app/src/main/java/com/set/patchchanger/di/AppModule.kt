package com.set.patchchanger.di

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.set.patchchanger.data.local.db.AppDatabase
import com.set.patchchanger.data.local.entities.BankEntity
import com.set.patchchanger.data.local.entities.PageEntity
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

    // FIX: Inject Provider<AppDatabase> to break circular dependency
    internal class AppDatabaseCallback(
        private val databaseProvider: Provider<AppDatabase>,
        private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {

        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            scope.launch(Dispatchers.IO) {
                populateDatabase()
            }
        }

        private suspend fun populateDatabase() {
            // Lazy retrieval of database and DAOs prevents cycle during build()
            val database = databaseProvider.get()
            val patchSlotDao = database.patchSlotDao()
            val bankDao = database.bankDao()
            val pageDao = database.pageDao()

            val defaultSlots = generateDefaultSlots().map { it.toEntity() }
            val defaultBanks = (0..7).map { BankEntity(it, "User ${it + 1}") }
            val defaultPages = (0..15).map { PageEntity(it, "Page ${it + 1}") }

            bankDao.insertBanks(defaultBanks)
            pageDao.insertPages(defaultPages)
            patchSlotDao.insertSlots(defaultSlots)
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
    fun provideAppDatabase(
        @ApplicationContext context: Context,
        // We inject the provider of the DB into the DB builder itself
        provider: Provider<AppDatabase>,
        scope: CoroutineScope
    ): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "live_patch_controller_db"
        )
            .fallbackToDestructiveMigration(false)
            .addCallback(AppDatabaseCallback(provider, scope))
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