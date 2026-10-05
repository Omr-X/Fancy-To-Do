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
import net.omar.fancytodo.model.Task

fun Route.taskRoutes() {
    route("/topics/{topicId}/tasks") {
        get {
            val topicId = call.parameters.getOrFail("topicId")
            call.respond(call.userData().tasks.values.filter { it.topicId == topicId })
        }

        post {
            val task = call.receive<Task>().copy(topicId = call.parameters.getOrFail("topicId"))
            call.userData().tasks[task.id] = task
            call.respondText("Task created", status = HttpStatusCode.Created)
        }
    }

    route("/tasks") {
        get {
            call.respond(call.userData().tasks.values.toList())
        }

        get("{id}") {
            val task = call.userData().tasks[call.parameters.getOrFail("id")]
                ?: return@get call.respondText("Not Found", status = HttpStatusCode.NotFound)
            call.respond(task)
        }

        put("{id}") {
            val tasks = call.userData().tasks
            val id = call.parameters.getOrFail("id")
            if (!tasks.containsKey(id)) return@put call.respondText("Not Found", status = HttpStatusCode.NotFound)
            tasks[id] = call.receive<Task>().copy(id = id)
            call.respondText("Task updated", status = HttpStatusCode.OK)
        }

        delete("{id}") {
            val data = call.userData()
            val id = call.parameters.getOrFail("id")
            if (data.tasks.remove(id) == null) return@delete call.respondText("Not Found", status = HttpStatusCode.NotFound)
            data.blocks.values.removeAll { it.taskId == id }
            call.respondText("Task removed", status = HttpStatusCode.Accepted)
        }
    }
}
