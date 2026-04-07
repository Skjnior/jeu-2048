# 2048 Android - Dossier complet de revision (architecture + code)

Ce document est une reference complete pour:
- comprendre l'application,
- expliquer l'architecture au professeur,
- pouvoir reecrire rapidement une partie supprimee du code.

---

## 1) Objectif de l'application

Application Android du jeu 2048 (Kotlin + Jetpack Compose) avec:
- mode classique,
- sauvegarde/reprise,
- classement local,
- parametres (theme, taille grille, animations, sons/musique),
- multijoueur local 2 joueurs,
- defi quotidien chronometre,
- tutoriel interactif,
- partage du score (texte + image PNG).

---

## 2) Technologies, frameworks, outils

## Langage et plateforme
- Kotlin
- Android SDK (min 26, target 35, compile 35)
- JVM target 17

## UI
- Jetpack Compose
- Material 3 + Material icons
- Navigation Compose

## Architecture et etat
- MVVM
- `StateFlow` / Coroutines
- `collectAsState` dans les ecrans

## Persistance
- DataStore Preferences:
  - preferences utilisateur
  - sauvegarde partie courante
- Room:
  - base locale des scores

## Audio
- `SoundPool` pour effets courts (move/merge/win/lose)
- `MediaPlayer` pour musique de fond
- `ToneGenerator` en fallback

## Partage
- `Intent.ACTION_SEND`
- `FileProvider` pour partager image PNG depuis le cache

## Build
- Gradle Kotlin DSL (`.kts`)
- KSP pour Room compiler
- AGP 8.13.2 / Kotlin 1.9.22

---

## 3) Architecture globale (vue mentale)

## Couches
1. **UI Compose** (`ui/screens`, `ui/components`)
2. **ViewModel** (`GameViewModel`)
3. **Game logic** (`GameEngine`)
4. **Data layer** (`SettingsPreferences`, `GamePreferences`, Room DAO/DB)
5. **Services** (`SoundManager`, `ShareHelper`)

## Flux principal d'un mouvement
1. Ecran detecte swipe/bouton -> envoie `Direction` au ViewModel.
2. ViewModel appelle `GameEngine.move(direction)`.
3. `GameEngine` fusionne/deplace, met a jour score et grille.
4. ViewModel joue son, sauvegarde etat, met a jour stats si besoin.
5. UI recomposee automatiquement via `StateFlow`.

---

## 4) Comment reecrire une partie supprimee (strategie examen)

Si le prof supprime un fichier:
1. Refaire la **signature publique** (classe/fonction + parametres).
2. Reconnecter les dependances minimales.
3. Remettre la logique coeur (moteur / persistence / UI).
4. Recompiler.

Priorites de reconstruction:
- `GameEngine.kt` (coeur algorithme)
- `GameViewModel.kt` (orchestration)
- `MainActivity.kt` (navigation et wiring)
- `GameScreen.kt` (interaction joueur)

---

## 5) Reference fichier par fichier

## Racine Gradle

### `settings.gradle.kts`
- Declare repositories plugin/dependencies.
- Inclut module `:app`.

### `build.gradle.kts` (racine)
- Versions des plugins:
  - Android application plugin
  - Kotlin Android
  - KSP

### `app/build.gradle.kts`
- Configuration Android app:
  - namespace/appId,
  - SDK versions,
  - buildTypes, minify, signing, Java 17,
  - Compose active.
- Dependances principales:
  - Compose, Material3, Navigation,
  - Room + KSP,
  - DataStore,
  - Lifecycle/coroutines.

Note importante securite:
- Le fichier contient actuellement des credentials de signature en clair.
- Bonne pratique: migrer vers `gradle.properties` local/non versionne.

---

## Manifest et application

### `app/src/main/AndroidManifest.xml`
- Definis:
  - permission vibration,
  - `GameApplication`,
  - `MainActivity` (launcher, portrait),
  - `FileProvider` pour partage image.

### `app/src/main/kotlin/com/jeu2048/app/GameApplication.kt`
- Classe `Application` minimale.
- Point d'entree process Android.

