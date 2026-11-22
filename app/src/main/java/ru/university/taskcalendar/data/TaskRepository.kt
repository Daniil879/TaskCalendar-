package ru.university.taskcalendar.data

import android.util.Log
import kotlinx.coroutines.flow.Flow
import ru.university.taskcalendar.util.toIso
import java.time.LocalDate

class TaskRepository(private val dao: TaskDao) {

    val allTasks: Flow<List<Task>> = dao.allTasks()

    fun observeTasksByDate(dateIso: String): Flow<List<Task>> = dao.observeTasksByDate(dateIso)

    suspend fun getTasksByDate(dateIso: String): List<Task> = dao.getTasksByDate(dateIso)

    fun observeDayCounts(startIso: String, endIso: String): Flow<List<DayTaskCount>> =
        dao.observeDayCounts(startIso, endIso)

    suspend fun getById(id: Long): Task? = dao.getById(id)

    suspend fun add(task: Task): Long = dao.insert(task)

    suspend fun update(task: Task) = dao.update(task)

    suspend fun delete(task: Task) = dao.delete(task)

    suspend fun setDone(id: Long, isDone: Boolean) = dao.setDone(id, isDone)

    suspend fun count(): Int = dao.count()
    suspend fun seedDemoDataIfEmpty() {
        if (dao.count() > 0) return

        Log.d(TAG, "База пуста — заполняю демо-данными")

        val today = LocalDate.now()
        val demo = listOf(
            Task(
                title = "Сдать отчёт по этапу 6",
                description = "Календарь + список задач дня",
                date = today.toIso(),
                time = "12:00"
            ),
            Task(
                title = "Созвон с преподавателем",
                description = "Показать демо календаря",
                date = today.toIso(),
                time = "18:30"
            ),
            Task(
                title = "Спринт-ревью",
                description = "Обсудить прогресс команды",
                date = today.plusDays(1).toIso(),
                time = "10:00"
            ),
            Task(
                title = "Купить продукты",
                description = "Молоко, хлеб, кофе",
                date = today.plusDays(3).toIso(),
                time = "19:00"
            ),
            Task(
                title = "Тренировка",
                description = "Бассейн, 45 минут",
                date = today.minusDays(2).toIso(),
                time = "08:00",
                isDone = true
            ),
            Task(
                title = "Оплатить интернет",
                description = "До конца месяца",
                date = today.plusDays(10).toIso(),
                time = "09:00"
            )
        )

        dao.insertAll(demo)

        Log.d(TAG, "В базе ${dao.count()} задач:")
        dao.getTasksByDate(today.toIso()).forEach {
            Log.d(TAG, "  #${it.id} ${it.date} ${it.time} — ${it.title} (done=${it.isDone})")
        }
    }

    private companion object {
        const val TAG = "DB_SEED"
    }
}