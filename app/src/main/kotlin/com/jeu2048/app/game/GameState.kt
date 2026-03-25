package com.jeu2048.app.game

data class GameState(
    val gridSize: Int,
    val cells: List<Int>,
    val score: Int,
    val bestScore: Int,
    val hasWon: Boolean
) {
    fun toGrid(): Array<IntArray> {
        val g = Array(gridSize) { IntArray(gridSize) }
        cells.forEachIndexed { i, v ->
            val r = i / gridSize
            val c = i % gridSize
            if (r in 0 until gridSize && c in 0 until gridSize) g[r][c] = v
        }
        return g
    }
}
