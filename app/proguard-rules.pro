# Add project specific ProGuard rules here.

# ── Room (base de données) ─────────────────────────────────────────
-keep class com.jeu2048.app.data.** { *; }
-keepclassmembers class * extends androidx.room.RoomDatabase { *; }

# ── Kotlin Coroutines ──────────────────────────────────────────────
-keepnames class kotlinx.coroutines.** { *; }

# ── Jetpack Compose ────────────────────────────────────────────────
-keep class androidx.compose.** { *; }
-keepclassmembers class androidx.compose.** { *; }

# ── DataStore ──────────────────────────────────────────────────────
-keep class androidx.datastore.** { *; }

# ── ViewModel ─────────────────────────────────────────────────────
-keep class * extends androidx.lifecycle.ViewModel { *; }

# ── Évite les crashs sur les Enum ─────────────────────────────────
-keepclassmembers enum * { *; }

# ── Supprime les logs en release ──────────────────────────────────
-assumenosideeffects class android.util.Log {
    public static int v(...);
    public static int d(...);
    public static int i(...);
}
