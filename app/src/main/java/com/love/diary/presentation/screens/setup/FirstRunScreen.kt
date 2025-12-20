// presentation/screens/setup/FirstRunScreen.kt
package com.love.diary.presentation.screens.setup

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.love.diary.data.repository.AppRepository
import com.love.diary.presentation.components.AppCard
import com.love.diary.presentation.components.Dimens
import com.love.diary.presentation.components.ShapeTokens
import com.love.diary.presentation.components.TimePickerDialog
import com.love.diary.presentation.components.UnifiedDatePickerDialog
import com.love.diary.util.ReminderScheduler
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
    var avatarUri by remember { mutableStateOf<String?>(null) }
    
    // Reminder settings
    var reminderEnabled by remember { mutableStateOf(false) }
    var reminderHour by remember { mutableStateOf(9) }
    var reminderMinute by remember { mutableStateOf(0) }
    var showTimePicker by remember { mutableStateOf(false) }
    
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val dateFormatter = remember { DateTimeFormatter.ofPattern("yyyy-MM-dd") }
    
    // Avatar picker launcher
    val avatarPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            // Take persistent URI permission so the URI remains valid across app restarts
            try {
                context.contentResolver.takePersistableUriPermission(
                    it,
                    android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (e: SecurityException) {
                // Permission not available, but continue anyway
            }
            avatarUri = it.toString()
        }
    }
    
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

            // Avatar selection
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
                    .clickable { avatarPicker.launch("image/*") },
                contentAlignment = Alignment.Center
            ) {
                if (avatarUri != null) {
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(avatarUri)
                            .crossfade(true)
                            .build(),
                        contentDescription = "头像",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        imageVector = Icons.Filled.Person,
                        contentDescription = "选择头像",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(48.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "点击选择头像",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
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

            // 提醒设置卡片
            AppCard(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(Dimens.SectionSpacing)
                ) {
                    Text(
                        text = "每日提醒",
                        style = MaterialTheme.typography.titleLarge
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "开启每日提醒",
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Text(
                                text = "每天定时提醒记录心情",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = reminderEnabled,
                            onCheckedChange = { reminderEnabled = it }
                        )
                    }

                    if (reminderEnabled) {
                        OutlinedTextField(
                            value = String.format("%02d:%02d", reminderHour, reminderMinute),
                            onValueChange = { /* 只允许通过时间选择器修改 */ },
                            label = { Text("提醒时间") },
                            leadingIcon = {
                                Icon(Icons.Default.Notifications, contentDescription = null)
                            },
                            trailingIcon = {
                                IconButton(onClick = { showTimePicker = true }) {
                                    Icon(Icons.Default.KeyboardArrowDown, contentDescription = "选择时间")
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showTimePicker = true },
                            singleLine = true,
                            readOnly = true,
                            shape = ShapeTokens.Field
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
                        val reminderTimeInMinutes = reminderHour * 60 + reminderMinute
                        
                        repository.initializeFirstRun(
                            startDate = startDate,
                            coupleName = if (coupleName.isNotBlank()) coupleName else null,
                            partnerNickname = if (partnerName.isNotBlank()) partnerName else null,
                            avatarUri = avatarUri,
                            reminderEnabled = reminderEnabled,
                            reminderTime = reminderTimeInMinutes
                        )
                        
                        // Schedule reminder if enabled
                        if (reminderEnabled) {
                            val reminderScheduler = ReminderScheduler(context)
                            reminderScheduler.scheduleDailyReminder(reminderTimeInMinutes)
                        }
                        
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
    
    // 日期选择器
    if (showDatePicker) {
        UnifiedDatePickerDialog(
            onDismiss = { showDatePicker = false },
            onDateSelected = { selectedDate ->
                startDate = selectedDate
            },
            initialDate = startDate.ifEmpty { null }
        )
    }
    
    // 时间选择器
    if (showTimePicker) {
        TimePickerDialog(
            onDismiss = { showTimePicker = false },
            onTimeSelected = { hour, minute ->
                reminderHour = hour
                reminderMinute = minute
            },
            initialHour = reminderHour,
            initialMinute = reminderMinute
        )
    }
}
