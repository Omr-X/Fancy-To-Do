package net.omar.fancytodo.platform

// Keeps the login token in the browser (localStorage) so you stay logged in after a reload.

expect fun loadToken(): String?

expect fun saveToken(token: String)

expect fun clearToken()

internal const val TOKEN_STORAGE_KEY = "fancytodo.sessionToken"
