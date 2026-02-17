package com.example.todoapp

import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.util.TypedValue
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.todoapp.databinding.ActivityTaskEditBinding
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup

class TaskEditActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTaskEditBinding
    private var taskId: String? = null
    private var existingTask: Task? = null
    private val selectedTags = mutableSetOf<Tag>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Инициализация binding
        binding = ActivityTaskEditBinding.inflate(layoutInflater)
        setContentView(binding.root)

        taskId = intent.getStringExtra("task_id")
        existingTask = TasksRepository.tasks.find { it.id == taskId }

        setupViews()
        setupTagGroups()
    }

    private fun setupViews() {
        existingTask?.let {
            binding.editTextTaskTitle.setText(it.title)
            selectedTags.addAll(it.tags)
        }

        binding.buttonSaveTask.setOnClickListener {
            saveTask()
        }

        binding.buttonAddCustomTag.setOnClickListener {
            showAddCustomTagDialog()
        }
    }

    private fun setupTagGroups() {
        // Очищаем контейнер перед добавлением
        binding.linearLayoutTagsContainer.removeAllViews()

        // Группируем теги
        val tagsByGroup = TagsRepository.tags.groupBy { it.group }

        // Создаем чипсы для каждой группы
        tagsByGroup.forEach { (group, tags) ->
            // Заголовок группы
            val groupTitle = TextView(this).apply {
                text = group.displayName
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 18f)
                setTypeface(null, Typeface.BOLD)
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
            }
            binding.linearLayoutTagsContainer.addView(groupTitle)

            // Группа чипсов
            val chipGroup = ChipGroup(this).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
            }

            tags.forEach { tag ->
                val chip = Chip(this).apply {
                    text = tag.name
                    setChipBackgroundColor(android.content.res.ColorStateList.valueOf(tag.color))
                    setTextColor(Color.WHITE)
                    isCheckable = true
                    isChecked = selectedTags.contains(tag)

                    setOnCheckedChangeListener { _, isChecked ->
                        if (isChecked) {
                            selectedTags.add(tag)
                        } else {
                            selectedTags.remove(tag)
                        }
                    }
                }
                chipGroup.addView(chip)
            }

            binding.linearLayoutTagsContainer.addView(chipGroup)
        }
    }

    private fun showAddCustomTagDialog() {
        val builder = AlertDialog.Builder(this)
        val input = android.widget.EditText(this).apply {
            hint = "Название тега"
        }

        builder.setTitle("Новый тег")
            .setView(input)
            .setPositiveButton("Добавить") { _, _ ->
                val tagName = input.text.toString()
                if (tagName.isNotBlank()) {
                    // Случайный цвет для нового тега
                    val randomColor = Color.rgb(
                        (0..255).random(),
                        (0..255).random(),
                        (0..255).random()
                    )
                    val newTag = TagsRepository.addCustomTag(tagName, randomColor)

                    // Обновляем UI с новым тегом
                    selectedTags.add(newTag)
                    recreate() // Простой способ обновить все теги
                }
            }
            .setNegativeButton("Отмена", null)
            .show()
    }

    private fun saveTask() {
        val title = binding.editTextTaskTitle.text.toString()
        if (title.isBlank()) {
            Toast.makeText(this, "Введите название задачи", Toast.LENGTH_SHORT).show()
            return
        }

        if (selectedTags.isEmpty()) {
            Toast.makeText(this, "Выберите хотя бы один тег", Toast.LENGTH_SHORT).show()
            return
        }

        val task = existingTask?.copy(
            title = title,
            tags = selectedTags.toList()
        ) ?: Task(
            title = title,
            tags = selectedTags.toList()
        )

        if (existingTask != null) {
            TasksRepository.updateTask(task)
        } else {
            TasksRepository.addTask(task)
        }

        finish()
    }
}