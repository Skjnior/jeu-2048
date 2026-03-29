package com.jeu2048.app.sound

import android.content.Context
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.ToneGenerator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Gestion des sons (mouvement, fusion) et de la musique de fond.
 * Pour la musique : ajouter un fichier res/raw/bg_music.mp3 (ou .ogg) pour qu'elle soit lue.
 */
class SoundManager(private val context: Context) {

    private val _soundEnabled = MutableStateFlow(true)
    val soundEnabled: StateFlow<Boolean> = _soundEnabled.asStateFlow()

    private var toneGenerator: ToneGenerator? = null
    private var mediaPlayer: MediaPlayer? = null
    private var soundPool: android.media.SoundPool? = null
    private var moveSoundId: Int = 0
    private var winSoundId: Int = 0
    private var loseSoundId: Int = 0

    init {
        try {
            toneGenerator = ToneGenerator(AudioManager.STREAM_MUSIC, 70)
        } catch (_: Exception) { }
        try {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                soundPool = android.media.SoundPool.Builder().setMaxStreams(5).build()
            } else {
                @Suppress("DEPRECATION")
                soundPool = android.media.SoundPool(5, AudioManager.STREAM_MUSIC, 0)
            }
            val resIdMove = context.resources.getIdentifier("move", "raw", context.packageName)
            if (resIdMove != 0) {
                moveSoundId = soundPool?.load(context, resIdMove, 1) ?: 0
            }
            val resIdWin = context.resources.getIdentifier("win", "raw", context.packageName)
            if (resIdWin != 0) {
                winSoundId = soundPool?.load(context, resIdWin, 1) ?: 0
            }
            val resIdLose = context.resources.getIdentifier("lose", "raw", context.packageName)
            if (resIdLose != 0) {
                loseSoundId = soundPool?.load(context, resIdLose, 1) ?: 0
            }
        } catch (_: Exception) { }
    }

    fun setEnabled(enabled: Boolean) {
        _soundEnabled.value = enabled
    }

    fun setMusicEnabled(enabled: Boolean) {
        if (enabled) startBackgroundMusic()
        else stopBackgroundMusic()
    }

    private fun startBackgroundMusic() {
        if (mediaPlayer?.isPlaying == true) return
        stopBackgroundMusic()
        val resId = context.resources.getIdentifier("bg_music", "raw", context.packageName)
        if (resId == 0) return
        try {
            MediaPlayer.create(context, resId)?.let { mp ->
                mediaPlayer = mp
                mp.isLooping = true
                mp.setVolume(0.3f, 0.3f)
                mp.start()
            }
        } catch (_: Exception) { }
    }

    private fun stopBackgroundMusic() {
        try {
            mediaPlayer?.apply {
                if (isPlaying) stop()
                release()
            }
        } catch (_: Exception) { }
        mediaPlayer = null
    }

    fun playMove() {
        if (!_soundEnabled.value) return
        if (moveSoundId != 0) {
            soundPool?.play(moveSoundId, 1f, 1f, 1, 0, 1f)
        } else {
            try { toneGenerator?.startTone(ToneGenerator.TONE_PROP_BEEP, 50) } catch (_: Exception) { }
        }
    }

    fun playMerge() {
        if (!_soundEnabled.value) return
        if (moveSoundId != 0) {
            // Même son que le déplacement, légèrement plus fort pour la fusion
            soundPool?.play(moveSoundId, 1f, 1f, 1, 0, 1.2f)
        } else {
            try { toneGenerator?.startTone(ToneGenerator.TONE_PROP_ACK, 80) } catch (_: Exception) { }
        }
    }

    fun playWin() {
        if (!_soundEnabled.value) return
        if (winSoundId != 0) {
            soundPool?.play(winSoundId, 1f, 1f, 1, 0, 1f)
        }
    }

    fun playLose() {
        if (!_soundEnabled.value) return
        if (loseSoundId != 0) {
            soundPool?.play(loseSoundId, 1f, 1f, 1, 0, 1f)
        }
    }

    fun release() {
        try { toneGenerator?.release() } catch (_: Exception) { }
        toneGenerator = null
        try { soundPool?.release() } catch (_: Exception) { }
        soundPool = null
        stopBackgroundMusic()
    }
}
