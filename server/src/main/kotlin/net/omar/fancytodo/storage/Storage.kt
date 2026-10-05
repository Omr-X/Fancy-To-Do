package net.omar.fancytodo.storage

import java.io.File
import java.util.concurrent.ConcurrentHashMap
import kotlin.concurrent.thread
import net.omar.fancytodo.model.Area
import net.omar.fancytodo.model.Habit
import net.omar.fancytodo.model.Reminder
import net.omar.fancytodo.model.Snapshot
import net.omar.fancytodo.model.Task
import net.omar.fancytodo.model.Topic
import net.omar.fancytodo.model.WorkBlock

/** One user's areas, topics, tasks, habits and work blocks, keyed by id. Saved to `users/<userId>.json`. */
class UserData {
    val areas = ConcurrentHashMap<String, Area>()
    val topics = ConcurrentHashMap<String, Topic>()
    val tasks = ConcurrentHashMap<String, Task>()
    val habits = ConcurrentHashMap<String, Habit>()
    val blocks = ConcurrentHashMap<String, WorkBlock>()
    var reminder = Reminder()

    /** Removes the topics' tasks and those tasks' work blocks (used when a topic or area is deleted). */
    fun removeTasksOf(topicIds: Collection<String>) {
        val taskIds = tasks.values.filter { it.topicId in topicIds }.map { it.id }.toSet()
        taskIds.forEach { tasks.remove(it) }
        blocks.values.removeAll { it.taskId in taskIds }
    }

    fun toSnapshot() = Snapshot(
        areas = areas.values.toList(),
        topics = topics.values.toList(),
        tasks = tasks.values.toList(),
        habits = habits.values.toList(),
        blocks = blocks.values.toList()
    )

    /** Adds everything in [snapshot] (entries with the same id are replaced). */
    fun addAll(snapshot: Snapshot) {
        snapshot.areas.forEach { areas[it.id] = it }
        snapshot.topics.forEach { topics[it.id] = it }
        snapshot.tasks.forEach { tasks[it.id] = it }
        snapshot.habits.forEach { habits[it.id] = it }
        snapshot.blocks.forEach { blocks[it.id] = it }
    }

    fun clear() {
        areas.clear()
        topics.clear()
        tasks.clear()
        habits.clear()
        blocks.clear()
    }
}

/** Store used while nobody is logged in. */
const val GUEST_ID = "guest"

private val userStores = ConcurrentHashMap<String, UserData>()

// Guest data lives in the file used before accounts existed, so earlier data shows up for guests.
private fun userFile(userId: String) =
    if (userId == GUEST_ID) File(dataDir(), "fancytodo.json") else File(dataDir(), "users/$userId.json")

/** The data of [userId] (or [GUEST_ID]), loaded from disk the first time it's needed. */
fun dataOf(userId: String): UserData = userStores.computeIfAbsent(userId) {
    UserData().apply { readJson(userFile(userId), Snapshot.serializer())?.let { addAll(it) } }
}

/** Moves everything made as a guest into [userId]'s account. */
fun moveGuestDataTo(userId: String) {
    val guest = dataOf(GUEST_ID)
    dataOf(userId).addAll(guest.toSnapshot())
    guest.clear()
}

fun saveUserData() {
    userStores.forEach { (userId, data) -> writeJson(userFile(userId), Snapshot.serializer(), data.toSnapshot()) }
}

/** Saves all data whenever it changes (checked every 1.5 s) and once more on shutdown. */
fun startAutosave() {
    val saveAll = {
        saveUserData()
        saveAccounts()
    }
    Runtime.getRuntime().addShutdownHook(thread(start = false) { saveAll() })
    thread(isDaemon = true) {
        while (true) {
            Thread.sleep(1500)
            saveAll()
        }
    }
}
