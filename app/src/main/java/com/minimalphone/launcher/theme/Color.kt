package com.minimalphone.launcher.theme

import androidx.compose.ui.graphics.Color

// Strict Grayscale Palette (Pure Black, Pure White, and neutral grays only)
val PureBlack = Color(0xFF000000)
val DarkGray900 = Color(0xFF0D0D0D)
val DarkGray800 = Color(0xFF161616)
val DarkGray700 = Color(0xFF222222)
val DarkGray600 = Color(0xFF2C2C2C)
val MidGray500 = Color(0xFF4A4A4A)
val MidGray400 = Color(0xFF757575)
val LightGray300 = Color(0xFFA0A0A0)
val LightGray200 = Color(0xFFD5D5D5)
val PureWhite = Color(0xFFFFFFFF)

// Universal Theme Design Tokens
val Black = PureBlack
val DarkSurface = DarkGray900
val DarkCard = DarkGray800
val AccentBorder = DarkGray600
val MidGray = MidGray400
val LightGray = LightGray300
val ChalkWhite = PureWhite
val OffWhite = Color(0xFFE8E8E8)

// Backward Compatibility Aliases for Grayscale Look
val PaperDarkBackground = PureBlack
val PaperDarkSurface = DarkGray900
val PaperDarkCard = DarkGray800
val PaperHairlineBorder = DarkGray600
val PaperChalkWhite = PureWhite
val PaperPencilGray = LightGray300
val PaperMutedInk = MidGray400
val PaperFaintDivider = DarkGray700
val PaperAccentDot = PureWhite
