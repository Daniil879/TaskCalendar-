package ru.university.taskcalendar.data

import androidx.room.Entity
import androidx.room.PrimaryKey
@Entity(tableName = "tasks")
data class Task(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,

    val title: String,
    val description: String,
    val date: String,        // "2026-11-22"  ← ISO, НЕ "22.11.2026"
    val time: String = "",   // "14:00"
    val isDone: Boolean = false
)