### `app/src/main/kotlin/com/jeu2048/app/MainActivity.kt`
- Point d'entree UI.
- Active edge-to-edge.
- Instancie `GameViewModel`.
- Navigation Compose:
  - `game`,
  - `settings`,
  - `scores`,
  - `multiplayer`,
  - `challenge`.
- Gere dialogs globaux:
  - victoire,
  - game over,
  - tutoriel.
- Sauvegarde auto sur `ON_PAUSE` via observer lifecycle.

---

## Game logic (coeur du 2048)

### `app/src/main/kotlin/com/jeu2048/app/game/Direction.kt`
- Enum des directions: `UP`, `DOWN`, `LEFT`, `RIGHT`.

### `app/src/main/kotlin/com/jeu2048/app/game/GameState.kt`
- DTO de sauvegarde:
  - `gridSize`, `cells`, `score`, `bestScore`, `hasWon`.
- `toGrid()` reconstruit matrice 2D depuis liste plate.

### `app/src/main/kotlin/com/jeu2048/app/game/GameEngine.kt`
- Classe cle: logique complete du jeu.
- `gridSize` borne a 3..6.
- Etats exposes en `StateFlow`:
  - grille,
  - score,
  - best,
  - hasWon,
  - gameOver,
  - lastMoveHadMerge.
- Fonctions principales:
  - `startNewGame()`: reset + 2 tuiles aleatoires.
  - `move(direction)`: deplacement/fusion + spawn + win/lose.
  - `mergeLine(...)`: algo fusion d'une ligne/colonne.
  - `undo()`: restauration depuis historique (max 10).
  - `startSeededGame(seed)`: random deterministe (defi).
  - `getStateForSave()`: serialisation objet metier.

Pseudo-algo `move(direction)`:
1. Copie grille courante.
2. Sauvegarde snapshot pour undo.
3. Traite chaque ligne/colonne selon direction.
4. Si aucun changement: annule snapshot, retourne false.
5. Sinon met a jour score/best.
6. Ajoute tuile aleatoire 2/4.
7. Teste victoire (2048) puis possibilite de mouvement.

---

## Data layer

### `app/src/main/kotlin/com/jeu2048/app/data/SettingsPreferences.kt`
- DataStore "settings".
- Stocke:
  - taille grille,
  - theme,
  - animations,
  - sons,
  - musique,
  - stats (played/won/lost).
- Expose des `Flow`.
- Methodes `set...` + `incrementGames...`.

### `app/src/main/kotlin/com/jeu2048/app/data/GamePreferences.kt`
- DataStore "game_prefs".
- Sauvegarde partie courante en string serializee.
- Expose `savedGameFlow`.
- Methodes:
  - `saveGame(state)`,
  - `clearSavedGame()`,
  - parse/serialize.

### `app/src/main/kotlin/com/jeu2048/app/data/ScoreEntity.kt`
- Entite Room `scores`:
  - `id`,
  - `score`,
  - `gridSize`,
  - `timestamp`.

### `app/src/main/kotlin/com/jeu2048/app/data/ScoreDao.kt`
- DAO Room:
  - `insert(scoreEntity)`,
  - `getTopScores(limit)` (tri score desc),
  - `deleteAll()`.

### `app/src/main/kotlin/com/jeu2048/app/data/ScoreDatabase.kt`
- Declaration DB Room.
- `DatabaseProvider` singleton thread-safe.

---

## ViewModel (orchestration)

### `app/src/main/kotlin/com/jeu2048/app/ui/GameViewModel.kt`
- Classe centrale MVVM.
- Gere:
  - moteur classique (`currentEngine`),
  - multijoueur (`player1Engine`, `player2Engine`, timer/resultat),
  - defi (`challengeEngine`, timer/target/success),
  - settings combines,
  - top scores + best score global.
- Au demarrage:
  - restaure partie si compatible taille,
  - sinon nouvelle partie.
- Fonctions classiques:
  - `startNewGame`, `move`, `undo`, `acceptWin`, `setGridSize`, `setTheme`, etc.
- Sauvegarde auto:
  - `saveState` et `saveStateOnPause`.
- Multijoueur:
  - `startMultiplayer(timed)`,
  - timer optionnel 180s,
  - fin par victoire 2048 (mode illimite) ou 2 game over.
