package com.jeu2048.app.game

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.random.Random
import java.util.Random as JavaRandom

/**
 * Moteur de jeu 2048 : grille NxN, déplacements, fusions, score, victoire/défaite.
 */
class GameEngine(private val gridSize: Int = 4) {

    private var random: Random = Random.Default

    private val _grid = MutableStateFlow(Array(gridSize) { IntArray(gridSize) })
    val grid: StateFlow<Array<IntArray>> = _grid.asStateFlow()

    private val _score = MutableStateFlow(0)
    val score: StateFlow<Int> = _score.asStateFlow()

    private val _bestScore = MutableStateFlow(0)
    val bestScore: StateFlow<Int> = _bestScore.asStateFlow()

    private val _hasWon = MutableStateFlow(false)
    val hasWon: StateFlow<Boolean> = _hasWon.asStateFlow()

    private val _gameOver = MutableStateFlow(false)
    val gameOver: StateFlow<Boolean> = _gameOver.asStateFlow()

    private val _lastMoveHadMerge = MutableStateFlow(false)
    val lastMoveHadMerge: StateFlow<Boolean> = _lastMoveHadMerge.asStateFlow()

    private var moveHistory: MutableList<GameSnapshot> = mutableListOf()
    private val maxUndo = 10

    data class GameSnapshot(
        val grid: Array<IntArray>,
        val score: Int
    ) {
        override fun equals(other: Any?) = (other as? GameSnapshot)?.let {
            grid.contentDeepEquals(it.grid) && score == it.score
        } ?: false
        override fun hashCode() = grid.contentDeepHashCode() + 31 * score
    }

    init {
        require(gridSize in 3..6) { "gridSize must be 3..6" }
    }

    fun setBestScore(value: Int) {
        _bestScore.value = value
    }

    fun startNewGame() {
        val g = Array(gridSize) { IntArray(gridSize) }
        _grid.value = g
        _score.value = 0
        _hasWon.value = false
        _gameOver.value = false
        moveHistory.clear()
        addRandomTile(g)
        addRandomTile(g)
        _grid.value = g.map { it.clone() }.toTypedArray()
    }

    fun restoreGame(grid: Array<IntArray>, score: Int, best: Int, hasWon: Boolean) {
        _grid.value = grid.map { it.clone() }.toTypedArray()
        _score.value = score
        _bestScore.value = best
        _hasWon.value = hasWon
        _gameOver.value = false
        moveHistory.clear()
    }

    fun canUndo(): Boolean = moveHistory.isNotEmpty()

    fun undo() {
        if (moveHistory.isEmpty()) return
        val snap = moveHistory.removeAt(moveHistory.lastIndex)
        _grid.value = snap.grid.map { it.clone() }.toTypedArray()
        _score.value = snap.score
        _gameOver.value = false
    }

    fun acceptWin() {
        _hasWon.value = true
    }

    fun move(direction: Direction): Boolean {
        if (_gameOver.value) return false
        val current = _grid.value.map { it.clone() }.toTypedArray()
        val currentScore = _score.value
        if (moveHistory.size >= maxUndo) moveHistory.removeAt(0)
        moveHistory.add(GameSnapshot(current.map { it.clone() }.toTypedArray(), currentScore))

        var newScore = currentScore
        var changed = false
        val merged = Array(gridSize) { BooleanArray(gridSize) }

        when (direction) {
            Direction.LEFT -> for (row in 0 until gridSize) {
                val (line, scoreDelta, moved) = mergeLine(current[row], merged[row])
                current[row] = line
                newScore += scoreDelta
                if (moved) changed = true
            }
            Direction.RIGHT -> for (row in 0 until gridSize) {
                val reversed = current[row].reversedArray()
                val (mergedLine, scoreDelta, moved) = mergeLine(reversed, BooleanArray(gridSize))
                current[row] = mergedLine.reversedArray()
                newScore += scoreDelta
                if (moved) changed = true
            }
            Direction.UP -> for (col in 0 until gridSize) {
                val column = IntArray(gridSize) { current[it][col] }
                val mergeCol = BooleanArray(gridSize)
                val (mergedCol, scoreDelta, moved) = mergeLine(column, mergeCol)
                if (moved) changed = true
                for (row in 0 until gridSize) current[row][col] = mergedCol[row]
                newScore += scoreDelta
            }
            Direction.DOWN -> for (col in 0 until gridSize) {
                val column = IntArray(gridSize) { current[gridSize - 1 - it][col] }
                val mergeCol = BooleanArray(gridSize)
                val (mergedCol, scoreDelta, moved) = mergeLine(column, mergeCol)
                if (moved) changed = true
                for (row in 0 until gridSize) current[gridSize - 1 - row][col] = mergedCol[row]
                newScore += scoreDelta
            }
        }

        _lastMoveHadMerge.value = newScore != currentScore
        if (!changed) {
            moveHistory.removeAt(moveHistory.lastIndex)
            return false
        }
        _score.value = newScore
        if (newScore > _bestScore.value) _bestScore.value = newScore
        addRandomTile(current)
        _grid.value = current
        if (!_hasWon.value && current.any { row -> row.any { it == 2048 } }) {
            _hasWon.value = true
        }
        if (!canMove(current)) _gameOver.value = true
        return true
    }

    private fun mergeLine(line: IntArray, merged: BooleanArray): Triple<IntArray, Int, Boolean> {
        val result = IntArray(line.size)
        var write = 0
        var i = 0
        var scoreDelta = 0
        var moved = false
        while (i < line.size) {
            if (line[i] == 0) {
                i++
                moved = true
                continue
            }
            val value = line[i]
            if (write > 0 && result[write - 1] == value && !merged[write - 1]) {
                result[write - 1] = value * 2
                merged[write - 1] = true
                scoreDelta += value * 2
                moved = true
            } else {
                if (write < line.size) {
                    if (result[write] != value) moved = true
                    result[write] = value
                    write++
                }
            }
            i++
        }
        return Triple(result, scoreDelta, moved)
    }

    private fun addRandomTile(g: Array<IntArray>) {
        val empty = mutableListOf<Pair<Int, Int>>()
        for (r in 0 until gridSize)
            for (c in 0 until gridSize)
                if (g[r][c] == 0) empty.add(r to c)
        if (empty.isEmpty()) return
        val (r, c) = empty.random(random)
        g[r][c] = if (random.nextFloat() < 0.9f) 2 else 4
    }

    fun startSeededGame(seed: Long) {
        random = Random(seed)
        startNewGame()
    }

    private fun canMove(g: Array<IntArray>): Boolean {
        for (r in 0 until gridSize)
            for (c in 0 until gridSize) {
                if (g[r][c] == 0) return true
                if (c < gridSize - 1 && g[r][c] == g[r][c + 1]) return true
                if (r < gridSize - 1 && g[r][c] == g[r + 1][c]) return true
            }
        return false
    }

    fun getStateForSave(): GameState = GameState(
        gridSize = gridSize,
        cells = _grid.value.flatMap { it.toList() },
        score = _score.value,
        bestScore = _bestScore.value,
        hasWon = _hasWon.value
    )
}
