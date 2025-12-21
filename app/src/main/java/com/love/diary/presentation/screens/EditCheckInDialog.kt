package com.love.diary.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.love.diary.data.model.UnifiedCheckInConfig
import com.love.diary.presentation.components.Dimens
import com.love.diary.presentation.components.TimePickerDialog

/**
 * Dialog for editing an existing check-in item
 * Allows editing: name, icon, description, reminder time, reminder enabled
 * Does NOT allow editing: check-in type, countdown mode, target values
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditCheckInDialog(
    config: UnifiedCheckInConfig,
    onDismiss: () -> Unit,
    onConfirm: (
        name: String,
        icon: String,
        description: String?,
        reminderTime: String?,
        reminderEnabled: Boolean
    ) -> Unit
) {
    var name by remember { mutableStateOf(config.name) }
    var description by remember { mutableStateOf(config.description ?: "") }
    var selectedIcon by remember { mutableStateOf(config.icon) }
    var reminderEnabled by remember { mutableStateOf(config.reminderTime != null) }
    var reminderHour by remember { 
        mutableStateOf(
            config.reminderTime?.split(":")?.getOrNull(0)?.toIntOrNull() ?: 9
        ) 
    }
    var reminderMinute by remember { 
        mutableStateOf(
            config.reminderTime?.split(":")?.getOrNull(1)?.toIntOrNull() ?: 0
        ) 
    }
    var showTimePickerDialog by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .fillMaxHeight(0.85f),
            shape = MaterialTheme.shapes.large,
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(Dimens.ScreenPadding)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "编辑打卡事项",
                        style = MaterialTheme.typography.headlineSmall
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "关闭")
                    }
                }

                Spacer(modifier = Modifier.height(Dimens.MediumSpacing))

                // Content
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(Dimens.MediumSpacing)
                ) {
                    // Name input
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("打卡事项名称") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    // Type display (read-only)
                    Text(
                        text = "打卡类型",
                        style = MaterialTheme.typography.labelLarge
                    )
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Text(
                            text = when (config.checkInCategory) {
                                com.love.diary.data.model.CheckInCategory.POSITIVE -> {
                                    "正向打卡 - ${
                                        when (config.recurrenceType) {
                                            com.love.diary.data.model.RecurrenceType.WEEKLY -> "周打卡"
                                            com.love.diary.data.model.RecurrenceType.MONTHLY -> "月度打卡"
                                            null -> "未知"
                                        }
                                    }"
                                }
                                com.love.diary.data.model.CheckInCategory.COUNTDOWN -> {
                                    "倒计时打卡 - ${
                                        when (config.countdownMode) {
                                            com.love.diary.data.model.CountdownMode.DAY_COUNTDOWN -> "天数倒计时"
                                            com.love.diary.data.model.CountdownMode.CHECKIN_COUNTDOWN -> "次数倒计时"
                                            null -> "未知"
                                        }
                                    }"
                                }
                                null -> "未知类型"
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(16.dp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Description
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("描述（可选）") },
                        placeholder = { Text("添加一些备注信息") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2,
                        maxLines = 4
                    )

                    // Reminder settings
                    Text(
                        text = "提醒设置",
                        style = MaterialTheme.typography.labelLarge
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "启用提醒",
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Text(
                                text = if (reminderEnabled) {
                                    "每天 ${String.format("%02d:%02d", reminderHour, reminderMinute)} 提醒"
                                } else {
                                    "关闭提醒"
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = reminderEnabled,
                            onCheckedChange = { reminderEnabled = it }
                        )
                    }
                    
                    if (reminderEnabled) {
                        OutlinedButton(
                            onClick = { showTimePickerDialog = true },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Default.Schedule,
                                contentDescription = "设置时间"
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("设置提醒时间：${String.format("%02d:%02d", reminderHour, reminderMinute)}")
                        }
                    }

                    // Icon selection
                    Text(
                        text = "图标",
                        style = MaterialTheme.typography.labelLarge
                    )
                    // Icon grid: 3 rows × 4 columns = 12 icons
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val icons = when (config.checkInCategory) {
                            com.love.diary.data.model.CheckInCategory.POSITIVE -> listOf(
                                "✅", "📝", "💪", "🎯",
                                "🏃", "📚", "🎨", "🎵",
                                "🍎", "💧", "🧘", "😊"
                            )
                            com.love.diary.data.model.CheckInCategory.COUNTDOWN -> {
                                when (config.countdownMode) {
                                    com.love.diary.data.model.CountdownMode.DAY_COUNTDOWN -> listOf(
                                        "⏰", "⏳", "📅", "🎯",
                                        "🚀", "🎓", "💼", "🏆",
                                        "🎊", "🎉", "⏱️", "📆"
                                    )
                                    com.love.diary.data.model.CountdownMode.CHECKIN_COUNTDOWN -> listOf(
                                        "📅", "✅", "📝", "💪",
                                        "🎯", "🏃", "📚", "🎨",
                                        "🍎", "💧", "🧘", "😊"
                                    )
                                    null -> listOf(
                                        "🎯", "✅", "📅", "⏰",
                                        "📝", "💪", "🏃", "📚",
                                        "🎨", "🎵", "🍎", "💧"
                                    )
                                }
                            }
                            null -> listOf(
                                "🎯", "✅", "📅", "⏰",
                                "📝", "💪", "🏃", "📚",
                                "🎨", "🎵", "🍎", "💧"
                            )
                        }
                        
                        // Display icons in 3 rows with 4 icons each
                        icons.chunked(4).forEach { rowIcons ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                rowIcons.forEach { icon ->
                                    FilterChip(
                                        selected = selectedIcon == icon,
                                        onClick = { selectedIcon = icon },
                                        label = { Text(icon, style = MaterialTheme.typography.headlineSmall) },
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                                // Add empty spacers if the row has fewer than 4 icons
                                repeat(4 - rowIcons.size) {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }
                }

                // Actions
                Spacer(modifier = Modifier.height(Dimens.MediumSpacing))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("取消")
                    }
                    Spacer(modifier = Modifier.width(Dimens.SmallSpacing))
                    Button(
                        onClick = {
                            if (name.isNotBlank()) {
                                val reminderTimeStr = if (reminderEnabled) {
                                    String.format("%02d:%02d", reminderHour, reminderMinute)
                                } else {
                                    null
                                }
                                
                                onConfirm(
                                    name,
                                    selectedIcon,
                                    description.ifBlank { null },
                                    reminderTimeStr,
                                    reminderEnabled
                                )
                                onDismiss()
                            }
                        },
                        enabled = name.isNotBlank()
                    ) {
                        Text("保存")
                    }
                }
            }
        }
    }
    
    // Time picker dialog for reminder
    if (showTimePickerDialog) {
        TimePickerDialog(
            onDismiss = { showTimePickerDialog = false },
            onTimeSelected = { hour, minute ->
                reminderHour = hour
                reminderMinute = minute
                showTimePickerDialog = false
            },
            initialHour = reminderHour,
            initialMinute = reminderMinute
        )
    }
}
