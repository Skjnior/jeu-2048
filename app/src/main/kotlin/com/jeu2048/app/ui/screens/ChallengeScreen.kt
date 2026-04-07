package com.jeu2048.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
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
    targetScore: Int,
    finished: Boolean,
    success: Boolean,
    themeIndex: Int,
    onMove: (Direction) -> Unit,
    onBack: () -> Unit,
    onRetry: () -> Unit
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
            .safeDrawingPadding()
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

        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            val gridSide = minOf(
                (maxWidth - 12.dp).coerceAtLeast(220.dp),
                (maxHeight * 0.5f).coerceAtLeast(220.dp)
            )
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Infos Défi
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ChallengeInfoCard(
                        label = "TEMPS",
                        value = timeStr,
                        color = if (timeLeft < 30) Color(0xFFE57373) else Color(0xFF8F7A66),
                        modifier = Modifier.weight(1f)
                    )
                    ChallengeInfoCard(
                        label = "OBJECTIF",
                        value = targetScore.toString(),
                        color = Color(0xFFEDC22E),
                        modifier = Modifier.weight(1f)
                    )
                    ChallengeInfoCard(
                        label = "SCORE",
                        value = scoreState.value.toString(),
                        color = Color(0xFF8F7A66),
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(Modifier.height(20.dp))

                // Grille
                Box(
                    modifier = Modifier
                        .size(gridSide)
                        .clip(RoundedCornerShape(12.dp))
                        .background(cs.surface)
                        .pointerInput(Unit) {
                            val start = FloatArray(2)
                            val last = FloatArray(2)
                            detectDragGestures(
                                onDragStart = {
                                    start[0] = it.x
                                    start[1] = it.y
                                    last[0] = it.x
                                    last[1] = it.y
                                },
                                onDrag = { change, _ ->
                                    last[0] = change.position.x
                                    last[1] = change.position.y
                                },
                                onDragEnd = {
                                    val dx = last[0] - start[0]
                                    val dy = last[1] - start[1]
                                    val threshold = 40f
                                    when {
                                        kotlin.math.abs(dx) > kotlin.math.abs(dy) ->
                                            if (dx > threshold) onMove(Direction.RIGHT)
                                            else if (dx < -threshold) onMove(Direction.LEFT)
                                        dy > threshold -> onMove(Direction.DOWN)
                                        dy < -threshold -> onMove(Direction.UP)
                                    }
                                }
                            )
                        }
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

                // Flèches de direction (en plus du swipe)
                Spacer(Modifier.height(10.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                    IconButton(onClick = { onMove(Direction.UP) }) {
                        Icon(Icons.Default.KeyboardArrowUp, contentDescription = "Haut", tint = cs.primary)
                    }
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    IconButton(onClick = { onMove(Direction.LEFT) }) {
                        Icon(Icons.Default.KeyboardArrowLeft, contentDescription = "Gauche", tint = cs.primary)
                    }
                    Spacer(Modifier.width(48.dp))
                    IconButton(onClick = { onMove(Direction.RIGHT) }) {
                        Icon(Icons.Default.KeyboardArrowRight, contentDescription = "Droite", tint = cs.primary)
                    }
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                    IconButton(onClick = { onMove(Direction.DOWN) }) {
                        Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Bas", tint = cs.primary)
                    }
                }

                Spacer(Modifier.height(10.dp))

                Text(
                    "Atteignez l'objectif avant la fin du temps !",
                    fontSize = 16.sp,
                    color = cs.onSurfaceVariant,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }

    if (finished) {
        AlertDialog(
            onDismissRequest = onBack,
            title = { Text(if (success) "Défi réussi !" else "Défi échoué", fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    if (success) "Bravo ! Objectif atteint."
                    else "Temps écoulé ou grille bloquée avant l'objectif."
                )
            },
            containerColor = cs.surface,
            tonalElevation = 8.dp,
            shape = RoundedCornerShape(20.dp),
            confirmButton = {
                Button(onClick = onRetry, shape = RoundedCornerShape(14.dp)) { Text("Rejouer") }
            },
            dismissButton = {
                TextButton(onClick = onBack) { Text("Retour") }
            }
        )
    }
}

@Composable
private fun ChallengeInfoCard(label: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
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
