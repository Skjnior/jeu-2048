package com.jeu2048.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jeu2048.app.game.Direction
import com.jeu2048.app.game.GameEngine
import com.jeu2048.app.ui.components.GameGrid

@Composable
fun MultiplayerScreen(
    p1Engine: GameEngine?,
    p2Engine: GameEngine?,
    themeIndex: Int,
    onMoveP1: (Direction) -> Unit,
    onMoveP2: (Direction) -> Unit,
    onBack: () -> Unit
) {
    val cs = MaterialTheme.colorScheme
    
    // États pour les deux grilles
    val p1GridState = p1Engine?.grid?.collectAsState(initial = emptyArray()) ?: remember { mutableStateOf(emptyArray<IntArray>()) }
    val p1ScoreState = p1Engine?.score?.collectAsState(initial = 0) ?: remember { mutableStateOf(0) }
    
    val p2GridState = p2Engine?.grid?.collectAsState(initial = emptyArray()) ?: remember { mutableStateOf(emptyArray<IntArray>()) }
    val p2ScoreState = p2Engine?.score?.collectAsState(initial = 0) ?: remember { mutableStateOf(0) }

    val tileThemeIndex = if (themeIndex == 3) 2 else if (themeIndex == 2) 1 else 0

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(cs.background)
    ) {
        // --- Joueur 2 (Haut, inversé) ---
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .graphicsLayer(rotationZ = 180f) // Inversé pour le face à face
                .pointerInput(Unit) {
                    detectMultiplayerDrag { onMoveP2(it) }
                }
                .padding(16.dp)
        ) {
            PlayerArea(
                label = "Joueur 2",
                score = p2ScoreState.value,
                grid = p2GridState.value,
                tileThemeIndex = tileThemeIndex,
                cs = cs
            )
        }

        // --- Ligne de séparation / Menu ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF8F7A66))
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Retour", tint = Color.White)
            }
            Text("MODE MULTIJOUEUR", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        }

        // --- Joueur 1 (Bas) ---
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .pointerInput(Unit) {
                    detectMultiplayerDrag { onMoveP1(it) }
                }
                .padding(16.dp)
        ) {
            PlayerArea(
                label = "Joueur 1",
                score = p1ScoreState.value,
                grid = p1GridState.value,
                tileThemeIndex = tileThemeIndex,
                cs = cs
            )
        }
    }
}

@Composable
private fun PlayerArea(
    label: String,
    score: Int,
    grid: Array<IntArray>,
    tileThemeIndex: Int,
    cs: ColorScheme
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(label, fontWeight = FontWeight.Black, fontSize = 18.sp, color = cs.onSurface)
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color(0xFF8F7A66))
                    .padding(horizontal = 12.dp, vertical = 4.dp)
            ) {
                Text("Score: $score", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
        Spacer(Modifier.height(8.dp))
        Box(
            modifier = Modifier
                .size(240.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(cs.surface)
                .padding(4.dp)
        ) {
            GameGrid(
                grid = grid,
                tileThemeIndex = tileThemeIndex,
                animationsEnabled = true,
                lastDirection = null,
                highlightedRow = null,
                highlightedCol = null
            )
        }
    }
}

private suspend fun androidx.compose.ui.input.pointer.PointerInputScope.detectMultiplayerDrag(onMove: (Direction) -> Unit) {
    var hasMoved = false
    detectDragGestures(
        onDragStart = { hasMoved = false },
        onDragEnd = { hasMoved = false }
    ) { change, dragAmount ->
        change.consume()
        if (hasMoved) return@detectDragGestures
        val threshold = 25f
        if (kotlin.math.abs(dragAmount.x) > kotlin.math.abs(dragAmount.y)) {
            if (dragAmount.x > threshold) {
                onMove(Direction.RIGHT)
                hasMoved = true
            } else if (dragAmount.x < -threshold) {
                onMove(Direction.LEFT)
                hasMoved = true
            }
        } else {
            if (dragAmount.y > threshold) {
                onMove(Direction.DOWN)
                hasMoved = true
            } else if (dragAmount.y < -threshold) {
                onMove(Direction.UP)
                hasMoved = true
            }
        }
    }
}

// Note: Le detectDragGestures simple s'active dès le mouvement. 
// Pour éviter de déclencher 10 déplacements par seconde, on pourrait utiliser un état de "verrouillage".
// Mais pour la démo, restons simple.
