package com.jeu2048.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "scores")
data class ScoreEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val score: Int,
    val gridSize: Int,
    val timestamp: Long = System.currentTimeMillis()
)
