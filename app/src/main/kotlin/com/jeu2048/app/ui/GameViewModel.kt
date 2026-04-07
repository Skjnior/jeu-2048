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
import android.graphics.Bitmap
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

class GameViewModel(application: Application) : AndroidViewModel(application) {

    private val gamePrefs = GamePreferences(application)
    private val settingsPrefs = SettingsPreferences(application)
    private val scoreDao = DatabaseProvider.get(application).scoreDao()
    private val soundManager = SoundManager(application)

    private val _currentEngine = MutableStateFlow<GameEngine?>(null)
    val currentEngine = _currentEngine.asStateFlow()

    // Multiplayer
    private val _player1Engine = MutableStateFlow<GameEngine?>(null)
    val player1Engine = _player1Engine.asStateFlow()
    private val _player2Engine = MutableStateFlow<GameEngine?>(null)
    val player2Engine = _player2Engine.asStateFlow()
    private val _multiplayerTimeLeft = MutableStateFlow<Int?>(null) // null = illimité
    val multiplayerTimeLeft = _multiplayerTimeLeft.asStateFlow()
    private val _multiplayerFinished = MutableStateFlow(false)
    val multiplayerFinished = _multiplayerFinished.asStateFlow()
    private val _multiplayerWinner = MutableStateFlow<String?>(null) // "Joueur 1", "Joueur 2", "Égalité"
    val multiplayerWinner = _multiplayerWinner.asStateFlow()
    private var multiplayerJob: Job? = null

    // Challenge
    private val _challengeEngine = MutableStateFlow<GameEngine?>(null)
    val challengeEngine = _challengeEngine.asStateFlow()
    private val _timeLeft = MutableStateFlow(300) // 5 minutes
    val timeLeft = _timeLeft.asStateFlow()
    private val _challengeTargetScore = MutableStateFlow(2000)
    val challengeTargetScore = _challengeTargetScore.asStateFlow()
    private val _challengeFinished = MutableStateFlow(false)
    val challengeFinished = _challengeFinished.asStateFlow()
    private val _challengeSuccess = MutableStateFlow(false)
    val challengeSuccess = _challengeSuccess.asStateFlow()
    private var challengeJob: Job? = null

    val topScores = scoreDao.getTopScores(20)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    private val bestScoreFlow = scoreDao.getTopScores(1)
        .map { scores -> scores.firstOrNull()?.score ?: 0 }

