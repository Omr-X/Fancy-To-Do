package net.omar.fancytodo

import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.route
import java.time.LocalDate

fun Route.dashboardRouting(){
    route("/dashboard") {
        get{
            val today = LocalDate.now().toString()
            val totalTasks = taskStorage.size
            val completedTasks = taskStorage.values.count { it.isCompleted }
            val overdueTasks = taskStorage.values.count { it.dueDate != null && it.dueDate!! < today && !it.isCompleted }
            val totalHabits = habitsStorage.size
            val doneToday = habitsStorage.values.count { it.completions[today] == true }
            call.respond(Dashboard(totalTasks, completedTasks, overdueTasks, totalHabits, doneToday))
        }
    }
}
