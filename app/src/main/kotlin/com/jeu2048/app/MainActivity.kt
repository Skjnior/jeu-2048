package com.jeu2048.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.jeu2048.app.ui.GameViewModel
import com.jeu2048.app.ui.screens.GameScreen
import com.jeu2048.app.ui.screens.ScoresScreen
import com.jeu2048.app.ui.screens.SettingsScreen
import com.jeu2048.app.ui.screens.TutorialDialog
import androidx.compose.material3.MaterialTheme
import com.jeu2048.app.ui.theme.Jeu2048Theme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: GameViewModel = remember { GameViewModel(application) }
            AppContent(viewModel = viewModel)
        }
    }
}

@Composable
private fun AppContent(viewModel: GameViewModel) {
    val settings by viewModel.settings.collectAsState()
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_PAUSE) viewModel.saveStateOnPause()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }
    Jeu2048Theme(themeIndex = settings.theme) {
        AppNav(viewModel = viewModel)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNav(viewModel: GameViewModel) {
    val navController = rememberNavController()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val settings by viewModel.settings.collectAsState()
    val engine by viewModel.currentEngine.collectAsState()
    val topScores by viewModel.topScores.collectAsState()

    var showWinDialog by remember { androidx.compose.runtime.mutableStateOf(false) }
    var showGameOverDialog by remember { androidx.compose.runtime.mutableStateOf(false) }
    var showTutorialDialog by remember { androidx.compose.runtime.mutableStateOf(false) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = "game",
            modifier = Modifier.padding(paddingValues)
        ) {
            composable("game") {
                GameScreen(
                    engine = engine,
                    themeIndex = settings.theme,
                    animationsEnabled = settings.animations,
                    canUndo = viewModel.canUndo(),
                    onMove = { viewModel.move(it) },
                    onNewGame = { viewModel.startNewGame() },
                    onUndo = { viewModel.undo() },
                    onOpenSettings = { navController.navigate("settings") },
                    onOpenScores = { navController.navigate("scores") },
                    onWin = {
                        viewModel.playWinSound()
                        showWinDialog = true
                    },
                    onGameOver = {
                        viewModel.playLoseSound()
                        showGameOverDialog = true
                    }
                )
            }
            composable("settings") {
                SettingsScreen(
                    settings = settings,
                    themeIndex = settings.theme,
                    onThemeSelect = { viewModel.setTheme(it) },
                    onGridSizeSelect = { viewModel.setGridSize(it) },
                    onAnimationsChange = { viewModel.setAnimations(it) },
                    onSoundChange = { viewModel.setSoundEnabled(it) },
                    onMusicChange = { viewModel.setMusicEnabled(it) },
                    onResetScores = {
                        viewModel.resetScores()
                        scope.launch {
                            snackbarHostState.showSnackbar("Classement réinitialisé")
                        }
                    },
                    onOpenScores = { navController.navigate("scores") },
                    onShowTutorial = { showTutorialDialog = true },
                    onBack = { navController.popBackStack() }
                )
            }
            composable("scores") {
                ScoresScreen(
                    scores = topScores,
                    themeIndex = settings.theme,
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }

    if (showWinDialog) {
        AlertDialog(
            onDismissRequest = { showWinDialog = false },
            title = { Text("Vous avez gagné !") },
            text = { Text("Vous avez atteint la tuile 2048. Continuer ou nouvelle partie ?") },
            confirmButton = {
                Button(onClick = {
                    viewModel.acceptWin()
                    showWinDialog = false
                }) { Text("Continuer") }
            },
            dismissButton = {
                Button(onClick = {
                    viewModel.startNewGame()
                    showWinDialog = false
                }) { Text("Nouvelle partie") }
            }
        )
    }
    if (showGameOverDialog) {
        AlertDialog(
            onDismissRequest = { showGameOverDialog = false },
            title = { Text("Game Over") },
            text = { Text("Plus aucun mouvement possible.") },
            confirmButton = {
                Button(onClick = {
                    viewModel.startNewGame()
                    showGameOverDialog = false
                }) { Text("Réessayer") }
            },
            dismissButton = {
                Button(onClick = {
                    viewModel.shareScore()
                    showGameOverDialog = false
                }) { Text("Partager le score") }
            }
        )
    }
    if (showTutorialDialog) {
        TutorialDialog(themeIndex = settings.theme, onDismiss = { showTutorialDialog = false })
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TopBarBack(navController: NavHostController, themeIndex: Int) {
    TopAppBar(
        title = { },
        navigationIcon = {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(
                    Icons.Filled.ArrowBack,
                    contentDescription = "Retour",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    )
}
