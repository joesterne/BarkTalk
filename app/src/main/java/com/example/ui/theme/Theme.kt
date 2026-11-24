package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

/**
 * Available app color themes selectable by the user.
 */
enum class BarkColorPalette(
    val id: String,
    val displayName: String,
    val emoji: String,
    val tagline: String,
    val previewPrimary: Color,
    val previewSecondary: Color,
    val previewContainer: Color
) {
    GOLDEN_AMBER(
        id = "golden_amber",
        displayName = "Golden Amber",
        emoji = "🦮",
        tagline = "Warm terracotta, honey & teal",
        previewPrimary = AmberPrimaryLight,
        previewSecondary = TealSecondaryLight,
        previewContainer = AmberPrimaryContainerLight
    ),
    FOREST_PARK(
        id = "forest_park",
        displayName = "Dog Park Pine",
        emoji = "🌲",
        tagline = "Fresh emerald trail & sunlight",
        previewPrimary = ForestPrimaryLight,
        previewSecondary = ForestSecondaryLight,
        previewContainer = ForestPrimaryContainerLight
    ),
    OCEAN_RETRIEVER(
        id = "ocean_retriever",
        displayName = "Ocean Fetch",
        emoji = "🌊",
        tagline = "Coastal cobalt, surf & coral",
        previewPrimary = OceanPrimaryLight,
        previewSecondary = OceanSecondaryLight,
        previewContainer = OceanPrimaryContainerLight
    ),
    BERRY_POODLE(
        id = "berry_poodle",
        displayName = "Royal Berry",
        emoji = "🍇",
        tagline = "Plum velvet, iris & rose gold",
        previewPrimary = BerryPrimaryLight,
        previewSecondary = BerrySecondaryLight,
        previewContainer = BerryPrimaryContainerLight
    ),
    MIDNIGHT_HUSKY(
        id = "midnight_husky",
        displayName = "Husky Slate",
        emoji = "🐺",
        tagline = "Arctic indigo & glacier frost",
        previewPrimary = HuskyPrimaryLight,
        previewSecondary = HuskySecondaryLight,
        previewContainer = HuskyPrimaryContainerLight
    ),
    DYNAMIC_WALLPAPER(
        id = "dynamic_wallpaper",
        displayName = "Material You",
        emoji = "✨",
        tagline = "Matches your Android wallpaper",
        previewPrimary = Color(0xFF4C662B),
        previewSecondary = Color(0xFF586249),
        previewContainer = Color(0xFFCDEDA3)
    );

    companion object {
        fun fromId(id: String?): BarkColorPalette {
            return entries.find { it.id.equals(id, ignoreCase = true) } ?: GOLDEN_AMBER
        }
    }
}

/**
 * Light/Dark appearance mode preference.
 */
enum class ThemeDarkMode(
    val id: String,
    val label: String,
    val emoji: String
) {
    SYSTEM("system", "Auto", "🌓"),
    LIGHT("light", "Light", "☀️"),
    DARK("dark", "Dark", "🌙");

    companion object {
        fun fromId(id: String?): ThemeDarkMode {
            return entries.find { it.id.equals(id, ignoreCase = true) } ?: SYSTEM
        }
    }
}

private val AmberDarkColorScheme = darkColorScheme(
    primary = AmberPrimaryDark,
    onPrimary = AmberOnPrimaryDark,
    primaryContainer = AmberPrimaryContainerDark,
    onPrimaryContainer = AmberOnPrimaryContainerDark,
    secondary = TealSecondaryDark,
    onSecondary = TealOnSecondaryDark,
    secondaryContainer = TealSecondaryContainerDark,
    onSecondaryContainer = TealOnSecondaryContainerDark,
    tertiary = CoralTertiaryDark,
    onTertiary = CoralOnTertiaryDark,
    tertiaryContainer = CoralTertiaryContainerDark,
    onTertiaryContainer = CoralOnTertiaryContainerDark,
    background = BackgroundDark,
    onBackground = OnBackgroundDark,
    surface = SurfaceDark,
    onSurface = OnSurfaceDark,
    surfaceVariant = SurfaceVariantDark,
    onSurfaceVariant = OnSurfaceVariantDark
)

