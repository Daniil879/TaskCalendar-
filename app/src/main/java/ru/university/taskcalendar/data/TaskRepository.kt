package ru.university.taskcalendar.data
import android.util.Log
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onEach
class TaskRepository(private val dao: TaskDao) {
    val allTasks: Flow<List<Task>> = dao.allTasks()
        .onEach { Log.d("DB_QUERY", "Запрос в базу: ${it.size} задач") }

    suspend fun getById(id: Long): Task? = dao.getById(id)

    suspend fun getByDate(date: String): List<Task> = dao.getByDate(date)

    suspend fun add(task: Task): Long = dao.insert(task)

    suspend fun update(task: Task) = dao.update(task)

    suspend fun delete(task: Task) = dao.delete(task)

    suspend fun setDone(id: Long, isDone: Boolean) = dao.setDone(id, isDone)

    suspend fun count(): Int = dao.count()
    suspend fun seedDemoDataIfEmpty() {
        if (dao.count() > 0) return

        Log.d(TAG, "База пуста — заполняю демо-данными")
        demo.forEach { dao.insert(it) }
        Log.d(TAG, "В базе теперь ${dao.count()} задач:")
        demo.forEach {
            Log.d(TAG, "  #${it.id} ${it.date} — ${it.title} (done=${it.isDone})")
        }
    }

    private companion object {
        const val TAG = "DB_SEED"

        val demo = listOf(
            Task(
                id = 1L,
                title = "Сходить в продуктовый магазин",
                description = "молоко (2 л), яйца (1 десяток), хлеб цельнозерновой (1 буханка), бананы (1 кг), куриная грудка (500 г), огурцы (3 шт.), гречка (1 упаковка).",
                date = "28.09.2026",
                isDone = false
            ),
            Task(
                id = 2L,
                title = "Забрать заказ из пункта выдачи",
                description = "заказ №123456, кроссовки и футболка; проверить целостность упаковки и размер при получении.",
                date = "30.09.2026",
                isDone = false
            ),
            Task(
                id = 3L,
                title = "Записаться к стоматологу и подтвердить визит",
                description = "зайти в приложение клиники, выбрать свободное окно на следующую неделю, записать дату и время в календарь, поставить напоминание за день.",
                date = "02.10.2026",
                isDone = true
            ),
            Task(
                id = 4L,
                title = "Купить канцелярию для работы",
                description = "блокнот на 96 листов, набор ручек (синяя, чёрная, красная), маркеры-текстовыделители (4 цвета), папка-скоросшиватель, стикеры.",
                date = "05.10.2026",
                isDone = false
            ),
            Task(
                id = 5L,
                title = "Подготовить продукты для ужина",
                description = "филе лосося (400 г), брокколи (300 г), лимон (1 шт.), оливковое масло, специи. Дома: промыть и обсушить рыбу, нарезать овощи, разложить по контейнерам, чтобы вечером быстро запечь.",
                date = "07.10.2026",
                isDone = false
            )
        )
    }
}