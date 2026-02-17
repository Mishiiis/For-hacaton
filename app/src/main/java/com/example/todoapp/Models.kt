package com.example.todoapp
import java.util.UUID
import android.graphics.Color
// ==================== Модели данных ====================

// Модель тега
data class Tag(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val group: TagGroup, // К какой группе относится
    val color: Int // Цвет для отображения
)

// Enum групп тегов
enum class TagGroup(val displayName: String) {
    READINESS("Готовность"),
    IMPORTANCE("Важность"),
    URGENCY("Срочность"),
    SPHERE("Сфера"),
    CUSTOM("Мои теги") // Для пользовательских тегов
}

// Модель задачи
data class Task(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val isCompleted: Boolean = false,
    val tags: List<Tag> = emptyList(),
    val createdAt: Long = System.currentTimeMillis()
)

// ==================== Репозиторий (работа с данными) ====================

object TagsRepository {
    // Предустановленные теги
    private val _tags = mutableListOf(
        // Готовность
        Tag(name = "Не начата", group = TagGroup.READINESS, color = Color.GRAY),
        Tag(name = "В процессе", group = TagGroup.READINESS, color = Color.BLUE),
        Tag(name = "Готова", group = TagGroup.READINESS, color = Color.GREEN),
        // Важность
        Tag(name = "Низкая", group = TagGroup.IMPORTANCE, color = Color.parseColor("#4CAF50")), // Светло-зеленый
        Tag(name = "Средняя", group = TagGroup.IMPORTANCE, color = Color.parseColor("#FFC107")), // Желтый
        Tag(name = "Высокая", group = TagGroup.IMPORTANCE, color = Color.parseColor("#FF9800")), // Оранжевый
        Tag(name = "Критическая", group = TagGroup.IMPORTANCE, color = Color.RED),
        // Срочность
        Tag(name = "Не срочно", group = TagGroup.URGENCY, color = Color.parseColor("#9E9E9E")), // Серый
        Tag(name = "Срочно", group = TagGroup.URGENCY, color = Color.parseColor("#FF5722")), // Оранжево-красный
        Tag(name = "Горит", group = TagGroup.URGENCY, color = Color.RED),
        // Сфера
        Tag(name = "Работа", group = TagGroup.SPHERE, color = Color.parseColor("#3F51B5")), // Индиго
        Tag(name = "Личное", group = TagGroup.SPHERE, color = Color.parseColor("#E91E63")), // Розовый
        Tag(name = "Дом", group = TagGroup.SPHERE, color = Color.parseColor("#8BC34A")), // Светло-зеленый
        Tag(name = "Покупки", group = TagGroup.SPHERE, color = Color.parseColor("#FFEB3B")), // Желтый
        Tag(name = "Здоровье", group = TagGroup.SPHERE, color = Color.parseColor("#00BCD4")), // Бирюзовый
        Tag(name = "Финансы", group = TagGroup.SPHERE, color = Color.parseColor("#4CAF50")), // Зеленый
        Tag(name = "Обучение", group = TagGroup.SPHERE, color = Color.parseColor("#9C27B0")) // Фиолетовый
    )

    val tags: List<Tag> get() = _tags

    // Добавление нового пользовательского тега
    fun addCustomTag(name: String, color: Int): Tag {
        val newTag = Tag(name = name, group = TagGroup.CUSTOM, color = color)
        _tags.add(newTag)
        return newTag
    }
}

object TasksRepository {
    private val _tasks = mutableListOf<Task>()
    val tasks: List<Task> get() = _tasks.sortedByDescending { it.createdAt }

    fun addTask(task: Task) {
        _tasks.add(task)
    }

    fun updateTask(updatedTask: Task) {
        val index = _tasks.indexOfFirst { it.id == updatedTask.id }
        if (index != -1) {
            _tasks[index] = updatedTask
        }
    }

    fun deleteTask(taskId: String) {
        _tasks.removeAll { it.id == taskId }
    }

    fun toggleTaskCompletion(taskId: String) {
        val index = _tasks.indexOfFirst { it.id == taskId }
        if (index != -1) {
            val task = _tasks[index]
            _tasks[index] = task.copy(isCompleted = !task.isCompleted)
        }
    }
}