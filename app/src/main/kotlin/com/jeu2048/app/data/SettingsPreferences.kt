package com.jeu2048.app.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.settingsStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class SettingsPreferences(private val context: Context) {

    companion object {
        private val KEY_GRID_SIZE = intPreferencesKey("grid_size")
        private val KEY_THEME = intPreferencesKey("theme") // 0 clair, 1 système, 2 sombre, 3 coloré
        private val KEY_ANIMATIONS = booleanPreferencesKey("animations")
        private val KEY_SOUND_ENABLED = booleanPreferencesKey("sound_enabled")
        private val KEY_MUSIC_ENABLED = booleanPreferencesKey("music_enabled")
        private val KEY_GAMES_PLAYED = intPreferencesKey("games_played")
        private val KEY_GAMES_WON = intPreferencesKey("games_won")
        private val KEY_GAMES_LOST = intPreferencesKey("games_lost")
    }

    val gridSizeFlow: Flow<Int> = context.settingsStore.data.map {
        it[KEY_GRID_SIZE] ?: 4
    }
    val themeFlow: Flow<Int> = context.settingsStore.data.map { it[KEY_THEME] ?: 0 }
    val animationsFlow: Flow<Boolean> = context.settingsStore.data.map { it[KEY_ANIMATIONS] ?: true }
    val soundEnabledFlow: Flow<Boolean> = context.settingsStore.data.map { it[KEY_SOUND_ENABLED] ?: true }
    val musicEnabledFlow: Flow<Boolean> = context.settingsStore.data.map { it[KEY_MUSIC_ENABLED] ?: false }
    val gamesPlayedFlow: Flow<Int> = context.settingsStore.data.map { it[KEY_GAMES_PLAYED] ?: 0 }
    val gamesWonFlow: Flow<Int> = context.settingsStore.data.map { it[KEY_GAMES_WON] ?: 0 }
    val gamesLostFlow: Flow<Int> = context.settingsStore.data.map { it[KEY_GAMES_LOST] ?: 0 }

    suspend fun setGridSize(size: Int) {
        context.settingsStore.edit { it[KEY_GRID_SIZE] = size }
    }
    suspend fun setTheme(theme: Int) {
        context.settingsStore.edit { it[KEY_THEME] = theme }
    }
    suspend fun setAnimations(enabled: Boolean) {
        context.settingsStore.edit { it[KEY_ANIMATIONS] = enabled }
    }
    suspend fun setSoundEnabled(enabled: Boolean) {
        context.settingsStore.edit { it[KEY_SOUND_ENABLED] = enabled }
    }
    suspend fun setMusicEnabled(enabled: Boolean) {
        context.settingsStore.edit { it[KEY_MUSIC_ENABLED] = enabled }
    }
    suspend fun incrementGamesPlayed() {
        context.settingsStore.edit { it[KEY_GAMES_PLAYED] = (it[KEY_GAMES_PLAYED] ?: 0) + 1 }
    }
    suspend fun incrementGamesWon() {
        context.settingsStore.edit { it[KEY_GAMES_WON] = (it[KEY_GAMES_WON] ?: 0) + 1 }
    }
    suspend fun incrementGamesLost() {
        context.settingsStore.edit { it[KEY_GAMES_LOST] = (it[KEY_GAMES_LOST] ?: 0) + 1 }
    }
}
