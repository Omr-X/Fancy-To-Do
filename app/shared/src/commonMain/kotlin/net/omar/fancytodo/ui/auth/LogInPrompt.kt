package net.omar.fancytodo.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import net.omar.fancytodo.ui.components.ArrowIcon
import net.omar.fancytodo.ui.components.CloseIcon
import net.omar.fancytodo.ui.theme.AccentColor
import net.omar.fancytodo.ui.theme.BorderColor
import net.omar.fancytodo.ui.theme.PanelColor
import net.omar.fancytodo.ui.theme.TextMuted
import net.omar.fancytodo.ui.theme.TextPrimary
import net.omar.fancytodo.ui.theme.TextSecondary

/** Small sidebar card inviting guests to log in; can be closed. */
@Composable
internal fun LogInPrompt(onLogIn: () -> Unit, onDismiss: () -> Unit) {
    val shape = RoundedCornerShape(12.dp)
    Column(Modifier.fillMaxWidth().clip(shape).background(PanelColor).border(1.dp, BorderColor, shape).padding(14.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                "Guest",
                color = AccentColor,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.clip(RoundedCornerShape(6.dp)).background(AccentColor.copy(alpha = 0.15f)).padding(horizontal = 7.dp, vertical = 2.dp)
            )
            Spacer(Modifier.weight(1f))
            Box(Modifier.size(22.dp).clip(RoundedCornerShape(6.dp)).clickable(onClick = onDismiss), contentAlignment = Alignment.Center) {
                CloseIcon(TextMuted, 9.dp)
            }
        }
        Spacer(Modifier.height(10.dp))
        Text("Log in to TaskMan", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Medium)
        Spacer(Modifier.height(4.dp))
        Text("Keep your tasks in your own account and use them on any device.", color = TextMuted, fontSize = 12.sp)
        Spacer(Modifier.height(12.dp))
        val buttonShape = RoundedCornerShape(8.dp)
        Row(
            Modifier.clip(buttonShape).border(1.dp, BorderColor, buttonShape).clickable(onClick = onLogIn).padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Log in", color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Medium)
            Spacer(Modifier.width(6.dp))
            ArrowIcon(left = false, color = TextSecondary, dimension = 12.dp)
        }
    }
}
