package net.omar.fancytodo

import androidx.compose.ui.graphics.Color

internal val BgColor = Color(0xFF0D0E12)
internal val SidebarColor = Color(0xFF090A0D)
internal val PanelColor = Color(0xFF15171C)
internal val PanelHiColor = Color(0xFF1D2027)
internal val BorderColor = Color(0xFF272B34)
internal val TextHi = Color(0xFFF3F4F6)
internal val TextMid = Color(0xFFA1A7B3)
internal val TextLow = Color(0xFF6B7280)
internal val AccentColor = Color(0xFF7C5CFF)
internal val SuccessColor = Color(0xFF34D399)
internal val WarningColor = Color(0xFFF5A524)
internal val DangerColor = Color(0xFFF87171)

internal val AreaPalette = listOf(
    Color(0xFF7C5CFF), Color(0xFF38BDF8), Color(0xFF34D399), Color(0xFFF472B6),
    Color(0xFFFBBF24), Color(0xFFFB7185), Color(0xFF60A5FA), Color(0xFFA3E635)
)

internal fun areaColor(index: Int) = AreaPalette[((index % AreaPalette.size) + AreaPalette.size) % AreaPalette.size]
