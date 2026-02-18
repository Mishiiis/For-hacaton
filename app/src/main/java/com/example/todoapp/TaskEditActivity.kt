package com.example.todoapp

import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup

class TaskEditActivity : AppCompatActivity() {

    private lateinit var editTextTaskTitle: EditText
    private lateinit var buttonSaveTask: Button
    private lateinit var linearLayoutTagsContainer: LinearLayout

    private var taskId: String? = null
    private var existingTask: Task? = null
    private val selectedTags = mutableSetOf<Tag>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("TaskEdit", "onCreate started")

        try {
            setContentView(R.layout.activity_task_edit)
            Log.d("TaskEdit", "ContentView set")

            // Инициализация view
            editTextTaskTitle = findViewById(R.id.editTextTaskTitle)
            buttonSaveTask = findViewById(R.id.buttonSaveTask)
            linearLayoutTagsContainer = findViewById(R.id.linearLayoutTagsContainer)

            Log.d("TaskEdit", "Views initialized")

            // Получаем ID задачи из Intent
            taskId = intent.getStringExtra("task_id")
            Log.d("TaskEdit", "taskId: $taskId")

            existingTask = taskId?.let { id ->
                TasksRepository.tasks.find { it.id == id }
            }
            Log.d("TaskEdit", "existingTask: $existingTask")

            // Загружаем существующие теги если редактируем
            existingTask?.let {
                editTextTaskTitle.setText(it.title)
                selectedTags.addAll(it.tags)
                Log.d("TaskEdit", "Loaded existing task with ${it.tags.size} tags")
            }

            // Создаем теги
            setupTagGroups()

            // Устанавливаем слушатель на кнопку сохранения
            buttonSaveTask.setOnClickListener {
                Log.d("TaskEdit", "Save button clicked")
                saveTask()
            }

            Log.d("TaskEdit", "onCreate completed successfully")

        } catch (e: Exception) {
            Log.e("TaskEdit", "Error in onCreate", e)
            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun setupTagGroups() {
        Log.d("TaskEdit", "setupTagGroups started")

        linearLayoutTagsContainer.removeAllViews()

        val allTags = TagsRepository.tags
        Log.d("TaskEdit", "Total tags from repository: ${allTags.size}")

        if (allTags.isEmpty()) {
            Log.e("TaskEdit", "No tags found in repository!")
            Toast.makeText(this, "Ошибка: теги не загружены", Toast.LENGTH_LONG).show()
            return
        }

        val tagsByGroup = allTags.groupBy { it.group }
        Log.d("TaskEdit", "Groups found: ${tagsByGroup.keys}")

        tagsByGroup.forEach { (group, tags) ->
            Log.d("TaskEdit", "Processing group: ${group.displayName} with ${tags.size} tags")

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
            linearLayoutTagsContainer.addView(groupTitle)

            // Группа чипсов
            val chipGroup = ChipGroup(this).apply {
                layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
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
                            Log.d("TaskEdit", "Tag selected: ${tag.name}")
                            Toast.makeText(this@TaskEditActivity, "Выбран: ${tag.name}", Toast.LENGTH_SHORT).show()
                        } else {
                            selectedTags.remove(tag)
                            Log.d("TaskEdit", "Tag deselected: ${tag.name}")
                        }
                    }
                }
                chipGroup.addView(chip)
            }

            linearLayoutTagsContainer.addView(chipGroup)
        }

        Log.d("TaskEdit", "setupTagGroups completed, selected tags count: ${selectedTags.size}")
    }

    private fun saveTask() {
        Log.d("TaskEdit", "saveTask started")

        try {
            val title = editTextTaskTitle.text.toString().trim()
            Log.d("TaskEdit", "Title: '$title'")

            if (title.isEmpty()) {
                Toast.makeText(this, "Введите название задачи", Toast.LENGTH_SHORT).show()
                return
            }

            Log.d("TaskEdit", "Selected tags count: ${selectedTags.size}")
            if (selectedTags.isEmpty()) {
                Toast.makeText(this, "Выберите хотя бы один тег", Toast.LENGTH_SHORT).show()
                return
            }

            // Создаем новую задачу
            val task = if (existingTask != null) {
                Log.d("TaskEdit", "Updating existing task")
                existingTask!!.copy(
                    title = title,
                    tags = selectedTags.toList()
                )
            } else {
                Log.d("TaskEdit", "Creating new task")
                Task(
                    title = title,
                    tags = selectedTags.toList()
                )
            }

            Log.d("TaskEdit", "Task created: $task")

            // Сохраняем в репозиторий
            if (existingTask != null) {
                TasksRepository.updateTask(task)
                Log.d("TaskEdit", "Task updated in repository")
            } else {
                TasksRepository.addTask(task)
                Log.d("TaskEdit", "Task added to repository")
            }

            Toast.makeText(this, "Задача сохранена!", Toast.LENGTH_SHORT).show()
            Log.d("TaskEdit", "Finishing activity")
            finish()

        } catch (e: Exception) {
            Log.e("TaskEdit", "Error in saveTask", e)
            Toast.makeText(this, "Ошибка: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
}

