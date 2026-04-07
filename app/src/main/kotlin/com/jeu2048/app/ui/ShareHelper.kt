package com.jeu2048.app.ui

import android.content.Context
import android.content.Intent
import android.content.ClipData
import android.graphics.Bitmap
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream

object ShareHelper {

    fun shareScore(context: Context, score: Int, bestScore: Int, gridSize: Int, bitmap: Bitmap? = null) {
        val text = "J'ai fait $score au 2048 ! (Meilleur: $bestScore, Grille ${gridSize}×$gridSize)"
        
        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_SUBJECT, "Mon score au 2048")
            putExtra(Intent.EXTRA_TEXT, text)
            
            if (bitmap != null) {
                val uri = saveBitmapToCache(context, bitmap)
                if (uri != null) {
                    putExtra(Intent.EXTRA_STREAM, uri)
                    type = "image/png"
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    // Certains share sheets / apps OEM nécessitent ClipData pour afficher un aperçu
                    clipData = ClipData.newUri(context.contentResolver, "score_2048", uri)
                } else {
                    type = "text/plain"
                }
            } else {
                type = "text/plain"
            }
        }
        
        val shareIntent = Intent.createChooser(sendIntent, "Partager le score")
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        // Re-propage la permission de lecture au chooser
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        context.startActivity(shareIntent)
    }

    private fun saveBitmapToCache(context: Context, bitmap: Bitmap): Uri? {
        return try {
            val cachePath = File(context.cacheDir, "images")
            cachePath.mkdirs()
            val file = File(cachePath, "score_2048.png")
            val stream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
            stream.close()
            FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
