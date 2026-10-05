@file:OptIn(ExperimentalWasmJsInterop::class)
package net.omar.fancytodo.platform

@JsFun("(key) => window.localStorage.getItem(key)")
private external fun getItem(key: String): String?

@JsFun("(key, value) => window.localStorage.setItem(key, value)")
private external fun setItem(key: String, value: String)

@JsFun("(key) => window.localStorage.removeItem(key)")
private external fun removeItem(key: String)

actual fun loadToken(): String? = getItem(TOKEN_STORAGE_KEY)

actual fun saveToken(token: String) = setItem(TOKEN_STORAGE_KEY, token)

actual fun clearToken() = removeItem(TOKEN_STORAGE_KEY)
