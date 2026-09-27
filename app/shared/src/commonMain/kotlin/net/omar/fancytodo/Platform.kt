package net.omar.fancytodo

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform