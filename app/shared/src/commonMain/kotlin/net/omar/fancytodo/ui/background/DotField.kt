package net.omar.fancytodo.ui.background

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.PointerType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sqrt

private val DotColor = Color(0xFFF3EDE2)
private const val BASE_DOT_ALPHA = 0.14f
private const val FADE_STEPS = 12

/**
 * Shared state for the dot grid. The pointer is tracked over the whole window (root coordinates),
 * so the glow still reaches the grid when the cursor is outside it, e.g. over the sidebar.
 */
internal class DotGrid {
    var pointer by mutableStateOf<Offset?>(null)
    var hostOrigin by mutableStateOf(Offset.Zero)
    var hostSize by mutableStateOf(IntSize.Zero)
}

internal val LocalDotGrid = staticCompositionLocalOf { DotGrid() }

/** Put on the root: watches (never consumes) pointer movement for the whole window. */
internal fun Modifier.trackDotGridPointer(grid: DotGrid): Modifier = pointerInput(grid) {
    awaitPointerEventScope {
        while (true) {
            val event = awaitPointerEvent(PointerEventPass.Initial)
            val change = event.changes.firstOrNull()
            grid.pointer = when {
                change == null || change.type == PointerType.Touch -> null
                event.type == PointerEventType.Exit -> null
                else -> change.position
            }
        }
    }
}

/** The area covered by the grid. Dots fade out towards its edges. */
internal fun Modifier.dotFieldHost(): Modifier = composed {
    val grid = LocalDotGrid.current
    this
        .onGloballyPositioned {
            grid.hostOrigin = it.positionInRoot()
            grid.hostSize = it.size
        }
        .dotLayer(grid) { grid.hostOrigin }
}

/**
 * Frosted-glass background: an opaque surface showing a blurred copy of the grid (and glow)
 * that sits behind it. Use on a layer placed under a panel's content.
 */
internal fun Modifier.frostedDots(): Modifier = composed {
    val grid = LocalDotGrid.current
    var origin by remember { mutableStateOf(Offset.Zero) }
    this
        .onGloballyPositioned { origin = it.positionInRoot() }
        .blur(5.dp)
        .dotLayer(grid) { origin }
}

private fun smoothstep(t: Float): Float {
    val x = t.coerceIn(0f, 1f)
    return x * x * (3f - 2f * x)
}

private fun Modifier.dotLayer(grid: DotGrid, origin: () -> Offset): Modifier = drawWithCache {
    val host = grid.hostOrigin
    val hostSize = grid.hostSize
    val self = origin()
    val spacing = 18.dp.toPx()
    val radius = 1.1.dp.toPx()
    val reach = 170.dp.toPx()
    val grow = 1.4.dp.toPx()
    val fadeBand = 72.dp.toPx()
    val hostW = hostSize.width.toFloat()
    val hostH = hostSize.height.toFloat()
    val startX = (hostW % spacing) / 2
    val startY = (hostH % spacing) / 2
    val columns = ceil((hostW - startX) / spacing).toInt()
    val rows = ceil((hostH - startY) / spacing).toInt()

    // This layer's bounds expressed in host coordinates, and the grid cells that fall inside it.
    val shift = self - host
    val cMin = ceil((shift.x - startX) / spacing).toInt().coerceAtLeast(0)
    val cMax = floor((shift.x + size.width - startX) / spacing).toInt().coerceAtMost(columns - 1)
    val rMin = ceil((shift.y - startY) / spacing).toInt().coerceAtLeast(0)
    val rMax = floor((shift.y + size.height - startY) / spacing).toInt().coerceAtMost(rows - 1)

    // Dots ease out over the last stretch before the grid's edges instead of stopping abruptly.
    fun fade(x: Float, y: Float) = smoothstep(min(min(x, hostW - x), min(y, hostH - y)) / fadeBand)

    // Resting dots grouped by fade level so they draw as a handful of paths.
    val levels = Array(FADE_STEPS + 1) { Path() }
    for (r in rMin..rMax) for (c in cMin..cMax) {
        val x = startX + c * spacing
        val y = startY + r * spacing
        val level = (fade(x, y) * FADE_STEPS).roundToInt()
        if (level > 0) levels[level].addOval(Rect(Offset(x - shift.x, y - shift.y), radius))
    }

    onDrawBehind {
        if (hostSize == IntSize.Zero) return@onDrawBehind
        for (level in 1..FADE_STEPS) {
            drawPath(levels[level], DotColor.copy(alpha = BASE_DOT_ALPHA * level / FADE_STEPS))
        }
        val p = (grid.pointer ?: return@onDrawBehind) - host
        val c0 = maxOf(cMin, floor((p.x - reach - startX) / spacing).toInt())
        val c1 = minOf(cMax, ceil((p.x + reach - startX) / spacing).toInt())
        val r0 = maxOf(rMin, floor((p.y - reach - startY) / spacing).toInt())
        val r1 = minOf(rMax, ceil((p.y + reach - startY) / spacing).toInt())
        for (r in r0..r1) for (c in c0..c1) {
            val x = startX + c * spacing
            val y = startY + r * spacing
            val dx = x - p.x
            val dy = y - p.y
            val falloff = 1f - sqrt(dx * dx + dy * dy) / reach
            if (falloff <= 0f) continue
            val glow = falloff * falloff
            if (glow < 0.02f) continue
            val f = fade(x, y)
            if (f <= 0f) continue
            drawCircle(
                DotColor.copy(alpha = (BASE_DOT_ALPHA + glow * 0.55f) * f),
                radius + glow * grow,
                Offset(x - shift.x, y - shift.y)
            )
        }
    }
}
