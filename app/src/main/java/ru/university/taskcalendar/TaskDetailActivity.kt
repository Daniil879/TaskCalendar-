package ru.university.taskcalendar

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.appbar.MaterialToolbar
import kotlinx.coroutines.launch
import ru.university.taskcalendar.data.Task
import ru.university.taskcalendar.ui.TaskViewModel

class TaskDetailActivity : AppCompatActivity() {

    private val viewModel: TaskViewModel by viewModels { TaskViewModel.Factory }

    private val taskId: Long by lazy { intent.getLongExtra(EXTRA_TASK_ID, -1L) }

    private lateinit var tvTitle: TextView
    private lateinit var tvDescription: TextView
    private lateinit var tvDate: TextView
    private lateinit var tvStatus: TextView
    private lateinit var tvId: TextView
    private lateinit var btnToggle: Button
    private lateinit var btnDelete: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_task_detail)

        findViewById<MaterialToolbar>(R.id.toolbar)
            .setNavigationOnClickListener { finish() }

        tvTitle = findViewById(R.id.tvDetailTitle)
        tvDescription = findViewById(R.id.tvDetailDescription)
        tvDate = findViewById(R.id.tvDetailDate)
        tvStatus = findViewById(R.id.tvDetailStatus)
        tvId = findViewById(R.id.tvDetailId)
        btnToggle = findViewById(R.id.btnToggleDone)
        btnDelete = findViewById(R.id.btnDelete)

        btnToggle.isEnabled = false
        btnDelete.isEnabled = false

        btnToggle.setOnClickListener {
            viewModel.currentTask.value?.let { viewModel.toggleDone(it) }
        }

        btnDelete.setOnClickListener {
            val task = viewModel.currentTask.value ?: return@setOnClickListener
            btnDelete.isEnabled = false
            viewModel.deleteTask(task) { finish() }
        }

        viewModel.loadTask(taskId)

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.currentTask.collect { task -> renderTask(task) }
            }
        }
    }

    private fun renderTask(task: Task?) {
        if (task == null) {
            tvTitle.text = getString(R.string.detail_not_found)
            tvDescription.text = ""
            tvDate.text = ""
            tvStatus.text = ""
            tvId.text = getString(R.string.detail_id, taskId)
            btnToggle.isEnabled = false
            btnDelete.isEnabled = false
            return
        }

        tvTitle.text = task.title
        tvDescription.text = task.description
        tvDate.text = if (task.time.isBlank()) task.date else "${task.date} · ${task.time}"
        tvStatus.text = getString(
            if (task.isDone) R.string.task_status_done else R.string.task_status_pending
        )
        tvId.text = getString(R.string.detail_id, task.id)

        btnToggle.isEnabled = true
        btnToggle.text = getString(
            if (task.isDone) R.string.detail_mark_pending else R.string.detail_mark_done
        )
        btnDelete.isEnabled = true
    }

    companion object {
        const val EXTRA_TASK_ID = "extra_task_id"
    }
}