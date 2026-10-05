package net.omar.fancytodo.platform

private val localStorage: dynamic get() = js("window.localStorage")

actual fun loadToken(): String? = localStorage.getItem(TOKEN_STORAGE_KEY) as String?

actual fun saveToken(token: String) {
    localStorage.setItem(TOKEN_STORAGE_KEY, token)
}

actual fun clearToken() {
    localStorage.removeItem(TOKEN_STORAGE_KEY)
}
