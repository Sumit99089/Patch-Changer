package com.set.patchchanger.di

import android.content.Context
import androidx.room.Room
import com.set.patchchanger.data.local.AppDatabase
import com.set.patchchanger.data.repository.MidiRepositoryImpl
import com.set.patchchanger.data.repository.PatchRepositoryImpl
import com.set.patchchanger.data.repository.SampleRepositoryImpl
import com.set.patchchanger.data.repository.SettingsRepositoryImpl
import com.set.patchchanger.domain.repository.AudioLibraryRepository
import com.set.patchchanger.domain.repository.MidiRepository
import com.set.patchchanger.domain.repository.PatchRepository
import com.set.patchchanger.domain.repository.SampleRepository
import com.set.patchchanger.domain.repository.SettingsRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideAppDatabase(
        @ApplicationContext context: Context
    ): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "live_patch_controller_db"
        )
            .fallbackToDestructiveMigration()
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

    /**
     * Provides repository implementations
     *
     * We explicitly state the Interface as the return type.
     * Hilt knows how to create the 'impl' parameter because
     * it has an @Inject constructor.
     */
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

    // Your SampleRepositoryImpl also implements AudioLibraryRepository,
    // so we should provide a binding for that interface as well.
    @Provides
    @Singleton
    fun provideAudioLibraryRepository(impl: SampleRepositoryImpl): AudioLibraryRepository = impl
}