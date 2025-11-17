package com.atelierversace.ui.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = ElectricSapphire,
    onPrimary = Color.White,
    primaryContainer = BabyBlueIce,
    onPrimaryContainer = DeepSapphire,

    secondary = Cornflower,
    onSecondary = Color.White,
    secondaryContainer = Periwinkle,
    onSecondaryContainer = ElectricSapphire,

    tertiary = BabyBlueIce,
    onTertiary = DeepSapphire,
    tertiaryContainer = LightCyan,
    onTertiaryContainer = Cornflower,

    background = IceBlue,
    onBackground = TextPrimary,

    surface = Color.White,
    onSurface = TextPrimary,
    surfaceVariant = SoftPeriwinkle,
    onSurfaceVariant = TextSecondary,

    error = Error,
    onError = Color.White,
    errorContainer = Color(0xFFFFEDED),
    onErrorContainer = Error,

    outline = GlassBorder,
    outlineVariant = Color(0xFFE5E7EB)
)

@Composable
fun AtelierVersaceTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = Typography(),
        content = content
    )
}