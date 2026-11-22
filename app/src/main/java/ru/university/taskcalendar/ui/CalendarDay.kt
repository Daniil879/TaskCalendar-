package ru.university.taskcalendar.ui

import java.time.LocalDate
import java.time.YearMonth

data class CalendarDay(
    val date: LocalDate,
    val inCurrentMonth: Boolean,
    val taskCount: Int,
    val isToday: Boolean
)

fun buildMonthGrid(
    month: YearMonth,
    counts: Map<LocalDate, Int>,
    today: LocalDate = LocalDate.now()
): List<CalendarDay> {
    val firstDay = month.atDay(1)

    // DayOfWeek.MONDAY.value == 1 → 0 пустых; SUNDAY.value == 7 → 6 пустых.
    val leadingEmpty = firstDay.dayOfWeek.value - 1
    val gridStart = firstDay.minusDays(leadingEmpty.toLong())

    val cellsNeeded = leadingEmpty + month.lengthOfMonth()
    val weeks = (cellsNeeded + 6) / 7          // округление вверх до целой недели
    val totalCells = weeks * 7

    return (0 until totalCells).map { index ->
        val date = gridStart.plusDays(index.toLong())
        CalendarDay(
            date = date,
            inCurrentMonth = date.year == month.year && date.monthValue == month.monthValue,
            taskCount = counts[date] ?: 0,
            isToday = date == today
        )
    }
}