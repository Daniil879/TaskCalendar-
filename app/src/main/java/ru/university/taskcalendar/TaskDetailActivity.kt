package ru.university.taskcalendar

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.appbar.MaterialToolbar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ru.university.taskcalendar.data.AppDatabase
import ru.university.taskcalendar.data.TaskRepository

class TaskDetailActivity : AppCompatActivity() {

    private lateinit var repository: TaskRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_task_detail)

        repository = TaskRepository(AppDatabase.getInstance(this).taskDao())

        findViewById<MaterialToolbar>(R.id.toolbar)
            .setNavigationOnClickListener { finish() }

        val taskId = intent.getLongExtra(EXTRA_TASK_ID, -1L)

        val tvTitle = findViewById<TextView>(R.id.tvDetailTitle)
        val tvDescription = findViewById<TextView>(R.id.tvDetailDescription)
        val tvDate = findViewById<TextView>(R.id.tvDetailDate)
        val tvStatus = findViewById<TextView>(R.id.tvDetailStatus)
        val tvId = findViewById<TextView>(R.id.tvDetailId)

        lifecycleScope.launch {
            val task = withContext(Dispatchers.IO) { repository.getById(taskId) }
            if (task == null) {
                tvTitle.text = getString(R.string.detail_not_found)
                tvDescription.text = ""
                tvDate.text = ""
                tvStatus.text = ""
                tvId.text = getString(R.string.detail_id, taskId)
                return@launch
            }
            tvTitle.text = task.title
            tvDescription.text = task.description
            tvDate.text = if (task.time.isBlank()) task.date else "${task.date} · ${task.time}"
            tvStatus.text = getString(
                if (task.isDone) R.string.task_status_done else R.string.task_status_pending
            )
            tvId.text = getString(R.string.detail_id, task.id)
        }
    }

    companion object {
        const val EXTRA_TASK_ID = "extra_task_id"
    }
}