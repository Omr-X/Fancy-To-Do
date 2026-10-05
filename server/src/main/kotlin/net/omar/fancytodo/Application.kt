package net.omar.fancytodo

import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.auth.Authentication
import io.ktor.server.auth.UserIdPrincipal
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.bearer
import io.ktor.server.netty.EngineMain
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.plugins.cors.routing.CORS
import io.ktor.server.plugins.origin
import io.ktor.server.plugins.ratelimit.RateLimit
import io.ktor.server.plugins.ratelimit.RateLimitName
import io.ktor.server.plugins.ratelimit.rateLimit
import io.ktor.server.routing.routing
import kotlin.time.Duration.Companion.minutes
import net.omar.fancytodo.routes.SESSION_AUTH
import net.omar.fancytodo.routes.areaRoutes
import net.omar.fancytodo.routes.authRoutes
import net.omar.fancytodo.routes.blockRoutes
import net.omar.fancytodo.routes.habitRoutes
import net.omar.fancytodo.routes.sessionRoutes
import net.omar.fancytodo.routes.summaryRoutes
import net.omar.fancytodo.routes.taskRoutes
import net.omar.fancytodo.routes.topicRoutes
import net.omar.fancytodo.storage.loadAccounts
import net.omar.fancytodo.storage.startAutosave
import net.omar.fancytodo.storage.userIdForToken

private val AUTH_RATE_LIMIT = RateLimitName("auth")

fun main(args: Array<String>) = EngineMain.main(args)

/** Ktor module (wired in application.conf): JSON REST API for the web app, with optional accounts. */
fun Application.module() {
    loadAccounts()
    startAutosave()

    // The web app is served from another port, so the browser needs CORS to call this API.
    install(CORS) {
        allowMethod(HttpMethod.Put)
        allowMethod(HttpMethod.Delete)
        allowMethod(HttpMethod.Patch)
        allowHeader(HttpHeaders.ContentType)
        allowHeader(HttpHeaders.Authorization)
        anyHost()
    }
    install(ContentNegotiation) {
        json()
    }
    // Requests carry "Authorization: Bearer <token>"; the token decides whose data they see.
    install(Authentication) {
        bearer(SESSION_AUTH) {
            authenticate { credential -> userIdForToken(credential.token)?.let { UserIdPrincipal(it) } }
        }
    }
    // Slows down password guessing: 10 login / sign-up attempts per minute per address.
    install(RateLimit) {
        register(AUTH_RATE_LIMIT) {
            rateLimiter(limit = 10, refillPeriod = 1.minutes)
            requestKey { call -> call.request.origin.remoteHost }
        }
    }
    routing {
        rateLimit(AUTH_RATE_LIMIT) {
            authRoutes()
        }
        authenticate(SESSION_AUTH) {
            sessionRoutes()
        }
        // Optional: without a token you're a guest and use the shared guest data.
        authenticate(SESSION_AUTH, optional = true) {
            areaRoutes()
            topicRoutes()
            taskRoutes()
            habitRoutes()
            blockRoutes()
            summaryRoutes()
        }
    }
}
