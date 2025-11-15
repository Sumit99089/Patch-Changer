package com.set.patchchanger.domain.repository

import com.set.patchchanger.domain.model.AppSettings
import com.set.patchchanger.domain.model.AppTheme
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    fun observeSettings(): Flow<AppSettings>
    suspend fun getSettings(): AppSettings
    suspend fun updateSettings(settings: AppSettings)
    suspend fun updateBankIndex(index: Int)
    suspend fun updatePageIndex(index: Int)
    suspend fun updateMidiChannel(channel: Int)
    suspend fun updateTranspose(transpose: Int)
    suspend fun updateTheme(theme: AppTheme)
}