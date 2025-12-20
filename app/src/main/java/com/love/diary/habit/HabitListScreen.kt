package com.love.diary.habit

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.format.DateTimeFormatter
import androidx.compose.ui.ExperimentalComposeUiApi
import com.love.diary.data.model.Habit
import com.love.diary.data.model.HabitType
import com.love.diary.data.model.PositiveDisplayType
import java.time.LocalDate
import android.content.Context
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.launch
import java.time.format.DateTimeParseException
import java.time.temporal.ChronoUnit
import androidx.compose.material3.DatePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DatePickerDefaults
import com.love.diary.presentation.components.AppCard
import com.love.diary.presentation.components.Dimens
import com.love.diary.presentation.components.SectionHeader
import com.love.diary.habit.HabitDisplayView
import com.love.diary.habit.HabitStatsView

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
    
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(Dimens.ScreenPadding)
    ) {
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(Dimens.SectionSpacing)
        ) {
            item {
                SectionHeader(
                    title = "打卡事项",
                    subtitle = "管理你的习惯与今日完成度"
                )
            }
            items(habits) { habit ->
                HabitItemCard(
                    habit = habit,
                    onCheckIn = { habitId, tag ->
                        coroutineScope.launch {
                            habitRepository.checkInHabit(habitId, tag)
                        }
                    },
                    onCheckInWithText = { habitId, text ->
                        coroutineScope.launch {
                            habitRepository.checkInHabit(habitId, text)
                        }
                    },
                    onClick = { /* 导航到详情页 */ }
                )
            }
        }

        FloatingActionButton(
            onClick = { showAddHabitDialog = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(Dimens.SectionSpacing)
        ) {
            Icon(Icons.Default.Add, contentDescription = "添加打卡")
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

@OptIn(ExperimentalMaterial3Api::class)
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
    
    // 追踪今天已使用的标签
    var todayUsedTag by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    
    // 从数据库获取今天的打卡记录和使用的标签
    LaunchedEffect(habit.id) {
        val database = com.love.diary.data.database.LoveDatabase.getInstance(context)
        val today = java.time.LocalDate.now().toString()
        
        // 从 UnifiedCheckIn 系统查询今天的打卡记录
        val todayCheckIn = database.unifiedCheckInDao().getCheckInByDateAndName(today, habit.name)
        todayUsedTag = todayCheckIn?.tag
    }

    AppCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick(habit.id) }
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(Dimens.SectionSpacing)
        ) {
            Text(
                text = habit.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium
            )

            // 显示打卡统计信息
            HabitStatsView(habit = habit)

            // 显示打卡展示视图
            HabitDisplayView(habit = habit)

            if (habit.tags.isNotEmpty()) {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(habit.tags.split(",").filter { it.isNotEmpty() }) { tag ->
                        InputChip(
                            selected = tag == todayUsedTag,
                            onClick = {
                                if (tag == "其它") {
                                    showOtherTagInputDialog = true
                                    currentHabitId.longValue = habit.id
                                } else {
                                    onCheckIn(habit.id, tag)
                                    todayUsedTag = tag
                                }
                            },
                            label = { Text(tag) }
                        )
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
                            // 更新UI状态以反映选中
                            todayUsedTag = otherTagText
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
    var selectedDisplayType by remember { mutableStateOf(PositiveDisplayType.WEEKLY) } // 正向打卡展示类型
    var targetDate by remember { mutableStateOf("") }
    var showDatePicker by remember { mutableStateOf(false) }
    var tagText by remember { mutableStateOf("") }
    var tags by remember { mutableStateOf(emptyList<String>()) }
    
    // 验证日期格式
    fun isValidDate(dateString: String): Boolean {
        return try {
            LocalDate.parse(dateString)
            true
        } catch (e: java.time.format.DateTimeParseException) {
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
                    
                    // 如果是正向打卡，显示展示类型选择
                    if (selectedType == HabitType.POSITIVE) {
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text("展示方式", style = MaterialTheme.typography.bodyMedium)
                        
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 24.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = selectedDisplayType == PositiveDisplayType.WEEKLY,
                                onClick = { selectedDisplayType = PositiveDisplayType.WEEKLY }
                            )
                            Text(
                                text = "周展示",
                                modifier = Modifier.clickable { selectedDisplayType = PositiveDisplayType.WEEKLY }
                            )
                            
                            RadioButton(
                                selected = selectedDisplayType == PositiveDisplayType.MONTHLY,
                                onClick = { selectedDisplayType = PositiveDisplayType.MONTHLY }
                            )
                            Text(
                                text = "月展示",
                                modifier = Modifier.clickable { selectedDisplayType = PositiveDisplayType.MONTHLY }
                            )
                        }
                    }
                    
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
                            displayType = selectedDisplayType, // 设置展示类型
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
