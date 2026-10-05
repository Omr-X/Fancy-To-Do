package net.omar.fancytodo.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import net.omar.fancytodo.model.Area
import net.omar.fancytodo.model.Topic
import net.omar.fancytodo.ui.theme.TextSecondary

@Composable
internal fun LockIcon(color: Color, dimension: Dp = 20.dp) {
    Canvas(Modifier.size(dimension)) {
        val w = size.width
        val h = size.height
        val stroke = Stroke(width = 1.8.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
        drawPath(
            Path().apply {
                moveTo(w * 0.32f, h * 0.46f)
                lineTo(w * 0.32f, h * 0.3f)
                cubicTo(w * 0.32f, h * 0.06f, w * 0.68f, h * 0.06f, w * 0.68f, h * 0.3f)
                lineTo(w * 0.68f, h * 0.46f)
            },
            color,
            style = stroke
        )
        drawRoundRect(color, Offset(w * 0.18f, h * 0.46f), Size(w * 0.64f, h * 0.44f), CornerRadius(2.5.dp.toPx()), style = stroke)
    }
}

/** The task details shown as columns next to a task's title. */
internal enum class MetaKind { Area, Topic, Due, Estimate, Worked }

// Small line icons for the task metadata columns: folder, hash, calendar, hourglass, stopwatch.
@Composable
internal fun MetaIcon(kind: MetaKind, color: Color, dimension: Dp = 14.dp) {
    Canvas(Modifier.size(dimension)) {
        val w = size.width
        val h = size.height
        val stroke = Stroke(width = 1.4.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
        when (kind) {
            MetaKind.Area -> drawPath(
                Path().apply {
                    moveTo(w * 0.08f, h * 0.22f)
                    lineTo(w * 0.4f, h * 0.22f)
                    lineTo(w * 0.5f, h * 0.34f)
                    lineTo(w * 0.92f, h * 0.34f)
                    lineTo(w * 0.92f, h * 0.84f)
                    lineTo(w * 0.08f, h * 0.84f)
                    close()
                },
                color,
                style = stroke
            )
            MetaKind.Topic -> {
                drawLine(color, Offset(w * 0.4f, h * 0.1f), Offset(w * 0.32f, h * 0.9f), stroke.width, StrokeCap.Round)
                drawLine(color, Offset(w * 0.72f, h * 0.1f), Offset(w * 0.64f, h * 0.9f), stroke.width, StrokeCap.Round)
                drawLine(color, Offset(w * 0.12f, h * 0.35f), Offset(w * 0.9f, h * 0.35f), stroke.width, StrokeCap.Round)
                drawLine(color, Offset(w * 0.1f, h * 0.65f), Offset(w * 0.88f, h * 0.65f), stroke.width, StrokeCap.Round)
            }
            MetaKind.Due -> {
                drawRoundRect(color, Offset(w * 0.1f, h * 0.18f), Size(w * 0.8f, h * 0.72f), CornerRadius(2.dp.toPx()), style = stroke)
                drawLine(color, Offset(w * 0.1f, h * 0.42f), Offset(w * 0.9f, h * 0.42f), stroke.width)
                drawLine(color, Offset(w * 0.32f, h * 0.06f), Offset(w * 0.32f, h * 0.26f), stroke.width, StrokeCap.Round)
                drawLine(color, Offset(w * 0.68f, h * 0.06f), Offset(w * 0.68f, h * 0.26f), stroke.width, StrokeCap.Round)
            }
            MetaKind.Estimate -> drawPath(
                Path().apply {
                    moveTo(w * 0.22f, h * 0.1f)
                    lineTo(w * 0.78f, h * 0.1f)
                    lineTo(w * 0.22f, h * 0.9f)
                    lineTo(w * 0.78f, h * 0.9f)
                    close()
                },
                color,
                style = stroke
            )
            MetaKind.Worked -> {
                val center = Offset(w * 0.5f, h * 0.56f)
                drawCircle(color, radius = w * 0.36f, center = center, style = stroke)
                drawLine(color, Offset(w * 0.4f, h * 0.06f), Offset(w * 0.6f, h * 0.06f), stroke.width, StrokeCap.Round)
                drawLine(color, center, Offset(w * 0.5f, h * 0.34f), stroke.width, StrokeCap.Round)
                drawLine(color, center, Offset(w * 0.66f, h * 0.64f), stroke.width, StrokeCap.Round)
            }
        }
    }
}

@Composable
internal fun CheckIcon(color: Color = Color.White, dimension: Dp = 16.dp) {
    Canvas(Modifier.size(dimension)) {
        drawPath(
            Path().apply {
                moveTo(size.width * 0.2f, size.height * 0.53f)
                lineTo(size.width * 0.42f, size.height * 0.75f)
                lineTo(size.width * 0.8f, size.height * 0.28f)
            },
            color,
            style = Stroke(width = 2.4.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
        )
    }
}

@Composable
internal fun PlusIcon(color: Color = Color.White, dimension: Dp = 20.dp) {
    Canvas(Modifier.size(dimension)) {
        drawLine(color, Offset(size.width * 0.2f, size.height / 2), Offset(size.width * 0.8f, size.height / 2), 2.4.dp.toPx(), StrokeCap.Round)
        drawLine(color, Offset(size.width / 2, size.height * 0.2f), Offset(size.width / 2, size.height * 0.8f), 2.4.dp.toPx(), StrokeCap.Round)
    }
}

@Composable
internal fun CloseIcon(color: Color = TextSecondary, dimension: Dp = 14.dp) {
    Canvas(Modifier.size(dimension)) {
        drawLine(color, Offset(size.width * 0.15f, size.height * 0.15f), Offset(size.width * 0.85f, size.height * 0.85f), 2.dp.toPx(), StrokeCap.Round)
        drawLine(color, Offset(size.width * 0.85f, size.height * 0.15f), Offset(size.width * 0.15f, size.height * 0.85f), 2.dp.toPx(), StrokeCap.Round)
    }
}

@Composable
internal fun ArrowIcon(left: Boolean, color: Color = TextSecondary, dimension: Dp = 18.dp) {
    Canvas(Modifier.size(dimension)) {
        val tip = if (left) 0.18f else 0.82f
        val back = 0.5f
        val tail = if (left) 0.84f else 0.16f
        val stroke = Stroke(width = 1.8.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
        drawLine(color, Offset(size.width * tail, size.height / 2), Offset(size.width * tip, size.height / 2), stroke.width, StrokeCap.Round)
        drawPath(
            Path().apply {
                moveTo(size.width * back, size.height * 0.2f)
                lineTo(size.width * tip, size.height * 0.5f)
                lineTo(size.width * back, size.height * 0.8f)
            },
            color,
            style = stroke
        )
    }
}

@Composable
internal fun ChevronIcon(left: Boolean, color: Color = TextSecondary, dimension: Dp = 16.dp) {
    Canvas(Modifier.size(dimension)) {
        val near = if (left) 0.62f else 0.38f
        val far = if (left) 0.38f else 0.62f
        drawPath(
            Path().apply {
                moveTo(size.width * near, size.height * 0.2f)
                lineTo(size.width * far, size.height * 0.5f)
                lineTo(size.width * near, size.height * 0.8f)
            },
            color,
            style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
        )
    }
}
