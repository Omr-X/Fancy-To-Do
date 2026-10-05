package net.omar.fancytodo.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import net.omar.fancytodo.auth.MIN_PASSWORD_LENGTH
import net.omar.fancytodo.state.SessionState
import net.omar.fancytodo.ui.components.AppLogo
import net.omar.fancytodo.ui.components.CloseIcon
import net.omar.fancytodo.ui.components.ErrorMessage
import net.omar.fancytodo.ui.components.InputField
import net.omar.fancytodo.ui.components.LinkText
import net.omar.fancytodo.ui.components.PrimaryButton
import net.omar.fancytodo.ui.theme.AccentColor
import net.omar.fancytodo.ui.theme.BackgroundColor
import net.omar.fancytodo.ui.theme.BorderColor
import net.omar.fancytodo.ui.theme.HeadingLineHeight
import net.omar.fancytodo.ui.theme.HeadingTracking
import net.omar.fancytodo.ui.theme.ScrimColor
import net.omar.fancytodo.ui.theme.TextPrimary
import net.omar.fancytodo.ui.theme.TextSecondary

internal enum class AuthMode { LogIn, CreateAccount }

/** Dialog for both logging in and creating an account, shown over the app. */
@Composable
internal fun AuthDialog(mode: AuthMode, session: SessionState, onSwitchMode: () -> Unit, onClose: () -> Unit) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }
    val creating = mode == AuthMode.CreateAccount
    val submit = { if (creating) session.createAccount(name, email, password) else session.logIn(email, password) }
    // An error from the other form (or from before) shouldn't follow you here.
    LaunchedEffect(mode) { session.clearError() }

    Box(
        Modifier.fillMaxSize().background(ScrimColor)
            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = onClose),
        contentAlignment = Alignment.Center
    ) {
        val shape = RoundedCornerShape(16.dp)
        Column(
            Modifier.verticalScroll(rememberScrollState()).padding(20.dp)
                .widthIn(max = 400.dp).fillMaxWidth()
                .clip(shape).background(BackgroundColor).border(1.dp, BorderColor, shape)
                .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) { }
                .padding(28.dp)
        ) {
            Row(verticalAlignment = Alignment.Top) {
                AppLogo(36.dp)
                Spacer(Modifier.weight(1f))
                Box(Modifier.size(28.dp).clip(RoundedCornerShape(7.dp)).clickable(onClick = onClose), contentAlignment = Alignment.Center) {
                    CloseIcon(TextSecondary, 11.dp)
                }
            }
            Spacer(Modifier.height(16.dp))
            Text(
                if (creating) "Create your account" else "Welcome back",
                color = TextPrimary,
                fontSize = 26.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = HeadingTracking,
                lineHeight = HeadingLineHeight
            )
            Spacer(Modifier.height(6.dp))
            Text(
                if (creating) "It's free and takes a minute." else "Log in to see your tasks and habits.",
                color = TextSecondary,
                fontSize = 14.sp
            )
            Spacer(Modifier.height(24.dp))

            if (creating) {
                FieldLabel("Name")
                InputField(name, { name = it }, "Your name", Modifier.fillMaxWidth())
                Spacer(Modifier.height(14.dp))
            }
            FieldLabel("Email")
            InputField(email, { email = it }, "you@example.com", Modifier.fillMaxWidth(), keyboardType = KeyboardType.Email)
            Spacer(Modifier.height(14.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                FieldLabel("Password", Modifier.weight(1f))
                LinkText(if (showPassword) "Hide" else "Show") { showPassword = !showPassword }
            }
            InputField(
                password,
                { password = it },
                if (creating) "At least $MIN_PASSWORD_LENGTH characters" else "Your password",
                Modifier.fillMaxWidth(),
                keyboardType = KeyboardType.Password,
                masked = !showPassword,
                onDone = submit
            )

            session.error?.let {
                Spacer(Modifier.height(14.dp))
                ErrorMessage(it)
            }
            Spacer(Modifier.height(22.dp))
            PrimaryButton(
                when {
                    session.busy -> "Please wait..."
                    creating -> "Create account"
                    else -> "Log in"
                },
                Modifier.fillMaxWidth(),
                onClick = submit
            )
            Spacer(Modifier.height(14.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                Text(if (creating) "Already have an account?" else "No account yet?", color = TextSecondary, fontSize = 13.sp)
                LinkText(if (creating) "Log in" else "Create one", AccentColor, onClick = onSwitchMode)
            }
        }
    }
}

@Composable
private fun FieldLabel(text: String, modifier: Modifier = Modifier) {
    Text(text, color = TextSecondary, fontSize = 13.sp, fontWeight = FontWeight.Medium, modifier = modifier.padding(bottom = 6.dp))
}
