package com.example.todoapp

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.todoapp.databinding.ActivityTaskListBinding

class TaskListActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTaskListBinding
    private lateinit var adapter: TasksAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Инициализация binding
        binding = ActivityTaskListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        setupListeners()
    }

    private fun setupRecyclerView() {
        adapter = TasksAdapter(
            onTaskClick = { task -> editTask(task) },
            onTaskCompleteToggle = { task -> toggleTaskCompletion(task) }
        )
        binding.recyclerViewTasks.adapter = adapter
        binding.recyclerViewTasks.layoutManager = LinearLayoutManager(this)

        // Swipe to delete
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
                deleteTask(task)
            }
        })
        itemTouchHelper.attachToRecyclerView(binding.recyclerViewTasks)
    }

    private fun setupListeners() {
        binding.fabAddTask.setOnClickListener {
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

    private fun deleteTask(task: Task) {
        AlertDialog.Builder(this)
            .setTitle("Удаление задачи")
            .setMessage("Удалить задачу \"${task.title}\"?")
            .setPositiveButton("Удалить") { _, _ ->
                TasksRepository.deleteTask(task.id)
                loadTasks()
            }
            .setNegativeButton("Отмена", null)
            .show()
    }

    private fun toggleTaskCompletion(task: Task) {
        TasksRepository.toggleTaskCompletion(task.id)
        loadTasks()
    }

    private fun editTask(task: Task) {
        openTaskDialog(task)
    }
}