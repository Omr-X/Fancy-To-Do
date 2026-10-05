package net.omar.fancytodo.ui.shell

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import net.omar.fancytodo.state.AppState
import net.omar.fancytodo.ui.components.ArrowIcon
import net.omar.fancytodo.ui.components.LinkText
import net.omar.fancytodo.ui.components.PlusIcon
import net.omar.fancytodo.ui.theme.AccentColor
import net.omar.fancytodo.ui.theme.BackgroundColor
import net.omar.fancytodo.ui.theme.BorderColor
import net.omar.fancytodo.ui.theme.DangerColor
import net.omar.fancytodo.ui.theme.ErrorSurfaceColor
import net.omar.fancytodo.ui.theme.ScrimColor
import net.omar.fancytodo.ui.theme.TextPrimary
import net.omar.fancytodo.ui.theme.TextSecondary

// Only the top-left corner is rounded; the outline bleeds past the right and bottom
// edges so the border runs off-screen there instead of tracing the window edge.
internal object MainPanelShape : Shape {
    override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density): Outline {
        val radius = with(density) { 18.dp.toPx() }
        val bleed = with(density) { 4.dp.toPx() }
        return Outline.Rounded(
            RoundRect(0f, 0f, size.width + bleed, size.height + bleed, topLeftCornerRadius = CornerRadius(radius))
        )
    }
}

internal val DayCalendarWidth = 340.dp

// Small screens: the calendar slides over the content instead of squeezing it.
@Composable
internal fun BoxScope.DayCalendarOverlay(width: Dp, onClose: () -> Unit, calendar: @Composable (Modifier) -> Unit) {
    val noRipple = remember { MutableInteractionSource() }
    Box(Modifier.fillMaxSize().background(ScrimColor).clickable(noRipple, null, onClick = onClose))
    Box(
        Modifier.align(Alignment.CenterEnd).width(width).fillMaxHeight()
            .background(BackgroundColor).clickable(noRipple, null) {}
    ) {
        Row(Modifier.fillMaxSize()) {
            Box(Modifier.width(1.dp).fillMaxHeight().background(BorderColor))
            calendar(Modifier.weight(1f).fillMaxHeight())
        }
    }
}

/** Arrow that hides (points right) or shows (points left) the day calendar. */
@Composable
internal fun DayCalendarToggle(open: Boolean, onToggle: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier.clip(RoundedCornerShape(8.dp)).clickable(onClick = onToggle).padding(6.dp),
        contentAlignment = Alignment.Center
    ) {
        ArrowIcon(left = !open, color = TextPrimary, dimension = 20.dp)
    }
}

@Composable
internal fun BoxScope.AddTaskButton(onClick: () -> Unit) {
    val hover = remember { MutableInteractionSource() }
    val hovered by hover.collectIsHoveredAsState()
    val spin by animateFloatAsState(if (hovered) 90f else 0f, tween(durationMillis = 260))
    Box(
        Modifier.align(Alignment.BottomEnd).padding(24.dp)
            .shadow(12.dp, CircleShape)
            .size(58.dp).clip(CircleShape).background(AccentColor)
            // No indication: the button keeps its color on hover/press; the spin is the feedback.
            .hoverable(hover).clickable(remember { MutableInteractionSource() }, indication = null, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Box(Modifier.graphicsLayer { rotationZ = spin }) { PlusIcon(Color.White, 24.dp) }
    }
}

@Composable
internal fun BoxScope.ErrorToast(state: AppState) {
    state.error?.let { message ->
        val shape = RoundedCornerShape(12.dp)
        Row(
            Modifier.align(Alignment.BottomCenter).padding(24.dp)
                .clip(shape).background(ErrorSurfaceColor).border(1.dp, DangerColor.copy(alpha = 0.5f), shape)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(message, color = TextPrimary, fontSize = 14.sp, modifier = Modifier.weight(1f, fill = false))
            Spacer(Modifier.width(16.dp))
            LinkText("Retry", DangerColor) {
                state.dismissError()
                state.refresh()
            }
            LinkText("Dismiss", TextSecondary) { state.dismissError() }
        }
    }
}
