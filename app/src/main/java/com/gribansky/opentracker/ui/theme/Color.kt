package com.gribansky.opentracker.ui.theme

import androidx.compose.material.darkColors
import androidx.compose.ui.graphics.Color
val Blue400 = Color(0xFF42A5F5)
val Green500 = Color(0xFF1EB980)
val DarkBlue900 = Color(0xFF26282F)

// Tracker is always dark themed.
val ColorPalette = darkColors(
    primary = Green500,
    surface = DarkBlue900,
    onSurface = Color.White,
    background = DarkBlue900,
    onBackground = Color.White
)
