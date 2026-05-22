package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.Modifier
import com.example.data.AppDatabase
import com.example.data.TodoRepository
import com.example.ui.TodoScreen
import com.example.ui.TodoViewModel
import com.example.ui.TodoViewModelFactory
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    Thread.setDefaultUncaughtExceptionHandler { thread, exception ->
        android.util.Log.e("CRASH_TEST", "Uncaught exception", exception)
    }
    enableEdgeToEdge()

    val database = AppDatabase.getDatabase(this)
    val repository = TodoRepository(database.todoDao())

    setContent {
      MyApplicationTheme {
        val viewModel: TodoViewModel = viewModel(
          factory = TodoViewModelFactory(repository)
        )
        TodoScreen(viewModel = viewModel)
      }
    }
  }
}
