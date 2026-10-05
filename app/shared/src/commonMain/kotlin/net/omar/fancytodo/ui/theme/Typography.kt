package net.omar.fancytodo.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import fancytodo.app.shared.generated.resources.Res
import fancytodo.app.shared.generated.resources.geist_bold
import fancytodo.app.shared.generated.resources.geist_medium
import fancytodo.app.shared.generated.resources.geist_mono_medium
import fancytodo.app.shared.generated.resources.geist_mono_regular
import fancytodo.app.shared.generated.resources.geist_regular
import fancytodo.app.shared.generated.resources.geist_semibold
import org.jetbrains.compose.resources.Font

// Geist for everything, Geist Mono for clock-like numbers (times, durations).

/** Geist Mono, provided by [AppTheme]. */
internal val LocalMonoFamily = staticCompositionLocalOf<FontFamily> { FontFamily.Monospace }

// Headings use a medium weight with tight negative tracking instead of heavy bold.
internal val HeadingTracking = (-0.035).em
internal val HeadingLineHeight = 1.1.em
internal val TitleTracking = (-0.015).em

@Composable
internal fun geistFamily() = FontFamily(
    Font(Res.font.geist_regular, FontWeight.Normal),
    Font(Res.font.geist_medium, FontWeight.Medium),
    Font(Res.font.geist_semibold, FontWeight.SemiBold),
    Font(Res.font.geist_bold, FontWeight.Bold)
)

@Composable
internal fun geistMonoFamily() = FontFamily(
    Font(Res.font.geist_mono_regular, FontWeight.Normal),
    Font(Res.font.geist_mono_medium, FontWeight.Medium)
)

/** Material's type scale, set in [family]. */
internal fun typographyWith(family: FontFamily) = Typography().run {
    Typography(
        displayLarge = displayLarge.withFamily(family),
        displayMedium = displayMedium.withFamily(family),
        displaySmall = displaySmall.withFamily(family),
        headlineLarge = headlineLarge.withFamily(family),
        headlineMedium = headlineMedium.withFamily(family),
        headlineSmall = headlineSmall.withFamily(family),
        titleLarge = titleLarge.withFamily(family),
        titleMedium = titleMedium.withFamily(family),
        titleSmall = titleSmall.withFamily(family),
        bodyLarge = bodyLarge.withFamily(family),
        bodyMedium = bodyMedium.withFamily(family),
        bodySmall = bodySmall.withFamily(family),
        labelLarge = labelLarge.withFamily(family),
        labelMedium = labelMedium.withFamily(family),
        labelSmall = labelSmall.withFamily(family)
    )
}

// Geist reads best at its native spacing, so Material's positive tracking is dropped. Line height is
// relative so text larger than the style's default size doesn't get its lines squeezed together.
private fun TextStyle.withFamily(family: FontFamily) = copy(fontFamily = family, letterSpacing = 0.sp, lineHeight = 1.4.em)
