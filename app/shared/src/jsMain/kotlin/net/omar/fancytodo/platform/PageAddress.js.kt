package net.omar.fancytodo.platform

actual fun pageHostname(): String = js("window.location.hostname") as String
