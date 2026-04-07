package com.jeu2048.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jeu2048.app.game.Direction
import com.jeu2048.app.game.GameEngine
import com.jeu2048.app.ui.components.GameGrid

@Composable
fun ChallengeScreen(
    engine: GameEngine?,
    timeLeft: Int,
    themeIndex: Int,
    onMove: (Direction) -> Unit,
    onBack: () -> Unit
) {
    val cs = MaterialTheme.colorScheme
    val gridState = engine?.grid?.collectAsState(initial = emptyArray()) ?: remember { mutableStateOf(emptyArray<IntArray>()) }
    val scoreState = engine?.score?.collectAsState(initial = 0) ?: remember { mutableStateOf(0) }
    
    val minutes = timeLeft / 60
    val seconds = timeLeft % 60
    val timeStr = String.format("%02d:%02d", minutes, seconds)

    val tileThemeIndex = if (themeIndex == 3) 2 else if (themeIndex == 2) 1 else 0

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(cs.background)
    ) {
        // AppBar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF8F7A66))
                .padding(horizontal = 4.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Retour", tint = Color.White)
            }
            Text("DÉFI QUOTIDIEN", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Infos Défi
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                ChallengeInfoCard("TEMPS", timeStr, if (timeLeft < 30) Color(0xFFE57373) else Color(0xFF8F7A66))
                ChallengeInfoCard("OBJECTIF", "2048", Color(0xFFEDC22E))
                ChallengeInfoCard("SCORE", scoreState.value.toString(), Color(0xFF8F7A66))
            }

            Spacer(Modifier.height(32.dp))

            // Grille
            Box(
                modifier = Modifier
                    .size(320.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(cs.surface)
                    .padding(8.dp)
            ) {
                GameGrid(
                    grid = gridState.value,
                    tileThemeIndex = tileThemeIndex,
                    animationsEnabled = true,
                    lastDirection = null,
                    highlightedRow = null,
                    highlightedCol = null
                )
            }
            
            Spacer(Modifier.height(32.dp))
            
            Text(
                "Atteignez 2048 avant la fin du temps !",
                fontSize = 16.sp,
                color = cs.onSurfaceVariant,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun ChallengeInfoCard(label: String, value: String, color: Color) {
    Box(
        modifier = Modifier
            .width(100.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(color)
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(label, fontSize = 12.sp, color = Color(0xFFEEE4DA), fontWeight = FontWeight.Bold)
            Text(value, fontSize = 18.sp, color = Color.White, fontWeight = FontWeight.Black)
        }
    }
}
