package net.omar.fancytodo

import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.taskRouting() {
    route("/chapters/{chapterId}/tasks") {
        get {
            val chapterId = call.parameters["chapterId"] ?: return@get call.respondText("Missing chapterId", status = HttpStatusCode.BadRequest)
            call.respond(taskStorage.values.filter { it.chapterId == chapterId })
        }

        post {
            val chapterId = call.parameters["chapterId"] ?: return@post call.respondText("Missing chapterId", status = HttpStatusCode.BadRequest)
            val task = call.receive<Task>().copy(chapterId = chapterId)
            taskStorage[task.id] = task
            call.respondText("Task created", status = HttpStatusCode.Created)
        }
    }

    route("/tasks/{id}") {
        get {
            val id = call.parameters["id"] ?: return@get call.respondText("Missing id", status = HttpStatusCode.BadRequest)
            val task = taskStorage[id] ?: return@get call.respondText("Not Found", status = HttpStatusCode.NotFound)
            call.respond(task)
        }

        put {
            val id = call.parameters["id"] ?: return@put call.respondText("Missing id", status = HttpStatusCode.BadRequest)
            val existing = taskStorage[id] ?: return@put call.respondText("Not Found", status = HttpStatusCode.NotFound)
            val updates = call.receive<Task>()
            taskStorage[id] = existing.copy(title = updates.title, description = updates.description, dueDate = updates.dueDate, guessedTime = updates.guessedTime, isCompleted = updates.isCompleted)
            call.respondText("Task updated", status = HttpStatusCode.OK)
        }

        delete {
            val id = call.parameters["id"] ?: return@delete call.respondText("Missing id", status = HttpStatusCode.BadRequest)
            if (taskStorage.remove(id) != null) {
                call.respondText("Task removed", status = HttpStatusCode.Accepted)
            } else {
                call.respondText("Not Found", status = HttpStatusCode.NotFound)
            }
        }
    }
}