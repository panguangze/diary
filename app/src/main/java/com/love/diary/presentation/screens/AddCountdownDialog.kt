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
import com.love.diary.data.model.CountdownMode
import com.love.diary.presentation.components.Dimens
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * Dialog for adding a new countdown check-in
 * Supports both DAY_COUNTDOWN and CHECKIN_COUNTDOWN modes
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCountdownDialog(
    onDismiss: () -> Unit,
    initialMode: CountdownMode? = null,
    onConfirm: (
        name: String,
        countdownMode: CountdownMode,
        targetDate: String?,
        countdownTarget: Int?,
        tag: String?,
        description: String?,
        icon: String,
        color: String
    ) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var selectedMode by remember { mutableStateOf(initialMode ?: CountdownMode.DAY_COUNTDOWN) }
    var targetDate by remember { mutableStateOf(LocalDate.now().plusDays(30).toString()) }
    var countdownTarget by remember { mutableStateOf("30") }
    var tag by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedIcon by remember { 
        mutableStateOf(
            when (initialMode) {
                CountdownMode.CHECKIN_COUNTDOWN -> "📅"
                else -> "⏰"
            }
        )
    }
    var selectedColor by remember { 
        mutableStateOf(
            when (initialMode) {
                CountdownMode.CHECKIN_COUNTDOWN -> "#2196F3"
                else -> "#FF5722"
            }
        )
    }
    
    val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

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
                        text = "添加倒计时打卡",
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
                        label = { Text("倒计时名称") },
                        placeholder = { Text("例如：考试倒计时、生日倒计时") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    // Mode selection
                    Text(
                        text = "倒计时类型",
                        style = MaterialTheme.typography.labelLarge
                    )
                    
                    Column(
                        verticalArrangement = Arrangement.spacedBy(Dimens.SmallSpacing)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = selectedMode == CountdownMode.DAY_COUNTDOWN,
                                onClick = { 
                                    selectedMode = CountdownMode.DAY_COUNTDOWN
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
                                    text = "按自然天数自动递减，不需要打卡",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = selectedMode == CountdownMode.CHECKIN_COUNTDOWN,
                                onClick = { 
                                    selectedMode = CountdownMode.CHECKIN_COUNTDOWN
                                    selectedIcon = "📅"
                                    selectedColor = "#2196F3"
                                }
                            )
                            Column(modifier = Modifier.padding(start = 8.dp)) {
                                Text(
                                    text = "打卡倒计时",
                                    style = MaterialTheme.typography.bodyLarge
                                )
                                Text(
                                    text = "需要每天打卡，打卡一次进度才变化",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    // Conditional fields based on mode
                    when (selectedMode) {
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
                                label = { Text("目标天数") },
                                placeholder = { Text("需要打卡的总天数") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                            )
                            
                            OutlinedTextField(
                                value = tag,
                                onValueChange = { tag = it },
                                label = { Text("标签（可选）") },
                                placeholder = { Text("例如：学习、运动") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )
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
                        val icons = if (selectedMode == CountdownMode.DAY_COUNTDOWN) {
                            listOf("⏰", "⏳", "📅", "🎯", "🚀")
                        } else {
                            listOf("📅", "✅", "📝", "💪", "🎯")
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
                            if (name.isNotBlank()) {
                                when (selectedMode) {
                                    CountdownMode.DAY_COUNTDOWN -> {
                                        onConfirm(
                                            name,
                                            selectedMode,
                                            targetDate,
                                            null,
                                            null,
                                            description.ifBlank { null },
                                            selectedIcon,
                                            selectedColor
                                        )
                                    }
                                    CountdownMode.CHECKIN_COUNTDOWN -> {
                                        val target = countdownTarget.toIntOrNull()
                                        if (target != null && target > 0) {
                                            onConfirm(
                                                name,
                                                selectedMode,
                                                null,
                                                target,
                                                tag.ifBlank { null },
                                                description.ifBlank { null },
                                                selectedIcon,
                                                selectedColor
                                            )
                                        }
                                    }
                                }
                                onDismiss()
                            }
                        },
                        enabled = name.isNotBlank() && when (selectedMode) {
                            CountdownMode.DAY_COUNTDOWN -> targetDate.isNotBlank()
                            CountdownMode.CHECKIN_COUNTDOWN -> {
                                val target = countdownTarget.toIntOrNull()
                                target != null && target > 0
                            }
                        }
                    ) {
                        Text("确定")
                    }
                }
            }
        }
    }
}
