package com.example.todoapp

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup

class TasksAdapter(
    private val onTaskClick: (Task) -> Unit,
    private val onTaskCompleteToggle: (Task) -> Unit
) : ListAdapter<Task, TasksAdapter.TaskViewHolder>(TaskDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_task, parent, false)
        return TaskViewHolder(view, onTaskClick, onTaskCompleteToggle)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class TaskViewHolder(
        itemView: View,
        private val onTaskClick: (Task) -> Unit,
        private val onTaskCompleteToggle: (Task) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {

        private val checkBoxCompleted: CheckBox = itemView.findViewById(R.id.checkBoxCompleted)
        private val textViewTaskTitle: TextView = itemView.findViewById(R.id.textViewTaskTitle)
        private val chipGroupTags: ChipGroup = itemView.findViewById(R.id.chipGroupTags)

        private var currentTask: Task? = null

        fun bind(task: Task) {
            currentTask = task

            textViewTaskTitle.text = task.title
            textViewTaskTitle.paint.isStrikeThruText = task.isCompleted

            // Изменение фона
            itemView.setBackgroundColor(
                if (task.isCompleted) Color.LTGRAY else Color.WHITE
            )

            chipGroupTags.removeAllViews()
            task.tags.forEach { tag ->
                val chip = Chip(itemView.context).apply {
                    text = tag.name
                    setChipBackgroundColor(android.content.res.ColorStateList.valueOf(tag.color))
                    setTextColor(Color.WHITE)
                    isClickable = false
                    isCheckable = false
                }
                chipGroupTags.addView(chip)
            }

            setupListeners(task)
        }

        private fun setupListeners(task: Task) {
            checkBoxCompleted.setOnCheckedChangeListener(null)
            checkBoxCompleted.isChecked = task.isCompleted
            checkBoxCompleted.setOnCheckedChangeListener { _, _ ->
                onTaskCompleteToggle(task)
            }

            itemView.setOnClickListener {
                onTaskClick(task)
            }
        }
    }

    class TaskDiffCallback : DiffUtil.ItemCallback<Task>() {
        override fun areItemsTheSame(oldItem: Task, newItem: Task): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Task, newItem: Task): Boolean {
            // Сравниваем простые поля
            if (oldItem.title != newItem.title) return false
            if (oldItem.isCompleted != newItem.isCompleted) return false

            // Безопасное сравнение списков тегов
            return areTagListsEqual(oldItem.tags, newItem.tags)
        }

        private fun areTagListsEqual(tags1: List<Tag>, tags2: List<Tag>): Boolean {
            if (tags1.size != tags2.size) return false

            val ids1 = tags1.map { it.id }.sorted()
            val ids2 = tags2.map { it.id }.sorted()

            return ids1 == ids2
        }
    }
}
