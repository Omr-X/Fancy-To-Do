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
import net.omar.fancytodo.model.Topic

fun Route.topicRoutes() {
    route("/areas/{areaId}/topics") {
        get {
            val areaId = call.parameters.getOrFail("areaId")
            call.respond(call.userData().topics.values.filter { it.areaId == areaId })
        }

        post {
            val topic = call.receive<Topic>().copy(areaId = call.parameters.getOrFail("areaId"))
            call.userData().topics[topic.id] = topic
            call.respondText("Topic created", status = HttpStatusCode.Created)
        }
    }

    route("/topics") {
        get {
            call.respond(call.userData().topics.values.toList())
        }

        get("{id}") {
            val topic = call.userData().topics[call.parameters.getOrFail("id")]
                ?: return@get call.respondText("Not Found", status = HttpStatusCode.NotFound)
            call.respond(topic)
        }

        put("{id}") {
            val topics = call.userData().topics
            val id = call.parameters.getOrFail("id")
            val existing = topics[id] ?: return@put call.respondText("Not Found", status = HttpStatusCode.NotFound)
            val updates = call.receive<Topic>()
            topics[id] = existing.copy(title = updates.title, color = updates.color)
            call.respondText("Topic updated", status = HttpStatusCode.OK)
        }

        // Deleting a topic deletes its tasks too.
        delete("{id}") {
            val data = call.userData()
            val id = call.parameters.getOrFail("id")
            if (data.topics.remove(id) == null) return@delete call.respondText("Not Found", status = HttpStatusCode.NotFound)
            data.removeTasksOf(listOf(id))
            call.respondText("Topic removed", status = HttpStatusCode.Accepted)
        }
    }
}
