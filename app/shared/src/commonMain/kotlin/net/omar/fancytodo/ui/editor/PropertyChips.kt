package net.omar.fancytodo.ui.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import net.omar.fancytodo.ui.components.CheckIcon
import net.omar.fancytodo.ui.components.CloseIcon
import net.omar.fancytodo.ui.components.ColorSquare
import net.omar.fancytodo.ui.components.Dot
import net.omar.fancytodo.ui.components.InputField
import net.omar.fancytodo.ui.components.MetaIcon
import net.omar.fancytodo.ui.components.MetaKind
import net.omar.fancytodo.ui.components.SmallButton
import net.omar.fancytodo.ui.theme.AccentColor
import net.omar.fancytodo.ui.theme.BorderColor
import net.omar.fancytodo.ui.theme.LocalMonoFamily
import net.omar.fancytodo.ui.theme.RaisedPanelColor
import net.omar.fancytodo.ui.theme.TextPrimary
import net.omar.fancytodo.ui.theme.TextSecondary

private val ChipShape = RoundedCornerShape(7.dp)

private fun Modifier.chip() = height(28.dp).clip(ChipShape).background(RaisedPanelColor).border(1.dp, BorderColor, ChipShape)

/** A property chip that opens a dropdown; the menu content gets a `close` callback. */
@Composable
internal fun ChipMenu(
    icon: @Composable () -> Unit,
    label: String,
    filled: Boolean,
    menu: @Composable (close: () -> Unit) -> Unit
) {
    var open by remember { mutableStateOf(false) }
    Box {
        Row(
            Modifier.chip().clickable { open = true }.padding(horizontal = 9.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            icon()
            Spacer(Modifier.width(6.dp))
            Text(
                label,
                color = if (filled) TextPrimary else TextSecondary,
                fontSize = 12.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.widthIn(max = 200.dp)
            )
        }
        DropdownMenu(expanded = open, onDismissRequest = { open = false }) {
            menu { open = false }
        }
    }
}

@Composable
internal fun MenuRow(text: String, dot: Color?, selected: Boolean, onClick: () -> Unit) {
    DropdownMenuItem(
        text = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (dot != null) {
                    Dot(dot, 8.dp)
                    Spacer(Modifier.width(10.dp))
                }
                Text(text, color = if (selected) TextPrimary else TextSecondary, fontSize = 13.sp, modifier = Modifier.weight(1f, fill = false))
                if (selected) {
                    Spacer(Modifier.width(10.dp))
                    CheckIcon(AccentColor, 13.dp)
                }
            }
        },
        onClick = onClick
    )
}

/** The estimate chip is typed into directly instead of opening a menu. */
@Composable
internal fun EstimateChip(value: String, onChange: (String) -> Unit) {
    Row(Modifier.chip().padding(horizontal = 9.dp), verticalAlignment = Alignment.CenterVertically) {
        MetaIcon(MetaKind.Estimate, if (value.isNotEmpty()) TextPrimary else TextSecondary, 13.dp)
        Spacer(Modifier.width(6.dp))
        val style = LocalTextStyle.current.merge(TextStyle(color = TextPrimary, fontSize = 12.sp, fontFamily = LocalMonoFamily.current))
        BasicTextField(
            value = value,
            onValueChange = onChange,
            singleLine = true,
            textStyle = style,
            cursorBrush = SolidColor(AccentColor),
            modifier = Modifier.widthIn(min = 6.dp, max = 48.dp),
            decorationBox = { inner ->
                Box {
                    if (value.isEmpty()) Text("Estimate", color = TextSecondary, fontSize = 12.sp, maxLines = 1)
                    inner()
                }
            }
        )
        if (value.isNotEmpty()) Text(" h", color = TextSecondary, fontSize = 12.sp)
    }
}

/** Inline "new area / new topic" row: color square, name, add. */
@Composable
internal fun CreateRow(kind: Creating, initialColor: Int, onCancel: () -> Unit, onCreate: (String, Int) -> Unit) {
    var name by remember(kind) { mutableStateOf("") }
    var color by remember(kind) { mutableStateOf(initialColor) }
    val submit = { if (name.isNotBlank()) onCreate(name, color) }
    Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp).padding(top = 10.dp), verticalAlignment = Alignment.CenterVertically) {
        ColorSquare(color) { color = it }
        Spacer(Modifier.width(8.dp))
        InputField(name, { name = it }, if (kind == Creating.Area) "New area name" else "New topic name", Modifier.weight(1f), onDone = submit)
        Spacer(Modifier.width(8.dp))
        SmallButton("Add", primary = true, onClick = submit)
        Spacer(Modifier.width(4.dp))
        Box(Modifier.size(28.dp).clip(RoundedCornerShape(7.dp)).clickable(onClick = onCancel), contentAlignment = Alignment.Center) {
            CloseIcon(TextSecondary, 10.dp)
        }
    }
}
