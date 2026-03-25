package com.jeu2048.app.ui.theme

import androidx.compose.ui.graphics.Color

// Clair (défaut)
val TileEmptyLight = Color(0xFFCDC1B4)
val Tile2Light = Color(0xFFEEE4DA)
val Tile4Light = Color(0xFFEDE0C8)
val Tile8Light = Color(0xFFF2B179)
val Tile16Light = Color(0xFFF59563)
val Tile32Light = Color(0xFFF67C5F)
val Tile64Light = Color(0xFFF65E3B)
val Tile128Light = Color(0xFFEDCF72)
val Tile256Light = Color(0xFFEDCC61)
val Tile512Light = Color(0xFFEDC850)
val Tile1024Light = Color(0xFFEDC53F)
val Tile2048Light = Color(0xFFEDC22E)
val TileSuperLight = Color(0xFF3C3A32)
val TextDark = Color(0xFF776E65)
val TextLight = Color(0xFFF9F6F2)
val BackgroundLight = Color(0xFFF8F5F0)
val SurfaceLight = Color(0xFFBBADA0)
val PrimaryLight = Color(0xFF6750A4)
val PrimaryDarkLight = Color(0xFF4A3D7A)

// Sombre
val TileEmptyDark = Color(0xFF2C2C44)
val Tile2Dark = Color(0xFF5A4FCF)
val Tile4Dark = Color(0xFF7B6FDF)
val Tile8Dark = Color(0xFF9B5BAE)
val Tile16Dark = Color(0xFFBB6BAE)
val Tile32Dark = Color(0xFFDB7BBE)
val Tile64Dark = Color(0xFFE84393)
val Tile128Dark = Color(0xFFE8A87C)
val Tile256Dark = Color(0xFFE8C87C)
val Tile512Dark = Color(0xFFE8D87C)
val Tile1024Dark = Color(0xFFE8E87C)
val Tile2048Dark = Color(0xFFFFEB3B)
val TileSuperDark = Color(0xFFFFFFFF)
val BackgroundDark = Color(0xFF1A1A2E)
val SurfaceDark = Color(0xFF25254A)
val PrimaryDark = Color(0xFFE94560)
val PrimaryDarkDark = Color(0xFF0F3460)

// Coloré
val TileEmptyColorful = Color(0xFF2D2D44)
val Tile2Colorful = Color(0xFF6C5CE7)
val Tile4Colorful = Color(0xFFA29BFE)
val Tile8Colorful = Color(0xFFFD79A8)
val Tile16Colorful = Color(0xFFE17055)
val Tile32Colorful = Color(0xFF00CEC9)
val Tile64Colorful = Color(0xFF00B894)
val Tile128Colorful = Color(0xFFFDCB6E)
val Tile256Colorful = Color(0xFFE17055)
val Tile512Colorful = Color(0xFFE84393)
val Tile1024Colorful = Color(0xFF6C5CE7)
val Tile2048Colorful = Color(0xFF00B894)
val TileSuperColorful = Color(0xFF2D3436)
val BackgroundColorful = Color(0xFF0F0F1A)
val SurfaceColorful = Color(0xFF2D2D44)
val PrimaryColorful = Color(0xFF6C5CE7)

fun tileColor(value: Int, theme: Int): Color = when (theme) {
    1 -> when (value) {
        0 -> TileEmptyDark
        2 -> Tile2Dark
        4 -> Tile4Dark
        8 -> Tile8Dark
        16 -> Tile16Dark
        32 -> Tile32Dark
        64 -> Tile64Dark
        128 -> Tile128Dark
        256 -> Tile256Dark
        512 -> Tile512Dark
        1024 -> Tile1024Dark
        2048 -> Tile2048Dark
        else -> TileSuperDark
    }
    2 -> when (value) {
        0 -> TileEmptyColorful
        2 -> Tile2Colorful
        4 -> Tile4Colorful
        8 -> Tile8Colorful
        16 -> Tile16Colorful
        32 -> Tile32Colorful
        64 -> Tile64Colorful
        128 -> Tile128Colorful
        256 -> Tile256Colorful
        512 -> Tile512Colorful
        1024 -> Tile1024Colorful
        2048 -> Tile2048Colorful
        else -> TileSuperColorful
    }
    else -> when (value) {
        0 -> TileEmptyLight
        2 -> Tile2Light
        4 -> Tile4Light
        8 -> Tile8Light
        16 -> Tile16Light
        32 -> Tile32Light
        64 -> Tile64Light
        128 -> Tile128Light
        256 -> Tile256Light
        512 -> Tile512Light
        1024 -> Tile1024Light
        2048 -> Tile2048Light
        else -> TileSuperLight
    }
}

fun textColorForTile(value: Int, theme: Int): Color = when (theme) {
    1 -> Color.White // Thème Sombre : texte toujours blanc ou très clair
    2 -> Color.White // Thème Coloré : texte toujours blanc
    else -> if (value <= 4) TextDark else Color.White // Thème Clair : texte chocolat pour 2 et 4, blanc pour les autres
}

fun backgroundColor(theme: Int): Color = when (theme) {
    1 -> BackgroundDark
    2 -> BackgroundColorful
    else -> BackgroundLight
}

fun surfaceColor(theme: Int): Color = when (theme) {
    1 -> SurfaceDark
    2 -> SurfaceColorful
    else -> SurfaceLight
}

fun primaryColor(theme: Int): Color = when (theme) {
    1 -> PrimaryDark
    2 -> PrimaryColorful
    else -> PrimaryLight
}
