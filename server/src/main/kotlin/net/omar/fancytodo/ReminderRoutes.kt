package net.omar.fancytodo

import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.response.respondText
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.put
import io.ktor.server.routing.route

fun Route.reminderRouting() {
    route("/reminder") {
        get {
            call.respond(reminder)
        }
        put {
            reminder = call.receive<Reminder>()
            call.respondText("Reminder updated", status = HttpStatusCode.OK)
        }
    }
}