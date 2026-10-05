package net.omar.fancytodo.routes

import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.response.respondText
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.put
import java.time.LocalDate
import net.omar.fancytodo.model.DashboardSummary
import net.omar.fancytodo.model.HomeSummary
import net.omar.fancytodo.model.Reminder

/** Read-only overviews (/dashboard, /home) and the user's editable reminder. */
fun Route.summaryRoutes() {
    get("/dashboard") {
        val data = call.userData()
        val today = LocalDate.now().toString()
        val tasks = data.tasks.values
        call.respond(
            DashboardSummary(
                totalTasks = tasks.size,
                completedTasks = tasks.count { it.isCompleted },
                overdueTasks = tasks.count { task -> !task.isCompleted && task.dueDate?.let { it < today } == true },
                totalHabits = data.habits.size,
                habitsDoneToday = data.habits.values.count { it.completions[today] == true }
            )
        )
    }

    get("/home") {
        val data = call.userData()
        val today = LocalDate.now().toString()
        call.respond(
            HomeSummary(
                habitsToday = data.habits.values.toList(),
                tasksDueToday = data.tasks.values.filter { it.dueDate == today },
                openTasks = data.tasks.values.filter { !it.isCompleted },
                reminder = data.reminder
            )
        )
    }

    get("/reminder") {
        call.respond(call.userData().reminder)
    }

    put("/reminder") {
        call.userData().reminder = call.receive<Reminder>()
        call.respondText("Reminder updated", status = HttpStatusCode.OK)
    }
}
