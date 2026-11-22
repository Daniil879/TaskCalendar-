package ru.university.taskcalendar.util

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

private val ISO_FORMAT: DateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE
private val DISPLAY_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")
private val TIME_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")
private val MONTH_TITLE_FORMAT: DateTimeFormatter =
    DateTimeFormatter.ofPattern("LLLL yyyy", Locale.forLanguageTag("ru"))
private val FULL_DATE_FORMAT: DateTimeFormatter =
    DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.forLanguageTag("ru"))

fun LocalDate.toIso(): String = format(ISO_FORMAT)

fun LocalDate.toDisplay(): String = format(DISPLAY_FORMAT)

fun LocalDate.toMonthTitle(): String =
    format(MONTH_TITLE_FORMAT).replaceFirstChar { it.titlecase(Locale.forLanguageTag("ru")) }

fun LocalDate.toFullDateTitle(): String = format(FULL_DATE_FORMAT)

fun String.parseIsoDate(): LocalDate? =
    runCatching { LocalDate.parse(this, ISO_FORMAT) }.getOrNull()

fun String.parseDisplayDate(): LocalDate? =
    runCatching { LocalDate.parse(this, DISPLAY_FORMAT) }.getOrNull()

fun String.displayToIsoOrNull(): String? = parseDisplayDate()?.toIso()

fun String.isoToDisplayOrNull(): String? = parseIsoDate()?.toDisplay()

fun String.parseTimeOrNull(): java.time.LocalTime? =
    runCatching { java.time.LocalTime.parse(this, TIME_FORMAT) }.getOrNull()