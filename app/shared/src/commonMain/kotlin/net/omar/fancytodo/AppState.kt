package net.omar.fancytodo

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.js.Js
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

private inline fun <reified T> HttpRequestBuilder.jsonBody(body: T) {
    contentType(ContentType.Application.Json)
    setBody(body)
}

internal class AppState(private val scope: CoroutineScope) {
    private val client = HttpClient(Js) {
        install(ContentNegotiation) { json() }
        defaultRequest { host = "localhost"; port = 8080 }
    }

    var areas by mutableStateOf<List<Book>>(emptyList())
        private set
    var topics by mutableStateOf<List<Chapter>>(emptyList())
        private set
    var tasks by mutableStateOf<List<Task>>(emptyList())
        private set
    var habits by mutableStateOf<List<Habit>>(emptyList())
        private set
    var today by mutableStateOf(todayIso())
        private set
    var loading by mutableStateOf(true)
        private set
    var error by mutableStateOf<String?>(null)
        private set

    fun refresh() {
        scope.launch { load() }
    }

    fun syncToday() {
        today = todayIso()
    }

    fun dismissError() {
        error = null
    }

    private suspend fun load() {
        try {
            areas = client.get("books").body<List<Book>>()
            topics = client.get("chapters").body<List<Chapter>>()
            tasks = client.get("tasks").body<List<Task>>()
            habits = client.get("habits").body<List<Habit>>()
            today = todayIso()
            error = null
        } catch (e: Exception) {
            error = "Can't reach the server. Make sure it's running on port 8080."
        }
        loading = false
    }

    private fun act(block: suspend () -> Unit) {
        scope.launch {
            try {
                block()
            } catch (e: Exception) {
                error = "Couldn't save your last change."
                load()
            }
        }
    }

    fun topicFor(task: Task) = topics.firstOrNull { it.id == task.chapterId }

    fun areaFor(task: Task) = topicFor(task)?.let { topic -> areas.firstOrNull { it.id == topic.bookId } }

    fun topicsOf(areaId: String?) = topics.filter { it.bookId == areaId }

    fun streak(habit: Habit): Int {
        var day = if (habit.completions[today] == true) today else addDays(today, -1)
        var count = 0
        while (habit.completions[day] == true) {
            count++
            day = addDays(day, -1)
        }
        return count
    }

    fun toggleTask(task: Task) {
        val updated = task.copy(isCompleted = !task.isCompleted)
        tasks = tasks.map { if (it.id == task.id) updated else it }
        act { client.put("tasks/${task.id}") { jsonBody(updated) } }
    }

    fun saveTask(task: Task, isNew: Boolean) {
        tasks = if (isNew) tasks + task else tasks.map { if (it.id == task.id) task else it }
        act {
            if (isNew) client.post("chapters/${task.chapterId}/tasks") { jsonBody(task) }
            else client.put("tasks/${task.id}") { jsonBody(task) }
        }
    }

    fun deleteTask(id: String) {
        tasks = tasks.filter { it.id != id }
        act { client.delete("tasks/$id") }
    }

    fun addHabit(title: String) {
        val habit = Habit(title = title.trim())
        habits = habits + habit
        act { client.post("habits") { jsonBody(habit) } }
    }

    fun toggleHabit(habit: Habit, date: String) {
        val updated = habit.copy(
            completions = if (habit.completions[date] == true) habit.completions - date else habit.completions + (date to true)
        )
        habits = habits.map { if (it.id == habit.id) updated else it }
        act { client.put("habits/${habit.id}") { jsonBody(updated) } }
    }

    fun renameHabit(habit: Habit, title: String) {
        val updated = habit.copy(title = title.trim())
        habits = habits.map { if (it.id == habit.id) updated else it }
        act { client.put("habits/${habit.id}") { jsonBody(updated) } }
    }

    fun deleteHabit(id: String) {
        habits = habits.filter { it.id != id }
        act { client.delete("habits/$id") }
    }

    private fun nextColor() = (0 until AreaPalette.size).firstOrNull { c -> areas.none { it.color == c } } ?: (areas.size % AreaPalette.size)

    fun addArea(title: String): Book {
        val area = Book(title = title.trim(), color = nextColor())
        val general = Chapter(title = "General", bookId = area.id)
        areas = areas + area
        topics = topics + general
        act {
            client.post("books") { jsonBody(area) }
            client.post("books/${area.id}/chapters") { jsonBody(general) }
        }
        return area
    }

    private fun updateArea(updated: Book) {
        areas = areas.map { if (it.id == updated.id) updated else it }
        act { client.put("books/${updated.id}") { jsonBody(updated) } }
    }

    fun renameArea(area: Book, title: String) = updateArea(area.copy(title = title.trim()))

    fun recolorArea(area: Book) = updateArea(area.copy(color = (area.color + 1) % AreaPalette.size))

    fun deleteArea(id: String) {
        val gone = topics.filter { it.bookId == id }.map { it.id }
        areas = areas.filter { it.id != id }
        topics = topics.filter { it.bookId != id }
        tasks = tasks.filter { it.chapterId !in gone }
        act { client.delete("books/$id") }
    }

    fun addTopic(areaId: String, title: String): Chapter {
        val topic = Chapter(title = title.trim(), bookId = areaId)
        topics = topics + topic
        act { client.post("books/$areaId/chapters") { jsonBody(topic) } }
        return topic
    }

    fun deleteTopic(id: String) {
        topics = topics.filter { it.id != id }
        tasks = tasks.filter { it.chapterId != id }
        act { client.delete("chapters/$id") }
    }
}
