package com.set.patchchanger.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.set.patchchanger.domain.model.AppSettings
import com.set.patchchanger.domain.model.AppTheme
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * DataStore extension for Context.
 */
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
    name = "app_settings"
)

/**
 * Manages app settings persistence using DataStore.
 */
@Singleton
class SettingsDataStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    /**
     * Preference keys for type-safe access.
     */
    private object PreferencesKeys {
        val BANK_INDEX = intPreferencesKey("bank_index")
        val PAGE_INDEX = intPreferencesKey("page_index")
        val MIDI_CHANNEL = intPreferencesKey("midi_channel")
        val TRANSPOSE = intPreferencesKey("transpose")
        val THEME = stringPreferencesKey("theme")
    }

    /**
     * Observes settings as a Flow.
     */
    val settingsFlow: Flow<AppSettings> = context.dataStore.data
        .map { preferences ->
            AppSettings(
                currentBankIndex = preferences[PreferencesKeys.BANK_INDEX] ?: 0,
                currentPageIndex = preferences[PreferencesKeys.PAGE_INDEX] ?: 0,
                currentMidiChannel = preferences[PreferencesKeys.MIDI_CHANNEL] ?: 1,
                currentTranspose = preferences[PreferencesKeys.TRANSPOSE] ?: 0,
                theme = AppTheme.fromId(preferences[PreferencesKeys.THEME] ?: "black")
            )
        }

    /**
     * Updates settings atomically.
     */
    suspend fun updateSettings(settings: AppSettings) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.BANK_INDEX] = settings.currentBankIndex
            preferences[PreferencesKeys.PAGE_INDEX] = settings.currentPageIndex
            preferences[PreferencesKeys.MIDI_CHANNEL] = settings.currentMidiChannel
            preferences[PreferencesKeys.TRANSPOSE] = settings.currentTranspose
            preferences[PreferencesKeys.THEME] = settings.theme.id
        }
    }

    suspend fun updateBankIndex(index: Int) {
        context.dataStore.edit { it[PreferencesKeys.BANK_INDEX] = index }
    }

    suspend fun updatePageIndex(index: Int) {
        context.dataStore.edit { it[PreferencesKeys.PAGE_INDEX] = index }
    }

    suspend fun updateMidiChannel(channel: Int) {
        context.dataStore.edit { it[PreferencesKeys.MIDI_CHANNEL] = channel }
    }

    suspend fun updateTranspose(transpose: Int) {
        context.dataStore.edit { it[PreferencesKeys.TRANSPOSE] = transpose }
    }

    suspend fun updateTheme(theme: AppTheme) {
        context.dataStore.edit { it[PreferencesKeys.THEME] = theme.id }
    }
}