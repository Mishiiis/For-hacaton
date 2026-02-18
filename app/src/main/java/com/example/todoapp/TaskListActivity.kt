package com.example.todoapp

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton  // Добавьте этот импорт
import com.google.android.material.snackbar.Snackbar

class TaskListActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var fabAddTask: FloatingActionButton
    private lateinit var adapter: TasksAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_task_list)

        recyclerView = findViewById(R.id.recyclerViewTasks)
        fabAddTask = findViewById(R.id.fabAddTask)

        setupRecyclerView()
        setupListeners()
    }

    private fun setupRecyclerView() {
        adapter = TasksAdapter(
            onTaskClick = { task -> editTask(task) },
            onTaskCompleteToggle = { task -> toggleTaskCompletion(task) }
        )
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Swipe to delete с подтверждением
        val itemTouchHelper = ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(
            0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
        ) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ) = false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                val task = adapter.currentList[position]

                // Показываем диалог подтверждения
                showDeleteConfirmation(task, position)
            }
        })
        itemTouchHelper.attachToRecyclerView(recyclerView)
    }

    private fun showDeleteConfirmation(task: Task, position: Int) {
        AlertDialog.Builder(this)
            .setTitle("Удаление задачи")
            .setMessage("Удалить задачу \"${task.title}\"?")
            .setPositiveButton("Удалить") { _, _ ->
                // Удаляем из репозитория
                TasksRepository.deleteTask(task.id)

                // Получаем обновленный список и передаем в адаптер
                val updatedList = TasksRepository.tasks
                adapter.submitList(updatedList)

                Toast.makeText(this, "Задача удалена", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Отмена") { _, _ ->
                // Восстанавливаем элемент в адаптере
                adapter.notifyItemChanged(position)
            }
            .setOnCancelListener {
                // Если диалог закрыли кнопкой "назад" - восстанавливаем
                adapter.notifyItemChanged(position)
            }
            .show()
    }

    private fun setupListeners() {
        fabAddTask.setOnClickListener {
            openTaskDialog()
        }
    }

    override fun onResume() {
        super.onResume()
        loadTasks()
    }

    private fun loadTasks() {
        adapter.submitList(TasksRepository.tasks)
    }

    private fun openTaskDialog(taskToEdit: Task? = null) {
        val intent = Intent(this, TaskEditActivity::class.java)
        intent.putExtra("task_id", taskToEdit?.id)
        startActivity(intent)
    }

    private fun toggleTaskCompletion(task: Task) {
        TasksRepository.toggleTaskCompletion(task.id)
        loadTasks()
    }

    private fun editTask(task: Task) {
        openTaskDialog(task)
    }
}