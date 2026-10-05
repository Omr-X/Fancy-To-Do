package net.omar.fancytodo.ui.tasks

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import net.omar.fancytodo.request.NamePromptRequest
import net.omar.fancytodo.ui.components.ColorSquare
import net.omar.fancytodo.ui.components.GhostButton
import net.omar.fancytodo.ui.components.InputField
import net.omar.fancytodo.ui.components.PrimaryButton
import net.omar.fancytodo.ui.theme.BorderColor
import net.omar.fancytodo.ui.theme.PanelColor
import net.omar.fancytodo.ui.theme.ScrimColor
import net.omar.fancytodo.ui.theme.TextPrimary

@Composable
internal fun NamePromptDialog(request: NamePromptRequest, initialColor: Int, onCancel: () -> Unit, onCreate: (String, Int) -> Unit) {
    var text by remember { mutableStateOf("") }
    var color by remember { mutableStateOf(initialColor) }
    val shape = RoundedCornerShape(16.dp)
    val submit: () -> Unit = { if (text.isNotBlank()) onCreate(text, color) }

    Box(
        Modifier.fillMaxSize().background(ScrimColor)
            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = onCancel),
        contentAlignment = Alignment.Center
    ) {
        Column(
            Modifier.padding(16.dp).widthIn(max = 380.dp).fillMaxWidth()
                .clip(shape).background(PanelColor).border(1.dp, BorderColor, shape)
                .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) { }
                .padding(24.dp)
        ) {
            Text(request.title, color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(16.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                ColorSquare(color) { color = it }
                Spacer(Modifier.width(10.dp))
                InputField(text, { text = it }, request.placeholder, Modifier.weight(1f), onDone = submit)
            }
            Spacer(Modifier.height(18.dp))
            Row {
                PrimaryButton("Create") { submit() }
                Spacer(Modifier.width(10.dp))
                GhostButton("Cancel", onClick = onCancel)
            }
        }
    }
}
