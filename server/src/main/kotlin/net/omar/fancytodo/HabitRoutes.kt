package net.omar.fancytodo

import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.response.respondText
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.put
import io.ktor.server.routing.route
import kotlin.collections.set

fun Route.habitRoutes() {
    route("/habits"){
        get{
            call.respond(habitsStorage.values.toList())
        }

        post {
            val habit = call.receive<Habit>()
            habitsStorage[habit.id] = habit
            call.respondText("Habit created", status = HttpStatusCode.Created)
        }

        get("{id}") {
            val id = call.parameters["id"] ?: return@get call.respondText("Missing id", status = HttpStatusCode.BadRequest)
            val habit = habitsStorage[id] ?: return@get call.respondText("Not Found", status = HttpStatusCode.NotFound)
            call.respond(habit)
        }

        put("{id}") {
            val id = call.parameters["id"] ?: return@put call.respondText("Missing id", status = HttpStatusCode.BadRequest)
            val existing = habitsStorage[id] ?: return@put call.respondText("Not Found", status = HttpStatusCode.NotFound)
            val updates = call.receive<Habit>()
            habitsStorage[id] = existing.copy(title = updates.title)
            call.respondText("Habit updated", status = HttpStatusCode.OK)
        }

        delete("{id}") {
            val id = call.parameters["id"] ?: return@delete call.respondText("Missing id", status = HttpStatusCode.BadRequest)
            if (habitsStorage.remove(id) != null) {
                call.respondText("Habit removed", status = HttpStatusCode.Accepted)
            } else {
                call.respondText("Not Found", status = HttpStatusCode.NotFound)
            }
        }
    }
}