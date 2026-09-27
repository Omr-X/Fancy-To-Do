package net.omar.fancytodo

import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.chapterRouting() {
    route("/books/{bookId}/chapters") {
        get {
            val bookId = call.parameters["bookId"] ?: return@get call.respondText("Missing bookId", status = HttpStatusCode.BadRequest)
            call.respond(chaptersStorage.values.filter { it.bookId == bookId })
        }

        post {
            val bookId = call.parameters["bookId"] ?: return@post call.respondText("Missing bookId", status = HttpStatusCode.BadRequest)
            val chapter = call.receive<Chapter>().copy(bookId = bookId)
            chaptersStorage[chapter.id] = chapter
            call.respondText("Chapter created", status = HttpStatusCode.Created)
        }
    }

    route("/chapters/{id}") {
        get {
            val id = call.parameters["id"] ?: return@get call.respondText("Missing id", status = HttpStatusCode.BadRequest)
            val chapter = chaptersStorage[id] ?: return@get call.respondText("Not Found", status = HttpStatusCode.NotFound)
            call.respond(chapter)
        }

        put {
            val id = call.parameters["id"] ?: return@put call.respondText("Missing id", status = HttpStatusCode.BadRequest)
            val existing = chaptersStorage[id] ?: return@put call.respondText("Not Found", status = HttpStatusCode.NotFound)
            val updates = call.receive<Chapter>()
            chaptersStorage[id] = existing.copy(title = updates.title)
            call.respondText("Chapter updated", status = HttpStatusCode.OK)
        }

        delete {
            val id = call.parameters["id"] ?: return@delete call.respondText("Missing id", status = HttpStatusCode.BadRequest)
            if (chaptersStorage.remove(id) != null) {
                call.respondText("Chapter removed", status = HttpStatusCode.Accepted)
            } else {
                call.respondText("Not Found", status = HttpStatusCode.NotFound)
            }
        }
    }
}