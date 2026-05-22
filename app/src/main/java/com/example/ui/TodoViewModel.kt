package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.TodoItem
import com.example.data.TodoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

val predefinedCategories = listOf("Default", "Personal", "Shopping", "Wishlist", "Work")

class TodoViewModel(private val repository: TodoRepository) : ViewModel() {

    private val _selectedCategory = MutableStateFlow("All Lists")
    val selectedCategory: StateFlow<String> = _selectedCategory

    val uiState: StateFlow<List<TodoItem>> = combine(
        repository.allTodos,
        _selectedCategory
    ) { todos, category ->
        when (category) {
            "All Lists" -> todos.filter { !it.isCompleted }
            "Finished" -> todos.filter { it.isCompleted }
            "Important" -> todos.filter { it.isImportant && !it.isCompleted }
            else -> todos.filter { it.category == category && !it.isCompleted }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val categoryCounts: StateFlow<Map<String, Int>> = repository.allTodos.map { todos ->
        val counts = mutableMapOf<String, Int>()
        counts["All Lists"] = todos.count { !it.isCompleted }
        counts["Finished"] = todos.count { it.isCompleted }
        counts["Important"] = todos.count { it.isImportant && !it.isCompleted }
        predefinedCategories.forEach { cat ->
            counts[cat] = todos.count { it.category == cat && !it.isCompleted }
        }
        counts
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyMap()
    )

    fun setCategory(category: String) {
        _selectedCategory.value = category
    }

    fun addTodo(text: String, dueDate: Long? = null, specificCategory: String? = null, notes: String = "", isImportantForced: Boolean? = null) {
        if (text.isNotBlank()) {
            viewModelScope.launch {
                val isImportant = isImportantForced ?: (_selectedCategory.value == "Important")
                val baseCat = specificCategory ?: _selectedCategory.value
                val actualCategory = if (baseCat in listOf("All Lists", "Important", "Finished")) "Default" else baseCat
                repository.insert(TodoItem(text = text.trim(), category = actualCategory, isImportant = isImportant, dueDate = dueDate, notes = notes.trim()))
            }
        }
    }

    fun toggleTodoCompleted(todo: TodoItem) {
        viewModelScope.launch {
            repository.update(todo.copy(isCompleted = !todo.isCompleted))
        }
    }

    fun toggleTodoImportant(todo: TodoItem) {
        viewModelScope.launch {
            repository.update(todo.copy(isImportant = !todo.isImportant))
        }
    }
    
    fun updateTodoNotes(todo: TodoItem, notes: String) {
        viewModelScope.launch {
            repository.update(todo.copy(notes = notes.trim()))
        }
    }

    fun deleteTodo(todo: TodoItem) {
        viewModelScope.launch {
            repository.delete(todo)
        }
    }
}
