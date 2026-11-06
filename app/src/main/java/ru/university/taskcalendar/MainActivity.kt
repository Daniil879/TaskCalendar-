package ru.university.taskcalendar

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.launch
import ru.university.taskcalendar.ui.SwipeToDeleteCallback
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

        val adapter = TaskAdapter { task -> openEditor(task.id) }
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

        attachSwipeToDelete(recyclerTasks, adapter)

        fabAddTask.setOnClickListener { openEditor(null) }
    }

    private fun openEditor(taskId: Long?) {
        val intent = Intent(this, AddEditTaskActivity::class.java)
        if (taskId != null) {
            intent.putExtra(AddEditTaskActivity.EXTRA_TASK_ID, taskId)
        }
        startActivity(intent)
    }

    private fun attachSwipeToDelete(recycler: RecyclerView, adapter: TaskAdapter) {
        val callback = SwipeToDeleteCallback { viewHolder, _ ->
            val position = viewHolder.bindingAdapterPosition
            if (position == RecyclerView.NO_POSITION) return@SwipeToDeleteCallback

            val task = adapter.itemAt(position)
            if (task == null) {
                adapter.notifyDataSetChanged()
                return@SwipeToDeleteCallback
            }

            AlertDialog.Builder(this)
                .setTitle(R.string.delete_confirm_title)
                .setMessage(getString(R.string.delete_confirm_message, task.title))
                .setPositiveButton(R.string.delete) { _, _ ->
                    viewModel.deleteTask(task) {
                    }
                }
                .setNegativeButton(R.string.cancel) { _, _ ->
                    restoreItem(adapter, position)
                }
                .setOnCancelListener {
                    restoreItem(adapter, position)
                }
                .show()
        }
        ItemTouchHelper(callback).attachToRecyclerView(recycler)
    }

    private fun restoreItem(adapter: TaskAdapter, position: Int) {
        if (position in 0 until adapter.itemCount) {
            adapter.notifyItemChanged(position)
        } else {
            adapter.notifyDataSetChanged()
        }
    }
}