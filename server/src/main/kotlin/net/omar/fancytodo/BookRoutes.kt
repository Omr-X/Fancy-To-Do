package net.omar.fancytodo

import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.bookRouting() {
    route("/books") {
        get {
            call.respond(booksStorage.values.toList())
        }

        post {
            val book = call.receive<Book>()
            booksStorage[book.id] = book
            call.respondText("Book created", status = HttpStatusCode.Created)
        }
    }

    route("/books/{id}") {
        get {
            val id = call.parameters["id"] ?: return@get call.respondText("Missing id", status = HttpStatusCode.BadRequest)
            val book = booksStorage[id] ?: return@get call.respondText("Not Found", status = HttpStatusCode.NotFound)
            call.respond(book)
        }

        put {
            val id = call.parameters["id"] ?: return@put call.respondText("Missing id", status = HttpStatusCode.BadRequest)
            val existing = booksStorage[id] ?: return@put call.respondText("Not Found", status = HttpStatusCode.NotFound)
            val updates = call.receive<Book>()
            booksStorage[id] = existing.copy(title = updates.title)
            call.respondText("Book updated", status = HttpStatusCode.OK)
        }

        delete {
            val id = call.parameters["id"] ?: return@delete call.respondText("Missing id", status = HttpStatusCode.BadRequest)
            if (booksStorage.remove(id) != null) {
                call.respondText("Book removed", status = HttpStatusCode.Accepted)
            } else {
                call.respondText("Not Found", status = HttpStatusCode.NotFound)
            }
        }
    }
}