private val AmberLightColorScheme = lightColorScheme(
    primary = AmberPrimaryLight,
    onPrimary = AmberOnPrimaryLight,
    primaryContainer = AmberPrimaryContainerLight,
    onPrimaryContainer = AmberOnPrimaryContainerLight,
    secondary = TealSecondaryLight,
    onSecondary = TealOnSecondaryLight,
    secondaryContainer = TealSecondaryContainerLight,
    onSecondaryContainer = TealOnSecondaryContainerLight,
    tertiary = CoralTertiaryLight,
    onTertiary = CoralOnTertiaryLight,
    tertiaryContainer = CoralTertiaryContainerLight,
    onTertiaryContainer = CoralOnTertiaryContainerLight,
    background = BackgroundLight,
    onBackground = OnBackgroundLight,
    surface = SurfaceLight,
    onSurface = OnSurfaceLight,
    surfaceVariant = SurfaceVariantLight,
    onSurfaceVariant = OnSurfaceVariantLight
)

private val ForestDarkColorScheme = darkColorScheme(
    primary = ForestPrimaryDark,
    onPrimary = ForestOnPrimaryDark,
    primaryContainer = ForestPrimaryContainerDark,
    onPrimaryContainer = ForestOnPrimaryContainerDark,
    secondary = ForestSecondaryDark,
    onSecondary = ForestOnSecondaryDark,
    secondaryContainer = ForestSecondaryContainerDark,
    onSecondaryContainer = ForestOnSecondaryContainerDark,
    tertiary = ForestTertiaryDark,
    onTertiary = ForestOnTertiaryDark,
    tertiaryContainer = ForestTertiaryContainerDark,
    onTertiaryContainer = ForestOnTertiaryContainerDark,
    background = ForestBackgroundDark,
    onBackground = ForestOnBackgroundDark,
    surface = ForestSurfaceDark,
    onSurface = ForestOnSurfaceDark,
    surfaceVariant = ForestSurfaceVariantDark,
    onSurfaceVariant = ForestOnSurfaceVariantDark
)

private val ForestLightColorScheme = lightColorScheme(
    primary = ForestPrimaryLight,
    onPrimary = ForestOnPrimaryLight,
    primaryContainer = ForestPrimaryContainerLight,
    onPrimaryContainer = ForestOnPrimaryContainerLight,
    secondary = ForestSecondaryLight,
    onSecondary = ForestOnSecondaryLight,
    secondaryContainer = ForestSecondaryContainerLight,
    onSecondaryContainer = ForestOnSecondaryContainerLight,
    tertiary = ForestTertiaryLight,
    onTertiary = ForestOnTertiaryLight,
    tertiaryContainer = ForestTertiaryContainerLight,
    onTertiaryContainer = ForestOnTertiaryContainerLight,
    background = ForestBackgroundLight,
    onBackground = ForestOnBackgroundLight,
    surface = ForestSurfaceLight,
    onSurface = ForestOnSurfaceLight,
    surfaceVariant = ForestSurfaceVariantLight,
    onSurfaceVariant = ForestOnSurfaceVariantLight
)

private val OceanDarkColorScheme = darkColorScheme(
    primary = OceanPrimaryDark,
    onPrimary = OceanOnPrimaryDark,
    primaryContainer = OceanPrimaryContainerDark,
    onPrimaryContainer = OceanOnPrimaryContainerDark,
    secondary = OceanSecondaryDark,
    onSecondary = OceanOnSecondaryDark,
    secondaryContainer = OceanSecondaryContainerDark,
    onSecondaryContainer = OceanOnSecondaryContainerDark,
    tertiary = OceanTertiaryDark,
    onTertiary = OceanOnTertiaryDark,
    tertiaryContainer = OceanTertiaryContainerDark,
    onTertiaryContainer = OceanOnTertiaryContainerDark,
    background = OceanBackgroundDark,
    onBackground = OceanOnBackgroundDark,
    surface = OceanSurfaceDark,
    onSurface = OceanOnSurfaceDark,
    surfaceVariant = OceanSurfaceVariantDark,
    onSurfaceVariant = OceanOnSurfaceVariantDark
)

