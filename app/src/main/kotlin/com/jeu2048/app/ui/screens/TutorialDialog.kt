package com.jeu2048.app.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jeu2048.app.ui.components.GameGrid
import kotlinx.coroutines.delay

@Composable
fun TutorialDialog(
    themeIndex: Int,
    onDismiss: () -> Unit
) {
    // État pour l'animation de démo
    var demoStep by remember { mutableIntStateOf(0) }
    val demoGrid = remember(demoStep) {
        when (demoStep) {
            0 -> arrayOf(intArrayOf(2, 0, 2), intArrayOf(0, 0, 0), intArrayOf(0, 0, 0))
            1 -> arrayOf(intArrayOf(0, 0, 4), intArrayOf(0, 0, 0), intArrayOf(0, 0, 0)) // Après fusion à droite
            2 -> arrayOf(intArrayOf(2, 0, 4), intArrayOf(0, 0, 0), intArrayOf(0, 0, 0)) // Nouvelle tuile
            3 -> arrayOf(intArrayOf(2, 0, 0), intArrayOf(0, 0, 0), intArrayOf(4, 0, 0)) // Après mouvement bas (partiel)
            else -> arrayOf(intArrayOf(2, 0, 2), intArrayOf(0, 0, 0), intArrayOf(0, 0, 0))
        }
    }

    LaunchedEffect(Unit) {
        while (true) {
            delay(1500)
            demoStep = (demoStep + 1) % 4
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Comment jouer") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Grille de démo animée
                Text(
                    "Démo interactive :", 
                    fontSize = 14.sp, 
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.Start).padding(bottom = 8.dp)
                )
                Box(
                    modifier = Modifier
                        .size(160.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .border(2.dp, Color(0xFF8F7A66), RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(4.dp)
                ) {
                    GameGrid(
                        grid = demoGrid,
                        tileThemeIndex = if (themeIndex == 3) 2 else if (themeIndex == 2) 1 else 0,
                        animationsEnabled = true,
                        lastDirection = null,
                        highlightedRow = null,
                        highlightedCol = null
                    )
                }
                
                Spacer(Modifier.height(20.dp))

                Column(modifier = Modifier.fillMaxWidth()) {
                    TutorialItem("• Glissez pour déplacer toutes les tuiles.")
                    TutorialItem("• Deux tuiles identiques fusionnent (ex: 2+2=4).")
                    TutorialItem("• Obtenez 2048 pour gagner !")
                    TutorialItem("• La partie s'arrête si la grille est bloquée.")
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("J'ai compris")
            }
        }
    )
}

@Composable
private fun TutorialItem(text: String) {
    Text(
        text = text,
        modifier = Modifier.padding(vertical = 4.dp),
        fontSize = 15.sp,
        lineHeight = 20.sp
    )
}
