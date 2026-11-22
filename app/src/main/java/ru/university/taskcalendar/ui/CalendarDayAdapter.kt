package ru.university.taskcalendar.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import ru.university.taskcalendar.R
import java.time.LocalDate

class CalendarDayAdapter(
    private val onDayClick: (LocalDate) -> Unit
) : RecyclerView.Adapter<CalendarDayAdapter.DayViewHolder>() {

    private val items = mutableListOf<CalendarDay>()
    private var selectedDate: LocalDate? = null

    class DayViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvDay: TextView = itemView.findViewById(R.id.tvDay)
        val dot: View = itemView.findViewById(R.id.dotDay)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DayViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_calendar_day, parent, false)
        return DayViewHolder(view)
    }

    override fun onBindViewHolder(holder: DayViewHolder, position: Int) {
        val day = items[position]
        val context = holder.itemView.context

        holder.tvDay.text = day.date.dayOfMonth.toString()

        val isSelected = day.date == selectedDate

        when {

            isSelected -> {
                holder.tvDay.setBackgroundResource(R.drawable.bg_day_selected)
                holder.tvDay.setTextColor(
                    ContextCompat.getColor(context, R.color.calendar_selected_text)
                )
            }

            day.isToday -> {
                holder.tvDay.setBackgroundResource(R.drawable.bg_day_today)
                holder.tvDay.setTextColor(
                    ContextCompat.getColor(context, R.color.calendar_accent)
                )
            }

            else -> {
                holder.tvDay.background = null
                holder.tvDay.setTextColor(
                    ContextCompat.getColor(
                        context,
                        if (day.inCurrentMonth) R.color.calendar_day_text
                        else R.color.calendar_other_month
                    )
                )
            }
        }

        holder.tvDay.alpha = if (day.inCurrentMonth) 1f else 0.45f

        holder.dot.visibility = if (day.taskCount > 0) View.VISIBLE else View.INVISIBLE

        holder.itemView.setOnClickListener { onDayClick(day.date) }
    }

    override fun getItemCount(): Int = items.size

    fun submitList(days: List<CalendarDay>, selected: LocalDate) {
        items.clear()
        items.addAll(days)
        selectedDate = selected
        notifyDataSetChanged()
    }
}