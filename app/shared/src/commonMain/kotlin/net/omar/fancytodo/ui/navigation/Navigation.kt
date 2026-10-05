package net.omar.fancytodo.ui.navigation

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import net.omar.fancytodo.ui.components.APP_NAME
import net.omar.fancytodo.ui.components.AppLogo
import net.omar.fancytodo.ui.theme.BorderColor
import net.omar.fancytodo.ui.theme.HeadingTracking
import net.omar.fancytodo.ui.theme.RaisedPanelColor
import net.omar.fancytodo.ui.theme.SidebarColor
import net.omar.fancytodo.ui.theme.TextPrimary
import net.omar.fancytodo.ui.theme.TextSecondary

internal enum class Page(val label: String) {
    Home("Home"),
    Calendar("Calendar"),
    Dashboard("Dashboard"),
    Tasks("Tasks"),
    Settings("Settings")
}

private val MainPages = Page.entries.filter { it != Page.Settings }

/** Side navigation; [notice] (e.g. the log-in prompt) sits just above the Settings item. */
@Composable
internal fun Sidebar(page: Page, onSelect: (Page) -> Unit, notice: (@Composable () -> Unit)? = null) {
    Column(Modifier.width(232.dp).fillMaxHeight().background(SidebarColor).padding(horizontal = 16.dp, vertical = 24.dp)) {
        Row(Modifier.padding(horizontal = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            AppLogo(30.dp)
            Spacer(Modifier.width(10.dp))
            Text(APP_NAME, color = TextPrimary, fontSize = 20.sp, fontWeight = FontWeight.Medium, letterSpacing = HeadingTracking)
        }
        Spacer(Modifier.height(32.dp))
        MainPages.forEach { entry ->
            SidebarItem(entry, entry == page, onSelect)
            Spacer(Modifier.height(2.dp))
        }
        Spacer(Modifier.weight(1f))
        notice?.let {
            it()
            Spacer(Modifier.height(16.dp))
        }
        Box(Modifier.fillMaxWidth().height(1.dp).background(BorderColor))
        Spacer(Modifier.height(12.dp))
        SidebarItem(Page.Settings, page == Page.Settings, onSelect)
    }
}

@Composable
private fun SidebarItem(entry: Page, selected: Boolean, onSelect: (Page) -> Unit) {
    val color = if (selected) TextPrimary else TextSecondary
    val hover = remember { MutableInteractionSource() }
    val hovered by hover.collectIsHoveredAsState()
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp))
            .background(if (hovered) RaisedPanelColor else Color.Transparent)
            .hoverable(hover)
            .clickable { onSelect(entry) }
            .padding(horizontal = 12.dp, vertical = 11.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        PageIcon(entry, color)
        Spacer(Modifier.width(12.dp))
        Text(entry.label, color = color, fontSize = 15.sp, fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal)
    }
}

@Composable
internal fun BottomNav(page: Page, onSelect: (Page) -> Unit) {
    Row(Modifier.fillMaxWidth().background(SidebarColor).padding(vertical = 6.dp)) {
        Page.entries.forEach { entry ->
            val selected = entry == page
            val color = if (selected) TextPrimary else TextSecondary
            Column(
                Modifier.weight(1f).clip(RoundedCornerShape(10.dp)).clickable { onSelect(entry) }.padding(vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                PageIcon(entry, color)
                Spacer(Modifier.height(4.dp))
                Text(entry.label, color = color, fontSize = 12.sp, fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal)
            }
        }
    }
}

@Composable
internal fun PageIcon(page: Page, color: Color, dimension: Dp = 20.dp) {
    Canvas(Modifier.size(dimension)) {
        val w = size.width
        val h = size.height
        val stroke = Stroke(width = 1.7.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
        val corner = CornerRadius(2.5.dp.toPx())
        when (page) {
            Page.Home -> {
                drawPath(
                    Path().apply {
                        moveTo(w * 0.12f, h * 0.47f)
                        lineTo(w * 0.5f, h * 0.14f)
                        lineTo(w * 0.88f, h * 0.47f)
                        moveTo(w * 0.24f, h * 0.38f)
                        lineTo(w * 0.24f, h * 0.86f)
                        lineTo(w * 0.76f, h * 0.86f)
                        lineTo(w * 0.76f, h * 0.38f)
                        moveTo(w * 0.42f, h * 0.86f)
                        lineTo(w * 0.42f, h * 0.62f)
                        lineTo(w * 0.58f, h * 0.62f)
                        lineTo(w * 0.58f, h * 0.86f)
                    },
                    color,
                    style = stroke
                )
            }
            Page.Calendar -> {
                drawRoundRect(color, Offset(w * 0.14f, h * 0.2f), Size(w * 0.72f, h * 0.68f), corner, style = stroke)
                drawLine(color, Offset(w * 0.14f, h * 0.42f), Offset(w * 0.86f, h * 0.42f), stroke.width)
                drawLine(color, Offset(w * 0.34f, h * 0.1f), Offset(w * 0.34f, h * 0.28f), stroke.width, StrokeCap.Round)
                drawLine(color, Offset(w * 0.66f, h * 0.1f), Offset(w * 0.66f, h * 0.28f), stroke.width, StrokeCap.Round)
            }
            Page.Dashboard -> {
                val cell = Size(w * 0.3f, h * 0.3f)
                listOf(0.14f to 0.14f, 0.56f to 0.14f, 0.14f to 0.56f, 0.56f to 0.56f).forEach { (x, y) ->
                    drawRoundRect(color, Offset(w * x, h * y), cell, corner, style = stroke)
                }
            }
            Page.Tasks -> {
                listOf(0.3f, 0.7f).forEach { y ->
                    drawPath(
                        Path().apply {
                            moveTo(w * 0.1f, h * y)
                            lineTo(w * 0.2f, h * (y + 0.09f))
                            lineTo(w * 0.36f, h * (y - 0.1f))
                        },
                        color,
                        style = stroke
                    )
                    drawLine(color, Offset(w * 0.5f, h * y), Offset(w * 0.9f, h * y), stroke.width, StrokeCap.Round)
                }
            }
            Page.Settings -> {
                val teeth = 8
                val steps = teeth * 4
                val outer = size.minDimension * 0.45f
                val inner = size.minDimension * 0.33f
                val c = Offset(w / 2, h / 2)
                drawPath(
                    Path().apply {
                        for (i in 0 until steps) {
                            val angle = 2 * PI * i / steps - PI / 2
                            val r = if (i % 4 == 1 || i % 4 == 2) outer else inner
                            val x = c.x + (r * cos(angle)).toFloat()
                            val y = c.y + (r * sin(angle)).toFloat()
                            if (i == 0) moveTo(x, y) else lineTo(x, y)
                        }
                        close()
                    },
                    color,
                    style = stroke
                )
                drawCircle(color, radius = size.minDimension * 0.13f, center = c, style = stroke)
            }
        }
    }
}
