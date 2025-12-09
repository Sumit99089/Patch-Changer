package com.set.patchchanger.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.set.patchchanger.domain.model.AppTheme

// Theme Definitions matching HTML CSS vars exactly
private val BlackTheme = darkColorScheme(
    background = Color(0xFF1c1c1c), surface = Color(0xFF2d2d2d),
    primary = Color(0xFFf36500), secondary = Color(0xFFcc5600),
    tertiary = Color(0xFFe8e100), outline = Color(0xFF4a4a4a)
)
private val WhiteTheme = lightColorScheme(
    background = Color(0xFFf0f0f0), surface = Color(0xFFffffff),
    primary = Color(0xFF3a3a3a), secondary = Color(0xFFd0d0d0), // using btn colors for primary
    outline = Color(0xFFcccccc)
)
private val BlueTheme = darkColorScheme(
    background = Color(0xFF0a192f), surface = Color(0xFF0f2240),
    primary = Color(0xFF64ffda), secondary = Color(0xFF00c7a7),
    tertiary = Color(0xFFffdd00), outline = Color(0xFF2a4c7a)
)
private val OrangeTheme = darkColorScheme(
    background = Color(0xFF2b1f13), surface = Color(0xFF3d2c1c),
    primary = Color(0xFFffa726), secondary = Color(0xFFfb8c00),
    tertiary = Color(0xFFffee58), outline = Color(0xFF6a4f31)
)
private val YellowTheme = lightColorScheme(
    background = Color(0xFFfffde7), surface = Color(0xFFffffff),
    primary = Color(0xFFff6f00), secondary = Color(0xFFe65100),
    tertiary = Color(0xFFf57f17), outline = Color(0xFFfdd835)
)
private val RedTheme = darkColorScheme(
    background = Color(0xFF2c0000), surface = Color(0xFF4a0101),
    primary = Color(0xFFff8a80), secondary = Color(0xFFff5252),
    tertiary = Color(0xFFffff8d), outline = Color(0xFF8b2525)
)
private val GreenTheme = lightColorScheme(
    background = Color(0xFFe8f5e9), surface = Color(0xFFffffff),
    primary = Color(0xFFff6d00), secondary = Color(0xFFe65100),
    tertiary = Color(0xFFfdd835), outline = Color(0xFFa5d6a7)
)
private val PurpleTheme = darkColorScheme(
    background = Color(0xFF1a1032), surface = Color(0xFF2c1e4e),
    primary = Color(0xFFff79c6), secondary = Color(0xFFe66ab2),
    tertiary = Color(0xFFf1fa8c), outline = Color(0xFF5a438f)
)
private val TealTheme = darkColorScheme(
    background = Color(0xFF002525), surface = Color(0xFF003636),
    primary = Color(0xFFff8a65), secondary = Color(0xFFff7043),
    tertiary = Color(0xFFfff176), outline = Color(0xFF006b6b)
)

@Composable
fun PatchChangerTheme(
    appTheme: AppTheme = AppTheme.BLACK,
    content: @Composable () -> Unit
) {
    val colorScheme = when (appTheme) {
        AppTheme.BLACK -> BlackTheme
        AppTheme.WHITE -> WhiteTheme
        AppTheme.BLUE -> BlueTheme
        AppTheme.ORANGE -> OrangeTheme
        AppTheme.YELLOW -> YellowTheme
        AppTheme.RED -> RedTheme
        AppTheme.GREEN -> GreenTheme
        AppTheme.PURPLE -> PurpleTheme
        AppTheme.TEAL -> TealTheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            window.navigationBarColor = colorScheme.background.toArgb()

            val isLight =
                appTheme == AppTheme.WHITE || appTheme == AppTheme.YELLOW || appTheme == AppTheme.GREEN
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = isLight
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = isLight
        }
    }

    MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}