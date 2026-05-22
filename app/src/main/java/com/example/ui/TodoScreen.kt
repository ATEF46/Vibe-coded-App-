package com.example.ui

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.TodoItem
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

val PrimaryBlue = Color(0xFF0073D1)
val BackgroundLight = Color(0xFFE3F2FD)
val OverdueRed = Color(0xFFD32F2F)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodoScreen(viewModel: TodoViewModel) {
    val items by viewModel.uiState.collectAsStateWithLifecycle()
    val categoryCounts by viewModel.categoryCounts.collectAsStateWithLifecycle()
    val currentCategory by viewModel.selectedCategory.collectAsStateWithLifecycle()
    
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var showAddSheet by remember { mutableStateOf(false) }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(drawerContainerColor = Color.White) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                        .background(PrimaryBlue),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(48.dp))
                        Text("To Do List", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                    }
                }
                Text("TASK LISTS", modifier = Modifier.padding(16.dp), color = Color.Gray, fontSize = 12.sp)
                
                val categories = listOf("All Lists", "Default", "Personal", "Shopping", "Wishlist", "Work", "Finished")
                LazyColumn(modifier = Modifier.fillMaxWidth()) {
                    items(categories) { cat ->
                        val count = categoryCounts[cat] ?: 0
                        NavigationDrawerItem(
                            label = { Text(cat) },
                            badge = {
                                Box(
                                    modifier = Modifier
                                        .background(PrimaryBlue, CircleShape)
                                        .padding(horizontal = 8.dp, vertical = 2.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(count.toString(), color = Color.White, fontSize = 10.sp)
                                }
                            },
                            selected = currentCategory == cat,
                            onClick = {
                                viewModel.setCategory(cat)
                                scope.launch { drawerState.close() }
                            },
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                            colors = NavigationDrawerItemDefaults.colors(
                                selectedContainerColor = PrimaryBlue.copy(alpha = 0.1f),
                                unselectedContainerColor = Color.Transparent,
                                selectedTextColor = PrimaryBlue,
                                unselectedTextColor = Color.Black
                            )
                        )
                    }
                }
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { 
                        Column {
                            Text("Tasks", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                            Text(currentCategory, fontSize = 14.sp, fontWeight = FontWeight.Normal, color = Color.White.copy(alpha = 0.8f))
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = PrimaryBlue,
                        titleContentColor = Color.White,
                        navigationIconContentColor = Color.White,
                        actionIconContentColor = Color.White
                    ),
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, contentDescription = "Menu")
                        }
                    }
                )
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = { showAddSheet = true },
                    containerColor = PrimaryBlue,
                    contentColor = Color.White,
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Task")
                }
            },
            containerColor = BackgroundLight
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
            ) {
                if (items.isEmpty()) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, tint = PrimaryBlue.copy(alpha = 0.3f), modifier = Modifier.size(100.dp))
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Nothing to do", color = Color.Gray, fontSize = 16.sp)
                    }
                } else {
                    val groupedItems = groupItemsByDate(items)
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        contentPadding = PaddingValues(top = 16.dp, bottom = 80.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        groupedItems.forEach { (groupName, groupItems) ->
                            item {
                                val color = if (groupName == "Overdue") OverdueRed else PrimaryBlue
                                Text(
                                    text = groupName,
                                    color = color,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                                )
                            }
                            items(groupItems, key = { it.id }) { item ->
                                TodoRow(
                                    item = item,
                                    onToggle = { viewModel.toggleTodoCompleted(item) },
                                    onDelete = { viewModel.deleteTodo(item) },
                                    onToggleImportant = { viewModel.toggleTodoImportant(item) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddSheet) {
        AddNewTaskSheet(
            onDismiss = { showAddSheet = false },
            onSave = { text, dueDate, cat, notes, isImportant ->
                viewModel.addTodo(text, dueDate, cat, notes, isImportant)
                showAddSheet = false
            }
        )
    }
}

@Composable
fun TodoRow(
    item: TodoItem,
    onToggle: () -> Unit,
    onDelete: () -> Unit,
    onToggleImportant: () -> Unit = {}
) {
    Card(
        modifier = Modifier.fillMaxWidth().alpha(if (item.isCompleted) 0.7f else 1f),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = item.isCompleted,
                onCheckedChange = { onToggle() },
                colors = CheckboxDefaults.colors(
                    checkedColor = PrimaryBlue,
                    uncheckedColor = Color.Gray
                )
            )
            Column(modifier = Modifier.weight(1f).padding(horizontal = 12.dp)) {
                Text(
                    text = item.text,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        textDecoration = if (item.isCompleted) TextDecoration.LineThrough else TextDecoration.None,
                        fontWeight = FontWeight.Medium
                    ),
                    color = Color.Black
                )
                if (item.notes.isNotBlank()) {
                    Text(
                        text = item.notes,
                        fontSize = 14.sp,
                        color = Color.DarkGray,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                if (item.dueDate != null) {
                    val group = getGroup(item.dueDate)
                    val dateFormatted = SimpleDateFormat("MMM d, h:mm a", Locale.getDefault()).format(java.util.Date(item.dueDate))
                    val color = if (group == "Overdue" && !item.isCompleted) OverdueRed else Color.Gray
                    Text(
                        text = "$group, $dateFormatted",
                        fontSize = 12.sp,
                        color = color
                    )
                }
            }
            IconButton(onClick = onToggleImportant) {
                Icon(
                    Icons.Default.Star,
                    contentDescription = "Important",
                    tint = if (item.isImportant) PrimaryBlue else Color.LightGray
                )
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.LightGray)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddNewTaskSheet(
    onDismiss: () -> Unit,
    onSave: (String, Long?, String?, String, Boolean) -> Unit
) {
    var text by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var selectedDate by remember { mutableStateOf<Long?>(null) }
    var selectedCategory by remember { mutableStateOf("Default") }
    var isImportant by remember { mutableStateOf(false) }
    var showDetails by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    
    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = selectedDate)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    selectedDate = datePickerState.selectedDateMillis
                    showDatePicker = false
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
    
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color.White,
        dragHandle = {}
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 16.dp)
        ) {
            TextField(
                value = text,
                onValueChange = { text = it },
                placeholder = { Text("New task", fontSize = 18.sp, color = Color.Gray) },
                modifier = Modifier.fillMaxWidth(),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                textStyle = MaterialTheme.typography.bodyLarge.copy(fontSize = 18.sp)
            )
            
            if (showDetails || notes.isNotBlank()) {
                TextField(
                    value = notes,
                    onValueChange = { notes = it },
                    placeholder = { Text("Add details", fontSize = 14.sp, color = Color.Gray) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    textStyle = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp, color = Color.DarkGray)
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row {
                    IconButton(onClick = { showDetails = !showDetails }) {
                        Icon(Icons.Default.Menu, contentDescription = "Add Details", tint = if (showDetails) PrimaryBlue else Color.Gray)
                    }
                    IconButton(
                        onClick = { showDatePicker = true }
                    ) {
                        Icon(Icons.Default.DateRange, contentDescription = "Due Date", tint = if (selectedDate != null) PrimaryBlue else Color.Gray)
                    }
                    IconButton(onClick = { isImportant = !isImportant }) {
                        Icon(Icons.Default.Star, contentDescription = "Important", tint = if (isImportant) PrimaryBlue else Color.LightGray)
                    }
                }
                
                TextButton(
                    onClick = {
                        if (text.isNotBlank()) {
                            scope.launch {
                                sheetState.hide()
                                onSave(text, selectedDate, selectedCategory, notes, isImportant)
                            }
                        }
                    },
                    enabled = text.isNotBlank()
                ) {
                    Text("Save", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = if (text.isNotBlank()) PrimaryBlue else Color.Gray)
                }
            }
            if (selectedDate != null) {
                Text(
                    text = "Due: " + SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(java.util.Date(selectedDate!!)),
                    fontSize = 12.sp,
                    color = PrimaryBlue,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )
            }
            Spacer(modifier = Modifier.height(32.dp)) // Extra padding for keyboard
        }
    }
}

fun groupItemsByDate(items: List<TodoItem>): Map<String, List<TodoItem>> {
    val map = mutableMapOf<String, MutableList<TodoItem>>()
    items.forEach { item ->
        val group = getGroup(item.dueDate)
        if (!map.containsKey(group)) map[group] = mutableListOf()
        map[group]!!.add(item)
    }
    
    val order = listOf("Overdue", "Today", "Tomorrow", "This week", "Later", "No Date")
    val sortedMap = linkedMapOf<String, List<TodoItem>>()
    order.forEach { key ->
        if (map.containsKey(key)) {
            sortedMap[key] = map[key]!!
        }
    }
    return sortedMap
}

fun getGroup(date: Long?): String {
    if (date == null) return "No Date"
    val today = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
    }
    val target = Calendar.getInstance().apply { timeInMillis = date }
    target.set(Calendar.HOUR_OF_DAY, 0); target.set(Calendar.MINUTE, 0); target.set(Calendar.SECOND, 0); target.set(Calendar.MILLISECOND, 0)
    
    val diff = (target.timeInMillis - today.timeInMillis) / (1000 * 60 * 60 * 24)
    return when {
        diff < 0L -> "Overdue"
        diff == 0L -> "Today"
        diff == 1L -> "Tomorrow"
        diff in 2L..7L -> "This week"
        else -> "Later"
    }
}
