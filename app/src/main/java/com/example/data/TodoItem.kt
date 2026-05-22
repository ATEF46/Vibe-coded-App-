package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "todo_items")
data class TodoItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val text: String,
    val isCompleted: Boolean = false,
    val isImportant: Boolean = false,
    val category: String = "Default",
    val notes: String = "",
    val dueDate: Long? = null,
    val timestamp: Long = System.currentTimeMillis()
)
