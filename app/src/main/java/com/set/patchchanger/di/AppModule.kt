package com.set.patchchanger.di


import android.content.Context
import androidx.room.Room
import com.set.patchchanger.data.local.AppDatabase
import com.set.patchchanger.data.repository.MidiRepositoryImpl
import com.set.patchchanger.data.repository.PatchRepositoryImpl
import com.set.patchchanger.data.repository.SampleRepositoryImpl
import com.set.patchchanger.data.repository.SettingsRepositoryImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt Dependency Injection Module
 *
 * Hilt is a dependency injection library built on top of Dagger.
 * It automatically manages object creation and lifecycle.
 *
 * @Module marks this as a Hilt module
 * @InstallIn(SingletonComponent::class) means these dependencies
 * live for the entire app lifecycle
 */
@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    /**
     * Provides Room Database instance.
     *
     * @Provides tells Hilt this method provides a dependency
     * @Singleton ensures only one instance exists
     */
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
            .fallbackToDestructiveMigration() // Recreate DB on version change
            .build()
    }

    /**
     * Provides DAOs from the database
     */
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
     * Note: We bind interface to implementation.
     * This allows easy testing by swapping implementations.
     */
    @Provides
    @Singleton
    fun providePatchRepository(impl: PatchRepositoryImpl) = impl

    @Provides
    @Singleton
    fun provideSettingsRepository(impl: SettingsRepositoryImpl) = impl

    @Provides
    @Singleton
    fun provideMidiRepository(impl: MidiRepositoryImpl) = impl

    @Provides
    @Singleton
    fun provideSampleRepository(impl: SampleRepositoryImpl)= impl
}