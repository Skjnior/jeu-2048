package com.jeu2048.app.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.QueryStats
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.TableRows
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jeu2048.app.ui.GameViewModel

@OptIn(ExperimentalMaterial3Api::class)
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
    onOpenMultiplayerTimed: () -> Unit,
    onOpenMultiplayerClassic: () -> Unit,
    onOpenChallenge: () -> Unit,
    onShowTutorial: () -> Unit,
    onBack: () -> Unit
) {
    val cs = MaterialTheme.colorScheme

    var showMultiplayerDialog by remember { mutableStateOf(false) }
    var showResetDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Paramètres") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Retour")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = cs.surface,
                    titleContentColor = cs.onSurface,
                    navigationIconContentColor = cs.onSurface
                )
            )
        },
        containerColor = cs.background
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                SectionCard(title = "Modes de jeu", icon = Icons.Default.SportsEsports) {
                    PreferenceAction(
                        title = "Multijoueur (2 joueurs)",
                        subtitle = "Temps limité ou jusqu’à 2048, avec vainqueur.",
                        icon = Icons.Default.People,
                        actionLabel = "Lancer",
                        onClick = { showMultiplayerDialog = true }
                    )
                    PreferenceAction(
                        title = "Défi quotidien",
                        subtitle = "Atteignez un score cible avant la fin du temps.",
                        icon = Icons.Default.Timer,
                        actionLabel = "Défier",
                        onClick = onOpenChallenge
                    )
                }
            }

            item {
                SectionCard(title = "Aide", icon = Icons.Default.Info) {
                    PreferenceAction(
                        title = "Comment jouer",
                        subtitle = "Tutoriel interactif + rappels.",
                        icon = Icons.Default.TouchApp,
                        actionLabel = "Voir",
                        onClick = onShowTutorial
                    )
                }
            }

            item {
                SectionCard(title = "Apparence", icon = Icons.Default.Palette) {
                    LabelSmall("Thème")
                    ChipRow(
                        items = listOf(
                            "Clair" to 0,
                            "Système" to 1,
                            "Sombre" to 2,
                            "Coloré" to 3
                        ),
                        selected = themeIndex,
                        onSelect = onThemeSelect
                    )

                    Spacer(Modifier.height(10.dp))

                    LabelSmall("Taille de la grille")
                    ChipRow(
                        items = (3..6).map { "${it}×$it" to it },
                        selected = settings.gridSize,
                        onSelect = onGridSizeSelect
                    )

                    Spacer(Modifier.height(6.dp))

                    PreferenceSwitch(
                        title = "Animations",
                        subtitle = "Effets et transitions sur les tuiles",
                        icon = Icons.Default.Tune,
                        checked = settings.animations,
                        onCheckedChange = onAnimationsChange
                    )
                }
            }

            item {
                SectionCard(title = "Son", icon = Icons.Default.VolumeUp) {
                    PreferenceSwitch(
                        title = "Sons",
                        subtitle = "Mouvements et fusions",
                        icon = Icons.Default.VolumeUp,
                        checked = settings.soundEnabled,
                        onCheckedChange = onSoundChange
                    )
                    PreferenceSwitch(
                        title = "Musique de fond",
                        subtitle = "Lecture en boucle",
                        icon = Icons.Default.MusicNote,
                        checked = settings.musicEnabled,
                        onCheckedChange = onMusicChange
                    )
                }
            }

            item {
                SectionCard(title = "Statistiques", icon = Icons.Default.QueryStats) {
                    StatLine("Meilleur score", settings.bestScore)
                    StatLine("Parties jouées", settings.gamesPlayed)
                    StatLine("Parties gagnées", settings.gamesWon)
                    StatLine("Parties perdues", settings.gamesLost)
                }
            }

            item {
                SectionCard(title = "Scores", icon = Icons.Default.TableRows) {
                    PreferenceAction(
                        title = "Classement",
                        subtitle = "Voir les meilleurs scores.",
                        icon = Icons.Default.TableRows,
                        actionLabel = "Ouvrir",
                        onClick = onOpenScores
                    )
                    PreferenceAction(
                        title = "Réinitialiser le classement",
                        subtitle = "Supprime définitivement tous les scores.",
                        icon = Icons.Default.DeleteForever,
                        actionLabel = "Supprimer",
                        actionColor = cs.error,
                        onClick = { showResetDialog = true }
                    )
                }
            }
        }
    }

    if (showMultiplayerDialog) {
        AlertDialog(
            onDismissRequest = { showMultiplayerDialog = false },
            title = { Text("Mode multijoueur") },
            text = { Text("Choisissez le type de partie :") },
            confirmButton = {
                TextButton(onClick = {
                    showMultiplayerDialog = false
                    onOpenMultiplayerClassic()
                }) { Text("Jusqu’à 2048") }
            },
            dismissButton = {
                TextButton(onClick = {
                    showMultiplayerDialog = false
                    onOpenMultiplayerTimed()
                }) { Text("Temps limité (3 min)") }
            }
        )
    }

    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text("Réinitialiser le classement ?") },
            text = { Text("Cette action supprimera définitivement tous les scores.") },
            confirmButton = {
                TextButton(onClick = {
                    showResetDialog = false
                    onResetScores()
                }) { Text("Supprimer", color = cs.error) }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) { Text("Annuler") }
            }
        )
    }
}

