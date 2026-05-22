package com.example.data

import kotlinx.coroutines.flow.Flow

class TodoRepository(private val todoDao: TodoDao) {
    val allTodos: Flow<List<TodoItem>> = todoDao.getAllTodos()

    suspend fun insert(todo: TodoItem) {
        todoDao.insertTodo(todo)
    }

    suspend fun update(todo: TodoItem) {
        todoDao.updateTodo(todo)
    }

    suspend fun delete(todo: TodoItem) {
        todoDao.deleteTodo(todo)
    }
}
