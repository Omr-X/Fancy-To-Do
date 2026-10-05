package net.omar.fancytodo.platform

@OptIn(ExperimentalWasmJsInterop::class)
@JsFun("() => window.location.hostname")
private external fun readPageHostname(): String

actual fun pageHostname(): String = readPageHostname()
