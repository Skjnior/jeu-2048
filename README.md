# 2048 — Jeu mobile Android

Projet Android (Kotlin) du jeu 2048 avec toutes les fonctionnalités demandées.

## Ouvrir le projet

1. Ouvrir **Android Studio** (version récente avec support SDK 34).
2. **File → Open** et sélectionner le dossier `Jeu_mobile`.
3. Laisser Android Studio synchroniser Gradle (téléchargement des dépendances si besoin).
4. Si le wrapper Gradle n’est pas présent : **File → Settings → Build → Gradle** et vérifier que le projet utilise le wrapper, ou exécuter en ligne de commande : `gradle wrapper` (si Gradle est installé).

## Lancer l’application

- Brancher un appareil Android (API 26+) ou démarrer un émulateur.
- Cliquer sur **Run** (▶) ou `Shift+F10`.

## Fonctionnalités implémentées

### Obligatoires
- **Jeu de base** : grille 4×4 (par défaut), gestes tactiles (glisser), fusion des tuiles, score, victoire (2048) et défaite.
- **Sauvegarde / reprise** : état de la grille et du score enregistrés automatiquement ; reprise après fermeture de l’app.
- **Tableau des scores** : meilleurs scores en base locale (Room), classement, option de réinitialisation.

### Optionnelles (menu Paramètres)
- **Thèmes** : Clair, Sombre, Coloré.
- **Taille de grille** : 3×3, 4×4, 5×5, 6×6.
- **Animations** : activation/désactivation des transitions sur les tuiles.
- **Sons** : sons pour les mouvements et les fusions (activation/désactivation).
- **Musique de fond** : option prévue (activation/désactivation dans les paramètres).
- **Statistiques** : parties jouées, gagnées, perdues (affichées dans Paramètres).
- **Partage** : partage du score (réseaux sociaux / message) depuis la boîte de dialogue Game Over.
- **Tutoriel** : règles du jeu dans Paramètres → « Règles du jeu (tutoriel) ».
- **Annuler** : bouton Annuler (undo) pendant la partie.

## Structure du projet

- `app/src/main/kotlin/com/jeu2048/app/`
  - `game/` : moteur de jeu (grille, déplacements, fusion, score).
  - `data/` : préférences (DataStore), base Room (scores), sauvegarde de partie.
  - `ui/` : ViewModel, thèmes, écrans Compose (jeu, paramètres, scores, tutoriel).
  - `sound/` : gestion des sons (mouvement / fusion).

## Rendu

- **Rapport** : 6 à 10 pages (page de garde, sommaire, introduction, conclusion, numérotation, explication du code, captures d’écran, tests).
- **Code source** : ce dépôt.
- **Date limite** : 12/04/2026 à 23h59.
