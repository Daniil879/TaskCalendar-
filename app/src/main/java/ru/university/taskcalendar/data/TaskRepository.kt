package ru.university.taskcalendar.data

import kotlinx.coroutines.flow.Flow

class TaskRepository(private val dao: TaskDao) {

    val allTasks: Flow<List<Task>> = dao.allTasks()

    suspend fun getById(id: Long): Task? = dao.getById(id)

    suspend fun getByDate(date: String): List<Task> = dao.getByDate(date)

    suspend fun add(task: Task): Long = dao.insert(task)

    suspend fun update(task: Task) = dao.update(task)

    suspend fun delete(task: Task) = dao.delete(task)

    suspend fun setDone(id: Long, isDone: Boolean) = dao.setDone(id, isDone)

    suspend fun count(): Int = dao.count()
}