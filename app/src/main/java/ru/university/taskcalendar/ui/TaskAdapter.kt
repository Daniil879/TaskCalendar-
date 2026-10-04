package ru.university.taskcalendar.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import ru.university.taskcalendar.R
import ru.university.taskcalendar.data.Task

class TaskAdapter(
    private val onItemClick: (Task) -> Unit
) : RecyclerView.Adapter<TaskAdapter.TaskViewHolder>() {

    private val items: MutableList<Task> = mutableListOf()

    class TaskViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val card: MaterialCardView = itemView.findViewById(R.id.cardTask)
        val title: TextView = itemView.findViewById(R.id.tvTaskTitle)
        val description: TextView = itemView.findViewById(R.id.tvTaskDescription)
        val date: TextView = itemView.findViewById(R.id.tvTaskDate)
        val status: TextView = itemView.findViewById(R.id.tvTaskStatus)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_task, parent, false)
        return TaskViewHolder(view)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        val task = items[position]
        val context = holder.itemView.context

        holder.title.text = task.title
        holder.description.text = task.description
        holder.date.text = if (task.time.isBlank()) task.date else "${task.date} · ${task.time}"

        if (task.isDone) {
            holder.status.setText(R.string.task_status_done)
            holder.status.setBackgroundResource(R.drawable.bg_status_done)
            holder.status.setTextColor(context.getColor(R.color.status_done_text))
        } else {
            holder.status.setText(R.string.task_status_pending)
            holder.status.setBackgroundResource(R.drawable.bg_status_pending)
            holder.status.setTextColor(context.getColor(R.color.status_pending_text))
        }

        holder.itemView.setOnClickListener { onItemClick(task) }
    }

    override fun getItemCount(): Int = items.size

    fun submitList(newItems: List<Task>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }
}