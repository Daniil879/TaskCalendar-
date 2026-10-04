package ru.university.taskcalendar

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ru.university.taskcalendar.data.AppDatabase
import ru.university.taskcalendar.data.Task
import ru.university.taskcalendar.data.TaskRepository
import ru.university.taskcalendar.ui.TaskAdapter

class MainActivity : AppCompatActivity() {

    private lateinit var recyclerTasks: RecyclerView
    private lateinit var tvEmpty: View
    private lateinit var fabAddTask: FloatingActionButton
    private lateinit var adapter: TaskAdapter
    private lateinit var repository: TaskRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        recyclerTasks = findViewById(R.id.recyclerTasks)
        tvEmpty = findViewById(R.id.tvEmpty)
        fabAddTask = findViewById(R.id.fabAddTask)

        repository = TaskRepository(AppDatabase.getInstance(this).taskDao())

        adapter = TaskAdapter { task -> openTaskDetail(task.id) }
        recyclerTasks.layoutManager = LinearLayoutManager(this)
        recyclerTasks.adapter = adapter

        lifecycleScope.launch {
            repository.allTasks
                .flowWithLifecycle(lifecycle, Lifecycle.State.STARTED)
                .collect { tasks ->
                    adapter.submitList(tasks)
                    updateEmptyState(tasks.isEmpty())
                }
        }

        lifecycleScope.launch { seedIfEmpty() }

        fabAddTask.setOnClickListener {
            startActivity(Intent(this, AddTaskActivity::class.java))
        }
    }

    private suspend fun seedIfEmpty() {
        withContext(Dispatchers.IO) {
            if (repository.count() == 0) {
                Log.d("DB_SEED", "База пуста — заполняю демо-данными")
                val demo = listOf(
                    Task(title = "Купить продукты на неделю", description = "Молоко, хлеб, яйца, овощи, фрукты", date = "05.10.2026", time = "18:30"),
                    Task(title = "Сходить в спортзал", description = "Тренировка: кардио + силовая", date = "06.10.2026", time = "19:00"),
                    Task(title = "Позвонить бабушке", description = "Узнать как дела, поздравить с праздником", date = "06.10.2026", time = "20:00"),
                    Task(title = "Записаться к стоматологу", description = "Плановый осмотр, раз в полгода", date = "07.10.2026", time = "10:00", isDone = true),
                    Task(title = "Оплатить коммунальные услуги", description = "Квартплата, электричество, интернет", date = "08.10.2026", time = "12:00"),
                    Task(title = "Встретиться с друзьями", description = "Кафе в центре, обсудить планы на выходные", date = "10.10.2026", time = "19:30"))
                demo.forEach { repository.add(it) }
            }
            repository.allTasks.collect { list ->
                Log.d("DB_SEED", "В базе ${list.size} задач:")
                list.forEach { Log.d("DB_SEED", "  #${it.id} ${it.date} ${it.time} — ${it.title} (done=${it.isDone})") }
                return@collect
            }
        }
    }

    private fun openTaskDetail(taskId: Long) {
        val intent = Intent(this, TaskDetailActivity::class.java)
        intent.putExtra(TaskDetailActivity.EXTRA_TASK_ID, taskId)
        startActivity(intent)
    }

    private fun updateEmptyState(isEmpty: Boolean) {
        tvEmpty.visibility = if (isEmpty) View.VISIBLE else View.GONE
    }
}