package com.love.diary.habit

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.clickable
import java.time.format.DateTimeFormatter
import androidx.compose.ui.ExperimentalComposeUiApi
import com.love.diary.data.model.Habit
import com.love.diary.data.model.HabitType
import java.time.LocalDate
import android.content.Context
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope
import java.time.format.DateTimeParseException
import java.time.temporal.ChronoUnit
import androidx.compose.material3.DatePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DatePickerDefaults

@OptIn(androidx.compose.ui.ExperimentalComposeUiApi::class)
@Composable
fun HabitListScreen(
    modifier: Modifier = Modifier,
    context: Context = LocalContext.current,
    habitRepository: HabitRepository = remember { HabitRepository.getInstance(context) }
) {
    val habits by habitRepository.getAllHabits().collectAsState(initial = emptyList())
    val coroutineScope = rememberCoroutineScope()
    
    // 添加习惯对话框状态
    var showAddHabitDialog by remember { mutableStateOf(false) }
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "打卡事项",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
            
            FloatingActionButton(
                onClick = { showAddHabitDialog = true }
            ) {
                Icon(Icons.Default.Add, contentDescription = "添加打卡")
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        LazyColumn {
            items(habits) { habit ->
                HabitItemCard(
                    habit = habit,
                    onCheckIn = { habitId, tag ->
                        // 使用HabitRepository的打卡功能，同时记录标签
                        coroutineScope.launch {
                            habitRepository.checkInHabit(habitId, tag)
                        }
                    },
                    onCheckInWithText = { habitId, text ->
                        // 使用HabitRepository的打卡功能，带文本
                        coroutineScope.launch {
                            habitRepository.checkInHabit(habitId, text)
                        }
                    },
                    onClick = { /* 导航到详情页 */ }
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
    
    // 添加习惯对话框
    if (showAddHabitDialog) {
        AddHabitDialog(
            onAdd = { habit ->
                coroutineScope.launch {
                    habitRepository.insertHabit(habit)
                }
                showAddHabitDialog = false
            },
            onDismiss = { showAddHabitDialog = false }
        )
    }
}

@Composable
fun HabitItemCard(
    habit: Habit,
    onCheckIn: (Long, String?) -> Unit, // 修改回调参数，支持打卡时传递标签
    onCheckInWithText: (Long, String) -> Unit, // 新增回调参数，支持带文本的打卡
    onClick: (Long) -> Unit
) {
    var showOtherTagInputDialog by remember { mutableStateOf(false) }
    var otherTagText by remember { mutableStateOf("") }
    val currentHabitId = remember { mutableLongStateOf(0L) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick(habit.id) },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = habit.name,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Text(
                        text = when (habit.type) {
                            HabitType.POSITIVE -> "已打卡 ${habit.currentCount} 次"
                            HabitType.COUNTDOWN -> {
                                val targetDate = habit.targetDate?.let { LocalDate.parse(it) }
                                val today = LocalDate.now()
                                val daysLeft = if (targetDate != null) {
                                    ChronoUnit.DAYS.between(today, targetDate).toInt()
                                } else 0
                                "距离目标还有 $daysLeft 天"
                            }
                        },
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Text(
                        text = if (habit.isCompletedToday) "今日已完成" else "今日未完成",
                        fontSize = 12.sp,
                        color = if (habit.isCompletedToday) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                    )
                    
                    // 显示标签
                    if (habit.tags.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        LazyRow {
                            items(habit.tags.split(",").filter { it.isNotEmpty() }) { tag ->
                                InputChip(
                                    selected = false,
                                    onClick = {
                                        if (tag == "其它") {
                                            // 对于"其它"标签，显示输入对话框
                                            showOtherTagInputDialog = true
                                            currentHabitId.longValue = habit.id
                                        } else {
                                            onCheckIn(habit.id, tag) // 点击标签时打卡并记录标签
                                        }
                                    },
                                    label = { Text(tag) }
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                            }
                        }
                    }
                }
            }
        }
    }

    // "其它"标签输入对话框
    if (showOtherTagInputDialog) {
        AlertDialog(
            onDismissRequest = { showOtherTagInputDialog = false },
            title = { Text("输入标签内容") },
            text = {
                OutlinedTextField(
                    value = otherTagText,
                    onValueChange = { otherTagText = it },
                    label = { Text("输入内容") },
                    placeholder = { Text("请输入标签内容") },
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (otherTagText.isNotBlank()) {
                            onCheckInWithText(currentHabitId.longValue, otherTagText)
                            otherTagText = ""
                        }
                        showOtherTagInputDialog = false
                    },
                    enabled = otherTagText.isNotBlank()
                ) {
                    Text("确认")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { 
                        showOtherTagInputDialog = false
                        otherTagText = ""
                    }
                ) {
                    Text("取消")
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddHabitDialog(
    onAdd: (Habit) -> Unit,
    onDismiss: () -> Unit
) {
    var habitName by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf(HabitType.POSITIVE) }
    var targetDate by remember { mutableStateOf("") }
    var showDatePicker by remember { mutableStateOf(false) }
    var tagText by remember { mutableStateOf("") }
    var tags by remember { mutableStateOf(emptyList<String>()) }
    
    // 验证日期格式
    fun isValidDate(dateString: String): Boolean {
        return try {
            LocalDate.parse(dateString)
            true
        } catch (e: DateTimeParseException) {
            false
        }
    }
    
    // 添加标签
    fun addTag() {
        if (tagText.isNotBlank() && !tags.contains(tagText.trim())) {
            tags = tags + listOf(tagText.trim())
            tagText = ""
        }
    }
    
    // 移除标签
    fun removeTag(tag: String) {
        tags = tags.filter { it != tag }
    }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("添加打卡事项") },
        text = {
            Column {
                OutlinedTextField(
                    value = habitName,
                    onValueChange = { habitName = it },
                    label = { Text("打卡名称") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text("打卡类型", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))
                
                // 打卡类型选择
                Column {
                    RadioButton(
                        selected = selectedType == HabitType.POSITIVE,
                        onClick = { selectedType = HabitType.POSITIVE }
                    )
                    Text(
                        text = "正向打卡",
                        modifier = Modifier.clickable { selectedType = HabitType.POSITIVE }
                    )
                    
                    RadioButton(
                        selected = selectedType == HabitType.COUNTDOWN,
                        onClick = { selectedType = HabitType.COUNTDOWN }
                    )
                    Text(
                        text = "倒计时打卡",
                        modifier = Modifier.clickable { selectedType = HabitType.COUNTDOWN }
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                if (selectedType == HabitType.COUNTDOWN) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        OutlinedTextField(
                            value = targetDate,
                            onValueChange = { },
                            label = { Text("目标日期") },
                            placeholder = { Text("请选择日期") },
                            leadingIcon = {
                                Icon(Icons.Default.DateRange, contentDescription = null)
                            },
                            readOnly = true,
                            modifier = Modifier
                                .weight(1f)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        IconButton(
                            onClick = { showDatePicker = true }
                        ) {
                            Icon(Icons.Default.DateRange, contentDescription = "选择日期")
                        }
                    }
                }
                
                
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text("标签", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))
                
                // 标签输入和管理
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = tagText,
                        onValueChange = { tagText = it },
                        label = { Text("输入标签") },
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = { addTag() },
                        enabled = tagText.isNotBlank()
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "添加标签")
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // 显示现有标签
                if (tags.isNotEmpty()) {
                    LazyRow {
                        items(tags.size) { index ->
                            val tag = tags[index]
                            InputChip(
                                selected = false,
                                onClick = { },
                                label = { Text(tag) },
                                trailingIcon = {
                                    IconButton(
                                        onClick = { removeTag(tag) }
                                    ) {
                                        Icon(Icons.Default.Close, contentDescription = "移除标签")
                                    }
                                }
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (habitName.isNotBlank()) {
                        // 默认添加"其它"标签
                        var finalTags = tags.toMutableList()
                        if (!finalTags.contains("其它")) {
                            finalTags.add("其它")
                        }
                        val habit = Habit(
                            name = habitName,
                            type = selectedType,
                            targetDate = if (selectedType == HabitType.COUNTDOWN && targetDate.isNotBlank() && isValidDate(targetDate)) targetDate else null,
                            tags = finalTags.joinToString(",")
                        )
                        onAdd(habit)
                    }
                },
                enabled = habitName.isNotBlank() && 
                         (selectedType == HabitType.POSITIVE || 
                          (selectedType == HabitType.COUNTDOWN && targetDate.isNotBlank() && isValidDate(targetDate)))
            ) {
                Text("添加")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
    
    // 日期选择器对话框
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = System.currentTimeMillis()
        )
        
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        // 获取选择的日期并更新targetDate
                        val selectedDateMillis = datePickerState.selectedDateMillis
                        if (selectedDateMillis != null) {
                            val selectedDate = LocalDate.ofEpochDay(selectedDateMillis / (24 * 60 * 60 * 1000))
                            targetDate = selectedDate.toString()
                        }
                        showDatePicker = false
                    },
                    enabled = datePickerState.selectedDateMillis != null
                ) {
                    Text("确定")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showDatePicker = false
                    }
                ) {
                    Text("取消")
                }
            },
            colors = DatePickerDefaults.colors()
        ) {
            DatePicker(
                state = datePickerState
            )
        }
    }
}