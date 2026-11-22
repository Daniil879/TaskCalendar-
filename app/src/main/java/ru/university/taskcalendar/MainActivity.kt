package ru.university.taskcalendar

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import ru.university.taskcalendar.ui.CalendarDayAdapter
import ru.university.taskcalendar.ui.SwipeToDeleteCallback
import ru.university.taskcalendar.ui.TaskAdapter
import ru.university.taskcalendar.ui.TaskViewModel
import ru.university.taskcalendar.util.toFullDateTitle
import ru.university.taskcalendar.util.toIso
import ru.university.taskcalendar.util.toMonthTitle
import java.time.LocalDate

class MainActivity : AppCompatActivity() {

    private val viewModel: TaskViewModel by viewModels { TaskViewModel.Factory }

    private lateinit var tvMonthLabel: TextView
    private lateinit var tvSelectedDate: TextView
    private lateinit var tvEmptyDay: View
    private lateinit var calendarAdapter: CalendarDayAdapter
    private lateinit var dayTaskAdapter: TaskAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<MaterialToolbar>(R.id.toolbar).setTitle(R.string.screen_main_title)

        tvMonthLabel = findViewById(R.id.tvMonthLabel)
        tvSelectedDate = findViewById(R.id.tvSelectedDate)
        tvEmptyDay = findViewById(R.id.tvEmptyDay)

        val recyclerCalendar = findViewById<RecyclerView>(R.id.recyclerCalendar)
        val recyclerDayTasks = findViewById<RecyclerView>(R.id.recyclerDayTasks)
        val btnPrev = findViewById<ImageButton>(R.id.btnPrevMonth)
        val btnNext = findViewById<ImageButton>(R.id.btnNextMonth)
        val btnToday = findViewById<MaterialButton>(R.id.btnToday)
        val fab = findViewById<FloatingActionButton>(R.id.fabAddTask)

        calendarAdapter = CalendarDayAdapter { date -> viewModel.selectDate(date) }
        recyclerCalendar.layoutManager = GridLayoutManager(this, 7)
        recyclerCalendar.adapter = calendarAdapter
        recyclerCalendar.isNestedScrollingEnabled = false

        dayTaskAdapter = TaskAdapter { task -> openEditor(task.id) }
        recyclerDayTasks.layoutManager = LinearLayoutManager(this)
        recyclerDayTasks.adapter = dayTaskAdapter
        recyclerDayTasks.clipToPadding = false
        recyclerDayTasks.setPadding(0, 0, 0, 96)

        attachSwipeToDelete(recyclerDayTasks)

        btnPrev.setOnClickListener { viewModel.showPreviousMonth() }
        btnNext.setOnClickListener { viewModel.showNextMonth() }
        btnToday.setOnClickListener { viewModel.goToToday() }

        fab.setOnClickListener {
            openEditor(taskId = null, presetDate = viewModel.selectedDate.value)
        }

        observeUi()
    }

    private fun observeUi() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.visibleMonth.collect { month ->
                    tvMonthLabel.text = month.atDay(1).toMonthTitle()
                }
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                combine(viewModel.calendarDays, viewModel.selectedDate) { days, selected ->
                    days to selected
                }.collect { (days, selected) ->
                    calendarAdapter.submitList(days, selected)
                }
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.selectedDate.collect { date ->
                    tvSelectedDate.text = getString(
                        R.string.tasks_on_date,
                        date.toFullDateTitle()
                    )
                }
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.tasksForSelectedDate.collect { tasks ->
                    dayTaskAdapter.submitList(tasks)
                    tvEmptyDay.visibility = if (tasks.isEmpty()) View.VISIBLE else View.GONE
                }
            }
        }
    }


    private fun openEditor(taskId: Long?, presetDate: LocalDate? = null) {
        val intent = Intent(this, AddEditTaskActivity::class.java)
        if (taskId != null) {
            intent.putExtra(AddEditTaskActivity.EXTRA_TASK_ID, taskId)
        }
        if (presetDate != null) {
            intent.putExtra(AddEditTaskActivity.EXTRA_PRESET_DATE, presetDate.toIso())
        }
        startActivity(intent)
    }

    private fun attachSwipeToDelete(recycler: RecyclerView) {
        val callback = SwipeToDeleteCallback { viewHolder, _ ->
            val position = viewHolder.bindingAdapterPosition
            if (position == RecyclerView.NO_POSITION) return@SwipeToDeleteCallback

            val task = dayTaskAdapter.itemAt(position)
            if (task == null) {
                dayTaskAdapter.notifyDataSetChanged()
                return@SwipeToDeleteCallback
            }

            AlertDialog.Builder(this)
                .setTitle(R.string.delete_confirm_title)
                .setMessage(getString(R.string.delete_confirm_message, task.title))
                .setPositiveButton(R.string.delete) { _, _ ->
                    viewModel.deleteTask(task)
                }
                .setNegativeButton(R.string.cancel) { _, _ ->
                    restoreItem(position)
                }
                .setOnCancelListener { restoreItem(position) }
                .show()
        }
        ItemTouchHelper(callback).attachToRecyclerView(recycler)
    }

    private fun restoreItem(position: Int) {
        if (position in 0 until dayTaskAdapter.itemCount) {
            dayTaskAdapter.notifyItemChanged(position)
        } else {
            dayTaskAdapter.notifyDataSetChanged()
        }
    }
}