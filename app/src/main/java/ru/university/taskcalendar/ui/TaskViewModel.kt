package ru.university.taskcalendar.ui
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ru.university.taskcalendar.TaskCalendarApp
import ru.university.taskcalendar.data.Task
import ru.university.taskcalendar.data.TaskRepository
class TaskViewModel(private val repository: TaskRepository) : ViewModel() {
    val tasks: StateFlow<List<Task>> = repository.allTasks
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000L),
            initialValue = emptyList()
        )

    private val _currentTask = MutableStateFlow<Task?>(null)
    val currentTask: StateFlow<Task?> = _currentTask.asStateFlow()

    private var loadedTaskId: Long? = null
    fun loadTask(id: Long) {
        if (loadedTaskId == id) return
        loadedTaskId = id
        viewModelScope.launch {
            _currentTask.value = withContext(Dispatchers.IO) { repository.getById(id) }
        }
    }

    fun addTask(
        title: String,
        description: String,
        date: String,
        time: String,
        onSaved: () -> Unit = {}
    ) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                repository.add(Task(title = title, description = description, date = date, time = time))
            }
            onSaved()
        }
    }

    fun updateTask(task: Task, onUpdated: () -> Unit = {}) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) { repository.update(task) }
            if (loadedTaskId == task.id) _currentTask.value = task
            onUpdated()
        }
    }

    fun deleteTask(task: Task, onDeleted: () -> Unit = {}) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) { repository.delete(task) }
            if (loadedTaskId == task.id) {
                _currentTask.value = null
                loadedTaskId = null
            }
            onDeleted()
        }
    }

    fun toggleDone(task: Task) {
        val newValue = !task.isDone
        viewModelScope.launch {
            withContext(Dispatchers.IO) { repository.setDone(task.id, newValue) }
            if (loadedTaskId == task.id) _currentTask.value = task.copy(isDone = newValue)
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY]
                        as TaskCalendarApp
                TaskViewModel(app.repository)
            }
        }
    }
}