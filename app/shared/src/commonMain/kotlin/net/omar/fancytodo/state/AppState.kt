package net.omar.fancytodo.state

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import io.ktor.client.call.body
import io.ktor.client.plugins.ResponseException
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.http.HttpStatusCode
import kotlin.math.abs
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import net.omar.fancytodo.model.Area
import net.omar.fancytodo.model.Habit
import net.omar.fancytodo.model.PALETTE_SIZE
import net.omar.fancytodo.model.Task
import net.omar.fancytodo.model.Topic
import net.omar.fancytodo.model.WorkBlock
import net.omar.fancytodo.platform.currentTime
import net.omar.fancytodo.platform.todayIso
import net.omar.fancytodo.time.MINUTES_PER_DAY
import net.omar.fancytodo.time.addDays
import net.omar.fancytodo.time.epochDay

/**
 * Everything the app shows, kept in sync with the server: the logged-in user's data, or the shared
 * guest data when [token] is null. Changes are applied locally first so the UI reacts instantly, then
 * sent to the server; if a request fails the data is reloaded. [onSessionExpired] runs when the
 * server no longer accepts [token].
 */
internal class AppState(
    private val scope: CoroutineScope,
    token: String?,
    private val onSessionExpired: () -> Unit
) {
    private val client = createApiClient(token, throwOnError = true)

    var areas by mutableStateOf<List<Area>>(emptyList())
        private set
    var topics by mutableStateOf<List<Topic>>(emptyList())
        private set
    var tasks by mutableStateOf<List<Task>>(emptyList())
        private set
    var habits by mutableStateOf<List<Habit>>(emptyList())
        private set
    var blocks by mutableStateOf<List<WorkBlock>>(emptyList())
        private set
    var today by mutableStateOf(todayIso())
        private set
    var loading by mutableStateOf(true)
        private set
    var error by mutableStateOf<String?>(null)
        private set

    // ---- Loading and syncing ----

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
            areas = client.get("areas").body<List<Area>>()
            topics = client.get("topics").body<List<Topic>>()
            tasks = client.get("tasks").body<List<Task>>()
            habits = client.get("habits").body<List<Habit>>()
            blocks = client.get("blocks").body<List<WorkBlock>>()
            today = todayIso()
            error = null
        } catch (e: CancellationException) {
            throw e
        } catch (e: Throwable) {
            // Throwable, not Exception: on web a failed fetch surfaces as a JsError.
            if (!endedSession(e)) error = "Can't reach the server. Check your connection and try again."
        }
        loading = false
        syncWorkedTime()
    }

    /** Runs a server request in the background; on failure shows an error and reloads. */
    private fun send(request: suspend () -> Unit) {
        scope.launch {
            try {
                request()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Throwable) {
                if (endedSession(e)) return@launch
                error = "Couldn't save your last change."
                load()
            }
        }
    }

    /** True (and logs out) when the failure means the server no longer accepts the login token. */
    private fun endedSession(e: Throwable): Boolean {
        val unauthorized = e is ResponseException && e.response.status == HttpStatusCode.Unauthorized
        if (unauthorized) onSessionExpired()
        return unauthorized
    }

    // ---- Areas and topics ----

    fun topicsOf(areaId: String?) = topics.filter { it.areaId == areaId }

    /** First palette color no area uses yet; the default pick when creating an area. */
    fun nextAreaColor() = (0 until PALETTE_SIZE).firstOrNull { c -> areas.none { it.color == c } } ?: (areas.size % PALETTE_SIZE)

    fun addArea(title: String, color: Int = nextAreaColor()): Area {
        val area = Area(title = title.trim(), color = color)
        areas = areas + area
        send { client.post("areas") { jsonBody(area) } }
        return area
    }

    fun setAreaColor(area: Area, color: Int) {
        val updated = area.copy(color = color)
        areas = areas.map { if (it.id == area.id) updated else it }
        send { client.put("areas/${area.id}") { jsonBody(updated) } }
    }

    fun addTopic(areaId: String, title: String, color: Int? = null): Topic {
        val topic = Topic(title = title.trim(), areaId = areaId, color = color)
        topics = topics + topic
        send { client.post("areas/$areaId/topics") { jsonBody(topic) } }
        return topic
    }

    fun setTopicColor(topic: Topic, color: Int) {
        val updated = topic.copy(color = color)
        topics = topics.map { if (it.id == topic.id) updated else it }
        send { client.put("topics/${topic.id}") { jsonBody(updated) } }
    }

    /** A topic's own color, else its area's. */
    fun topicColorIndex(topic: Topic) = topic.color ?: areas.firstOrNull { it.id == topic.areaId }?.color ?: 0

    // ---- Task lookups ----

    fun taskById(id: String) = tasks.firstOrNull { it.id == id }

    fun topicOf(task: Task) = topics.firstOrNull { it.id == task.topicId }

    fun areaOf(task: Task) = topicOf(task)?.let { topic -> areas.firstOrNull { it.id == topic.areaId } }

    /** Color a task is shown in: its topic's color. */
    fun colorIndexOf(task: Task) = topicOf(task)?.let { topicColorIndex(it) } ?: 0

    fun childrenOf(id: String) = tasks.filter { it.parentTaskId == id }

    fun isParent(task: Task) = tasks.any { it.parentTaskId == task.id }

    fun tasksInTopic(topicId: String) = tasks.count { it.topicId == topicId }

    fun tasksInArea(areaId: String): Int {
        val topicIds = topicsOf(areaId).map { it.id }.toSet()
        return tasks.count { it.topicId in topicIds }
    }

    /** The task's own due date, or else the nearest ancestor's: subtasks inherit unless they set their own. */
    fun effectiveDue(task: Task): String? {
        val seen = mutableSetOf<String>()
        var cursor: Task? = task
        while (cursor != null && seen.add(cursor.id)) {
            cursor.dueDate?.let { return it }
            cursor = cursor.parentTaskId?.let { taskById(it) }
        }
        return null
    }

    /** Blocked while any of the tasks it waits for is unfinished. */
    fun isBlocked(task: Task) = task.blockedByIds.any { id -> id != task.id && taskById(id)?.isCompleted == false }

    // ---- Task changes ----

    fun saveTask(task: Task, isNew: Boolean) {
        val previousParent = if (isNew) null else taskById(task.id)?.parentTaskId
        tasks = if (isNew) tasks + task else tasks.map { if (it.id == task.id) task else it }
        send {
            if (isNew) client.post("topics/${task.topicId}/tasks") { jsonBody(task) }
            else client.put("tasks/${task.id}") { jsonBody(task) }
        }
        // A new or moved subtask can change whether its old and new parents count as done.
        val changed = mutableMapOf<String, Task>()
        reconcileParents(task.parentTaskId, changed)
        if (previousParent != task.parentTaskId) reconcileParents(previousParent, changed)
        saveChanged(changed.values)
    }

    /** Ticking a task ticks all its subtasks; parents then follow whether all their children are done. */
    fun toggleTask(task: Task) {
        if (isBlocked(task)) return
        val done = !task.isCompleted
        val changed = mutableMapOf<String, Task>()
        val now = nowInMinutes()
        fun cascade(t: Task) {
            if (t.id in changed) return
            // Finishing freezes the worked time at this moment; reopening lets the blocks count again.
            val worked = if (blocks.any { it.taskId == t.id }) elapsedMinutes(t, now) / 60.0 else t.loggedHours
            changed[t.id] = t.copy(isCompleted = done, loggedHours = worked)
            childrenOf(t.id).forEach { cascade(it) }
        }
        cascade(task)
        reconcileParents(task.parentTaskId, changed)
        saveChanged(changed.values)
    }

    fun deleteTask(id: String) {
        val parentId = taskById(id)?.parentTaskId
        tasks = tasks.filter { it.id != id }
        send { client.delete("tasks/$id") }
        blocks.filter { it.taskId == id }.forEach { deleteBlock(it.id) }
        // Detach subtasks and drop the deleted task from anything it was blocking.
        val detached = tasks.filter { it.parentTaskId == id || id in it.blockedByIds }.map { t ->
            t.copy(parentTaskId = t.parentTaskId.takeIf { it != id }, blockedByIds = t.blockedByIds - id)
        }
        saveChanged(detached)
        val changed = mutableMapOf<String, Task>()
        reconcileParents(parentId, changed)
        saveChanged(changed.values)
    }

    // Walks up from parentId, marking each parent done exactly when all of its children are done.
    private fun reconcileParents(parentId: String?, changed: MutableMap<String, Task>) {
        val seen = mutableSetOf<String>()
        var id = parentId
        while (id != null && seen.add(id)) {
            val parent = changed[id] ?: taskById(id) ?: return
            val children = childrenOf(id).map { changed[it.id] ?: it }
            val allDone = children.isNotEmpty() && children.all { it.isCompleted }
            if (parent.isCompleted == allDone) return
            changed[id] = parent.copy(isCompleted = allDone)
            id = parent.parentTaskId
        }
    }

    private fun saveChanged(updated: Collection<Task>) {
        if (updated.isEmpty()) return
        val byId = updated.associateBy { it.id }
        tasks = tasks.map { byId[it.id] ?: it }
        send { updated.forEach { t -> client.put("tasks/${t.id}") { jsonBody(t) } } }
    }

    // ---- Habits ----

    fun addHabit(title: String) {
        val habit = Habit(title = title.trim())
        habits = habits + habit
        send { client.post("habits") { jsonBody(habit) } }
    }

    fun toggleHabit(habit: Habit, date: String) {
        val doneThatDay = habit.completions[date] == true
        val updated = habit.copy(completions = if (doneThatDay) habit.completions - date else habit.completions + (date to true))
        habits = habits.map { if (it.id == habit.id) updated else it }
        send { client.put("habits/${habit.id}") { jsonBody(updated) } }
    }

    /** Consecutive days done, counting back from today (or yesterday if today isn't done yet). */
    fun streak(habit: Habit): Int {
        var day = if (habit.completions[today] == true) today else addDays(today, -1)
        var count = 0
        while (habit.completions[day] == true) {
            count++
            day = addDays(day, -1)
        }
        return count
    }

    // ---- Work blocks and the time they add up to ----

    fun blocksOn(date: String) = blocks.filter { it.date == date }.sortedBy { it.start }

    fun addBlock(taskId: String, date: String, start: Int, end: Int) {
        val block = WorkBlock(taskId = taskId, date = date, start = start, end = end)
        blocks = blocks + block
        send { client.post("blocks") { jsonBody(block) } }
    }

    fun updateBlock(block: WorkBlock) {
        blocks = blocks.map { if (it.id == block.id) block else it }
        send { client.put("blocks/${block.id}") { jsonBody(block) } }
        syncWorkedTime()
    }

    fun deleteBlock(id: String) {
        blocks = blocks.filter { it.id != id }
        send { client.delete("blocks/$id") }
        syncWorkedTime()
    }

    /**
     * Hours worked on [task]: the scheduled time that has passed. Once the task is done the value
     * is frozen (stored in loggedHours), so planned time after finishing doesn't count.
     */
    fun workedHours(task: Task): Double =
        if (task.isCompleted || blocks.none { it.taskId == task.id }) task.loggedHours else elapsedMinutes(task) / 60.0

    /** Stores the running worked time of unfinished tasks so other views (and the server) see it. */
    fun syncWorkedTime() {
        val now = nowInMinutes()
        val changed = tasks.filter { t -> !t.isCompleted && blocks.any { it.taskId == t.id } }.mapNotNull { t ->
            val hours = elapsedMinutes(t, now) / 60.0
            if (abs(hours - t.loggedHours) >= 1.0 / 120) t.copy(loggedHours = hours) else null
        }
        saveChanged(changed)
    }

    /** Minutes since 1970-01-01 00:00 local time; lets blocks on different days be compared. */
    private fun nowInMinutes(): Int {
        val now = currentTime()
        return epochDay(now.isoDate) * MINUTES_PER_DAY + now.minuteOfDay
    }

    /** Minutes of [task]'s blocks that have already gone by. */
    private fun elapsedMinutes(task: Task, now: Int = nowInMinutes()) = blocks.filter { it.taskId == task.id }.sumOf { block ->
        val start = epochDay(block.date) * MINUTES_PER_DAY + block.start
        (now - start).coerceIn(0, block.end - block.start)
    }
}