private val OceanLightColorScheme = lightColorScheme(
    primary = OceanPrimaryLight,
    onPrimary = OceanOnPrimaryLight,
    primaryContainer = OceanPrimaryContainerLight,
    onPrimaryContainer = OceanOnPrimaryContainerLight,
    secondary = OceanSecondaryLight,
    onSecondary = OceanOnSecondaryLight,
    secondaryContainer = OceanSecondaryContainerLight,
    onSecondaryContainer = OceanOnSecondaryContainerLight,
    tertiary = OceanTertiaryLight,
    onTertiary = OceanOnTertiaryLight,
    tertiaryContainer = OceanTertiaryContainerLight,
    onTertiaryContainer = OceanOnTertiaryContainerLight,
    background = OceanBackgroundLight,
    onBackground = OceanOnBackgroundLight,
    surface = OceanSurfaceLight,
    onSurface = OceanOnSurfaceLight,
    surfaceVariant = OceanSurfaceVariantLight,
    onSurfaceVariant = OceanOnSurfaceVariantLight
)

private val BerryDarkColorScheme = darkColorScheme(
    primary = BerryPrimaryDark,
    onPrimary = BerryOnPrimaryDark,
    primaryContainer = BerryPrimaryContainerDark,
    onPrimaryContainer = BerryOnPrimaryContainerDark,
    secondary = BerrySecondaryDark,
    onSecondary = BerryOnSecondaryDark,
    secondaryContainer = BerrySecondaryContainerDark,
    onSecondaryContainer = BerryOnSecondaryContainerDark,
    tertiary = BerryTertiaryDark,
    onTertiary = BerryOnTertiaryDark,
    tertiaryContainer = BerryTertiaryContainerDark,
    onTertiaryContainer = BerryOnTertiaryContainerDark,
    background = BerryBackgroundDark,
    onBackground = BerryOnBackgroundDark,
    surface = BerrySurfaceDark,
    onSurface = BerryOnSurfaceDark,
    surfaceVariant = BerrySurfaceVariantDark,
    onSurfaceVariant = BerryOnSurfaceVariantDark
)

private val BerryLightColorScheme = lightColorScheme(
    primary = BerryPrimaryLight,
    onPrimary = BerryOnPrimaryLight,
    primaryContainer = BerryPrimaryContainerLight,
    onPrimaryContainer = BerryOnPrimaryContainerLight,
    secondary = BerrySecondaryLight,
    onSecondary = BerryOnSecondaryLight,
    secondaryContainer = BerrySecondaryContainerLight,
    onSecondaryContainer = BerryOnSecondaryContainerLight,
    tertiary = BerryTertiaryLight,
    onTertiary = BerryOnTertiaryLight,
    tertiaryContainer = BerryTertiaryContainerLight,
    onTertiaryContainer = BerryOnTertiaryContainerLight,
    background = BerryBackgroundLight,
    onBackground = BerryOnBackgroundLight,
    surface = BerrySurfaceLight,
    onSurface = BerryOnSurfaceLight,
    surfaceVariant = BerrySurfaceVariantLight,
    onSurfaceVariant = BerryOnSurfaceVariantLight
)