    val settings = combine(
        settingsPrefs.gridSizeFlow,
        settingsPrefs.themeFlow,
        settingsPrefs.animationsFlow,
        settingsPrefs.soundEnabledFlow,
        settingsPrefs.musicEnabledFlow,
        settingsPrefs.gamesPlayedFlow,
        settingsPrefs.gamesWonFlow,
        settingsPrefs.gamesLostFlow,
        bestScoreFlow
    ) { values ->
        SettingsUi(
            gridSize = values[0] as Int,
            theme = values[1] as Int,
            animations = values[2] as Boolean,
            soundEnabled = values[3] as Boolean,
            musicEnabled = values[4] as Boolean,
            gamesPlayed = values[5] as Int,
            gamesWon = values[6] as Int,
            gamesLost = values[7] as Int,
            bestScore = values[8] as Int
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SettingsUi(4, 0, true, true, false, 0, 0, 0, 0))

    data class SettingsUi(
        val gridSize: Int,
        val theme: Int,
        val animations: Boolean,
        val soundEnabled: Boolean,
        val musicEnabled: Boolean,
        val gamesPlayed: Int,
        val gamesWon: Int,
        val gamesLost: Int,
        val bestScore: Int
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
        val e = engine() ?: return
        e.acceptWin()
        viewModelScope.launch {
            settingsPrefs.incrementGamesWon()
            scoreDao.insert(ScoreEntity(score = e.score.value, gridSize = settings.value.gridSize))
        }
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

    fun playWinSound() {
        soundManager.playWin()
    }

    fun playLoseSound() {
        soundManager.playLose()
    }

    fun resetScores() {
        viewModelScope.launch { scoreDao.deleteAll() }
    }

    fun shareScore(bitmap: Bitmap? = null) {
        val e = engine() ?: return
        ShareHelper.shareScore(
            getApplication(),
            e.score.value,
            e.bestScore.value,
            settings.value.gridSize,
            bitmap
        )
    }

    private fun selectedGridSize(): Int = settings.value.gridSize.coerceIn(3, 6)

    private fun challengeDurationForGrid(gridSize: Int): Int = when (gridSize) {
        3 -> 210
        4 -> 300
        5 -> 390
        else -> 480
    }

    private fun challengeTargetFor(seed: Long, gridSize: Int): Int {
        val base = 1500 + ((seed % 10).toInt() * 250) // 1500..3750
        val scaled = (base * (gridSize * gridSize / 16f)).roundToInt()
        return ((scaled / 50).coerceAtLeast(12)) * 50
    }

    // --- Multiplayer Actions ---
    fun startMultiplayer(timed: Boolean) {
        multiplayerJob?.cancel()
        _multiplayerFinished.value = false
        _multiplayerWinner.value = null
        val gridSize = selectedGridSize()
        _player1Engine.value = GameEngine(gridSize).apply { startNewGame() }
        _player2Engine.value = GameEngine(gridSize).apply { startNewGame() }

        if (timed) {
            _multiplayerTimeLeft.value = 180
            multiplayerJob = viewModelScope.launch {
                while (true) {
                    val left = _multiplayerTimeLeft.value ?: break
                    if (left <= 0) break
                    delay(1000)
                    _multiplayerTimeLeft.value = left - 1
                    if (_multiplayerFinished.value) break
                }
                if (!_multiplayerFinished.value) finishMultiplayer()
            }
        } else {
            _multiplayerTimeLeft.value = null
        }
    }

    private fun finishMultiplayer() {
        val p1 = _player1Engine.value
        val p2 = _player2Engine.value
        val s1 = p1?.score?.value ?: 0
        val s2 = p2?.score?.value ?: 0
        _multiplayerWinner.value = when {
            s1 > s2 -> "Joueur 1"
            s2 > s1 -> "Joueur 2"
            else -> "Égalité"
        }
        _multiplayerFinished.value = true
    }

    fun movePlayer1(direction: Direction) {
        val e = _player1Engine.value ?: return
        if (_multiplayerFinished.value) return
        val moved = e.move(direction)
        if (moved) checkMultiplayerEnd()
    }

    fun movePlayer2(direction: Direction) {
        val e = _player2Engine.value ?: return
        if (_multiplayerFinished.value) return
        val moved = e.move(direction)
        if (moved) checkMultiplayerEnd()
    }

    private fun checkMultiplayerEnd() {
        val p1 = _player1Engine.value ?: return
        val p2 = _player2Engine.value ?: return
        // Fin si quelqu'un atteint 2048 (mode illimité / jusqu'à 2048)
        if (_multiplayerTimeLeft.value == null && (p1.hasWon.value || p2.hasWon.value)) {
            finishMultiplayer()
            return
        }
        // Fin si les deux sont bloqués
        if (p1.gameOver.value && p2.gameOver.value) {
            finishMultiplayer()
        }
    }

    // --- Challenge Actions ---
    fun startDailyChallenge() {
        challengeJob?.cancel()
        _challengeFinished.value = false
        _challengeSuccess.value = false
        val gridSize = selectedGridSize()
        val seed = java.time.LocalDate.now().toEpochDay()
        val target = challengeTargetFor(seed, gridSize)
        _challengeTargetScore.value = target
        val engine = GameEngine(gridSize)
        engine.startSeededGame(seed)
        _challengeEngine.value = engine
        _timeLeft.value = challengeDurationForGrid(gridSize)
        
        challengeJob = viewModelScope.launch {
            while (_timeLeft.value > 0 && !_challengeFinished.value) {
                delay(1000)
                _timeLeft.value -= 1
                if (engine.score.value >= _challengeTargetScore.value) {
                    _challengeSuccess.value = true
                    _challengeFinished.value = true
                    break
                }
                if (engine.gameOver.value) {
                    _challengeSuccess.value = false
                    _challengeFinished.value = true
                    break
                }
            }
            if (!_challengeFinished.value && _timeLeft.value <= 0) {
                _challengeSuccess.value = engine.score.value >= _challengeTargetScore.value
                _challengeFinished.value = true
            }
        }
    }
}
