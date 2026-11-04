package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme =
  darkColorScheme(
    primary = GoldAccent,
    onPrimary = NavyDark,
    primaryContainer = NavyLight,
    onPrimaryContainer = GoldLight,
    secondary = MedicalTeal,
    onSecondary = PureWhite,
    secondaryContainer = DarkSurfaceVariant,
    onSecondaryContainer = MedicalTealLight,
    tertiary = GoldLight,
    onTertiary = NavyDark,
    background = DarkBackground,
    onBackground = DarkTextPrimary,
    surface = DarkSurface,
    onSurface = DarkTextPrimary,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkTextSecondary,
    error = ErrorRed,
    onError = PureWhite,
    errorContainer = Color(0xFF450A0A),
    onErrorContainer = ErrorRedLight,
    outline = Color(0xFF334155),
    outlineVariant = Color(0xFF1E293B)
  )

private val LightColorScheme =
  lightColorScheme(
    primary = NavyPrimary,
    onPrimary = PureWhite,
    primaryContainer = NavyLight,
    onPrimaryContainer = GoldLight,
    secondary = GoldDark,
    onSecondary = PureWhite,
    secondaryContainer = GoldContainer,
    onSecondaryContainer = NavyDark,
    tertiary = MedicalTeal,
    onTertiary = PureWhite,
    tertiaryContainer = MedicalTealLight,
    onTertiaryContainer = Color(0xFF134E4A),
    background = SlateBackground,
    onBackground = TextPrimary,
    surface = PureWhite,
    onSurface = TextPrimary,
    surfaceVariant = Color(0xFFE2E8F0),
    onSurfaceVariant = TextSecondary,
    error = ErrorRed,
    onError = PureWhite,
    errorContainer = ErrorRedLight,
    onErrorContainer = Color(0xFF7F1D1D),
    outline = Color(0xFFCBD5E1),
    outlineVariant = Color(0xFFE2E8F0)
  )

@Composable
fun AvtarAcademyTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  content: @Composable () -> Unit,
) {
  val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

  MaterialTheme(
    colorScheme = colorScheme,
    typography = Typography,
    content = content
  )
}
