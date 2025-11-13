package com.set.patchchanger.data.repository

import com.set.patchchanger.data.local.SettingsDataStore
import com.set.patchchanger.domain.model.AppSettings
import com.set.patchchanger.domain.model.AppTheme
import com.set.patchchanger.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsRepositoryImpl @Inject constructor(
    private val dataStore: SettingsDataStore
) : SettingsRepository {

    override fun observeSettings(): Flow<AppSettings> {
        return dataStore.settingsFlow
    }

    override suspend fun getSettings(): AppSettings {
        // Use .first() to get the current value from the Flow
        return dataStore.settingsFlow.first()
    }

    override suspend fun updateSettings(settings: AppSettings) {
        dataStore.updateSettings(settings)
    }

    override suspend fun updateBankIndex(index: Int) {
        dataStore.updateBankIndex(index)
    }

    override suspend fun updatePageIndex(index: Int) {
        dataStore.updatePageIndex(index)
    }

    override suspend fun updateMidiChannel(channel: Int) {
        dataStore.updateMidiChannel(channel)
    }

    override suspend fun updateTranspose(transpose: Int) {
        dataStore.updateTranspose(transpose)
    }

    override suspend fun updateTheme(theme: AppTheme) {
        dataStore.updateTheme(theme)
    }
}