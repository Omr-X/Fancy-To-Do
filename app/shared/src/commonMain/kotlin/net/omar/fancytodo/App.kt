package net.omar.fancytodo

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import org.jetbrains.compose.resources.painterResource

import fancytodo.app.shared.generated.resources.Res
import fancytodo.app.shared.generated.resources.compose_multiplatform
import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.get
import io.ktor.client.call.body
import io.ktor.serialization.kotlinx.json.json
import io.ktor.client.engine.js.Js
import androidx.compose.runtime.*

@Composable
fun App() {
    var books by remember { mutableStateOf<List<Book>>(emptyList()) }
    var error by remember { mutableStateOf<String?>(null) }

    val client = remember {
        HttpClient(Js) {
            install(ContentNegotiation) { json() }
            defaultRequest { host = "localhost"; port = 8080 }
        }
    }

    LaunchedEffect(Unit) {
            books = client.get("books").body()
    }

    MaterialTheme {
        Column {
            error?.let { Text("Error: $it") }
            books.forEach { book -> Text(book.title) }
        }
        Row {
            Text(text = "Item 1")
            Text(text = "Item 2")
            Text(text = "Item 3")
        }

    }
}