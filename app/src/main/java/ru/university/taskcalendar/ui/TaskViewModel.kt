package ru.university.taskcalendar.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ru.university.taskcalendar.TaskCalendarApp
import ru.university.taskcalendar.data.Task
import ru.university.taskcalendar.data.TaskRepository
import ru.university.taskcalendar.util.parseIsoDate
import ru.university.taskcalendar.util.toIso
import java.time.LocalDate
import java.time.YearMonth

@OptIn(ExperimentalCoroutinesApi::class)
class TaskViewModel(private val repository: TaskRepository) : ViewModel() {


    private val _selectedDate = MutableStateFlow(LocalDate.now())
    val selectedDate: StateFlow<LocalDate> = _selectedDate.asStateFlow()

    private val _visibleMonth = MutableStateFlow(YearMonth.now())
    val visibleMonth: StateFlow<YearMonth> = _visibleMonth.asStateFlow()

    val tasksForSelectedDate: StateFlow<List<Task>> = _selectedDate
        .flatMapLatest { date -> repository.observeTasksByDate(date.toIso()) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000L),
            initialValue = emptyList()
        )


    private val dayCounts: StateFlow<Map<LocalDate, Int>> = _visibleMonth
        .flatMapLatest { month ->
            repository.observeDayCounts(
                startIso = month.atDay(1).toIso(),
                endIso = month.atEndOfMonth().toIso()
            )
        }
        .map { rows ->
            rows.mapNotNull { row -> row.date.parseIsoDate()?.let { it to row.count } }.toMap()
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000L),
            initialValue = emptyMap()
        )

    val calendarDays: StateFlow<List<CalendarDay>> =
        combine(_visibleMonth, dayCounts) { month, counts ->
            buildMonthGrid(month, counts)
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000L),
            initialValue = buildMonthGrid(YearMonth.now(), emptyMap())
        )


    fun selectDate(date: LocalDate) {
        _selectedDate.value = date
        val month = YearMonth.from(date)
        if (month != _visibleMonth.value) {
            _visibleMonth.value = month
        }
    }

    fun showPreviousMonth() {
        _visibleMonth.value = _visibleMonth.value.minusMonths(1)
    }

    fun showNextMonth() {
        _visibleMonth.value = _visibleMonth.value.plusMonths(1)
    }

    fun goToToday() {
        selectDate(LocalDate.now())
    }


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
        dateIso: String,
        time: String,
        onSaved: () -> Unit = {}
    ) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                repository.add(
                    Task(
                        title = title,
                        description = description,
                        date = dateIso,
                        time = time
                    )
                )
            }

            dateIso.parseIsoDate()?.let { selectDate(it) }
            onSaved()
        }
    }

    fun updateTask(task: Task, onUpdated: () -> Unit = {}) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) { repository.update(task) }
            if (loadedTaskId == task.id) _currentTask.value = task
            task.date.parseIsoDate()?.let { selectDate(it) }
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
            if (loadedTaskId == task.id) {
                _currentTask.value = task.copy(isDone = newValue)
            }
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