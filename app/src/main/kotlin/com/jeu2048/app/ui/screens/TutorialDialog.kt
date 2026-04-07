package com.jeu2048.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jeu2048.app.game.Direction
import com.jeu2048.app.ui.components.GameGrid
import kotlinx.coroutines.delay

@Composable
fun TutorialDialog(
    themeIndex: Int,
    onDismiss: () -> Unit
) {
    val cs = MaterialTheme.colorScheme
    val tileThemeIndex = when (themeIndex) {
        3 -> 2
        2 -> 1
        else -> 0
    }

    data class TutorialStep(
        val title: String,
        val description: String,
        val grid: Array<IntArray>,
        val direction: Direction?,
        val highlightedRow: Int? = null,
        val highlightedCol: Int? = null
    )

    val steps = remember {
        listOf(
            TutorialStep(
                title = "1) Déplacer",
                description = "Glisse dans une direction : toutes les tuiles bougent ensemble.",
                grid = arrayOf(
                    intArrayOf(2, 0, 0),
                    intArrayOf(0, 0, 0),
                    intArrayOf(0, 0, 2)
                ),
                direction = null
            ),
            TutorialStep(
                title = "2) Fusionner",
                description = "Deux tuiles identiques fusionnent : 2 + 2 = 4 (et +4 au score).",
                grid = arrayOf(
                    intArrayOf(2, 0, 2),
                    intArrayOf(0, 0, 0),
                    intArrayOf(0, 0, 0)
                ),
                direction = Direction.RIGHT,
                highlightedRow = 0
            ),
            TutorialStep(
                title = "3) Nouvelle tuile",
                description = "Après chaque coup valide, une nouvelle tuile (2 ou 4) apparaît.",
                grid = arrayOf(
                    intArrayOf(0, 0, 4),
                    intArrayOf(2, 0, 0),
                    intArrayOf(0, 0, 0)
                ),
                direction = null
            ),
            TutorialStep(
                title = "4) Colonne active",
                description = "En montant/descendant, une colonne se réorganise (exemple).",
                grid = arrayOf(
                    intArrayOf(0, 2, 0),
                    intArrayOf(0, 2, 0),
                    intArrayOf(0, 0, 0)
                ),
                direction = Direction.UP,
                highlightedCol = 1
            ),
            TutorialStep(
                title = "5) Fin de partie",
                description = "La partie se termine quand aucun mouvement n’est possible.",
                grid = arrayOf(
                    intArrayOf(2, 4, 2),
                    intArrayOf(4, 2, 4),
                    intArrayOf(2, 4, 2)
                ),
                direction = null
            )
        )
    }

    var stepIndex by remember { mutableIntStateOf(0) }
    var autoPlay by remember { mutableStateOf(true) }
    val current = steps[stepIndex]

    LaunchedEffect(autoPlay) {
        while (autoPlay) {
            delay(1800)
            stepIndex = (stepIndex + 1) % steps.size
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
                Text(
                    "Démo interactive",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Black,
                    color = cs.primary,
                    modifier = Modifier.align(Alignment.Start).padding(bottom = 6.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(current.title, fontWeight = FontWeight.Bold, color = cs.onSurface)
                        Text(
                            current.description,
                            fontSize = 13.sp,
                            color = cs.onSurfaceVariant,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Auto", fontSize = 12.sp, color = cs.onSurfaceVariant)
                        Switch(
                            checked = autoPlay,
                            onCheckedChange = { autoPlay = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = Color(0xFF8F7A66),
                                uncheckedTrackColor = cs.surfaceVariant
                            )
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .padding(top = 10.dp)
                        .size(180.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .border(2.dp, Color(0xFF8F7A66), RoundedCornerShape(8.dp))
                        .background(cs.surface)
                        .padding(4.dp)
                ) {
                    GameGrid(
                        grid = current.grid,
                        tileThemeIndex = tileThemeIndex,
                        animationsEnabled = true,
                        lastDirection = current.direction,
                        highlightedRow = current.highlightedRow,
                        highlightedCol = current.highlightedCol
                    )
                }
                
                Spacer(Modifier.height(10.dp))

                if (!autoPlay) {
                    val canPrev = stepIndex > 0
                    val canNext = stepIndex < steps.lastIndex
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        FilledTonalButton(
                            onClick = { stepIndex -= 1 },
                            enabled = canPrev,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Précédent", maxLines = 1)
                        }
                        Spacer(Modifier.width(10.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            IconButton(onClick = { stepIndex = 1 }) {
                                Icon(Icons.Default.KeyboardArrowRight, contentDescription = "Fusion")
                            }
                            IconButton(onClick = { stepIndex = 3 }) {
                                Icon(Icons.Default.KeyboardArrowUp, contentDescription = "Colonne")
                            }
                        }
                        Spacer(Modifier.width(10.dp))
                        FilledTonalButton(
                            onClick = { stepIndex += 1 },
                            enabled = canNext,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Suivant", maxLines = 1)
                        }
                    }
                }

                Spacer(Modifier.height(14.dp))

                Text(
                    "Rappels rapides",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Black,
                    color = cs.primary,
                    modifier = Modifier.align(Alignment.Start)
                )
                Spacer(Modifier.height(6.dp))
                Column(modifier = Modifier.fillMaxWidth()) {
                    TutorialItem("• Glissez (haut/bas/gauche/droite) pour déplacer toutes les tuiles.")
                    TutorialItem("• Les fusions ne se font qu’entre tuiles identiques, une fois par coup.")
                    TutorialItem("• Après chaque coup valide : une nouvelle tuile (2 ou 4) apparaît.")
                    TutorialItem("• Objectif : atteindre 2048. Fin si aucun mouvement n’est possible.")
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
