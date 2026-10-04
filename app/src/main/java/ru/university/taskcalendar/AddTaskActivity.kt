package ru.university.taskcalendar

import android.os.Bundle
import android.text.TextUtils
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.appbar.MaterialToolbar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ru.university.taskcalendar.data.AppDatabase
import ru.university.taskcalendar.data.Task
import ru.university.taskcalendar.data.TaskRepository

class AddTaskActivity : AppCompatActivity() {

    private lateinit var repository: TaskRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_task)

        repository = TaskRepository(AppDatabase.getInstance(this).taskDao())

        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        toolbar.setNavigationOnClickListener { finish() }

        val etTitle = findViewById<EditText>(R.id.etTitle)
        val etDescription = findViewById<EditText>(R.id.etDescription)
        val etDate = findViewById<EditText>(R.id.etDate)
        val etTime = findViewById<EditText>(R.id.etTime)
        val btnSave = findViewById<Button>(R.id.btnSave)

        btnSave.setOnClickListener {
            val title = etTitle.text.toString().trim()
            val description = etDescription.text.toString().trim()
            val date = etDate.text.toString().trim()
            val time = etTime.text.toString().trim()

            if (TextUtils.isEmpty(title) || TextUtils.isEmpty(date)) {
                Toast.makeText(this, R.string.form_error, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }


            lifecycleScope.launch {
                withContext(Dispatchers.IO) {
                    repository.add(
                        Task(
                            title = title,
                            description = description,
                            date = date,
                            time = time
                        )
                    )
                }
                Toast.makeText(this@AddTaskActivity, R.string.task_saved, Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }
}