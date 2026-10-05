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
import net.omar.fancytodo.model.Area

fun Route.areaRoutes() {
    route("/areas") {
        get {
            call.respond(call.userData().areas.values.toList())
        }

        post {
            val area = call.receive<Area>()
            call.userData().areas[area.id] = area
            call.respondText("Area created", status = HttpStatusCode.Created)
        }

        get("{id}") {
            val area = call.userData().areas[call.parameters.getOrFail("id")]
                ?: return@get call.respondText("Not Found", status = HttpStatusCode.NotFound)
            call.respond(area)
        }

        put("{id}") {
            val areas = call.userData().areas
            val id = call.parameters.getOrFail("id")
            val existing = areas[id] ?: return@put call.respondText("Not Found", status = HttpStatusCode.NotFound)
            val updates = call.receive<Area>()
            areas[id] = existing.copy(title = updates.title, color = updates.color)
            call.respondText("Area updated", status = HttpStatusCode.OK)
        }

        // Deleting an area deletes its topics and their tasks too.
        delete("{id}") {
            val data = call.userData()
            val id = call.parameters.getOrFail("id")
            if (data.areas.remove(id) == null) return@delete call.respondText("Not Found", status = HttpStatusCode.NotFound)
            val topicIds = data.topics.values.filter { it.areaId == id }.map { it.id }
            topicIds.forEach { data.topics.remove(it) }
            data.removeTasksOf(topicIds)
            call.respondText("Area removed", status = HttpStatusCode.Accepted)
        }
    }
}
