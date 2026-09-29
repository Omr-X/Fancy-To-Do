package net.omar.fancytodo

import kotlinx.serialization.json.Json
import java.io.File
import java.util.concurrent.ConcurrentHashMap
import kotlin.concurrent.thread

val taskStorage = ConcurrentHashMap<String, Task>()
val chaptersStorage = ConcurrentHashMap<String, Chapter>()
val booksStorage = ConcurrentHashMap<String, Book>()
val habitsStorage = ConcurrentHashMap<String, Habit>()

var reminder = Reminder()

private val json = Json { prettyPrint = true; ignoreUnknownKeys = true; encodeDefaults = true }
private val dataFile = File("data/fancytodo.json")

private fun snapshot() = json.encodeToString(
    Snapshot.serializer(),
    Snapshot(booksStorage.values.toList(), chaptersStorage.values.toList(), taskStorage.values.toList(), habitsStorage.values.toList())
)

private fun write(text: String) {
    dataFile.parentFile?.mkdirs()
    dataFile.writeText(text)
}

fun loadStorage() {
    if (!dataFile.exists()) return
    try {
        val data = json.decodeFromString(Snapshot.serializer(), dataFile.readText())
        data.books.forEach { booksStorage[it.id] = it }
        data.chapters.forEach { chaptersStorage[it.id] = it }
        data.tasks.forEach { taskStorage[it.id] = it }
        data.habits.forEach { habitsStorage[it.id] = it }
    } catch (e: Exception) {
        println("Could not load saved data from ${dataFile.absolutePath}: ${e.message}")
    }
}

fun startAutosave() {
    var last = snapshot()
    Runtime.getRuntime().addShutdownHook(thread(start = false) { write(snapshot()) })
    thread(isDaemon = true) {
        while (true) {
            Thread.sleep(1500)
            val current = snapshot()
            if (current != last) {
                write(current)
                last = current
            }
        }
    }
}
