package ru.university.taskcalendar

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.text.format.DateFormat
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.launch
import ru.university.taskcalendar.data.Task
import ru.university.taskcalendar.ui.TaskViewModel
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

class AddEditTaskActivity : AppCompatActivity() {

    private val viewModel: TaskViewModel by viewModels { TaskViewModel.Factory }

    private val taskId: Long by lazy {
        intent.getLongExtra(EXTRA_TASK_ID, NEW_TASK_ID)
    }

    private val isEditMode: Boolean get() = taskId != NEW_TASK_ID

    private lateinit var tilTitle: TextInputLayout
    private lateinit var tilDate: TextInputLayout
    private lateinit var tilTime: TextInputLayout
    private lateinit var etTitle: TextInputEditText
    private lateinit var etDescription: TextInputEditText
    private lateinit var etDate: TextInputEditText
    private lateinit var etTime: TextInputEditText
    private lateinit var btnSave: Button
    private lateinit var btnDelete: Button

    private var currentTask: Task? = null

    private var prefillDone: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_edit_task)

        prefillDone = savedInstanceState?.getBoolean(STATE_PREFILLED, false) ?: false

        bindViews()
        setupToolbar()
        setupDateAndTimePickers()
        setupValidation()
        setupButtons()

        if (isEditMode) {
            viewModel.loadTask(taskId)
        }

        observeTask()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(STATE_PREFILLED, prefillDone)
    }

    private fun bindViews() {
        tilTitle = findViewById(R.id.tilTitle)
        tilDate = findViewById(R.id.tilDate)
        tilTime = findViewById(R.id.tilTime)
        etTitle = findViewById(R.id.etTitle)
        etDescription = findViewById(R.id.etDescription)
        etDate = findViewById(R.id.etDate)
        etTime = findViewById(R.id.etTime)
        btnSave = findViewById(R.id.btnSave)
        btnDelete = findViewById(R.id.btnDelete)
    }

    private fun setupToolbar() {
        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        toolbar.setNavigationOnClickListener { finish() }
        toolbar.setTitle(
            if (isEditMode) R.string.screen_edit_task_title else R.string.screen_add_task_title
        )
    }

    private fun setupDateAndTimePickers() {

        etDate.setOnClickListener {
            val base = parseDate(etDate.text?.toString().orEmpty()) ?: LocalDate.now()
            DatePickerDialog(
                this,
                { _, year, month, day ->
                    val picked = LocalDate.of(year, month + 1, day)
                    etDate.setText(picked.format(DATE_FORMAT))
                    tilDate.error = null
                },
                base.year,
                base.monthValue - 1,
                base.dayOfMonth
            ).show()
        }

        etTime.setOnClickListener {
            val base = parseTime(etTime.text?.toString().orEmpty()) ?: LocalTime.now()
            TimePickerDialog(
                this,
                { _, hour, minute ->
                    val picked = LocalTime.of(hour, minute)
                    etTime.setText(picked.format(TIME_FORMAT))
                    tilTime.error = null
                },
                base.hour,
                base.minute,
                DateFormat.is24HourFormat(this)   // уважаем настройку системы
            ).show()
        }
    }


    private fun setupValidation() {
        etTitle.doAfterTextChanged { tilTitle.error = null }
        etTime.doAfterTextChanged {
            if (etTime.text?.isNotEmpty() == true) tilTime.error = null
        }
    }
    private fun validate(): Boolean {
        var valid = true

        val title = etTitle.text?.toString()?.trim().orEmpty()
        if (title.isEmpty()) {
            tilTitle.error = getString(R.string.error_title_required)
            valid = false
        } else {
            tilTitle.error = null
        }

        val dateRaw = etDate.text?.toString()?.trim().orEmpty()
        when {
            dateRaw.isEmpty() -> {
                tilDate.error = getString(R.string.error_date_required)
                valid = false
            }
            parseDate(dateRaw) == null -> {
                tilDate.error = getString(R.string.error_date_invalid)
                valid = false
            }
            else -> tilDate.error = null
        }

        val timeRaw = etTime.text?.toString()?.trim().orEmpty()
        if (timeRaw.isNotEmpty() && parseTime(timeRaw) == null) {
            tilTime.error = getString(R.string.error_time_invalid)
            valid = false
        } else {
            tilTime.error = null
        }

        if (!valid) {
            if (tilTitle.error != null) etTitle.requestFocus()
            else if (tilDate.error != null) etDate.requestFocus()
            else etTime.requestFocus()
        }

        return valid
    }


    private fun setupButtons() {
        if (isEditMode) {
            btnDelete.visibility = View.VISIBLE
            btnDelete.setOnClickListener { confirmDelete() }
        }

        btnSave.setOnClickListener {
            if (!validate()) return@setOnClickListener

            val title = etTitle.text?.toString()?.trim().orEmpty()
            val description = etDescription.text?.toString()?.trim().orEmpty()
            val date = etDate.text?.toString()?.trim().orEmpty()
            val time = etTime.text?.toString()?.trim().orEmpty()

            btnSave.isEnabled = false

            if (isEditMode) {
                val existing = currentTask
                if (existing == null) {
                    Toast.makeText(this, R.string.task_not_found, Toast.LENGTH_SHORT).show()
                    btnSave.isEnabled = true
                    return@setOnClickListener
                }
                viewModel.updateTask(
                    existing.copy(
                        title = title,
                        description = description,
                        date = date,
                        time = time
                    )
                ) {
                    Toast.makeText(this, R.string.task_updated, Toast.LENGTH_SHORT).show()
                    finish()
                }
            } else {
                viewModel.addTask(title, description, date, time) {
                    Toast.makeText(this, R.string.task_saved, Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
        }
    }

    private fun confirmDelete() {
        val task = currentTask ?: return
        AlertDialog.Builder(this)
            .setTitle(R.string.delete_confirm_title)
            .setMessage(getString(R.string.delete_confirm_message, task.title))
            .setPositiveButton(R.string.delete) { _, _ ->
                viewModel.deleteTask(task) {
                    Toast.makeText(this, R.string.task_deleted, Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private fun observeTask() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.currentTask.collect { task ->
                    currentTask = task
                    if (task != null && !prefillDone) {
                        prefillDone = true
                        fillForm(task)
                    }
                }
            }
        }
    }

    private fun fillForm(task: Task) {
        etTitle.setText(task.title)
        etDescription.setText(task.description)
        etDate.setText(task.date)
        etTime.setText(task.time)
    }

    companion object {
        const val EXTRA_TASK_ID = "extra_task_id"
        const val NEW_TASK_ID = -1L

        private const val STATE_PREFILLED = "state_prefilled"

        private val DATE_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")
        private val TIME_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")

        fun parseDate(raw: String): LocalDate? = try {
            if (raw.isBlank()) null else LocalDate.parse(raw, DATE_FORMAT)
        } catch (e: DateTimeParseException) {
            null
        }
        fun parseTime(raw: String): LocalTime? = try {
            if (raw.isBlank()) null else LocalTime.parse(raw, TIME_FORMAT)
        } catch (e: DateTimeParseException) {
            null
        }
    }
}