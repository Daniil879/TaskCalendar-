package ru.university.taskcalendar.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {

    @Insert
    suspend fun insert(task: Task): Long

    @Update
    suspend fun update(task: Task)

    @Delete
    suspend fun delete(task: Task)

    @Query("SELECT * FROM tasks ORDER BY date ASC, time ASC")
    fun allTasks(): Flow<List<Task>>

    @Query("SELECT * FROM tasks WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): Task?

    @Query("SELECT * FROM tasks WHERE date = :date ORDER BY time ASC")
    suspend fun getByDate(date: String): List<Task>

    @Query("UPDATE tasks SET isDone = :isDone WHERE id = :id")
    suspend fun setDone(id: Long, isDone: Boolean)

    @Query("SELECT COUNT(*) FROM tasks")
    suspend fun count(): Int
}