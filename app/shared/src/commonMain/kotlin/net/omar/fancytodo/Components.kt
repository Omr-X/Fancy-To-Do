package net.omar.fancytodo

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
internal fun ScreenScaffold(compact: Boolean, content: @Composable ColumnScope.() -> Unit) {
    Box(Modifier.fillMaxSize().verticalScroll(rememberScrollState()), contentAlignment = Alignment.TopCenter) {
        Column(
            Modifier.widthIn(max = 980.dp).fillMaxWidth().padding(
                start = if (compact) 18.dp else 40.dp,
                top = if (compact) 22.dp else 36.dp,
                end = if (compact) 18.dp else 40.dp,
                bottom = 110.dp
            ),
            content = content
        )
    }
}

@Composable
internal fun PageHeader(title: String, subtitle: String, action: @Composable () -> Unit = {}) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text(title, color = TextHi, fontSize = 30.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(4.dp))
            Text(subtitle, color = TextMid, fontSize = 15.sp)
        }
        action()
    }
}

@Composable
internal fun SectionTitle(text: String) {
    Text(text, color = TextHi, fontSize = 28.sp, fontWeight = FontWeight.Bold)
}

@Composable
internal fun SectionLabel(text: String, color: Color = TextLow) {
    Text(text.uppercase(), color = color, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 0.8.sp)
}

@Composable
internal fun Card(modifier: Modifier = Modifier, onClick: (() -> Unit)? = null, content: @Composable ColumnScope.() -> Unit) {
    val shape = RoundedCornerShape(14.dp)
    Column(
        modifier.clip(shape).background(PanelColor).border(1.dp, BorderColor, shape)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier),
        content = content
    )
}

@Composable
internal fun StatCard(label: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Card(modifier) {
        Column(Modifier.padding(16.dp)) {
            Text(label, color = TextMid, fontSize = 13.sp)
            Spacer(Modifier.height(6.dp))
            Text(value, color = color, fontSize = 28.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
internal fun PrimaryButton(text: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Box(
        modifier.clip(RoundedCornerShape(10.dp)).background(AccentColor).clickable(onClick = onClick).padding(horizontal = 18.dp, vertical = 11.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
internal fun GhostButton(text: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    val shape = RoundedCornerShape(10.dp)
    Box(
        modifier.clip(shape).border(1.dp, BorderColor, shape).clickable(onClick = onClick).padding(horizontal = 16.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text, color = TextHi, fontSize = 14.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
internal fun LinkText(text: String, color: Color = TextMid, onClick: () -> Unit) {
    Text(
        text,
        color = color,
        fontSize = 13.sp,
        fontWeight = FontWeight.Medium,
        modifier = Modifier.clip(RoundedCornerShape(6.dp)).clickable(onClick = onClick).padding(horizontal = 6.dp, vertical = 4.dp)
    )
}

@Composable
internal fun Dot(color: Color, dimension: Dp = 8.dp) {
    Box(Modifier.size(dimension).clip(CircleShape).background(color))
}

@Composable
internal fun Chip(text: String, color: Color = AccentColor, selected: Boolean = false, dot: Boolean = false, onClick: (() -> Unit)? = null) {
    val shape = RoundedCornerShape(50)
    Row(
        Modifier.clip(shape)
            .background(if (selected) color.copy(alpha = 0.2f) else Color.Transparent)
            .border(1.dp, if (selected) color else BorderColor, shape)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(horizontal = 13.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (dot) {
            Dot(color, 8.dp)
            Spacer(Modifier.width(7.dp))
        }
        Text(text, color = if (selected) TextHi else TextMid, fontSize = 13.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
internal fun Segmented(options: List<String>, selected: String, onSelect: (String) -> Unit) {
    val shape = RoundedCornerShape(10.dp)
    Row(Modifier.clip(shape).background(PanelColor).border(1.dp, BorderColor, shape).padding(3.dp)) {
        options.forEach { option ->
            Box(
                Modifier.clip(RoundedCornerShape(8.dp))
                    .background(if (option == selected) PanelHiColor else Color.Transparent)
                    .clickable { onSelect(option) }
                    .padding(horizontal = 16.dp, vertical = 7.dp)
            ) {
                Text(option, color = if (option == selected) TextHi else TextMid, fontSize = 13.sp, fontWeight = FontWeight.Medium)
            }
        }
    }
}

@Composable
internal fun ProgressBar(fraction: Float, color: Color = AccentColor) {
    Box(Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)).background(BorderColor)) {
        Box(Modifier.fillMaxWidth(fraction.coerceIn(0f, 1f)).fillMaxHeight().clip(RoundedCornerShape(3.dp)).background(color))
    }
}

@Composable
internal fun EmptyState(title: String, subtitle: String, content: @Composable () -> Unit = {}) {
    Column(Modifier.fillMaxWidth().padding(vertical = 36.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text(title, color = TextHi, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(6.dp))
        Text(subtitle, color = TextMid, fontSize = 14.sp, textAlign = TextAlign.Center)
        Spacer(Modifier.height(18.dp))
        content()
    }
}

@Composable
internal fun InputField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    singleLine: Boolean = true,
    minLines: Int = 1,
    onDone: (() -> Unit)? = null
) {
    val shape = RoundedCornerShape(10.dp)
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        singleLine = singleLine,
        minLines = minLines,
        textStyle = TextStyle(color = TextHi, fontSize = 15.sp),
        cursorBrush = SolidColor(AccentColor),
        keyboardOptions = if (onDone != null) KeyboardOptions(imeAction = ImeAction.Done) else KeyboardOptions.Default,
        keyboardActions = KeyboardActions(onDone = { onDone?.invoke() }),
        decorationBox = { inner ->
            Box(Modifier.fillMaxWidth().clip(shape).background(PanelHiColor).border(1.dp, BorderColor, shape).padding(horizontal = 14.dp, vertical = 11.dp)) {
                if (value.isEmpty()) Text(placeholder, color = TextLow, fontSize = 15.sp)
                inner()
            }
        }
    )
}

@Composable
internal fun CheckCircle(checked: Boolean, color: Color = AccentColor, dimension: Dp = 24.dp, onToggle: () -> Unit) {
    Canvas(Modifier.size(dimension).clip(CircleShape).clickable(onClick = onToggle)) {
        if (checked) {
            drawCircle(color)
            drawPath(
                Path().apply {
                    moveTo(size.width * 0.28f, size.height * 0.53f)
                    lineTo(size.width * 0.44f, size.height * 0.69f)
                    lineTo(size.width * 0.74f, size.height * 0.35f)
                },
                Color.White,
                style = Stroke(width = 2.2.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
            )
        } else {
            drawCircle(TextLow, radius = size.minDimension / 2 - 1.dp.toPx(), style = Stroke(width = 1.8.dp.toPx()))
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
internal fun CloseIcon(color: Color = TextMid, dimension: Dp = 14.dp) {
    Canvas(Modifier.size(dimension)) {
        drawLine(color, Offset(size.width * 0.15f, size.height * 0.15f), Offset(size.width * 0.85f, size.height * 0.85f), 2.dp.toPx(), StrokeCap.Round)
        drawLine(color, Offset(size.width * 0.85f, size.height * 0.15f), Offset(size.width * 0.15f, size.height * 0.85f), 2.dp.toPx(), StrokeCap.Round)
    }
}

@Composable
internal fun ChevronIcon(left: Boolean, color: Color = TextMid, dimension: Dp = 16.dp) {
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