- Defi:
  - seed journalier (`LocalDate.now().toEpochDay()`),
  - objectif ajuste a la taille de grille,
  - temps ajuste a la taille de grille,
  - succes/fin determines dans coroutine.

---

## Services UI

### `app/src/main/kotlin/com/jeu2048/app/ui/ShareHelper.kt`
- Construit texte de partage.
- Option image:
  - sauve bitmap dans `cache/images/score_2048.png`,
  - recupere URI via `FileProvider`.
- Lance chooser Android.

### `app/src/main/kotlin/com/jeu2048/app/sound/SoundManager.kt`
- Gestion des sons.
- `SoundPool` charge:
  - `move`,
  - `win`,
  - `lose`.
- Musique de fond:
  - charge `bg_music` via `MediaPlayer` si present.
- API:
  - `setEnabled`,
  - `setMusicEnabled`,
  - `playMove/merge/win/lose`,
  - `release`.

Note:
- Dans `res/raw`, `win.mp3` et `lose.mp3` existent.
- Si `move.mp3` ou `bg_music.mp3` absents, fallback partiel (beep / pas de musique).

---

## UI components reutilisables

### `app/src/main/kotlin/com/jeu2048/app/ui/components/GameComponents.kt`
- `GameGrid(...)`:
  - affichage grille NxN,
  - surbrillance ligne/colonne.
- `TileCell(...)`:
  - couleur selon valeur/theme,
  - texte adaptatif selon longueur nombre.
- `ScoreCard(...)`:
  - carte score/best.

---

## Ecrans UI

### `app/src/main/kotlin/com/jeu2048/app/ui/screens/GameScreen.kt`
- Ecran principal.
- Observe engine (`grid`, `score`, `best`, `hasWon`, `gameOver`).
- Header:
  - logo 2048,
  - score/best,
  - boutons scores/settings/share/undo/new game.
- Interaction:
  - swipe (drag gestures),
  - boutons directionnels.
- UI feedback:
  - surbrillance ligne/colonne pendant drag,
  - bouton direction active 400ms.
- Partage:
  - capture vue courante en bitmap + callback `onShare`.

### `app/src/main/kotlin/com/jeu2048/app/ui/screens/SettingsScreen.kt`
- Ecran parametres en sections:
  - modes (multi, defi),
  - aide (tutoriel),
  - apparence (theme, taille grille, animations),
  - son (sons/musique),
  - statistiques (meilleur score + parties),
  - scores (ouvrir/reset).
- Dialog choix mode multijoueur.
- Dialog confirmation reset classement.

### `app/src/main/kotlin/com/jeu2048/app/ui/screens/ScoresScreen.kt`
- Affiche top scores.
- Tri deja fait par DAO.
- Badge rang, medailles top 3.
- Affiche score + date.

### `app/src/main/kotlin/com/jeu2048/app/ui/screens/MultiplayerScreen.kt`
- Split-screen face-a-face:
  - joueur 2 en haut rotation 180 degres,
  - joueur 1 en bas.
- Barre centrale:
  - titre mode,
  - timer ou mode illimite,
  - bouton retour.
- Chaque zone capte son propre swipe.
- Layout responsive pour eviter elements coupes.
- Dialog final:
  - scores P1/P2,
  - vainqueur.

### `app/src/main/kotlin/com/jeu2048/app/ui/screens/ChallengeScreen.kt`
- Defi quotidien:
  - timer,
  - objectif score,
  - score courant.
- Grille responsive + swipe + fleches.
- Message objectif.
- Dialog de fin (reussite/echec) avec options retour/rejouer.

### `app/src/main/kotlin/com/jeu2048/app/ui/screens/TutorialDialog.kt`
- Tutoriel interactif en popup.
- Sequence de steps predefinis:
  - deplacement,
  - fusion,
  - nouvelle tuile,
  - colonne active,
  - fin de partie.
- Mode auto-play (boucle coroutine) ou manuel (precedent/suivant).

---

## Theme et design system

### `app/src/main/kotlin/com/jeu2048/app/ui/theme/AppTheme.kt`
- Definit 3 schemes:
  - Light,
  - Dark,
  - Colorful.
- Selection par `themeIndex`:
  - 0 clair, 1 systeme, 2 sombre, 3 colore.
