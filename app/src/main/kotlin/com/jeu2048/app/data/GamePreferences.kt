package com.jeu2048.app.data

import android.content.Context
import com.jeu2048.app.game.GameState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "game_prefs")

class GamePreferences(private val context: Context) {

    companion object {
        private val KEY_SAVED_GAME = stringPreferencesKey("saved_game")
        private const val SEP = "|"
    }

    val savedGameFlow: Flow<GameState?> = context.dataStore.data.map { prefs ->
        prefs[KEY_SAVED_GAME]?.let { parseGameState(it) }
    }

    suspend fun saveGame(state: GameState) {
        context.dataStore.edit { it[KEY_SAVED_GAME] = serialize(state) }
    }

    suspend fun clearSavedGame() {
        context.dataStore.edit { it.remove(KEY_SAVED_GAME) }
    }

    private fun serialize(s: GameState): String = buildString {
        append(s.gridSize)
        append(SEP)
        append(s.cells.joinToString(","))
        append(SEP)
        append(s.score)
        append(SEP)
        append(s.bestScore)
        append(SEP)
        append(if (s.hasWon) "1" else "0")
    }

    private fun parseGameState(str: String): GameState? {
        val parts = str.split(SEP)
        if (parts.size != 5) return null
        return try {
            val gridSize = parts[0].toInt()
            val cells = parts[1].split(",").map { it.toIntOrNull() ?: 0 }
            val score = parts[2].toInt()
            val bestScore = parts[3].toInt()
            val hasWon = parts[4] == "1"
            GameState(gridSize, cells, score, bestScore, hasWon)
        } catch (_: Exception) {
            null
        }
    }
}
