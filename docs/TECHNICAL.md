# Documentation Technique — Jeu 2048 Android

## Sommaire
1. [Architecture générale](#1-architecture-générale)
2. [Moteur de jeu — GameEngine](#2-moteur-de-jeu--gameengine)
3. [Persistance des données](#3-persistance-des-données)
4. [ViewModel et états UI](#4-viewmodel-et-états-ui)
5. [Interface graphique — Compose](#5-interface-graphique--compose)
6. [Thèmes et couleurs](#6-thèmes-et-couleurs)
7. [Gestion des sons](#7-gestion-des-sons)
8. [Navigation](#8-navigation)
9. [Flux de données complet](#9-flux-de-données-complet)
10. [Modes avancés](#10-modes-avancés)
11. [Capture et Partage](#11-capture-et-partage)

---

## 1. Architecture générale

Le projet suit l'architecture **MVVM (Model-View-ViewModel)** avec les couches suivantes :

```
Couche UI (Compose)
        ↕ observe StateFlow / collectAsState()
Couche ViewModel (GameViewModel)
        ↕ coroutines / suspend functions
Couche Data (Room + DataStore + FileProvider)
        ↕
Couche Game Logic (GameEngine × 3)
```

**Technologies utilisées :**
- **Kotlin** — langage principal
- **Jetpack Compose** — UI déclarative
- **Navigation Compose** — navigation entre écrans (NavHost)
- **Room** — base de données locale pour les scores
- **DataStore Preferences** — persistance des paramètres et de l'état de jeu
- **SoundPool + MediaPlayer** — gestion audio
- **StateFlow / coroutines** — réactivité des états

---

## 2. Moteur de jeu — GameEngine

**Fichier :** `game/GameEngine.kt`

### Initialisation

```kotlin
class GameEngine(private val gridSize: Int = 4)
```
La grille est configurable de **3×3 à 6×6**.

### Mode Aléatoire vs Seedé
- **`startNewGame()`** : Utilise `Random.Default` pour une expérience imprévisible.
- **`startSeededGame(seed: Long)`** : Utilise une instance de `Random(seed)` dédiée. Utilisé pour le **Défi Quotidien**, garantissant que tous les joueurs reçoivent les mêmes tuiles à chaque coup pour une date donnée.

### États internes (StateFlow)

| StateFlow | Type | Rôle |
|---|---|---|
| `_grid` | `Array<IntArray>` | Valeur de chaque cellule |
| `_score` | `Int` | Score courant |
| `_bestScore` | `Int` | Meilleur score de la session |
| `_hasWon` | `Boolean` | Victoire atteinte (tuile 2048) |
| `_gameOver` | `Boolean` | Aucun mouvement possible |
| `_lastMoveHadMerge` | `Boolean` | Indique si le dernier coup a fusionné |

### Déplacement — `move(direction: Direction)`

1. Sauvegarde l'état avant mouvement dans `moveHistory` (undo).
2. Pour chaque ligne (LEFT/RIGHT) ou colonne (UP/DOWN), appelle `mergeLine()`.
3. Si au moins une cellule a bougé, ajoute une nouvelle tuile aléatoire (2 ou 4).
4. Vérifie la victoire (`2048` dans la grille) et la défaite (`canMove() == false`).

### Fusion d'une ligne — `mergeLine()`

Algorithme en O(n) :
1. Parcourir la ligne de gauche à droite.
2. Ignorer les zéros (cellules vides).
3. Si la dernière cellule ajoutée est identique et non encore fusionnée → doubler.
4. Sinon → ajouter à la fin.
5. Retourner `Triple(nouvelleGrille, scoreDelta, aChangé)`.

### Undo — `undo()`
Le jeu garde un historique (`moveHistory`) des 10 derniers états. `undo()` restaure le dernier snapshot.

---

## 3. Persistance des données

### 3.1 DataStore — Paramètres (`SettingsPreferences.kt`)

Stocke les préférences utilisateur de façon asynchrone :
- Thème actif (0=Clair, 1=Système, 2=Sombre, 3=Coloré)
- Taille de grille (3 à 6)
- Animations activées (booléen)
- Sons activés (booléen)
- Musique activée (booléen)
- Statistiques (parties jouées, gagnées, perdues)

### 3.2 DataStore — État de partie (`GamePreferences.kt`)

Sauvegarde automatiquement l'état complet de la grille :
- Taille de la grille
- Cellules (liste plate de n² entiers)
- Score courant, meilleur score
- État `hasWon`

La sauvegarde est déclenchée lors du `ON_PAUSE` de l'activité.

### 3.3 Room — Tableau des scores (`ScoreDatabase.kt`)

```kotlin
@Entity data class ScoreEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val score: Int,
    val timestamp: Long
)
```

Le DAO expose :
- `insertScore(score)` — insertion d'un nouveau score
- `getTopScores(limit)` — récupération des meilleurs scores triés

---

## 4. ViewModel et états UI

**Fichier :** `ui/GameViewModel.kt`

Le `GameViewModel` est le point central qui lie la logique de jeu à l'interface :

### États exposés

```kotlin
val settings: StateFlow<SettingsUi>     // tous les paramètres
val currentEngine: StateFlow<GameEngine> // moteur de jeu courant
val topScores: StateFlow<List<ScoreEntity>> // classement
```

### Actions principales

| Méthode | Effet |
|---|---|
| `startNewGame()` | Crée un nouveau GameEngine et démarre une partie |
| `move(direction)` | Transmet le déplacement au moteur, joue le son |
| `undo()` | Annule le dernier déplacement |
| `setTheme(index)` | Change le thème et persiste |
| `setGridSize(size)` | Change la taille et relance une partie |
| `setSoundEnabled(b)` | Active/désactive les sons |
| `setMusicEnabled(b)` | Démarre/arrête la musique de fond |
| `saveStateOnPause()` | Sauvegarde l'état lors de la mise en pause |
| `startMultiplayer()` | Initialise deux moteurs indépendants pour le mode PVP |
| `startDailyChallenge()`| Initialise un moteur seedé et lance le compte à rebours |
| `shareScore(bitmap)` | Génère et envoie l'image via ShareHelper |

---

## 5. Interface graphique — Compose

### 5.1 GameScreen

Écran principal. Composants clés :

**Header** : logo "2048" (tuile jaune), ScoreCards SCORE/BEST, boutons (⭐ scores, ⚙️ paramètres, ↩ undo, 🔄 nouvelle partie).

**Grille** (`BoxWithConstraints`) :
- Calcule la taille optimale en fonction de l'espace disponible.
- Détecte les gestes de glissement (`detectDragGestures`).
- En temps réel pendant le glissement : calcule la ligne ou colonne touchée et l'illumine.

**GameGrid** : Affiche la grille NxN avec espacement uniforme via `weight(1f)`.

**TileCell** :
- `animateColorAsState` pour la transition de couleur.
- Taille du texte adaptative selon le nombre de chiffres (0.45× pour 1-2 chiffres, 0.35× pour 3, etc.).
- Bordure semi-transparente sur chaque cellule.

**DirectionArrowButton** : Bouton circulaire animé. S'illumine (`animateColorAsState`) lors du clic et reste allumé 400ms (`LaunchedEffect` + `delay`).

### 5.2 SettingsScreen

- AppBar marron avec bouton retour.
- Chips de thème et de taille de grille (Box 52×52dp, taille fixe).
- Switchs pour animations, sons et musique.
- Statistiques et gestion du classement.

### 5.4 MultiplayerScreen (Nouveau)
- **Split-screen** : Deux joueurs sur la même vue.
- **Orientation** : La vue du joueur 2 est pivotée de **180°** (`graphicsLayer(rotationZ = 180f)`) pour un usage face-à-face.
- **Indépendance** : Chaque zone de l'écran capture les gestes (`detectDragGestures`) et les transmet à son propre `GameEngine`.

### 5.5 ChallengeScreen (Nouveau)
- **Mode chronométré** : Affiche un compte à rebours (`timeLeft`) synchronisé avec une `Coroutine` du ViewModel.
- **Objectif fixe** : Condition de victoire visuelle (Atteindre 2048).

### 5.6 GameComponents (Reusability)
- Extraction de `GameGrid` et `TileCell` pour permettre leur affichage dans 4 contextes différents : Jeu classique, Multijoueur, Défi, et Tutoriel Animé.

---

## 6. Thèmes et couleurs

**Fichier :** `ui/theme/Theme.kt`

Trois thèmes de tuiles indépendants du thème système :

| Index | Nom | Tuile 2 | Tuile 2048 |
|---|---|---|---|
| 0 | Clair | `#EEE4DA` (beige clair) | `#EDC22E` (or) |
| 1 | Sombre | `#5A4FCF` (indigo) | `#FFEB3B` (jaune vif) |
| 2 | Coloré | `#6C5CE7` (violet) | `#00B894` (vert) |

Fonctions exposées :
- `tileColor(value, theme)` — couleur de fond d'une tuile
- `textColorForTile(value, theme)` — blanc pour value > 4, sombre pour 2 et 4 (thème clair)

---

## 7. Gestion des sons

**Fichier :** `sound/SoundManager.kt`

### Architecture audio

```
SoundPool ─── move.mp3──→ playMove() (pitch 1.0x)
                      └──→ playMerge() (pitch 1.2x)
MediaPlayer ─── bg_music.mp3 ──→ lecture en boucle (volume 0.3)
```

### Initialisation
- `SoundPool` est initialisé avec `SoundPool.Builder().setMaxStreams(5)`.
- `move.mp3` est chargé via `getIdentifier("move", "raw", ...)`.
- La musique de fond démarre uniquement si l'utilisateur l'active depuis les Paramètres.

### Nettoyage
`release()` libère SoundPool, ToneGenerator et MediaPlayer pour éviter les fuites mémoire.

---

## 10. Modes avancés

### 10.1 Tutoriel Animé
Le tutoriel n'est plus une simple liste de texte. Il inclut un `LaunchedEffect` qui boucle sur une séquence d'états de grille prédéfinis (fusion de 2+2, apparition de tuile, mouvement) pour illustrer dynamiquement les règles.

### 10.2 Multijoueur
Deux instances de `GameEngine` sont maintenues en parallèle dans le `GameViewModel`. Le score final est comparé lors de la navigation retour ou lors d'un Game Over.

---

## 11. Capture et Partage

### Partage d'image récapitulative
1. **Capture** : Lorsqu'un utilisateur clique sur "Partager", `GameScreen` utilise `LocalView.current` pour dessiner la vue actuelle dans un `android.graphics.Canvas` lié à un `Bitmap`.
2. **Stockage** : Le `Bitmap` est compressé en PNG et stocké dans le dossier `cache/images/` de l'application.
3. **Partage** : Un `content://` URI est généré via **FileProvider** (`androidx.core.content.FileProvider`) pour accorder des permissions temporaires de lecture à l'application de partage choisie (WhatsApp, Email, etc.).
