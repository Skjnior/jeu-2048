# 2048 Android - Version 2 (revision soutenance/examen)

Ce document complete `docs/REVISION_COMPLETE_APPLICATION.md` (qui reste inchangé).
Ici l'objectif est: **repondre vite en oral** et **reecrire rapidement** une partie de code supprimee.

---

## 1) Pitch technique en 30 secondes

"L'application est un 2048 Android en Kotlin/Compose, architecture MVVM.  
Le `GameEngine` contient toute la logique metier (mouvement, fusion, score, victoire/defaite, undo).  
Le `GameViewModel` orchestre les modes (classique, multijoueur, defi), la persistence (DataStore + Room), le son et le partage.  
L'UI Compose observe des `StateFlow` et se met a jour automatiquement."

---

## 2) Schema mental ultra-court

1. UI envoie action (`Direction`, click, toggle)  
2. ViewModel appelle use case/metier  
3. GameEngine calcule nouvel etat  
4. Persistance (DataStore/Room) si necessaire  
5. UI recompose

---

## 3) Pseudo-code des parties critiques

## 3.1 `GameEngine.move(direction)`

```kotlin
if (gameOver) return false

snapshot = copie(grille, score)
historique.add(snapshot)

changed = false
newScore = score

for each line/column selon direction:
    mergedLine, delta, moved = mergeLine(...)
    ecrire mergedLine dans la grille
    newScore += delta
    changed = changed || moved

if (!changed):
    historique.removeLast()
    return false

score = newScore
best = max(best, score)
addRandomTile(2 ou 4)

if (tuile 2048 presente) hasWon = true
if (aucun mouvement possible) gameOver = true

return true
```

## 3.2 `GameEngine.mergeLine(line)`

```kotlin
result = [0,0,...]
write = 0
scoreDelta = 0

for value in line:
    if value == 0: continue

    if write > 0 && result[write-1] == value && pas_deja_fusionne(write-1):
        result[write-1] *= 2
        markFusion(write-1)
        scoreDelta += result[write-1]
    else:
        result[write] = value
        write++

return (result, scoreDelta, moved)
```

## 3.3 `GameViewModel.startDailyChallenge()`

```kotlin
cancelJobChallenge()
reset flags (finished/success)
gridSize = settings.gridSize
seed = LocalDate.now().toEpochDay()
target = challengeTargetFor(seed, gridSize)
duration = challengeDurationForGrid(gridSize)

engine = GameEngine(gridSize)
engine.startSeededGame(seed)
timeLeft = duration

launch coroutine:
    while timeLeft > 0 && !finished:
        delay(1000)
        timeLeft--
        if engine.score >= target -> success=true, finished=true
        if engine.gameOver -> success=false, finished=true
    if timeout -> success = (score >= target), finished=true
```

---

## 4) Questions "pieges prof" + reponses courtes

## Q1. Pourquoi `GameEngine` n'est pas dans l'UI ?
Parce que la logique metier doit etre testable, reutilisable et independante du rendu Compose.

## Q2. Pourquoi `StateFlow` et pas variables simples ?
Pour un flux reactif, observe par Compose, compatible lifecycle et coroutines.

## Q3. Pourquoi DataStore + Room ?
DataStore = preferences/etat simple. Room = collection structuree (scores tries, requetes SQL).

## Q4. Comment eviter qu'une tuile fusionne 2 fois dans le meme coup ?
On garde un tableau `merged[]` par ligne/colonne pour marquer les positions deja fusionnees.

## Q5. Comment garantir que le defi quotidien est identique pour tous ?
Avec `seed = LocalDate.now().toEpochDay()` puis `Random(seed)`.

## Q6. Pourquoi `undo` limite a 10 ?
Compromis memoire/perf/gameplay.

## Q7. Ou sont gerees les stats utilisateur ?
Dans `SettingsPreferences` (DataStore): played/won/lost, plus `bestScore` calcule depuis Room.

## Q8. Si le prof supprime `GameViewModel`, que refaire en premier ?
1) etats exposes, 2) actions classiques, 3) sauvegarde/DB, 4) multi/defi, 5) wiring UI.

---

## 5) Plan de reecriture express par fichier cle

## `GameEngine.kt` (priorite max)
- Etat: grille/score/best/win/lose
- `startNewGame()`
- `move()` + `mergeLine()`
- `addRandomTile()`, `canMove()`
- `undo()`, `getStateForSave()`

## `GameViewModel.kt`
- Instancier prefs + dao + sound
- Exposer `currentEngine`, `settings`, `topScores`
- Actions: `move/start/undo/acceptWin`
- Sauvegarde pause
- Ajouter modes multi + defi

## `MainActivity.kt`
- `setContent { ... }`
- `NavHost` routes
- Callbacks entre ecrans et ViewModel
- Dialogs victoire/defaite

## `GameScreen.kt`
- Collecter score/grille/best
- Swipe + boutons direction
- Header actions
- callback win/gameover/share

---

## 6) "Je sais reecrire..." (mini templates)

## Template fonction move (signature)

```kotlin
fun move(direction: Direction): Boolean
```

## Template settings combine

```kotlin
val settings = combine(
    gridSizeFlow, themeFlow, animationsFlow, soundFlow, musicFlow,
    gamesPlayedFlow, gamesWonFlow, gamesLostFlow, bestScoreFlow
) { values ->
    SettingsUi(...)
}.stateIn(...)
```

## Template route NavHost

```kotlin
composable("challenge") {
    ChallengeScreen(
        engine = challengeEngine,
        timeLeft = timeLeft,
        targetScore = target,
        finished = finished,
        success = success,
        ...
    )
}
```

---

## 7) Checklist "pret soutenance"

- Je peux expliquer MVVM en 20 sec.
- Je peux expliquer `move()` sans lire le code.
- Je sais dire pourquoi DataStore et Room coexistent.
- Je sais justifier seed du defi.
- Je peux localiser rapidement:
  - logique metier,
  - persistence,
  - navigation,
  - UI de chaque mode.

---

## 8) Fichiers de reference a citer a l'oral

- Entree/navigation: `MainActivity.kt`
- Metier 2048: `game/GameEngine.kt`
- Orchestration: `ui/GameViewModel.kt`
- Ecran principal: `ui/screens/GameScreen.kt`
- Multi: `ui/screens/MultiplayerScreen.kt`
- Defi: `ui/screens/ChallengeScreen.kt`
- Parametres/stats: `ui/screens/SettingsScreen.kt`
- Scores DB: `data/ScoreDao.kt`, `data/ScoreDatabase.kt`
- Sauvegarde: `data/GamePreferences.kt`, `data/SettingsPreferences.kt`
- Themes: `ui/theme/AppTheme.kt`, `ui/theme/Theme.kt`

---

## 9) Ce que tu dois retenir par coeur (top 10)

1. MVVM + StateFlow + Compose  
2. `GameEngine` = coeur du jeu  
3. `move()` appelle `mergeLine()`  
4. New tile apres coup valide uniquement  
5. Win = tuile 2048, lose = aucun mouvement  
6. Undo via snapshots (10)  
7. DataStore pour prefs + save game  
8. Room pour classement  
9. Defi seed journalier + timer + target  
10. Multi = 2 engines independants + comparaison finale

