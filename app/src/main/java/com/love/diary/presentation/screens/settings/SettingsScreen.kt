// presentation/screens/settings/SettingsScreen.kt
package com.love.diary.presentation.screens.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.hilt.navigation.compose.hiltViewModel
import com.love.diary.presentation.viewmodel.SettingsViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.Instant
import java.time.ZoneId
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.net.Uri
import com.love.diary.presentation.components.AppCard
import com.love.diary.presentation.components.Dimens
import com.love.diary.presentation.components.SectionHeader

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    
    // Show error/success messages
    LaunchedEffect(uiState.errorMessage, uiState.successMessage) {
        uiState.errorMessage?.let { message ->
            snackbarHostState.showSnackbar(
                message = message,
                duration = SnackbarDuration.Long
            )
            viewModel.clearMessage()
        }
        uiState.successMessage?.let { message ->
            snackbarHostState.showSnackbar(
                message = message,
                duration = SnackbarDuration.Short
            )
            viewModel.clearMessage()
        }
    }
    
    // 添加状态来控制弹窗
    var showDatePickerDialog by remember { mutableStateOf(false) }
    var showNameEditDialog by remember { mutableStateOf(false) }
    var showNicknameEditDialog by remember { mutableStateOf(false) }
    var tempInput by remember { mutableStateOf("") }
    var currentEditType by remember { mutableStateOf("") } // "start_date", "couple_name", "partner_nickname"
    
    // 添加文件选择器
    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            viewModel.importDataFromUri(it)
        }
    }
    
    // 添加导出文件选择器
    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri: Uri? ->
        uri?.let {
            viewModel.exportDataToUri(it)
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(innerPadding),
        contentPadding = PaddingValues(Dimens.ScreenPadding),
        verticalArrangement = Arrangement.spacedBy(Dimens.SectionSpacing)
    ) {

        // 关于我们的卡片
        item {
            SettingsCard(title = "关于我们") {
                SettingsItem(
                    icon = Icons.Default.Favorite,
                    title = "我们的开始",
                    subtitle = uiState.startDate ?: "未设置",
                    onClick = { 
                        currentEditType = "start_date"
                        tempInput = uiState.startDate ?: ""
                        showDatePickerDialog = true
                    }
                )

                Divider(modifier = Modifier.padding(horizontal = 16.dp))

                SettingsItem(
                    icon = Icons.Default.Person,
                    title = "我们的名字",
                    subtitle = uiState.coupleName ?: "未设置",
                    onClick = { 
                        currentEditType = "couple_name"
                        tempInput = uiState.coupleName ?: ""
                        showNameEditDialog = true
                    }
                )

                Divider(modifier = Modifier.padding(horizontal = 16.dp))

                SettingsItem(
                    icon = Icons.Default.Face,
                    title = "她的昵称",
                    subtitle = uiState.partnerNickname ?: "未设置",
                    onClick = { 
                        currentEditType = "partner_nickname"
                        tempInput = uiState.partnerNickname ?: ""
                        showNicknameEditDialog = true
                    }
                )
            }
        }

        // 显示设置
        item {
            SettingsCard(title = "显示设置") {
                ThemeSettingsItem(
                    currentDarkMode = uiState.darkMode,
                    onDarkModeChange = viewModel::setDarkMode
                )
                
                Divider(modifier = Modifier.padding(horizontal = 16.dp))
                
                SwitchSettingsItem(
                    title = "心情小提示",
                    subtitle = "在首页显示心情反馈文案",
                    checked = uiState.showMoodTip,
                    onCheckedChange = viewModel::toggleMoodTip
                )

                Divider(modifier = Modifier.padding(horizontal = 16.dp))

                SwitchSettingsItem(
                    title = "连续打卡提醒",
                    subtitle = "显示连续记录天数",
                    checked = uiState.showStreak,
                    onCheckedChange = viewModel::toggleStreak
                )

                Divider(modifier = Modifier.padding(horizontal = 16.dp))

                SwitchSettingsItem(
                    title = "纪念日提醒",
                    subtitle = "显示100天/周年纪念日",
                    checked = uiState.showAnniversary,
                    onCheckedChange = viewModel::toggleAnniversary
                )
            }
        }

        // 数据管理
        item {
            SettingsCard(title = "数据管理") {
                SettingsItem(
                    icon = Icons.Default.Download,
                    title = "导出记录",
                    subtitle = "导出所有配置和记录",
                    onClick = { 
                        val timestamp = System.currentTimeMillis()
                        val fileName = "love_diary_backup_${timestamp}.json"
                        exportLauncher.launch(fileName) 
                    }
                )

                Divider(modifier = Modifier.padding(horizontal = 16.dp))

                SettingsItem(
                    icon = Icons.Default.Upload,
                    title = "导入记录",
                    subtitle = "从备份文件恢复",
                    onClick = { importLauncher.launch("*/*") }
                )

                Divider(modifier = Modifier.padding(horizontal = 16.dp))

                SettingsItem(
                    icon = Icons.Default.Delete,
                    title = "清除所有数据",
                    subtitle = "重置应用",
                    onClick = viewModel::resetData
                )
            }
        }

        // 关于应用
        item {
            SettingsCard(title = "关于应用") {
                SettingsItem(
                    icon = Icons.Default.Info,
                    title = "版本信息",
                    subtitle = "版本 1.0.0"
                )

                Divider(modifier = Modifier.padding(horizontal = 16.dp))

                SettingsItem(
                    icon = Icons.Default.Share,
                    title = "分享应用",
                    subtitle = "推荐给其他情侣",
                    onClick = { /* 分享逻辑 */ }
                )
            }
        }

        // 底部空间
        item {
            Spacer(modifier = Modifier.height(32.dp))
            Text(
                text = "记录我们的每一个瞬间 💕",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
    
    // 添加日期选择对话框
    if (showDatePickerDialog) {
        val formatter = remember { DateTimeFormatter.ofPattern("yyyy-MM-dd") }
        val initialMillis = remember(uiState.startDate) {
            uiState.startDate?.let {
                runCatching {
                    LocalDate.parse(it, formatter)
                        .atStartOfDay()
                        .atZone(ZoneId.systemDefault())
                        .toInstant()
                        .toEpochMilli()
                }.getOrNull()
            }
        }

        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = initialMillis ?: System.currentTimeMillis()
        )

        DatePickerDialog(
            onDismissRequest = { showDatePickerDialog = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            val selectedDate = Instant.ofEpochMilli(millis)
                                .atZone(ZoneId.systemDefault())
                                .toLocalDate()
                                .format(formatter)
                            viewModel.updateStartDate(selectedDate)
                            showDatePickerDialog = false
                        }
                    }
                ) {
                    Text("确定")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePickerDialog = false }) {
                    Text("取消")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }


    // 添加名字编辑对话框
    if (showNameEditDialog) {
        AlertDialog(
            onDismissRequest = { showNameEditDialog = false },
            title = { Text("编辑我们的名字") },
            text = {
                OutlinedTextField(
                    value = tempInput,
                    onValueChange = { tempInput = it },
                    label = { Text("名字") },
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (tempInput.isNotBlank()) {
                            viewModel.updateCoupleName(tempInput)
                            showNameEditDialog = false
                        }
                    },
                    enabled = tempInput.isNotBlank()
                ) {
                    Text("确定")
                }
            },
            dismissButton = {
                TextButton(onClick = { showNameEditDialog = false }) {
                    Text("取消")
                }
            }
        )
    }

    // 添加昵称编辑对话框
    if (showNicknameEditDialog) {
        AlertDialog(
            onDismissRequest = { showNicknameEditDialog = false },
            title = { Text("编辑她的昵称") },
            text = {
                OutlinedTextField(
                    value = tempInput,
                    onValueChange = { tempInput = it },
                    label = { Text("昵称") },
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (tempInput.isNotBlank()) {
                            viewModel.updatePartnerNickname(tempInput)
                            showNicknameEditDialog = false
                        }
                    },
                    enabled = tempInput.isNotBlank()
                ) {
                    Text("确定")
                }
            },
            dismissButton = {
                TextButton(onClick = { showNicknameEditDialog = false }) {
                    Text("取消")
                }
            }
        )
    }
    }
}

