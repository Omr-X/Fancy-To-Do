package net.omar.fancytodo.storage

import java.io.File
import java.util.concurrent.ConcurrentHashMap
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json

private val storageJson = Json { prettyPrint = true; ignoreUnknownKeys = true; encodeDefaults = true }

/** Folder holding all saved data; the `fancytodo.dataDir` system property overrides it (used by tests). */
internal fun dataDir() = File(System.getProperty("fancytodo.dataDir") ?: "data")

private val lastWritten = ConcurrentHashMap<String, String>()

/** Saves [value] as JSON, skipping the write when the file already holds exactly that. */
internal fun <T> writeJson(file: File, serializer: KSerializer<T>, value: T) {
    val text = storageJson.encodeToString(serializer, value)
    if (lastWritten[file.path] == text) return
    file.parentFile?.mkdirs()
    file.writeText(text)
    lastWritten[file.path] = text
}

internal fun <T> readJson(file: File, serializer: KSerializer<T>): T? {
    if (!file.exists()) return null
    return try {
        storageJson.decodeFromString(serializer, file.readText())
    } catch (e: Exception) {
        println("Could not load ${file.absolutePath}: ${e.message}")
        null
    }
}
