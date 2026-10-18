package ru.university.taskcalendar

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.launch
import ru.university.taskcalendar.ui.TaskAdapter
import ru.university.taskcalendar.ui.TaskViewModel

class MainActivity : AppCompatActivity() {

    private val viewModel: TaskViewModel by viewModels { TaskViewModel.Factory }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val recyclerTasks = findViewById<RecyclerView>(R.id.recyclerTasks)
        val tvEmpty = findViewById<View>(R.id.tvEmpty)
        val fabAddTask = findViewById<FloatingActionButton>(R.id.fabAddTask)

        val adapter = TaskAdapter { task -> openTaskDetail(task.id) }
        recyclerTasks.layoutManager = LinearLayoutManager(this)
        recyclerTasks.adapter = adapter

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.tasks.collect { tasks ->
                    adapter.submitList(tasks)
                    tvEmpty.visibility = if (tasks.isEmpty()) View.VISIBLE else View.GONE
                }
            }
        }

        fabAddTask.setOnClickListener {
            startActivity(Intent(this, AddTaskActivity::class.java))
        }
    }

    private fun openTaskDetail(taskId: Long) {
        val intent = Intent(this, TaskDetailActivity::class.java)
        intent.putExtra(TaskDetailActivity.EXTRA_TASK_ID, taskId)
        startActivity(intent)
    }
}