private val HuskyDarkColorScheme = darkColorScheme(
    primary = HuskyPrimaryDark,
    onPrimary = HuskyOnPrimaryDark,
    primaryContainer = HuskyPrimaryContainerDark,
    onPrimaryContainer = HuskyOnPrimaryContainerDark,
    secondary = HuskySecondaryDark,
    onSecondary = HuskyOnSecondaryDark,
    secondaryContainer = HuskySecondaryContainerDark,
    onSecondaryContainer = HuskyOnSecondaryContainerDark,
    tertiary = HuskyTertiaryDark,
    onTertiary = HuskyOnTertiaryDark,
    tertiaryContainer = HuskyTertiaryContainerDark,
    onTertiaryContainer = HuskyOnTertiaryContainerDark,
    background = HuskyBackgroundDark,
    onBackground = HuskyOnBackgroundDark,
    surface = HuskySurfaceDark,
    onSurface = HuskyOnSurfaceDark,
    surfaceVariant = HuskySurfaceVariantDark,
    onSurfaceVariant = HuskyOnSurfaceVariantDark
)

private val HuskyLightColorScheme = lightColorScheme(
    primary = HuskyPrimaryLight,
    onPrimary = HuskyOnPrimaryLight,
    primaryContainer = HuskyPrimaryContainerLight,
    onPrimaryContainer = HuskyOnPrimaryContainerLight,
    secondary = HuskySecondaryLight,
    onSecondary = HuskyOnSecondaryLight,
    secondaryContainer = HuskySecondaryContainerLight,
    onSecondaryContainer = HuskyOnSecondaryContainerLight,
    tertiary = HuskyTertiaryLight,
    onTertiary = HuskyOnTertiaryLight,
    tertiaryContainer = HuskyTertiaryContainerLight,
    onTertiaryContainer = HuskyOnTertiaryContainerLight,
    background = HuskyBackgroundLight,
    onBackground = HuskyOnBackgroundLight,
    surface = HuskySurfaceLight,
    onSurface = HuskyOnSurfaceLight,
    surfaceVariant = HuskySurfaceVariantLight,
    onSurfaceVariant = HuskyOnSurfaceVariantLight
)

/**
 * Resolves the Material 3 [ColorScheme] for a given [BarkColorPalette] and dark mode flag.
 */
fun resolveColorScheme(
    palette: BarkColorPalette,
    darkTheme: Boolean
): ColorScheme {
    return when (palette) {
        BarkColorPalette.GOLDEN_AMBER -> if (darkTheme) AmberDarkColorScheme else AmberLightColorScheme
        BarkColorPalette.FOREST_PARK -> if (darkTheme) ForestDarkColorScheme else ForestLightColorScheme
        BarkColorPalette.OCEAN_RETRIEVER -> if (darkTheme) OceanDarkColorScheme else OceanLightColorScheme
        BarkColorPalette.BERRY_POODLE -> if (darkTheme) BerryDarkColorScheme else BerryLightColorScheme
        BarkColorPalette.MIDNIGHT_HUSKY -> if (darkTheme) HuskyDarkColorScheme else HuskyLightColorScheme
        BarkColorPalette.DYNAMIC_WALLPAPER -> if (darkTheme) AmberDarkColorScheme else AmberLightColorScheme
    }
}

@Composable
fun BarkTalkTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    colorPalette: BarkColorPalette = BarkColorPalette.GOLDEN_AMBER,
    darkMode: ThemeDarkMode? = null,
    content: @Composable () -> Unit,
) {
    val resolvedDarkTheme = when (darkMode) {
        ThemeDarkMode.LIGHT -> false
        ThemeDarkMode.DARK -> true
        ThemeDarkMode.SYSTEM -> isSystemInDarkTheme()
        null -> darkTheme
    }

    val useDynamic = (dynamicColor || colorPalette == BarkColorPalette.DYNAMIC_WALLPAPER) &&
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.S

    val colorScheme = if (useDynamic) {
        val context = LocalContext.current
        if (resolvedDarkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
    } else {
        resolveColorScheme(colorPalette, resolvedDarkTheme)
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) = BarkTalkTheme(darkTheme = darkTheme, dynamicColor = dynamicColor, content = content)
