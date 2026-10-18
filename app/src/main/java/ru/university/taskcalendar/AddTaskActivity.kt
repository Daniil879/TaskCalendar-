package ru.university.taskcalendar

import android.os.Bundle
import android.text.TextUtils
import android.widget.Button
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.textfield.TextInputEditText
import ru.university.taskcalendar.ui.TaskViewModel

class AddTaskActivity : AppCompatActivity() {

    private val viewModel: TaskViewModel by viewModels { TaskViewModel.Factory }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_task)

        findViewById<MaterialToolbar>(R.id.toolbar)
            .setNavigationOnClickListener { finish() }

        val etTitle = findViewById<TextInputEditText>(R.id.etTitle)
        val etDescription = findViewById<TextInputEditText>(R.id.etDescription)
        val etDate = findViewById<TextInputEditText>(R.id.etDate)
        val etTime = findViewById<TextInputEditText>(R.id.etTime)
        val btnSave = findViewById<Button>(R.id.btnSave)

        btnSave.setOnClickListener {
            val title = etTitle.text?.toString()?.trim().orEmpty()
            val description = etDescription.text?.toString()?.trim().orEmpty()
            val date = etDate.text?.toString()?.trim().orEmpty()
            val time = etTime.text?.toString()?.trim().orEmpty()

            if (TextUtils.isEmpty(title) || TextUtils.isEmpty(date)) {
                Toast.makeText(this, R.string.form_error, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            btnSave.isEnabled = false
            viewModel.addTask(title, description, date, time) {
                Toast.makeText(this, R.string.task_saved, Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }
}