@Composable
private fun SectionCard(
    title: String,
    icon: ImageVector,
    content: @Composable ColumnScope.() -> Unit
) {
    val cs = MaterialTheme.colorScheme
    ElevatedCard(
        colors = CardDefaults.elevatedCardColors(containerColor = cs.surface),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = null, tint = cs.primary)
                Spacer(Modifier.width(8.dp))
                Text(title, fontWeight = FontWeight.Bold, color = cs.onSurface)
            }
            Spacer(Modifier.height(6.dp))
            content()
        }
    }
}

@Composable
private fun LabelSmall(text: String) {
    val cs = MaterialTheme.colorScheme
    Text(
        text,
        fontSize = 12.sp,
        color = cs.onSurfaceVariant,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(horizontal = 4.dp, vertical = 6.dp)
    )
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun ChipRow(
    items: List<Pair<String, Int>>,
    selected: Int,
    onSelect: (Int) -> Unit
) {
    val cs = MaterialTheme.colorScheme
    // Layout simple: 2 lignes max pour rester propre
    val firstRow = items.take(2)
    val secondRow = items.drop(2)
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            firstRow.forEach { (label, value) ->
                FilterChip(
                    selected = selected == value,
                    onClick = { onSelect(value) },
                    label = { Text(label, maxLines = 1) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = cs.primary,
                        selectedLabelColor = cs.onPrimary
                    )
                )
            }
        }
        if (secondRow.isNotEmpty()) {
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                secondRow.forEach { (label, value) ->
                    FilterChip(
                        selected = selected == value,
                        onClick = { onSelect(value) },
                        label = { Text(label, maxLines = 1) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = cs.primary,
                            selectedLabelColor = cs.onPrimary
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun PreferenceAction(
    title: String,
    subtitle: String,
    icon: ImageVector,
    actionLabel: String,
    actionColor: androidx.compose.ui.graphics.Color? = null,
    onClick: () -> Unit
) {
    val cs = MaterialTheme.colorScheme
    val color = actionColor ?: cs.primary
    ListItem(
        headlineContent = { Text(title) },
        supportingContent = { Text(subtitle) },
        leadingContent = { Icon(icon, contentDescription = null, tint = if (actionColor != null) color else cs.onSurfaceVariant) },
        trailingContent = { Text(actionLabel, color = color, fontWeight = FontWeight.SemiBold) },
        modifier = Modifier
            .clip(MaterialTheme.shapes.medium)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = rememberRipple(),
                onClick = onClick
            )
    )
}

@Composable
private fun PreferenceSwitch(
    title: String,
    subtitle: String,
    icon: ImageVector,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    val cs = MaterialTheme.colorScheme
    ListItem(
        headlineContent = { Text(title) },
        supportingContent = { Text(subtitle) },
        leadingContent = { Icon(icon, contentDescription = null, tint = cs.onSurfaceVariant) },
        trailingContent = {
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedTrackColor = cs.primary,
                    checkedThumbColor = cs.onPrimary,
                    uncheckedTrackColor = cs.surfaceVariant
                )
            )
        }
    )
}

@Composable
private fun StatLine(label: String, value: Int) {
    val cs = MaterialTheme.colorScheme
    ListItem(
        headlineContent = { Text(label) },
        trailingContent = { Text(value.toString(), fontWeight = FontWeight.SemiBold, color = cs.onSurface) }
    )
}
