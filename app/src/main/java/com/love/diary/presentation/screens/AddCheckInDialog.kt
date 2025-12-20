package com.love.diary.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.love.diary.data.model.CheckInCategory
import com.love.diary.data.model.CountdownMode
import com.love.diary.data.model.RecurrenceType
import com.love.diary.presentation.components.Dimens
import java.time.LocalDate

/**
 * Dialog for adding a new check-in item
 * Supports both POSITIVE and COUNTDOWN categories
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCheckInDialog(
    onDismiss: () -> Unit,
    onConfirm: (
        category: CheckInCategory,
        recurrenceType: RecurrenceType?,
        countdownMode: CountdownMode?,
        name: String,
        targetDate: String?,
        countdownTarget: Int?,
        description: String?,
        icon: String,
        color: String
    ) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf<CheckInCategory?>(null) }
    var selectedRecurrenceType by remember { mutableStateOf<RecurrenceType?>(null) }
    var selectedCountdownMode by remember { mutableStateOf<CountdownMode?>(null) }
    var targetDate by remember { mutableStateOf(LocalDate.now().plusDays(30).toString()) }
    var countdownTarget by remember { mutableStateOf("30") }
    var description by remember { mutableStateOf("") }
    var selectedIcon by remember { mutableStateOf("🎯") }
    var selectedColor by remember { mutableStateOf("#6200EE") }

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
                        text = "添加打卡事项",
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
                        placeholder = { Text("例如：每周运动、考试倒计时") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    // Category selection
                    Text(
                        text = "打卡类型",
                        style = MaterialTheme.typography.labelLarge
                    )
                    
                    Column(
                        verticalArrangement = Arrangement.spacedBy(Dimens.SmallSpacing)
                    ) {
                        // Positive check-in
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = selectedCategory == CheckInCategory.POSITIVE,
                                onClick = { 
                                    selectedCategory = CheckInCategory.POSITIVE
                                    selectedCountdownMode = null
                                    selectedRecurrenceType = RecurrenceType.WEEKLY
                                    selectedIcon = "✅"
                                    selectedColor = "#4CAF50"
                                }
                            )
                            Column(modifier = Modifier.padding(start = 8.dp)) {
                                Text(
                                    text = "正向打卡",
                                    style = MaterialTheme.typography.bodyLarge
                                )
                                Text(
                                    text = "周打卡或月度打卡",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        // Countdown check-in
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = selectedCategory == CheckInCategory.COUNTDOWN,
                                onClick = { 
                                    selectedCategory = CheckInCategory.COUNTDOWN
                                    selectedRecurrenceType = null
                                    selectedCountdownMode = CountdownMode.DAY_COUNTDOWN
                                    selectedIcon = "⏰"
                                    selectedColor = "#FF5722"
                                }
                            )
                            Column(modifier = Modifier.padding(start = 8.dp)) {
                                Text(
                                    text = "倒计时打卡",
                                    style = MaterialTheme.typography.bodyLarge
                                )
                                Text(
                                    text = "天数倒计时或次数倒计时",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    // Sub-type selection based on category
                    selectedCategory?.let { category ->
                        Spacer(modifier = Modifier.height(Dimens.SmallSpacing))
                        
                        when (category) {
                            CheckInCategory.POSITIVE -> {
                                Text(
                                    text = "选择打卡频率",
                                    style = MaterialTheme.typography.labelLarge
                                )
                                
                                Column(
                                    verticalArrangement = Arrangement.spacedBy(Dimens.SmallSpacing)
                                ) {
                                    // Weekly
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        RadioButton(
                                            selected = selectedRecurrenceType == RecurrenceType.WEEKLY,
                                            onClick = { selectedRecurrenceType = RecurrenceType.WEEKLY }
                                        )
                                        Text(
                                            text = "周打卡",
                                            style = MaterialTheme.typography.bodyLarge,
                                            modifier = Modifier.padding(start = 8.dp)
                                        )
                                    }
                                    
                                    // Monthly
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        RadioButton(
                                            selected = selectedRecurrenceType == RecurrenceType.MONTHLY,
                                            onClick = { selectedRecurrenceType = RecurrenceType.MONTHLY }
                                        )
                                        Text(
                                            text = "月度打卡",
                                            style = MaterialTheme.typography.bodyLarge,
                                            modifier = Modifier.padding(start = 8.dp)
                                        )
                                    }
                                }
                            }
                            
                            CheckInCategory.COUNTDOWN -> {
                                Text(
                                    text = "选择倒计时类型",
                                    style = MaterialTheme.typography.labelLarge
                                )
                                
                                Column(
                                    verticalArrangement = Arrangement.spacedBy(Dimens.SmallSpacing)
                                ) {
                                    // Day countdown
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        RadioButton(
                                            selected = selectedCountdownMode == CountdownMode.DAY_COUNTDOWN,
                                            onClick = { 
                                                selectedCountdownMode = CountdownMode.DAY_COUNTDOWN
                                                selectedIcon = "⏰"
                                                selectedColor = "#FF5722"
                                            }
                                        )
                                        Column(modifier = Modifier.padding(start = 8.dp)) {
                                            Text(
                                                text = "天数倒计时",
                                                style = MaterialTheme.typography.bodyLarge
                                            )
                                            Text(
                                                text = "按自然天数自动递减",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                    
                                    // Check-in countdown
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        RadioButton(
                                            selected = selectedCountdownMode == CountdownMode.CHECKIN_COUNTDOWN,
                                            onClick = { 
                                                selectedCountdownMode = CountdownMode.CHECKIN_COUNTDOWN
                                                selectedIcon = "📅"
                                                selectedColor = "#2196F3"
                                            }
                                        )
                                        Column(modifier = Modifier.padding(start = 8.dp)) {
                                            Text(
                                                text = "次数倒计时",
                                                style = MaterialTheme.typography.bodyLarge
                                            )
                                            Text(
                                                text = "每天打卡一次，进度递减",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                                
                                // Conditional fields for countdown types
                                Spacer(modifier = Modifier.height(Dimens.SmallSpacing))
                                
                                when (selectedCountdownMode) {
                                    CountdownMode.DAY_COUNTDOWN -> {
                                        OutlinedTextField(
                                            value = targetDate,
                                            onValueChange = { targetDate = it },
                                            label = { Text("目标日期") },
                                            placeholder = { Text("yyyy-MM-dd") },
                                            modifier = Modifier.fillMaxWidth(),
                                            singleLine = true
                                        )
                                    }
                                    CountdownMode.CHECKIN_COUNTDOWN -> {
                                        OutlinedTextField(
                                            value = countdownTarget,
                                            onValueChange = { 
                                                if (it.isEmpty() || it.toIntOrNull() != null) {
                                                    countdownTarget = it
                                                }
                                            },
                                            label = { Text("目标次数") },
                                            placeholder = { Text("需要打卡的总次数") },
                                            modifier = Modifier.fillMaxWidth(),
                                            singleLine = true,
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                                        )
                                    }
                                    null -> {}
                                }
                            }
                        }
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

                    // Icon selection
                    Text(
                        text = "图标",
                        style = MaterialTheme.typography.labelLarge
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val icons = when (selectedCategory) {
                            CheckInCategory.POSITIVE -> listOf("✅", "📝", "💪", "🎯", "🏃")
                            CheckInCategory.COUNTDOWN -> {
                                when (selectedCountdownMode) {
                                    CountdownMode.DAY_COUNTDOWN -> listOf("⏰", "⏳", "📅", "🎯", "🚀")
                                    CountdownMode.CHECKIN_COUNTDOWN -> listOf("📅", "✅", "📝", "💪", "🎯")
                                    null -> listOf("🎯", "✅", "📅", "⏰", "📝")
                                }
                            }
                            null -> listOf("🎯", "✅", "📅", "⏰", "📝")
                        }
                        
                        icons.forEach { icon ->
                            FilterChip(
                                selected = selectedIcon == icon,
                                onClick = { selectedIcon = icon },
                                label = { Text(icon, style = MaterialTheme.typography.headlineSmall) }
                            )
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
                            if (name.isNotBlank() && selectedCategory != null) {
                                val isValid = when (selectedCategory) {
                                    CheckInCategory.POSITIVE -> selectedRecurrenceType != null
                                    CheckInCategory.COUNTDOWN -> {
                                        when (selectedCountdownMode) {
                                            CountdownMode.DAY_COUNTDOWN -> targetDate.isNotBlank()
                                            CountdownMode.CHECKIN_COUNTDOWN -> {
                                                val target = countdownTarget.toIntOrNull()
                                                target != null && target > 0
                                            }
                                            null -> false
                                        }
                                    }
                                    null -> false
                                }
                                
                                if (isValid) {
                                    onConfirm(
                                        selectedCategory!!,
                                        selectedRecurrenceType,
                                        selectedCountdownMode,
                                        name,
                                        if (selectedCountdownMode == CountdownMode.DAY_COUNTDOWN) targetDate else null,
                                        if (selectedCountdownMode == CountdownMode.CHECKIN_COUNTDOWN) countdownTarget.toIntOrNull() else null,
                                        description.ifBlank { null },
                                        selectedIcon,
                                        selectedColor
                                    )
                                    onDismiss()
                                }
                            }
                        },
                        enabled = name.isNotBlank() && selectedCategory != null && when (selectedCategory) {
                            CheckInCategory.POSITIVE -> selectedRecurrenceType != null
                            CheckInCategory.COUNTDOWN -> {
                                when (selectedCountdownMode) {
                                    CountdownMode.DAY_COUNTDOWN -> targetDate.isNotBlank()
                                    CountdownMode.CHECKIN_COUNTDOWN -> {
                                        val target = countdownTarget.toIntOrNull()
                                        target != null && target > 0
                                    }
                                    null -> false
                                }
                            }
                            null -> false
                        }
                    ) {
                        Text("确定")
                    }
                }
            }
        }
    }
}
