// presentation/screens/settings/SettingsScreen.kt
package com.love.diary.presentation.screens.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.style.TextAlign
import androidx.hilt.navigation.compose.hiltViewModel
import com.love.diary.presentation.viewmodel.SettingsViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 标题
        item {
            Text(
                text = "我们的设置",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
        }

        // 关于我们的卡片
        item {
            SettingsCard(title = "关于我们") {
                SettingsItem(
                    icon = Icons.Default.Favorite,
                    title = "我们的开始",
                    subtitle = uiState.startDate ?: "未设置",
                    onClick = { /* 打开日期选择器 */ }
                )

                Divider(modifier = Modifier.padding(horizontal = 16.dp))

                SettingsItem(
                    icon = Icons.Default.Person,
                    title = "我们的名字",
                    subtitle = uiState.coupleName ?: "未设置",
                    onClick = { /* 打开名称编辑 */ }
                )

                Divider(modifier = Modifier.padding(horizontal = 16.dp))

                SettingsItem(
                    icon = Icons.Default.Face,
                    title = "她的昵称",
                    subtitle = uiState.partnerNickname ?: "未设置",
                    onClick = { /* 打开昵称编辑 */ }
                )
            }
        }

        // 显示设置
        item {
            SettingsCard(title = "显示设置") {
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
                    subtitle = "导出所有心情记录",
                    onClick = viewModel::exportData
                )

                Divider(modifier = Modifier.padding(horizontal = 16.dp))

                SettingsItem(
                    icon = Icons.Default.Upload,
                    title = "导入记录",
                    subtitle = "从备份文件恢复",
                    onClick = viewModel::importData
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
}

@Composable
fun SettingsCard(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp)
        )
    ) {
        Column(
            modifier = Modifier.padding(vertical = 8.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(start = 16.dp, top = 12.dp, bottom = 8.dp)
            )

            content()
        }
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
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary
        )

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge
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
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge
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