package com.jeu2048.app.game

// Import pour les états réactifs (mise à jour automatique de l’UI)
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

// Générateur aléatoire
import kotlin.random.Random

/**
 * 🧠 Moteur principal du jeu 2048
 * Gère :
 * - la grille
 * - les déplacements
 * - les fusions
 * - le score
 * - la victoire et la défaite
 */
class GameEngine(private val gridSize: Int = 4) {

    // Générateur aléatoire (utilisé pour les nouvelles tuiles)
    private var random: Random = Random.Default

    // 🟩 Grille du jeu (StateFlow pour mise à jour automatique de l’UI)
    private val _grid = MutableStateFlow(Array(gridSize) { IntArray(gridSize) })
    val grid: StateFlow<Array<IntArray>> = _grid.asStateFlow()

    // 💯 Score actuel
    private val _score = MutableStateFlow(0)
    val score: StateFlow<Int> = _score.asStateFlow()

    // 🏆 Meilleur score
    private val _bestScore = MutableStateFlow(0)
    val bestScore: StateFlow<Int> = _bestScore.asStateFlow()

    // 🎉 Indique si le joueur a gagné (tuile 2048)
    private val _hasWon = MutableStateFlow(false)
    val hasWon: StateFlow<Boolean> = _hasWon.asStateFlow()

    // ❌ Indique si la partie est terminée
    private val _gameOver = MutableStateFlow(false)
    val gameOver: StateFlow<Boolean> = _gameOver.asStateFlow()

    // 🔥 Indique si une fusion a eu lieu au dernier coup
    private val _lastMoveHadMerge = MutableStateFlow(false)
    val lastMoveHadMerge: StateFlow<Boolean> = _lastMoveHadMerge.asStateFlow()

    // ↩️ Historique des coups pour Undo
    private var moveHistory: MutableList<GameSnapshot> = mutableListOf()
    private val maxUndo = 10 // limite de 10 annulations

    /**
     * 📦 Snapshot du jeu (pour Undo)
     */
    data class GameSnapshot(
        val grid: Array<IntArray>,
        val score: Int
    )

    // Vérifie que la taille de la grille est valide
    init {
        require(gridSize in 3..6) { "gridSize must be 3..6" }
    }

    /**
     * 🏆 Met à jour le meilleur score
     */
    fun setBestScore(value: Int) {
        _bestScore.value = value
    }

    /**
     * 🚀 Démarre une nouvelle partie
     */
    fun startNewGame() {
        val g = Array(gridSize) { IntArray(gridSize) }

        _grid.value = g
        _score.value = 0
        _hasWon.value = false
        _gameOver.value = false

        moveHistory.clear()

        // Ajoute 2 tuiles aléatoires au début
        addRandomTile(g)
        addRandomTile(g)

        _grid.value = g.map { it.clone() }.toTypedArray()
    }

    /**
     * Démarre une partie avec un générateur pseudo-aléatoire fixé par [seed].
     * Utilisé pour le défi quotidien : même date → même séquence de tuiles.
     */
    fun startSeededGame(seed: Long) {
        random = Random(seed)
        startNewGame()
    }

    /**
     * 🔄 Restaure une partie sauvegardée
     */
    fun restoreGame(grid: Array<IntArray>, score: Int, best: Int, hasWon: Boolean) {
        _grid.value = grid.map { it.clone() }.toTypedArray()
        _score.value = score
        _bestScore.value = best
        _hasWon.value = hasWon
        _gameOver.value = false
        moveHistory.clear()
    }

    /**
     * ↩️ Vérifie si on peut annuler
     */
    fun canUndo(): Boolean = moveHistory.isNotEmpty()

    /**
     * ↩️ Annule le dernier coup
     */
    fun undo() {
        if (moveHistory.isEmpty()) return

        val snap = moveHistory.removeAt(moveHistory.lastIndex)

        _grid.value = snap.grid.map { it.clone() }.toTypedArray()
        _score.value = snap.score
        _gameOver.value = false
    }

    /**
     * 🎉 Accepte la victoire (quand 2048 est atteint)
     */
    fun acceptWin() {
        _hasWon.value = true
    }

