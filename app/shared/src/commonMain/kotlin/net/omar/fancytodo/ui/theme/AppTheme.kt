package net.omar.fancytodo.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember

/** Dark Material theme with the app's colors, Geist as the main font and Geist Mono for numbers. */
@Composable
internal fun AppTheme(content: @Composable () -> Unit) {
    val sans = geistFamily()
    val mono = geistMonoFamily()
    val typography = remember(sans) { typographyWith(sans) }
    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = AccentColor,
            background = BackgroundColor,
            surface = PanelColor,
            onBackground = TextPrimary,
            onSurface = TextPrimary
        ),
        typography = typography
    ) {
        CompositionLocalProvider(LocalMonoFamily provides mono, content = content)
    }
}
