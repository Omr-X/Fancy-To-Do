package net.omar.fancytodo.routes

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
import io.ktor.server.util.getOrFail
import net.omar.fancytodo.model.Habit

fun Route.habitRoutes() {
    route("/habits") {
        get {
            call.respond(call.userData().habits.values.toList())
        }

        post {
            val habit = call.receive<Habit>()
            call.userData().habits[habit.id] = habit
            call.respondText("Habit created", status = HttpStatusCode.Created)
        }

        get("{id}") {
            val habit = call.userData().habits[call.parameters.getOrFail("id")]
                ?: return@get call.respondText("Not Found", status = HttpStatusCode.NotFound)
            call.respond(habit)
        }

        put("{id}") {
            val habits = call.userData().habits
            val id = call.parameters.getOrFail("id")
            val existing = habits[id] ?: return@put call.respondText("Not Found", status = HttpStatusCode.NotFound)
            val updates = call.receive<Habit>()
            habits[id] = existing.copy(title = updates.title, completions = updates.completions)
            call.respondText("Habit updated", status = HttpStatusCode.OK)
        }

        delete("{id}") {
            if (call.userData().habits.remove(call.parameters.getOrFail("id")) == null) {
                return@delete call.respondText("Not Found", status = HttpStatusCode.NotFound)
            }
            call.respondText("Habit removed", status = HttpStatusCode.Accepted)
        }
    }
}
