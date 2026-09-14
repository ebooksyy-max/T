package com.example.mybillbook.ui.theme

import android.app.Activity
import android.graphics.Color as AndroidColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat

val LightColorScheme = lightColorScheme(
    primary = BillBookColors.Primary,
    onPrimary = BillBookColors.OnPrimary,
    primaryContainer = BillBookColors.PrimaryLight,
    onPrimaryContainer = BillBookColors.PrimaryDark,
    secondary = BillBookColors.Secondary,
    onSecondary = BillBookColors.OnSecondary,
    secondaryContainer = BillBookColors.SecondaryLight,
    onSecondaryContainer = BillBookColors.SecondaryDark,
    tertiary = BillBookColors.Tertiary,
    onTertiary = BillBookColors.OnTertiary,
    tertiaryContainer = BillBookColors.TertiaryLight,
    onTertiaryContainer = BillBookColors.TertiaryDark,
    background = BillBookColors.Background,
    onBackground = BillBookColors.OnBackground,
    surface = BillBookColors.Surface,
    onSurface = BillBookColors.OnSurface,
    surfaceVariant = BillBookColors.SurfaceVariant,
    onSurfaceVariant = BillBookColors.OnSurfaceVariant,
    outline = BillBookColors.Outline,
    outlineVariant = BillBookColors.OutlineVariant,
    error = BillBookColors.Error,
    onError = BillBookColors.OnError,
    errorContainer = BillBookColors.ErrorLight,
    onErrorContainer = BillBookColors.ErrorDark
)

@Composable
fun MyBillBookTheme(
    darkTheme: Boolean = false, // Strictly Light Theme
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                WindowCompat.setDecorFitsSystemWindows(window, false)
                val insetsController = WindowCompat.getInsetsController(window, window.decorView)
                insetsController.show(WindowInsetsCompat.Type.statusBars())
                insetsController.isAppearanceLightStatusBars = true
                insetsController.isAppearanceLightNavigationBars = true
                window.statusBarColor = AndroidColor.parseColor("#F7F9FC")
                window.navigationBarColor = AndroidColor.parseColor("#FFFFFF")
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
