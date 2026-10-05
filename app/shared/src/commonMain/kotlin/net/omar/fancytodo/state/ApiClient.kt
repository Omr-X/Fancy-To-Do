package net.omar.fancytodo.state

import io.ktor.client.HttpClient
import io.ktor.client.engine.js.Js
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import net.omar.fancytodo.platform.pageHostname

private const val API_PORT = 8080

/**
 * Client for the API, which runs on the same machine that served the page (so it also works from
 * other devices). With a [token] every request is made as that logged-in user. With
 * [throwOnError], non-2xx responses throw (a ResponseException carrying the status).
 */
internal fun createApiClient(token: String? = null, throwOnError: Boolean = false) = HttpClient(Js) {
    expectSuccess = throwOnError
    install(ContentNegotiation) { json() }
    defaultRequest {
        host = pageHostname()
        port = API_PORT
        token?.let { bearerAuth(it) }
    }
}

internal inline fun <reified T> HttpRequestBuilder.jsonBody(body: T) {
    contentType(ContentType.Application.Json)
    setBody(body)
}
