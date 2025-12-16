// presentation/screens/setup/FirstRunScreen.kt
package com.love.diary.presentation.screens.setup

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import com.love.diary.data.repository.AppRepository
import com.love.diary.presentation.components.AppCard
import com.love.diary.presentation.components.Dimens
import com.love.diary.presentation.components.ShapeTokens
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)

@Composable
fun FirstRunScreen(
    repository: AppRepository,
    onSetupComplete: () -> Unit,
    modifier: Modifier = Modifier
) {
    var startDate by remember { mutableStateOf("") }
    var coupleName by remember { mutableStateOf("") }
    var yourName by remember { mutableStateOf("") }
    var partnerName by remember { mutableStateOf("") }
    var showDatePicker by remember { mutableStateOf(false) }
    
    val coroutineScope = rememberCoroutineScope()
    val dateFormatter = remember { DateTimeFormatter.ofPattern("yyyy-MM-dd") }
    
    // 默认使用今天的日期作为初始值
    LaunchedEffect(Unit) {
        if (startDate.isEmpty()) {
            startDate = LocalDate.now().format(dateFormatter)
        }
    }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Dimens.ScreenPadding)
                .padding(top = Dimens.LargeSpacing, bottom = Dimens.ScreenPadding),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 标题
            Text(
                text = "欢迎使用恋爱日记",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(Dimens.SectionSpacing))

            Text(
                text = "先完成基础信息，之后就可以开始记录与打卡。",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(Dimens.LargeSpacing))

            AppCard(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(Dimens.SectionSpacing)
                ) {
                    Text(
                        text = "让我们开始记录吧",
                        style = MaterialTheme.typography.titleLarge
                    )

                    OutlinedTextField(
                        value = startDate,
                        onValueChange = { /* 只允许通过日期选择器修改 */ },
                        label = { Text("恋爱开始日期") },
                        placeholder = { Text("例如：2023-06-01") },
                        leadingIcon = {
                            Icon(Icons.Default.DateRange, contentDescription = null)
                        },
                        trailingIcon = {
                            IconButton(onClick = { showDatePicker = true }) {
                                Icon(Icons.Default.KeyboardArrowDown, contentDescription = "选择日期")
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showDatePicker = true },
                        singleLine = true,
                        readOnly = true,
                        shape = ShapeTokens.Field
                    )
                    
                    // 日期选择器
                    if (showDatePicker) {
                        // 确保有有效的日期用于初始化日期选择器
                        val initialDate = try {
                            if (startDate.isNotEmpty()) {
                                LocalDate.parse(startDate)
                            } else {
                                LocalDate.now()
                            }
                        } catch (e: Exception) {
                            LocalDate.now()
                        }
                        
                        val datePickerState = rememberDatePickerState(
                            initialSelectedDateMillis = initialDate.atStartOfDay().atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()
                        )
                        
                        DatePickerDialog(
                            onDismissRequest = { showDatePicker = false },
                            confirmButton = {
                                TextButton(
                                    onClick = {
                                        val selectedDate = datePickerState.selectedDateMillis
                                        if (selectedDate != null) {
                                            val newDate = java.util.Date(selectedDate).toInstant()
                                                .atZone(java.time.ZoneId.systemDefault())
                                                .toLocalDate()
                                            startDate = newDate.format(dateFormatter)
                                        }
                                        showDatePicker = false
                                    }
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
                            }
                        ) {
                            DatePicker(
                                state = datePickerState
                            )
                        }
                    }

                    // 组合名字
                    OutlinedTextField(
                        value = coupleName,
                        onValueChange = { coupleName = it },
                        label = { Text("给我们的组合起个名字") },
                        placeholder = { Text("例如：小猫和大熊") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    // 个人昵称
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(Dimens.SectionSpacing),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = yourName,
                            onValueChange = { yourName = it },
                            label = { Text("你的昵称") },
                            placeholder = { Text("例如：小猫") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = partnerName,
                            onValueChange = { partnerName = it },
                            label = { Text("TA的昵称") },
                            placeholder = { Text("例如：大熊") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(Dimens.LargeSpacing))

            // 说明文字
            Text(
                text = "这些信息将用于计算恋爱天数，生成专属的恋爱日记。",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = Dimens.ScreenPadding)
            )

            Spacer(modifier = Modifier.height(Dimens.LargeSpacing))

            // 开始按钮
            Button(
                onClick = {
                    coroutineScope.launch {
                        repository.initializeFirstRun(
                            startDate = startDate,
                            coupleName = if (coupleName.isNotBlank()) coupleName else null,
                            partnerNickname = if (partnerName.isNotBlank()) partnerName else null
                        )
                        
                        onSetupComplete()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = startDate.isNotBlank() && yourName.isNotBlank() && partnerName.isNotBlank()
            ) {
                Text(text = "开始使用")
            }
        }
    }
}
