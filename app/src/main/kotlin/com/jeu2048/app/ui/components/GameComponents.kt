package com.jeu2048.app.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jeu2048.app.game.Direction
import com.jeu2048.app.ui.theme.textColorForTile
import com.jeu2048.app.ui.theme.tileColor

@Composable
fun GameGrid(
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
    val highlightColor = Color(0x55FFDD44)

    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(spacing)
    ) {
        for (r in 0 until size) {
            val rowActive = when {
                highlightedRow != null -> highlightedRow == r
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
                        highlightedCol != null -> highlightedCol == c
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
fun TileCell(
    value: Int,
    tileThemeIndex: Int,
    animationsEnabled: Boolean
) {
    val color by animateColorAsState(
        targetValue = tileColor(value, tileThemeIndex),
        animationSpec = if (animationsEnabled) tween(150) else tween(0),
        label = "tileColor"
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
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun ScoreCard(
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
