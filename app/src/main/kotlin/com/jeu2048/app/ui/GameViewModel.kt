package com.jeu2048.app.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.jeu2048.app.data.DatabaseProvider
import com.jeu2048.app.data.GamePreferences
import com.jeu2048.app.data.ScoreEntity
import com.jeu2048.app.data.SettingsPreferences
import com.jeu2048.app.game.Direction
import com.jeu2048.app.game.GameEngine
import com.jeu2048.app.game.GameState
import com.jeu2048.app.sound.SoundManager
import com.jeu2048.app.ui.ShareHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class GameViewModel(application: Application) : AndroidViewModel(application) {

    private val gamePrefs = GamePreferences(application)
    private val settingsPrefs = SettingsPreferences(application)
    private val scoreDao = DatabaseProvider.get(application).scoreDao()
    private val soundManager = SoundManager(application)

    private val _currentEngine = MutableStateFlow<GameEngine?>(null)
    val currentEngine = _currentEngine.asStateFlow()

    val topScores = scoreDao.getTopScores(20)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val settings = combine(
        settingsPrefs.gridSizeFlow,
        settingsPrefs.themeFlow,
        settingsPrefs.animationsFlow,
        settingsPrefs.soundEnabledFlow,
        settingsPrefs.musicEnabledFlow,
        settingsPrefs.gamesPlayedFlow,
        settingsPrefs.gamesWonFlow,
        settingsPrefs.gamesLostFlow
    ) { values ->
        SettingsUi(
            gridSize = values[0] as Int,
            theme = values[1] as Int,
            animations = values[2] as Boolean,
            soundEnabled = values[3] as Boolean,
            musicEnabled = values[4] as Boolean,
            gamesPlayed = values[5] as Int,
            gamesWon = values[6] as Int,
            gamesLost = values[7] as Int
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SettingsUi(4, 0, true, true, false, 0, 0, 0))

    data class SettingsUi(
        val gridSize: Int,
        val theme: Int,
        val animations: Boolean,
        val soundEnabled: Boolean,
        val musicEnabled: Boolean,
        val gamesPlayed: Int,
        val gamesWon: Int,
        val gamesLost: Int
    )

    init {
        viewModelScope.launch {
            try {
                val size = settingsPrefs.gridSizeFlow.first().coerceIn(3, 6)
                val saved = gamePrefs.savedGameFlow.first()
                val engine = GameEngine(size)
                if (saved != null && saved.gridSize == size && saved.cells.size == size * size) {
                    engine.setBestScore(saved.bestScore)
                    engine.restoreGame(saved.toGrid(), saved.score, saved.bestScore, saved.hasWon)
                } else {
                    engine.startNewGame()
                }
                _currentEngine.value = engine
            } catch (_: Exception) {
                _currentEngine.value = GameEngine(4).also { it.startNewGame() }
            }
        }
        viewModelScope.launch {
            soundManager.setMusicEnabled(settingsPrefs.musicEnabledFlow.first())
        }
    }

    private fun engine(): GameEngine? = _currentEngine.value

    fun startNewGame() {
        val e = engine() ?: return
        viewModelScope.launch { gamePrefs.clearSavedGame() }
        e.startNewGame()
        viewModelScope.launch { settingsPrefs.incrementGamesPlayed() }
    }

    fun move(direction: Direction): Boolean {
        val e = engine() ?: return false
        val moved = e.move(direction)
        if (moved) {
            if (settings.value.soundEnabled) {
                if (e.lastMoveHadMerge.value) soundManager.playMerge()
                else soundManager.playMove()
            }
            saveState()
        }
        return moved
    }

    fun undo() {
        engine()?.undo()
        saveState()
    }

    fun acceptWin() {
        engine()?.acceptWin()
        viewModelScope.launch { settingsPrefs.incrementGamesWon() }
        saveState()
    }

    fun canUndo(): Boolean = engine()?.canUndo() ?: false

    private fun saveState() {
        val e = engine() ?: return
        viewModelScope.launch {
            gamePrefs.saveGame(e.getStateForSave())
            if (e.gameOver.value) {
                settingsPrefs.incrementGamesLost()
                scoreDao.insert(ScoreEntity(score = e.score.value, gridSize = settings.value.gridSize))
            }
        }
    }

    fun saveStateOnPause() {
        engine()?.let { viewModelScope.launch { gamePrefs.saveGame(it.getStateForSave()) } }
    }

    fun setGridSize(size: Int) {
        viewModelScope.launch { settingsPrefs.setGridSize(size) }
        val engine = GameEngine(size)
        engine.startNewGame()
        _currentEngine.value = engine
    }

    fun setTheme(theme: Int) {
        viewModelScope.launch { settingsPrefs.setTheme(theme) }
    }
    fun setAnimations(enabled: Boolean) {
        viewModelScope.launch { settingsPrefs.setAnimations(enabled) }
    }
    fun setSoundEnabled(enabled: Boolean) {
        soundManager.setEnabled(enabled)
        viewModelScope.launch { settingsPrefs.setSoundEnabled(enabled) }
    }
    fun setMusicEnabled(enabled: Boolean) {
        soundManager.setMusicEnabled(enabled)
        viewModelScope.launch { settingsPrefs.setMusicEnabled(enabled) }
    }

    fun resetScores() {
        viewModelScope.launch { scoreDao.deleteAll() }
    }

    fun shareScore() {
        val e = engine() ?: return
        ShareHelper.shareScore(
            getApplication(),
            e.score.value,
            e.bestScore.value,
            settings.value.gridSize
        )
    }
}
