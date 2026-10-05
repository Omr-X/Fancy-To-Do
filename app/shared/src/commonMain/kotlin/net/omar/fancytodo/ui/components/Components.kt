package net.omar.fancytodo.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import net.omar.fancytodo.ui.theme.AccentColor
import net.omar.fancytodo.ui.theme.BorderColor
import net.omar.fancytodo.ui.theme.ColorPalette
import net.omar.fancytodo.ui.theme.DangerColor
import net.omar.fancytodo.ui.theme.ErrorSurfaceColor
import net.omar.fancytodo.ui.theme.HeadingLineHeight
import net.omar.fancytodo.ui.theme.HeadingTracking
import net.omar.fancytodo.ui.theme.PanelColor
import net.omar.fancytodo.ui.theme.RaisedPanelColor
import net.omar.fancytodo.ui.theme.TextMuted
import net.omar.fancytodo.ui.theme.TextPrimary
import net.omar.fancytodo.ui.theme.TextSecondary
import net.omar.fancytodo.ui.theme.paletteColor

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
            Text(title, color = TextPrimary, fontSize = 32.sp, fontWeight = FontWeight.Medium, letterSpacing = HeadingTracking, lineHeight = HeadingLineHeight)
            Spacer(Modifier.height(4.dp))
            Text(subtitle, color = TextSecondary, fontSize = 15.sp)
        }
        action()
    }
}

internal const val APP_NAME = "TaskMan"

/** The app's mark: a purple rounded square with a check. */
@Composable
internal fun AppLogo(size: Dp) {
    Box(Modifier.size(size).clip(RoundedCornerShape(size * 0.3f)).background(AccentColor), contentAlignment = Alignment.Center) {
        CheckIcon(Color.White, size * 0.53f)
    }
}

/** Red message box for form errors. */
@Composable
internal fun ErrorMessage(text: String, modifier: Modifier = Modifier) {
    val shape = RoundedCornerShape(10.dp)
    Row(
        modifier.fillMaxWidth().clip(shape).background(ErrorSurfaceColor).border(1.dp, DangerColor.copy(alpha = 0.5f), shape)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Dot(DangerColor, 7.dp)
        Spacer(Modifier.width(10.dp))
        Text(text, color = TextPrimary, fontSize = 13.sp)
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
        Text(text, color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
internal fun LinkText(text: String, color: Color = TextSecondary, onClick: () -> Unit) {
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
internal fun Segmented(options: List<String>, selected: String, onSelect: (String) -> Unit) {
    val shape = RoundedCornerShape(10.dp)
    Row(Modifier.clip(shape).background(PanelColor).border(1.dp, BorderColor, shape).padding(3.dp)) {
        options.forEach { option ->
            Box(
                Modifier.clip(RoundedCornerShape(8.dp))
                    .background(if (option == selected) RaisedPanelColor else Color.Transparent)
                    .clickable { onSelect(option) }
                    .padding(horizontal = 16.dp, vertical = 7.dp)
            ) {
                Text(option, color = if (option == selected) TextPrimary else TextSecondary, fontSize = 13.sp, fontWeight = FontWeight.Medium)
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
        Text(title, color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(6.dp))
        Text(subtitle, color = TextSecondary, fontSize = 14.sp, textAlign = TextAlign.Center)
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
    keyboardType: KeyboardType = KeyboardType.Text,
    /** Shows dots instead of the text (passwords). */
    masked: Boolean = false,
    onDone: (() -> Unit)? = null
) {
    val shape = RoundedCornerShape(10.dp)
    BasicTextField(
        value = value,
        // Names are one line: also catches line breaks that arrive by pasting.
        onValueChange = { onValueChange(if (singleLine) it.filter { c -> c != '\n' && c != '\r' } else it) },
        modifier = modifier,
        singleLine = singleLine,
        minLines = minLines,
        textStyle = LocalTextStyle.current.merge(TextStyle(color = TextPrimary, fontSize = 15.sp)),
        cursorBrush = SolidColor(AccentColor),
        visualTransformation = if (masked) PasswordVisualTransformation() else VisualTransformation.None,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType, imeAction = if (onDone != null) ImeAction.Done else ImeAction.Default),
        keyboardActions = KeyboardActions(onDone = { onDone?.invoke() }),
        decorationBox = { inner ->
            Box(Modifier.fillMaxWidth().clip(shape).background(RaisedPanelColor).border(1.dp, BorderColor, shape).padding(horizontal = 14.dp, vertical = 11.dp)) {
                if (value.isEmpty()) Text(placeholder, color = TextMuted, fontSize = 15.sp)
                inner()
            }
        }
    )
}

/** Small square showing the area color; clicking it opens the palette to pick from. */
@Composable
internal fun ColorSquare(selected: Int, onChange: (Int) -> Unit) {
    var open by remember { mutableStateOf(false) }
    val shape = RoundedCornerShape(7.dp)
    Box {
        Box(
            Modifier.size(26.dp).clip(shape).background(paletteColor(selected)).border(1.dp, BorderColor, shape)
                .clickable { open = true }
        )
        DropdownMenu(expanded = open, onDismissRequest = { open = false }) {
            Column(Modifier.padding(horizontal = 10.dp, vertical = 4.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                ColorPalette.indices.chunked(4).forEach { row ->
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        row.forEach { index ->
                            val swatch = RoundedCornerShape(7.dp)
                            Box(
                                Modifier.size(28.dp).clip(swatch).background(paletteColor(index))
                                    .border(2.dp, if (index == selected) TextPrimary else Color.Transparent, swatch)
                                    .clickable { onChange(index); open = false }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
internal fun CheckSquare(checked: Boolean, color: Color = AccentColor, dimension: Dp = 20.dp, outline: Color = TextMuted, onToggle: () -> Unit) {
    Canvas(Modifier.size(dimension).clip(RoundedCornerShape(5.dp)).clickable(onClick = onToggle)) {
        val corner = CornerRadius(4.dp.toPx())
        if (checked) {
            drawRoundRect(color, cornerRadius = corner)
            drawPath(
                Path().apply {
                    moveTo(size.width * 0.26f, size.height * 0.52f)
                    lineTo(size.width * 0.43f, size.height * 0.69f)
                    lineTo(size.width * 0.75f, size.height * 0.33f)
                },
                Color.White,
                style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
            )
        } else {
            val inset = 0.75.dp.toPx()
            drawRoundRect(
                outline,
                topLeft = Offset(inset, inset),
                size = Size(size.width - inset * 2, size.height - inset * 2),
                cornerRadius = corner,
                style = Stroke(width = 1.5.dp.toPx())
            )
        }
    }
}

@Composable
internal fun SmallButton(text: String, primary: Boolean, onClick: () -> Unit) {
    val shape = RoundedCornerShape(8.dp)
    Box(
        Modifier.clip(shape)
            .then(if (primary) Modifier.background(AccentColor) else Modifier)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 7.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text, color = if (primary) Color.White else TextSecondary, fontSize = 13.sp, fontWeight = FontWeight.Medium)
    }
}
