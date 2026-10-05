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
import net.omar.fancytodo.model.WorkBlock

fun Route.blockRoutes() {
    route("/blocks") {
        get {
            call.respond(call.userData().blocks.values.toList())
        }

        post {
            val block = call.receive<WorkBlock>()
            call.userData().blocks[block.id] = block
            call.respondText("Block created", status = HttpStatusCode.Created)
        }

        put("{id}") {
            val blocks = call.userData().blocks
            val id = call.parameters.getOrFail("id")
            if (!blocks.containsKey(id)) return@put call.respondText("Not Found", status = HttpStatusCode.NotFound)
            blocks[id] = call.receive<WorkBlock>().copy(id = id)
            call.respondText("Block updated", status = HttpStatusCode.OK)
        }

        delete("{id}") {
            if (call.userData().blocks.remove(call.parameters.getOrFail("id")) == null) {
                return@delete call.respondText("Not Found", status = HttpStatusCode.NotFound)
            }
            call.respondText("Block removed", status = HttpStatusCode.Accepted)
        }
    }
}
