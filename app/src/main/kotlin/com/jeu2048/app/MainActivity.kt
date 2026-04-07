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
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.unit.dp
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
import com.jeu2048.app.ui.screens.MultiplayerScreen
import com.jeu2048.app.ui.screens.ChallengeScreen

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
    val currentEngine by viewModel.currentEngine.collectAsState()
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
                    engine = currentEngine,
                    themeIndex = settings.theme,
                    animationsEnabled = settings.animations,
                    canUndo = viewModel.canUndo(),
                    onMove = { viewModel.move(it) },
                    onNewGame = { viewModel.startNewGame() },
                    onUndo = { viewModel.undo() },
                    onOpenSettings = { navController.navigate("settings") },
                    onOpenScores = { navController.navigate("scores") },
                    onShare = { viewModel.shareScore(it) },
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
                    onOpenMultiplayerTimed = {
                        viewModel.startMultiplayer(timed = true)
                        navController.navigate("multiplayer")
                    },
                    onOpenMultiplayerClassic = {
                        viewModel.startMultiplayer(timed = false)
                        navController.navigate("multiplayer")
                    },
                    onOpenChallenge = {
                        viewModel.startDailyChallenge()
                        navController.navigate("challenge")
                    },
                    onShowTutorial = { showTutorialDialog = true },
                    onBack = { navController.popBackStack() }
                )
            }
            composable("scores") {
                ScoresScreen(
                    scores = topScores,
                    onBack = { navController.popBackStack() }
                )
            }
            composable("multiplayer") {
                val p1Engine by viewModel.player1Engine.collectAsState()
                val p2Engine by viewModel.player2Engine.collectAsState()
                val mpTimeLeft by viewModel.multiplayerTimeLeft.collectAsState()
                val mpFinished by viewModel.multiplayerFinished.collectAsState()
                val mpWinner by viewModel.multiplayerWinner.collectAsState()
                MultiplayerScreen(
                    p1Engine = p1Engine,
                    p2Engine = p2Engine,
                    themeIndex = settings.theme,
                    timeLeft = mpTimeLeft,
                    finished = mpFinished,
                    winner = mpWinner,
                    onMoveP1 = { viewModel.movePlayer1(it) },
                    onMoveP2 = { viewModel.movePlayer2(it) },
                    onBack = { navController.popBackStack() }
                )
            }
            composable("challenge") {
                val challengeEngine by viewModel.challengeEngine.collectAsState()
                val timeLeft by viewModel.timeLeft.collectAsState()
                val target by viewModel.challengeTargetScore.collectAsState()
                val finished by viewModel.challengeFinished.collectAsState()
                val success by viewModel.challengeSuccess.collectAsState()
                ChallengeScreen(
                    engine = challengeEngine,
                    timeLeft = timeLeft,
                    targetScore = target,
                    finished = finished,
                    success = success,
                    themeIndex = settings.theme,
                    onMove = { challengeEngine?.move(it) },
                    onBack = { navController.popBackStack() },
                    onRetry = { viewModel.startDailyChallenge() }
                )
            }
        }
    }

    if (showWinDialog) {
        val cs = MaterialTheme.colorScheme
        AlertDialog(
            onDismissRequest = { showWinDialog = false },
            title = { Text("Victoire !") },
            text = { Text("Vous avez atteint la tuile 2048. Continuer ou nouvelle partie ?") },
            containerColor = cs.surface,
            tonalElevation = 8.dp,
            confirmButton = {
                Button(onClick = {
                    viewModel.acceptWin()
                    showWinDialog = false
                }) { Text("Continuer") }
            },
            dismissButton = {
                TextButton(onClick = {
                    viewModel.startNewGame()
                    showWinDialog = false
                }) { Text("Nouvelle partie") }
            }
        )
    }
    if (showGameOverDialog) {
        val cs = MaterialTheme.colorScheme
        AlertDialog(
            onDismissRequest = { showGameOverDialog = false },
            title = { Text("Game Over") },
            text = { Text("Plus aucun mouvement possible.") },
            containerColor = cs.surface,
            tonalElevation = 8.dp,
            confirmButton = {
                Button(onClick = {
                    viewModel.startNewGame()
                    showGameOverDialog = false
                }) { Text("Réessayer") }
            },
            dismissButton = {
                TextButton(onClick = {
                    viewModel.shareScore(null)
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
private fun TopBarBack(navController: NavHostController) {
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
