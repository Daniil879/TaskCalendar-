package ru.university.taskcalendar.data

import androidx.room.Entity
import androidx.room.PrimaryKey
@Entity(tableName = "tasks")
data class Task(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,

    val title: String,
    val description: String,
    val date: String,        // "28.09.2026"
    val time: String = "",   // "14:00"
    val isDone: Boolean = false
)