@Composable
fun SettingsCard(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    AppCard(
        modifier = modifier.fillMaxWidth()
    ) {
        SectionHeader(title = title)
        Spacer(modifier = Modifier.height(Dimens.SectionSpacing))

        content()
    }
}

@Composable
fun SettingsItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    onClick: (() -> Unit)? = null
) {
    val itemModifier = if (onClick != null) {
        Modifier.clickable(onClick = onClick)
    } else {
        Modifier
    }

    Row(
        modifier = itemModifier
            .fillMaxWidth()
            .padding(horizontal = Dimens.CardPadding, vertical = Dimens.SectionSpacing / 1.5f),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        if (onClick != null) {
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.outline
            )
        }
    }
}

@Composable
fun SwitchSettingsItem(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Dimens.CardPadding, vertical = Dimens.SectionSpacing / 1.5f),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}

/**
 * Theme settings item with radio button selection
 */
@Composable
fun ThemeSettingsItem(
    currentDarkMode: Boolean?,
    onDarkModeChange: (Boolean?) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Dimens.CardPadding, vertical = Dimens.SectionSpacing / 1.5f)
    ) {
        Text(
            text = "主题设置",
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            ThemeOption(
                label = "跟随系统",
                isSelected = currentDarkMode == null,
                onClick = { onDarkModeChange(null) }
            )
            
            ThemeOption(
                label = "浅色",
                isSelected = currentDarkMode == false,
                onClick = { onDarkModeChange(false) }
            )
            
            ThemeOption(
                label = "深色",
                isSelected = currentDarkMode == true,
                onClick = { onDarkModeChange(true) }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThemeOption(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    FilterChip(
        selected = isSelected,
        onClick = onClick,
        label = { Text(label) },
        leadingIcon = if (isSelected) {
            { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp)) }
        } else null
    )
}
