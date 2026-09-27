package com.example

import io.ktor.server.application.*
import io.ktor.server.http.content.*
import io.ktor.server.routing.*
import net.omar.fancytodo.bookRouting
import net.omar.fancytodo.chapterRouting
import net.omar.fancytodo.habitRoutes
import net.omar.fancytodo.taskRouting

fun Application.configureRouting() {
    routing {
        taskRouting()
        chapterRouting()
        bookRouting()
        habitRoutes()
        staticResources("/content", "mycontent")
    }
}