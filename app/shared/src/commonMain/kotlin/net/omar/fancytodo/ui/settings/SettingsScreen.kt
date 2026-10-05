package net.omar.fancytodo.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import net.omar.fancytodo.auth.UserProfile
import net.omar.fancytodo.ui.auth.AuthMode
import net.omar.fancytodo.ui.components.GhostButton
import net.omar.fancytodo.ui.components.PageHeader
import net.omar.fancytodo.ui.components.PrimaryButton
import net.omar.fancytodo.ui.components.ScreenScaffold
import net.omar.fancytodo.ui.theme.AccentColor
import net.omar.fancytodo.ui.theme.BackgroundColor
import net.omar.fancytodo.ui.theme.BorderColor
import net.omar.fancytodo.ui.theme.RaisedPanelColor
import net.omar.fancytodo.ui.theme.TextPrimary
import net.omar.fancytodo.ui.theme.TextSecondary

/** The account you're logged in with (and logging out), or the way in when using TaskMan as a guest. */
@Composable
internal fun SettingsScreen(user: UserProfile?, compact: Boolean, onLogOut: () -> Unit, onOpenAuth: (AuthMode) -> Unit) {
    ScreenScaffold(compact) {
        PageHeader("Settings", "Your account")
        Spacer(Modifier.height(24.dp))
        val shape = RoundedCornerShape(12.dp)
        Row(
            Modifier.widthIn(max = 620.dp).fillMaxWidth()
                .clip(shape).background(BackgroundColor).border(1.dp, BorderColor, shape)
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                Modifier.size(44.dp).clip(CircleShape).background(if (user != null) AccentColor else RaisedPanelColor),
                contentAlignment = Alignment.Center
            ) {
                Text(user?.name?.take(1)?.uppercase() ?: "?", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Medium)
            }
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(user?.name ?: "Guest", color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Spacer(Modifier.height(2.dp))
                Text(
                    user?.email ?: "Not logged in",
                    color = TextSecondary,
                    fontSize = 13.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(Modifier.width(14.dp))
            if (user != null) {
                GhostButton("Log out", onClick = onLogOut)
            } else {
                GhostButton("Create account") { onOpenAuth(AuthMode.CreateAccount) }
                Spacer(Modifier.width(8.dp))
                PrimaryButton("Log in") { onOpenAuth(AuthMode.LogIn) }
            }
        }
    }
}
