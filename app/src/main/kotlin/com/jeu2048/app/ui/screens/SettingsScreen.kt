package com.jeu2048.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jeu2048.app.ui.GameViewModel

@Composable
fun SettingsScreen(
    settings: GameViewModel.SettingsUi,
    themeIndex: Int,
    onThemeSelect: (Int) -> Unit,
    onGridSizeSelect: (Int) -> Unit,
    onAnimationsChange: (Boolean) -> Unit,
    onSoundChange: (Boolean) -> Unit,
    onMusicChange: (Boolean) -> Unit,
    onResetScores: () -> Unit,
    onOpenScores: () -> Unit,
    onOpenMultiplayer: () -> Unit,
    onOpenChallenge: () -> Unit,
    onShowTutorial: () -> Unit,
    onBack: () -> Unit
) {
    val cs = MaterialTheme.colorScheme
    val scroll = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(cs.background)
    ) {
        // ── AppBar ──────────────────────────────────────────
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
            Spacer(Modifier.width(4.dp))
            Text(
                "Paramètres",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }

        // ── Contenu scrollable ───────────────────────────────
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scroll)
                .padding(16.dp)
        ) {
            // ─ Modes de Jeu ───────────────────────────────
            SectionTitle("Modes de Jeu")
            SettingRow(
                label = "Mode Multijoueur (2 joueurs)",
                onClick = onOpenMultiplayer,
                trailing = { Text("Lancer →", color = cs.primary, fontWeight = FontWeight.Medium) }
            )
            Divider(color = cs.surfaceVariant, thickness = 1.dp)
            SettingRow(
                label = "Défi Quotidien (Temps limité)",
                onClick = onOpenChallenge,
                trailing = { Text("Défier →", color = cs.primary, fontWeight = FontWeight.Medium) }
            )
            Divider(color = cs.surfaceVariant, thickness = 1.dp)

            // ─ Aide ─────────────────────────────────────────
            SectionTitle("Aide")
            SettingRow(
                label = "Règles du jeu (tutoriel)",
                onClick = onShowTutorial,
                trailing = { Text("Voir →", color = cs.primary, fontWeight = FontWeight.Medium) }
            )
            Divider(color = cs.surfaceVariant, thickness = 1.dp)

            // ─ Apparence ────────────────────────────────────
            SectionTitle("Apparence")
            // Thème — chips en deux lignes si nécessaire
            Text("Thème", color = cs.onSurface, fontSize = 16.sp,
                modifier = Modifier.padding(vertical = 6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("Clair" to 0, "Système" to 1, "Sombre" to 2, "Coloré" to 3)
                    .forEach { (label, index) ->
                        val selected = themeIndex == index
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (selected) Color(0xFF8F7A66) else cs.surfaceVariant)
                                .clickable(
                                    indication = rememberRipple(),
                                    interactionSource = remember { MutableInteractionSource() },
                                    onClick = { onThemeSelect(index) }
                                )
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                label,
                                color = if (selected) Color.White else cs.onSurfaceVariant,
                                fontSize = 14.sp,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                maxLines = 1
                            )
                        }
                    }
            }

            Spacer(Modifier.height(12.dp))

            // Taille de grille
            Text("Taille de la grille", color = cs.onSurface, fontSize = 16.sp,
                modifier = Modifier.padding(vertical = 6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                (3..6).forEach { size ->
                    val selected = settings.gridSize == size
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (selected) Color(0xFF8F7A66) else cs.surfaceVariant)
                            .clickable(
                                indication = rememberRipple(),
                                interactionSource = remember { MutableInteractionSource() },
                                onClick = { onGridSizeSelect(size) }
                            )
                            .size(52.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "${size}×${size}",
                            color = if (selected) Color.White else cs.onSurfaceVariant,
                            fontSize = 13.sp,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }

            Spacer(Modifier.height(8.dp))
            Divider(color = cs.surfaceVariant, thickness = 1.dp)

            SwitchRow("Animations", settings.animations, onAnimationsChange)
            Divider(color = cs.surfaceVariant, thickness = 1.dp)

            // ─ Son ──────────────────────────────────────────
            SectionTitle("Son")
            SwitchRow("Sons (mouvements, fusions)", settings.soundEnabled, onSoundChange)
            Divider(color = cs.surfaceVariant, thickness = 1.dp)
            SwitchRow("Musique de fond", settings.musicEnabled, onMusicChange)
            Divider(color = cs.surfaceVariant, thickness = 1.dp)

            // ─ Statistiques ─────────────────────────────────
            SectionTitle("Statistiques")
            StatRow("Parties jouées", settings.gamesPlayed)
            Divider(color = cs.surfaceVariant, thickness = 1.dp)
            StatRow("Parties gagnées", settings.gamesWon)
            Divider(color = cs.surfaceVariant, thickness = 1.dp)
            StatRow("Parties perdues", settings.gamesLost)
            Divider(color = cs.surfaceVariant, thickness = 1.dp)

            // ─ Scores ────────────────────────────────────────
            SectionTitle("Scores")
            SettingRow(
                label = "Voir le classement",
                onClick = onOpenScores,
                trailing = { Text("Voir →", color = cs.primary, fontWeight = FontWeight.Medium) }
            )
            Divider(color = cs.surfaceVariant, thickness = 1.dp)
            SettingRow(
                label = "Réinitialiser le classement",
                onClick = onResetScores,
                trailing = { Text("Supprimer", color = Color(0xFFE57373), fontWeight = FontWeight.Medium) }
            )
        }
    }
}

@Composable
private fun SectionTitle(title: String) {
    Text(
        title,
        modifier = Modifier.padding(top = 20.dp, bottom = 4.dp),
        color = Color(0xFF8F7A66),
        fontSize = 12.sp,
        fontWeight = FontWeight.Black
    )
}

@Composable
private fun SwitchRow(label: String, checked: Boolean, onChecked: (Boolean) -> Unit) {
    val cs = MaterialTheme.colorScheme
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, color = cs.onSurface, fontSize = 16.sp)
        Switch(
            checked = checked,
            onCheckedChange = onChecked,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = Color(0xFF8F7A66),
                uncheckedTrackColor = cs.surfaceVariant
            )
        )
    }
}

@Composable
private fun StatRow(label: String, value: Int) {
    val cs = MaterialTheme.colorScheme
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, color = cs.onSurface, fontSize = 16.sp)
        Text(
            value.toString(),
            color = cs.onSurface,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun SettingRow(
    label: String,
    onClick: (() -> Unit)? = null,
    trailing: @Composable (() -> Unit)? = null
) {
    val cs = MaterialTheme.colorScheme
    val rowModifier = Modifier
        .fillMaxWidth()
        .padding(vertical = 12.dp)
        .then(
            if (onClick != null) {
                Modifier.clickable(
                    indication = rememberRipple(color = cs.onSurface.copy(alpha = 0.15f)),
                    interactionSource = remember { MutableInteractionSource() },
                    onClick = onClick
                )
            } else Modifier
        )
    Row(
        modifier = rowModifier,
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, color = cs.onSurface, fontSize = 16.sp)
        trailing?.invoke()
    }
}
