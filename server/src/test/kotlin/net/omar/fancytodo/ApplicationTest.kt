package net.omar.fancytodo

import io.ktor.client.HttpClient
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.server.testing.testApplication
import kotlin.io.path.createTempDirectory
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlinx.serialization.json.Json
import net.omar.fancytodo.auth.AuthResponse

class ApplicationTest {

    private suspend fun HttpClient.postJson(path: String, body: String, token: String? = null) = post(path) {
        contentType(ContentType.Application.Json)
        token?.let { bearerAuth(it) }
        setBody(body)
    }

    private suspend fun HttpClient.register(name: String, email: String, password: String): String {
        val response = postJson("/auth/register", """{"name":"$name","email":"$email","password":"$password"}""")
        assertEquals(HttpStatusCode.Created, response.status, response.bodyAsText())
        return Json.decodeFromString<AuthResponse>(response.bodyAsText()).token
    }

    @Test
    fun accountsKeepDataSeparate() = testApplication {
        // Keep the test away from the real data folder.
        System.setProperty("fancytodo.dataDir", createTempDirectory("fancytodo").toString())
        application { module() }

        // Without logging in you're a guest; the first account takes over what guests made.
        client.postJson("/areas", """{"id":"g1","title":"Inbox","color":0}""")
        assertTrue(client.get("/areas").bodyAsText().contains("Inbox"))
        val ada = client.register("Ada", "ada@example.com", "correct horse")
        assertTrue(client.get("/areas") { bearerAuth(ada) }.bodyAsText().contains("Inbox"))
        assertFalse(client.get("/areas").bodyAsText().contains("Inbox"))
        assertEquals(HttpStatusCode.Unauthorized, client.get("/auth/me").status)

        assertEquals(HttpStatusCode.Conflict, client.postJson("/auth/register", """{"name":"Ada","email":"ADA@example.com","password":"another one"}""").status)

        // Logging in: wrong password is refused, right one works and /auth/me knows who it is.
        assertEquals(HttpStatusCode.Unauthorized, client.postJson("/auth/login", """{"email":"ada@example.com","password":"wrong pass"}""").status)
        val login = client.postJson("/auth/login", """{"email":"ada@example.com","password":"correct horse"}""")
        assertEquals(HttpStatusCode.OK, login.status)
        assertTrue(client.get("/auth/me") { bearerAuth(ada) }.bodyAsText().contains("ada@example.com"))

        // Each account only sees its own data.
        client.postJson("/areas", """{"id":"a1","title":"Gym","color":2}""", ada)
        val bob = client.register("Bob", "bob@example.com", "hunter2 hunter2")
        assertTrue(client.get("/areas") { bearerAuth(ada) }.bodyAsText().contains("Gym"))
        assertFalse(client.get("/areas") { bearerAuth(bob) }.bodyAsText().contains("Gym"))

        // Logging out ends the session: the old token is refused.
        client.postJson("/auth/logout", "", ada)
        assertEquals(HttpStatusCode.Unauthorized, client.get("/areas") { bearerAuth(ada) }.status)
        assertEquals(HttpStatusCode.Unauthorized, client.get("/auth/me") { bearerAuth(ada) }.status)
    }
}
