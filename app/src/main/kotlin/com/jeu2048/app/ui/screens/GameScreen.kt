package com.jeu2048.app.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jeu2048.app.game.Direction
import com.jeu2048.app.game.GameEngine
import com.jeu2048.app.ui.theme.textColorForTile
import com.jeu2048.app.ui.theme.tileColor
import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.graphics.asAndroidBitmap
import com.jeu2048.app.ui.components.GameGrid
import com.jeu2048.app.ui.components.ScoreCard
import com.jeu2048.app.ui.components.TileCell

@Composable
fun GameScreen(
    engine: GameEngine?,
    themeIndex: Int,
    animationsEnabled: Boolean,
    canUndo: Boolean,
    onMove: (Direction) -> Unit,
    onNewGame: () -> Unit,
    onUndo: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenScores: () -> Unit,
    onShare: (Bitmap) -> Unit,
    onWin: () -> Unit,
    onGameOver: () -> Unit
) {
    val gridState = engine?.grid?.collectAsState(initial = emptyArray()) ?: remember { mutableStateOf(emptyArray<IntArray>()) }
    val grid = gridState.value
    val scoreState = engine?.score?.collectAsState(initial = 0) ?: remember { mutableStateOf(0) }
    val score = scoreState.value
    val bestScoreState = engine?.bestScore?.collectAsState(initial = 0) ?: remember { mutableStateOf(0) }
    val bestScore = bestScoreState.value
    val hasWonState = engine?.hasWon?.collectAsState(initial = false) ?: remember { mutableStateOf(false) }
    val hasWon = hasWonState.value
    val gameOverState = engine?.gameOver?.collectAsState(initial = false) ?: remember { mutableStateOf(false) }
    val gameOver = gameOverState.value

    LaunchedEffect(hasWon) {
        if (hasWon) onWin()
    }
    LaunchedEffect(gameOver) {
        if (gameOver) onGameOver()
    }

    val cs = MaterialTheme.colorScheme
    val lastDirection = remember { mutableStateOf<Direction?>(null) }
    val highlightedRow = remember { mutableStateOf<Int?>(null) }
    val highlightedCol = remember { mutableStateOf<Int?>(null) }
    val gridSizePx = remember { mutableStateOf(0f) }
    val currentGridSize = grid.size.coerceIn(3, 6)

    fun handleMove(dir: Direction) {
        lastDirection.value = dir
        onMove(dir)
    }

    // Effacer la surbrillance "lastDirection" après 400ms (utilisée par les boutons fléchés)
    LaunchedEffect(lastDirection.value) {
        if (lastDirection.value != null) {
            kotlinx.coroutines.delay(400)
            lastDirection.value = null
        }
    }

    val view = LocalView.current

    fun captureAndShare() {
        val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        view.draw(canvas)
        onShare(bitmap)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(cs.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Titre "2048" façon tuile
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFEDC22E)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "2048",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
                
                Column(horizontalAlignment = Alignment.End) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        ScoreCard("SCORE", score)
                        ScoreCard("BEST", bestScore)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = onOpenScores) {
                            Icon(Icons.Default.Star, contentDescription = "Scores", tint = cs.onSurfaceVariant)
                        }
                        IconButton(onClick = onOpenSettings) {
                            Icon(Icons.Default.Settings, contentDescription = "Paramètres", tint = cs.onSurfaceVariant)
                        }
                        IconButton(onClick = ::captureAndShare) {
                            Icon(Icons.Default.Share, contentDescription = "Partager", tint = cs.onSurfaceVariant)
                        }
                        if (canUndo) {
                            IconButton(onClick = onUndo) {
                                Icon(Icons.Default.ArrowBack, contentDescription = "Annuler", tint = cs.onSurfaceVariant)
                            }
                        }
                        IconButton(onClick = onNewGame) {
                            Icon(Icons.Default.Refresh, contentDescription = "Nouvelle partie", tint = cs.onSurfaceVariant)
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
            val gridReady = grid.isNotEmpty() && grid.size in 3..6 && grid.getOrNull(0)?.size in 3..6
            if (engine == null || !gridReady) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Chargement…", color = cs.primary)
                }
            } else {
                // Index tuiles : 0=clair, 1=sombre, 2=coloré (thème app : 0=Clair, 1=Système, 2=Sombre, 3=Coloré)
                val tileThemeIndex = when (themeIndex) {
                    0 -> 0
                    1 -> if (isSystemInDarkTheme()) 1 else 0
                    2 -> 1
                    3 -> 2
                    else -> 0
                }
                Column(modifier = Modifier.fillMaxWidth().fillMaxHeight(1f)) {
                    BoxWithConstraints(
                        modifier = Modifier
                            .fillMaxWidth()
                            .pointerInput(currentGridSize) {
                                val start = FloatArray(2)
                                val last = FloatArray(2)
                                detectDragGestures(
                                    onDragStart = {
                                        start[0] = it.x
                                        start[1] = it.y
                                        last[0] = it.x
                                        last[1] = it.y
                                        highlightedRow.value = null
                                        highlightedCol.value = null
                                    },
                                    onDrag = { change, _ ->
                                        last[0] = change.position.x
                                        last[1] = change.position.y
                                        val dx = last[0] - start[0]
                                        val dy = last[1] - start[1]
                                        val gSizePx = gridSizePx.value
                                        if (gSizePx > 0) {
                                            if (kotlin.math.abs(dx) > kotlin.math.abs(dy)) {
                                                // Mouvement horizontal → montrer la ligne
                                                val row = ((start[1] / gSizePx) * currentGridSize)
                                                    .toInt().coerceIn(0, currentGridSize - 1)
                                                highlightedRow.value = row
                                                highlightedCol.value = null
                                            } else {
                                                // Mouvement vertical → montrer la colonne
                                                val col = ((start[0] / gSizePx) * currentGridSize)
                                                    .toInt().coerceIn(0, currentGridSize - 1)
                                                highlightedCol.value = col
                                                highlightedRow.value = null
                                            }
                                        }
                                    },
                                    onDragEnd = {
                                        val dx = last[0] - start[0]
                                        val dy = last[1] - start[1]
                                        val threshold = 40f
                                        when {
                                            kotlin.math.abs(dx) > kotlin.math.abs(dy) ->
                                                if (dx > threshold) handleMove(Direction.RIGHT)
                                                else if (dx < -threshold) handleMove(Direction.LEFT)
                                            dy > threshold -> handleMove(Direction.DOWN)
                                            dy < -threshold -> handleMove(Direction.UP)
                                        }
                                        highlightedRow.value = null
                                        highlightedCol.value = null
                                    }
                                )
                            }
                    ) {
                        val arrowReserve = 140.dp
                        val side = minOf(maxWidth, (maxHeight - arrowReserve).coerceAtLeast(0.dp))
                        // Mémoriser la taille en px pour les calculs de drag
                        gridSizePx.value = with(LocalDensity.current) { side.toPx() }
                        Box(
                            modifier = Modifier
                                .size(side)
                                .align(Alignment.Center)
                                .clip(RoundedCornerShape(12.dp))
                                .border(4.dp, Color(0xFF8F7A66), RoundedCornerShape(12.dp))
                                .background(cs.surface)
                                .padding(8.dp)
                        ) {
                            GameGrid(
                                grid = grid,
                                tileThemeIndex = tileThemeIndex,
                                animationsEnabled = animationsEnabled,
                                lastDirection = lastDirection.value,
                                highlightedRow = highlightedRow.value,
                                highlightedCol = highlightedCol.value,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                    // Flèches de direction toujours visibles
                    Spacer(Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        DirectionArrowButton(
                            direction = Direction.UP,
                            lastDirection = lastDirection.value,
                            onClick = { handleMove(Direction.UP) }
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        DirectionArrowButton(
                            direction = Direction.LEFT,
                            lastDirection = lastDirection.value,
                            onClick = { handleMove(Direction.LEFT) }
                        )
                        Spacer(Modifier.width(48.dp))
                        DirectionArrowButton(
                            direction = Direction.RIGHT,
                            lastDirection = lastDirection.value,
                            onClick = { handleMove(Direction.RIGHT) }
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        DirectionArrowButton(
                            direction = Direction.DOWN,
                            lastDirection = lastDirection.value,
                            onClick = { handleMove(Direction.DOWN) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DirectionArrowButton(
    direction: Direction,
    lastDirection: Direction?,
    onClick: () -> Unit
) {
    val cs = MaterialTheme.colorScheme
    val isActive = lastDirection == direction
    val icon = when (direction) {
        Direction.UP    -> Icons.Default.KeyboardArrowUp
        Direction.DOWN  -> Icons.Default.KeyboardArrowDown
        Direction.LEFT  -> Icons.Default.KeyboardArrowLeft
        Direction.RIGHT -> Icons.Default.KeyboardArrowRight
    }
    val label = when (direction) {
        Direction.UP    -> "Haut"
        Direction.DOWN  -> "Bas"
        Direction.LEFT  -> "Gauche"
        Direction.RIGHT -> "Droite"
    }
    val bgColor by animateColorAsState(
        targetValue = if (isActive) cs.primary else cs.surface,
        animationSpec = tween(200),
        label = "arrowBg"
    )
    val contentColor by animateColorAsState(
        targetValue = if (isActive) cs.onPrimary else cs.onSurfaceVariant,
        animationSpec = tween(200),
        label = "arrowContent"
    )
    Box(
        modifier = Modifier
            .size(52.dp)
            .clip(RoundedCornerShape(50))
            .background(bgColor)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = contentColor,
            modifier = Modifier.size(32.dp)
        )
    }
}