- Applique `MaterialTheme`.
- Adapte status bar selon scheme.

### `app/src/main/kotlin/com/jeu2048/app/ui/theme/Theme.kt`
- Palette des tuiles (0,2,4,...,2048+).
- Fonctions utilitaires:
  - `tileColor(value, theme)`,
  - `textColorForTile(value, theme)`,
  - `backgroundColor`, `surfaceColor`, `primaryColor`.

---

## Ressources Android (XML)

### `app/src/main/res/xml/file_paths.xml`
- Autorise partage des fichiers depuis `cache/images/`.

### `app/src/main/res/values/strings.xml`
- Textes de base (app name, score, best, etc.).

### `app/src/main/res/values/colors.xml`
- Couleurs globales legacy/compat.

### `app/src/main/res/values/themes.xml`
- Theme Android clair/noActionBar.

### `app/src/main/res/values-night/themes.xml`
- Theme Android nuit.

### `app/src/main/res/mipmap-anydpi-v26/ic_launcher.xml`
### `app/src/main/res/mipmap-anydpi-v26/ic_launcher_round.xml`
- Adaptive icons app.

### `app/src/main/res/drawable/ic_launcher_background.xml`
### `app/src/main/res/drawable/ic_launcher_foreground.xml`
- Assets vectoriels icone launcher.

---

## 6) Mapping exigences PDF -> implementation

### Fonctionnalites principales
- Jeu de base: OK (`GameEngine`, `GameScreen`)
- Sauvegarde/reprise: OK (`GamePreferences`, lifecycle pause)
- Tableau scores + reset: OK (Room + `ScoresScreen` + reset settings)

### Fonctionnalites optionnelles
- Multijoueur: OK
- Personnalisation UI: OK (themes, grille 3..6, animations)
- Sons/musique: OK (avec reserve si fichiers audio manquants)
- Defi quotidien: OK
- Statistiques perso: OK (incluant meilleur score)
- Partage score + image: OK
- Tutoriel interactif: OK

---

## 7) Points sensibles a connaitre pour l'oral

1. **Pourquoi StateFlow partout?**
- Reactive, simple avec Compose, lifecycle-friendly.

2. **Pourquoi DataStore + Room?**
- DataStore pour key-value/preferences.
- Room pour liste structuree des scores et requetes SQL.

3. **Pourquoi seed dans le defi?**
- Reproductibilite du scenario quotidien.

4. **Pourquoi GameEngine separe de l'UI?**
- Testabilite, reusabilite, architecture propre.

5. **Undo limite a 10**
- Controle memoire + gameplay raisonnable.

---

## 8) Plan de reecriture rapide (si suppression de code)

## Si `GameEngine` est supprime
Reecrire dans cet ordre:
1. Etats `StateFlow` (grid/score/best/win/lose).
2. `startNewGame` (2 tuiles).
3. `move` + `mergeLine`.
4. `addRandomTile`, `canMove`.
5. `undo` + snapshots.

## Si `GameViewModel` est supprime
1. Refaire settings combine.
2. Reconnecter moteur classique.
3. Refaire actions UI (move/new/undo/save).
4. Ajouter multi + defi.
5. Ajouter sons/partage/scores.

## Si un ecran Compose est supprime
1. Refaire signature et parametres.
2. Recollect des states.
3. Refaire layout minimal.
4. Rebrancher callbacks.

---

## 9) Checklist pre-demo finale

- Build debug OK.
- Navigation complete entre tous les ecrans.
- Changement taille grille impacte classique + multi + defi.
- Defi: timer + objectif + fin.
- Multi: timer/illimite + winner dialog.
- Scores: insertion + affichage + reset.
- Stats: meilleur score + parties.
- Partage image fonctionne sur appareil.
- Sons actifs/desactives selon parametres.
- Tutoriel interactif ouvrable depuis parametres.

---

## 10) Fichiers de documentation existants (complements)

- `README.md`: vue generale projet et captures.
- `docs/TECHNICAL.md`: doc technique existante.
- `docs/GAME_GUIDE.md`: guide utilisateur.
- `PRIVACY_POLICY.md`: politique de confidentialite.

Ce fichier `REVISION_COMPLETE_APPLICATION.md` est la version "revision examen".

