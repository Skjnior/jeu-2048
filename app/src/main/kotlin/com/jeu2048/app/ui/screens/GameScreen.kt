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

@Composable
private fun ScoreCard(
    label: String,
    value: Int,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(Color(0xFF8F7A66))
            .padding(horizontal = 16.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                label,
                fontSize = 11.sp,
                color = Color(0xFFEEE4DA),
                fontWeight = FontWeight.Black
            )
            Text(
                value.toString(),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}

@Composable
private fun GameGrid(
    grid: Array<IntArray>,
    tileThemeIndex: Int,
    animationsEnabled: Boolean,
    lastDirection: Direction?,
    highlightedRow: Int?,
    highlightedCol: Int?,
    modifier: Modifier = Modifier
) {
    if (grid.isEmpty()) return
    val size = grid.size.coerceIn(3, 6)
    val spacing = 8.dp
    val highlightColor = Color(0x55FFDD44) // jaune-doré translucide, bien visible

    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(spacing)
    ) {
        for (r in 0 until size) {
            val rowActive = when {
                // Pendant le glissement : ligne précise détectée
                highlightedRow != null -> highlightedRow == r
                // Après clic sur flèche horizontale : toutes les lignes
                lastDirection == Direction.LEFT || lastDirection == Direction.RIGHT -> true
                else -> false
            }
            Row(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(6.dp))
                    .then(if (rowActive) Modifier.background(highlightColor) else Modifier),
                horizontalArrangement = Arrangement.spacedBy(spacing)
            ) {
                for (c in 0 until size) {
                    val value = grid.getOrNull(r)?.getOrNull(c) ?: 0
                    val colActive = when {
                        // Pendant le glissement : colonne précise détectée
                        highlightedCol != null -> highlightedCol == c
                        // Après clic sur flèche verticale : toutes les colonnes
                        lastDirection == Direction.UP || lastDirection == Direction.DOWN -> true
                        else -> false
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(6.dp))
                            .then(if (colActive) Modifier.background(highlightColor) else Modifier)
                    ) {
                        TileCell(
                            value = value,
                            tileThemeIndex = tileThemeIndex,
                            animationsEnabled = animationsEnabled
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TileCell(
    value: Int,
    tileThemeIndex: Int,
    animationsEnabled: Boolean
) {
    val color by animateColorAsState(
        targetValue = tileColor(value, tileThemeIndex),
        animationSpec = if (animationsEnabled) tween(150) else tween(0),
        label = "tileColor"
    )
    val scale by animateFloatAsState(
        targetValue = if (value > 0 && animationsEnabled) 1f else 0.8f,
        animationSpec = spring(dampingRatio = 0.6f),
        label = "scale"
    )
    val textColor = textColorForTile(value, tileThemeIndex)
    val density = LocalDensity.current

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(6.dp))
            .border(1.5.dp, Color(0x33000000), RoundedCornerShape(6.dp))
            .background(color),
        contentAlignment = Alignment.Center
    ) {
        val minSidePx = minOf(constraints.maxWidth, constraints.maxHeight)
        val minSideDp = with(density) { minSidePx.toDp() }
        val valueStr = value.toString()
        val textMultiplier = when (valueStr.length) {
            1, 2 -> 0.45f
            3 -> 0.35f
            4 -> 0.28f
            else -> 0.22f
        }
        val fontSizeSp = (minSideDp.value * textMultiplier).sp

        if (value > 0) {
            Text(
                text = valueStr,
                fontSize = fontSizeSp,
                fontWeight = FontWeight.Bold,
                color = textColor,
                maxLines = 1,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}
