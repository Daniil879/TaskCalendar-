package ru.university.taskcalendar.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {

    // ---------------------------------------------------------- Запись

    @Insert
    suspend fun insert(task: Task): Long

    @Insert
    suspend fun insertAll(tasks: List<Task>)

    @Update
    suspend fun update(task: Task)

    @Delete
    suspend fun delete(task: Task)

    @Query("DELETE FROM tasks WHERE id = :id")
    suspend fun deleteById(id: Long)

    // ---------------------------------------------------------- Чтение

    @Query("SELECT * FROM tasks ORDER BY date ASC, time ASC, id ASC")
    fun allTasks(): Flow<List<Task>>

    @Query("SELECT * FROM tasks WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): Task?

    @Query("SELECT * FROM tasks WHERE date = :date ORDER BY time ASC, id ASC")
    fun observeTasksByDate(date: String): Flow<List<Task>>

    @Query("SELECT * FROM tasks WHERE date = :date ORDER BY time ASC, id ASC")
    suspend fun getTasksByDate(date: String): List<Task>

    @Query(
        "SELECT date, COUNT(*) AS count FROM tasks " +
                "WHERE date BETWEEN :startDate AND :endDate " +
                "GROUP BY date"
    )
    fun observeDayCounts(startDate: String, endDate: String): Flow<List<DayTaskCount>>

    @Query("SELECT COUNT(*) FROM tasks WHERE date = :date")
    suspend fun countOnDate(date: String): Int

    @Query("UPDATE tasks SET isDone = :isDone WHERE id = :id")
    suspend fun setDone(id: Long, isDone: Boolean)

    @Query("SELECT COUNT(*) FROM tasks")
    suspend fun count(): Int
}