    /**
     * 🎮 Fonction principale : déplacement du joueur
     */
    fun move(direction: Direction): Boolean {

        // Si partie terminée → rien faire
        if (_gameOver.value) return false

        // Copie de la grille actuelle
        val current = _grid.value.map { it.clone() }.toTypedArray()
        val currentScore = _score.value

        // Sauvegarde pour Undo
        if (moveHistory.size >= maxUndo) moveHistory.removeAt(0)
        moveHistory.add(GameSnapshot(current.map { it.clone() }.toTypedArray(), currentScore))

        var newScore = currentScore
        var changed = false

        val merged = Array(gridSize) { BooleanArray(gridSize) }

        // Gestion selon direction
        when (direction) {

            // ⬅️ Gauche
            Direction.LEFT -> for (row in 0 until gridSize) {
                val (line, scoreDelta, moved) = mergeLine(current[row], merged[row])
                current[row] = line
                newScore += scoreDelta
                if (moved) changed = true
            }

            // ➡️ Droite (on inverse la ligne)
            Direction.RIGHT -> for (row in 0 until gridSize) {
                val reversed = current[row].reversedArray()
                val (mergedLine, scoreDelta, moved) = mergeLine(reversed, BooleanArray(gridSize))
                current[row] = mergedLine.reversedArray()
                newScore += scoreDelta
                if (moved) changed = true
            }

            // ⬆️ Haut (on travaille colonne par colonne)
            Direction.UP -> for (col in 0 until gridSize) {
                val column = IntArray(gridSize) { current[it][col] }
                val (mergedCol, scoreDelta, moved) = mergeLine(column, BooleanArray(gridSize))
                if (moved) changed = true
                for (row in 0 until gridSize) current[row][col] = mergedCol[row]
                newScore += scoreDelta
            }

            // ⬇️ Bas (colonne inversée)
            Direction.DOWN -> for (col in 0 until gridSize) {
                val column = IntArray(gridSize) { current[gridSize - 1 - it][col] }
                val (mergedCol, scoreDelta, moved) = mergeLine(column, BooleanArray(gridSize))
                if (moved) changed = true
                for (row in 0 until gridSize) current[gridSize - 1 - row][col] = mergedCol[row]
                newScore += scoreDelta
            }
        }

        // Indique si une fusion a eu lieu
        _lastMoveHadMerge.value = newScore != currentScore

        // Si rien n’a changé → annule le coup
        if (!changed) {
            moveHistory.removeAt(moveHistory.lastIndex)
            return false
        }

        // Mise à jour score
        _score.value = newScore
        if (newScore > _bestScore.value) _bestScore.value = newScore

        // Ajoute nouvelle tuile
        addRandomTile(current)

        _grid.value = current

        // Vérifie victoire
        if (!_hasWon.value && current.any { row -> row.any { it == 2048 } }) {
            _hasWon.value = true
        }

        // Vérifie défaite
        if (!canMove(current)) _gameOver.value = true

        return true
    }

    /**
     * 🔗 Fusion d’une ligne (algorithme principal)
     */
    private fun mergeLine(line: IntArray, merged: BooleanArray): Triple<IntArray, Int, Boolean> {

        val result = IntArray(line.size)
        var write = 0
        var scoreDelta = 0
        var moved = false

        for (value in line) {
            if (value == 0) continue

            // Fusion si même valeur
            if (write > 0 && result[write - 1] == value && !merged[write - 1]) {
                result[write - 1] *= 2
                merged[write - 1] = true
                scoreDelta += result[write - 1]
                moved = true
            } else {
                result[write++] = value
            }
        }

        return Triple(result, scoreDelta, moved)
    }

    /**
     * 🎲 Ajoute une tuile aléatoire (2 ou 4)
     */
    private fun addRandomTile(g: Array<IntArray>) {

        val empty = mutableListOf<Pair<Int, Int>>()

        // Cherche les cases vides
        for (r in 0 until gridSize)
            for (c in 0 until gridSize)
                if (g[r][c] == 0) empty.add(r to c)

        if (empty.isEmpty()) return

        val (r, c) = empty.random(random)

        // 90% → 2, 10% → 4
        g[r][c] = if (random.nextFloat() < 0.9f) 2 else 4
    }

    /**
     * ❌ Vérifie si des mouvements sont encore possibles
     */
    private fun canMove(g: Array<IntArray>): Boolean {
        for (r in 0 until gridSize)
            for (c in 0 until gridSize) {
                if (g[r][c] == 0) return true
                if (c < gridSize - 1 && g[r][c] == g[r][c + 1]) return true
                if (r < gridSize - 1 && g[r][c] == g[r + 1][c]) return true
            }
        return false
    }

    /**
     * 💾 Prépare les données pour la sauvegarde
     */
    fun getStateForSave(): GameState = GameState(
        gridSize = gridSize,
        cells = _grid.value.flatMap { it.toList() },
        score = _score.value,
        bestScore = _bestScore.value,
        hasWon = _hasWon.value
    )
}