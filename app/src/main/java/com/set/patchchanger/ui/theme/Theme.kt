package com.set.patchchanger.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.set.patchchanger.domain.model.AppTheme

// region --- Theme Colors ---

private val BlackThemeDark = darkColorScheme(
    primary = Color(0xFFFFA726),
    secondary = Color(0xFFE57373),
    tertiary = Color(0xFF81C784),
    background = Color(0xFF121212),
    surface = Color(0xFF1E1E1E),
    onPrimary = Color.Black,
    onSecondary = Color.Black,
    onTertiary = Color.Black,
    onBackground = Color.White,
    onSurface = Color.White,
    surfaceVariant = Color(0xFF333333),
    onSurfaceVariant = Color(0xFFCCCCCC),
    outline = Color(0xFF555555)
)

private val WhiteThemeLight = lightColorScheme(
    primary = Color(0xFFE65100),
    secondary = Color(0xFFD32F2F),
    tertiary = Color(0xFF388E3C),
    background = Color(0xFFFAFAFA),
    surface = Color(0xFFFFFFFF),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color.Black,
    onSurface = Color.Black,
    surfaceVariant = Color(0xFFEEEEEE),
    onSurfaceVariant = Color(0xFF333333),
    outline = Color(0xFFBDBDBD)
)

private val BlueThemeDark = darkColorScheme(
    primary = Color(0xFF64B5F6),
    secondary = Color(0xFF81C784),
    tertiary = Color(0xFFF06292),
    background = Color(0xFF0D1B2A),
    surface = Color(0xFF1B263B),
    onPrimary = Color.Black,
    onSecondary = Color.Black,
    onTertiary = Color.Black,
    onBackground = Color(0xFFE0E1DD),
    onSurface = Color(0xFFE0E1DD),
    surfaceVariant = Color(0xFF415A77),
    onSurfaceVariant = Color(0xFFE0E1DD),
    outline = Color(0xFF778DA9)
)

private val OrangeThemeDark = darkColorScheme(
    primary = Color(0xFFFFB74D),
    secondary = Color(0xFFF06292),
    tertiary = Color(0xFF4DB6AC),
    background = Color(0xFF2E1B0A),
    surface = Color(0xFF4A2C0F),
    onPrimary = Color.Black,
    onSecondary = Color.Black,
    onTertiary = Color.Black,
    onBackground = Color(0xFFFFE0B2),
    onSurface = Color(0xFFFFE0B2),
    surfaceVariant = Color(0xFF6B4B2C),
    onSurfaceVariant = Color(0xFFFFE0B2),
    outline = Color(0xFF8C6D4D)
)

private val YellowThemeLight = lightColorScheme(
    primary = Color(0xFF795548),
    secondary = Color(0xFF009688),
    tertiary = Color(0xFFE91E63),
    background = Color(0xFFFFFDE7),
    surface = Color(0xFFFFF9C4),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF4E342E),
    onSurface = Color(0xFF4E342E),
    surfaceVariant = Color(0xFFFFF59D),
    onSurfaceVariant = Color(0xFF4E342E),
    outline = Color(0xFFFBC02D)
)

private val RedThemeDark = darkColorScheme(
    primary = Color(0xFFEF9A9A),
    secondary = Color(0xFFCE93D8),
    tertiary = Color(0xFFA5D6A7),
    background = Color(0xFF2E0000),
    surface = Color(0xFF4E0000),
    onPrimary = Color.Black,
    onSecondary = Color.Black,
    onTertiary = Color.Black,
    onBackground = Color(0xFFFFCDD2),
    onSurface = Color(0xFFFFCDD2),
    surfaceVariant = Color(0xFF6C0000),
    onSurfaceVariant = Color(0xFFFFCDD2),
    outline = Color(0xFF8B0000)
)

private val GreenThemeLight = lightColorScheme(
    primary = Color(0xFF388E3C),
    secondary = Color(0xFFFFA000),
    tertiary = Color(0xFF512DA8),
    background = Color(0xFFF1F8E9),
    surface = Color(0xFFE8F5E9),
    onPrimary = Color.White,
    onSecondary = Color.Black,
    onTertiary = Color.White,
    onBackground = Color(0xFF1B5E20),
    onSurface = Color(0xFF1B5E20),
    surfaceVariant = Color(0xFFC8E6C9),
    onSurfaceVariant = Color(0xFF1B5E20),
    outline = Color(0xFFA5D6A7)
)

private val PurpleThemeDark = darkColorScheme(
    primary = Color(0xFFCE93D8),
    secondary = Color(0xFF80CBC4),
    tertiary = Color(0xFFFFCC80),
    background = Color(0xFF200D24),
    surface = Color(0xFF36183E),
    onPrimary = Color.Black,
    onSecondary = Color.Black,
    onTertiary = Color.Black,
    onBackground = Color(0xFFF3E5F5),
    onSurface = Color(0xFFF3E5F5),
    surfaceVariant = Color(0xFF4A148C),
    onSurfaceVariant = Color(0xFFF3E5F5),
    outline = Color(0xFF7B1FA2)
)

private val TealThemeDark = darkColorScheme(
    primary = Color(0xFF80CBC4),
    secondary = Color(0xFFFFAB91),
    tertiary = Color(0xFFC5E1A5),
    background = Color(0xFF00251E),
    surface = Color(0xFF003D32),
    onPrimary = Color.Black,
    onSecondary = Color.Black,
    onTertiary = Color.Black,
    onBackground = Color(0xFFE0F2F1),
    onSurface = Color(0xFFE0F2F1),
    surfaceVariant = Color(0xFF004D40),
    onSurfaceVariant = Color(0xFFE0F2F1),
    outline = Color(0xFF00695C)
)

// Fallback dynamic colors
private val DarkColorScheme = darkColorScheme(
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80
)

private val LightColorScheme = lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40
)

// endregion

@Composable
fun PatchChangerTheme(
    appTheme: AppTheme = AppTheme.BLACK,
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false, // Disabled dynamic color to prefer app themes
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        else -> when (appTheme) {
            AppTheme.BLACK -> BlackThemeDark
            AppTheme.WHITE -> WhiteThemeLight
            AppTheme.BLUE -> BlueThemeDark
            AppTheme.ORANGE -> OrangeThemeDark
            AppTheme.YELLOW -> YellowThemeLight
            AppTheme.RED -> RedThemeDark
            AppTheme.GREEN -> GreenThemeLight
            AppTheme.PURPLE -> PurpleThemeDark
            AppTheme.TEAL -> TealThemeDark
        }
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            window.navigationBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars =
                appTheme == AppTheme.WHITE || appTheme == AppTheme.YELLOW || appTheme == AppTheme.GREEN
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars =
                appTheme == AppTheme.WHITE || appTheme == AppTheme.YELLOW || appTheme == AppTheme.GREEN
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}