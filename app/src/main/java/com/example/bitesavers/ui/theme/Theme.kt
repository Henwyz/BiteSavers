package com.example.bitesavers.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val BiteSaverLightColorScheme = lightColorScheme(
    primary = BiteSaverColors.PrimaryGreen,
    onPrimary = BiteSaverColors.TextOnDark,
    primaryContainer = BiteSaverColors.SoftGreen,
    onPrimaryContainer = BiteSaverColors.TextPrimary,

    secondary = BiteSaverColors.HeaderGreen,
    onSecondary = BiteSaverColors.TextOnDark,
    secondaryContainer = BiteSaverColors.ChipGreen,
    onSecondaryContainer = BiteSaverColors.TextPrimary,

    tertiary = BiteSaverColors.DiscountOrange,
    onTertiary = BiteSaverColors.TextOnDark,
    tertiaryContainer = Color(0xFFFFE7CC),
    onTertiaryContainer = BiteSaverColors.TextPrimary,

    background = BiteSaverColors.OffWhite,
    onBackground = BiteSaverColors.TextPrimary,

    surface = BiteSaverColors.White,
    onSurface = BiteSaverColors.TextPrimary,
    surfaceVariant = BiteSaverColors.SoftGreen,
    onSurfaceVariant = BiteSaverColors.TextSecondary,

    outline = BiteSaverColors.Border,
    error = Color(0xFFB3261E)
)

private val BiteSaverDarkColorScheme = darkColorScheme(
    primary = Color(0xFF7FD18D),
    onPrimary = Color(0xFF0F2C17),
    primaryContainer = Color(0xFF1D4D28),
    onPrimaryContainer = Color(0xFFCFF3D5),

    secondary = Color(0xFF9DD7A8),
    onSecondary = Color(0xFF11301A),
    secondaryContainer = Color(0xFF2A5C35),
    onSecondaryContainer = Color(0xFFD4F5DA),

    tertiary = Color(0xFFFFB15A),
    onTertiary = Color(0xFF3D2300),
    tertiaryContainer = Color(0xFF6A3E00),
    onTertiaryContainer = Color(0xFFFFE2BE),

    background = Color(0xFF0F1511),
    onBackground = Color(0xFFE3ECE5),

    surface = Color(0xFF17211A),
    onSurface = Color(0xFFE3ECE5),
    surfaceVariant = Color(0xFF223228),
    onSurfaceVariant = Color(0xFFB8C9BC),

    outline = Color(0xFF87988A),
    error = Color(0xFFF2B8B5)
)

@Composable
fun BiteSaversTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // keep false for strict brand consistency
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> BiteSaverDarkColorScheme
        else -> BiteSaverLightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}