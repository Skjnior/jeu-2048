package com.jeu2048.app.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun TutorialDialog(
    themeIndex: Int,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Comment jouer") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                Text("• Glissez vers le haut, le bas, la gauche ou la droite pour déplacer toutes les tuiles.")
                Spacer(Modifier.height(8.dp))
                Text("• Deux tuiles de même valeur fusionnent en une seule (ex: 2+2=4). Le total des fusions donne votre score.")
                Spacer(Modifier.height(8.dp))
                Text("• Après chaque coup, une nouvelle tuile (2 ou 4) apparaît.")
                Spacer(Modifier.height(8.dp))
                Text("• Objectif : atteindre la tuile 2048 !")
                Spacer(Modifier.height(8.dp))
                Text("• La partie s'arrête quand plus aucun mouvement n'est possible.")
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("J'ai compris")
            }
        }
    )
}
