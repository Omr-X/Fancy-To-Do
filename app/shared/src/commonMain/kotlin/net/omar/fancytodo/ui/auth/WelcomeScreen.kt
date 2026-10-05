package net.omar.fancytodo.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import net.omar.fancytodo.ui.background.dotFieldHost
import net.omar.fancytodo.ui.components.APP_NAME
import net.omar.fancytodo.ui.components.AppLogo
import net.omar.fancytodo.ui.components.GhostButton
import net.omar.fancytodo.ui.components.PrimaryButton
import net.omar.fancytodo.ui.navigation.Page
import net.omar.fancytodo.ui.navigation.PageIcon
import net.omar.fancytodo.ui.theme.AccentColor
import net.omar.fancytodo.ui.theme.BackgroundColor
import net.omar.fancytodo.ui.theme.BorderColor
import net.omar.fancytodo.ui.theme.HeadingLineHeight
import net.omar.fancytodo.ui.theme.HeadingTracking
import net.omar.fancytodo.ui.theme.TextPrimary
import net.omar.fancytodo.ui.theme.TextSecondary
import net.omar.fancytodo.ui.theme.TitleTracking

/** Landing page shown before logging in: what the app is, plus Create account / Log in. */
@Composable
internal fun WelcomeScreen(compact: Boolean, onCreateAccount: () -> Unit, onLogIn: () -> Unit) {
    Box(Modifier.fillMaxSize().dotFieldHost()) {
        Column(
            Modifier.fillMaxSize().verticalScroll(rememberScrollState())
                .padding(horizontal = if (compact) 20.dp else 40.dp, vertical = 48.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Column(Modifier.widthIn(max = 900.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                AppLogo(52.dp)
                Spacer(Modifier.height(22.dp))
                Text(
                    APP_NAME,
                    color = TextPrimary,
                    fontSize = if (compact) 42.sp else 60.sp,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = HeadingTracking,
                    lineHeight = HeadingLineHeight
                )
                Spacer(Modifier.height(12.dp))
                Text(
                    "School, the gym and your hobbies, planned in one calm place.",
                    color = TextSecondary,
                    fontSize = if (compact) 16.sp else 19.sp,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(30.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    PrimaryButton("Create account", onClick = onCreateAccount)
                    Spacer(Modifier.width(10.dp))
                    GhostButton("Log in", onClick = onLogIn)
                }
                Spacer(Modifier.height(if (compact) 40.dp else 64.dp))
                FeatureList(compact)
            }
        }
    }
}

@Composable
private fun FeatureList(compact: Boolean) {
    val cards: List<@Composable (Modifier) -> Unit> = listOf(
        { FeatureCard(Page.Tasks, "Tasks, your way", "Areas and topics, subtasks, and tasks that wait on each other.", it) },
        { FeatureCard(Page.Calendar, "Plan your day", "Drag a task onto the calendar; time worked adds up on its own.", it) },
        { FeatureCard(Page.Dashboard, "Build habits", "Tick them off daily and watch the 30-day grid fill up.", it) }
    )
    if (compact) {
        Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            cards.forEach { it(Modifier.fillMaxWidth()) }
        }
    } else {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            cards.forEach { it(Modifier.weight(1f)) }
        }
    }
}

@Composable
private fun FeatureCard(icon: Page, title: String, description: String, modifier: Modifier) {
    val shape = RoundedCornerShape(14.dp)
    Column(modifier.clip(shape).background(BackgroundColor).border(1.dp, BorderColor, shape).padding(20.dp)) {
        PageIcon(icon, AccentColor, 22.dp)
        Spacer(Modifier.height(14.dp))
        Text(title, color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Medium, letterSpacing = TitleTracking)
        Spacer(Modifier.height(6.dp))
        Text(description, color = TextSecondary, fontSize = 13.sp)
    }
}
