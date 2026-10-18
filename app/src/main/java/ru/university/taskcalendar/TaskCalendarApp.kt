package ru.university.taskcalendar

import android.app.Application
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import ru.university.taskcalendar.data.AppDatabase
import ru.university.taskcalendar.data.TaskRepository
class TaskCalendarApp : Application() {

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    val repository: TaskRepository by lazy {
        TaskRepository(AppDatabase.getInstance(this).taskDao())
    }

    override fun onCreate() {
        super.onCreate()
        // Демо-данные (сдаётся скриншотом Logcat). Идемпотентно:
        // если база не пуста — ничего не произойдёт.
        applicationScope.launch { repository.seedDemoDataIfEmpty() }
    }
}