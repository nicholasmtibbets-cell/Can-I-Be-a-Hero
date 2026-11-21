package com.example.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

private val GateboundDarkColorScheme = darkColorScheme(
    primary = BurnishedGold,
    onPrimary = ObsidianVoid,
    primaryContainer = ElevatedObsidian,
    onPrimaryContainer = BurnishedGold,
    secondary = ArcaneSapphire,
    onSecondary = ObsidianVoid,
    secondaryContainer = CardObsidian,
    onSecondaryContainer = ParchmentIvory,
    tertiary = TorchAmber,
    onTertiary = ObsidianVoid,
    background = ObsidianVoid,
    onBackground = ParchmentIvory,
    surface = DungeonSlate,
    onSurface = ParchmentIvory,
    surfaceVariant = CardObsidian,
    onSurfaceVariant = AshMuted,
    outline = StoneBorder,
    error = EmberCrimson,
    onError = ParchmentIvory
)

val GateboundShapes = Shapes(
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(14.dp),
    large = RoundedCornerShape(20.dp),
    extraLarge = RoundedCornerShape(26.dp)
)

@Composable
fun GateboundTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = GateboundDarkColorScheme,
        typography = GateboundTypography,
        shapes = GateboundShapes,
        content = content
    )
}
