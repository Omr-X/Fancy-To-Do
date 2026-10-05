package net.omar.fancytodo.ui.theme

import androidx.compose.ui.graphics.Color
import net.omar.fancytodo.model.PALETTE_SIZE

// Surfaces
internal val BackgroundColor = Color(0xFF090A0D)
internal val SidebarColor = Color(0xFF090A0D)
internal val PanelColor = Color(0xFF15171C)
internal val RaisedPanelColor = Color(0xFF1D2027)
internal val BorderColor = Color(0xFF3D4350)
/** The outline separating the main panel from the sidebar. */
internal val StrongBorderColor = Color(0xFF595459)
/** Background of error toasts. */
internal val ErrorSurfaceColor = Color(0xFF2A1A1E)
/** Dims the page behind dialogs and overlays. */
internal val ScrimColor = Color(0x99000000)

// Text
internal val TextPrimary = Color(0xFFF3F4F6)
internal val TextSecondary = Color(0xFFA1A7B3)
internal val TextMuted = Color(0xFF6B7280)

// Accents
internal val AccentColor = Color(0xFF7C5CFF)
internal val SuccessColor = Color(0xFF34D399)
internal val WarningColor = Color(0xFFF5A524)
internal val DangerColor = Color(0xFFF87171)
/** The red "now" line on calendars. */
internal val NowLineColor = Color(0xFFE0554A)

/** Colors areas and topics can pick from, referenced by index. */
internal val ColorPalette = listOf(
    Color(0xFF7C5CFF), Color(0xFF38BDF8), Color(0xFF34D399), Color(0xFFF472B6),
    Color(0xFFFBBF24), Color(0xFFFB7185), Color(0xFF60A5FA), Color(0xFFA3E635)
).also { check(it.size == PALETTE_SIZE) }

/** Palette color for an index; out-of-range indexes wrap around. */
internal fun paletteColor(index: Int) = ColorPalette[index.mod(ColorPalette.size)]
