package com.jeu2048.app.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [ScoreEntity::class], version = 1, exportSchema = false)
abstract class ScoreDatabase : RoomDatabase() {
    abstract fun scoreDao(): ScoreDao
}

object DatabaseProvider {
    @Volatile
    private var instance: ScoreDatabase? = null
    fun get(context: Context): ScoreDatabase {
        return instance ?: synchronized(this) {
            instance ?: Room.databaseBuilder(
                context.applicationContext,
                ScoreDatabase::class.java,
                "jeu2048_scores"
            ).build().also { instance = it }
        }
    }
}
