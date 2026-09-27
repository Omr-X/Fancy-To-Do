package net.omar.fancytodo

import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.route
import java.time.LocalDate

fun Route.homeRouting() {
    route("/home") {
        get {
            val today = LocalDate.now().toString()
            val habitsToday = habitsStorage.values.toList()
            val tasksDueToday = taskStorage.values.filter { it.dueDate == today }
            val tasksByPriority = taskStorage.values.filter { !it.isCompleted }
            call.respond(Home(habitsToday, tasksDueToday, tasksByPriority, reminder))
        }
    }
}