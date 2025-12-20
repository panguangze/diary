package com.love.diary.presentation.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.clickable
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.love.diary.data.model.CheckInType
import com.love.diary.data.model.UnifiedCheckIn
import com.love.diary.presentation.viewmodel.CheckInViewModel
import com.love.diary.presentation.components.AppCard
import com.love.diary.presentation.components.AppScaffold
import com.love.diary.presentation.components.Dimens
import com.love.diary.presentation.components.SectionHeader
import com.love.diary.presentation.components.StatusBadge

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun CheckInDashboardScreen(
    viewModel: CheckInViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    var showAllCheckInsDialog by remember { mutableStateOf(false) }
    var selectedCheckIn by remember { mutableStateOf<UnifiedCheckIn?>(null) }
    val detailSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    
    AppScaffold(title = "打卡") { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(Dimens.ScreenPadding),
            verticalArrangement = Arrangement.spacedBy(Dimens.SectionSpacing),
            contentPadding = PaddingValues(bottom = Dimens.LargeSpacing)
        ) {
            item {
                SectionHeader(
                    title = "今天的状态",
                    subtitle = "选择一种方式快速记录"
                )
            }

            item {
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(Dimens.SectionSpacing),
                    verticalArrangement = Arrangement.spacedBy(Dimens.SectionSpacing),
                    maxItemsInEachRow = 2
                ) {
                    uiState.checkInTypes.forEach { checkInType ->
                        CheckInTypeCard(
                            type = checkInType,
                            onClick = { performQuickCheckIn(checkInType, viewModel) }
                        )
                    }
                }
            }

            item { 
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SectionHeader(
                        title = "最近打卡", 
                        subtitle = "历史记录一目了然",
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = "更多",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.clickable { showAllCheckInsDialog = true }
                    )
                }
            }

            if (uiState.checkInRecords.isEmpty()) {
                item {
                    AppCard(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "还没有打卡记录，先从一个类别开始吧",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                items(uiState.checkInRecords) { checkIn ->
                    CheckInHistoryRow(
                        checkIn = checkIn,
                        onMoreClick = { selectedCheckIn = checkIn }
                    )
                }
            }
        }
    }
    
    // Show detail bottom sheet when check-in is selected
    selectedCheckIn?.let { checkIn ->
        ModalBottomSheet(
            sheetState = detailSheetState,
            onDismissRequest = { selectedCheckIn = null }
        ) {
            CheckInDetailSheet(checkIn = checkIn)
        }
    }
}

@Composable
private fun CheckInTypeCard(
    type: CheckInType,
    onClick: () -> Unit
) {
    AppCard(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 120.dp),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(Dimens.SectionSpacing)
        ) {
            StatusBadge(text = checkInIcon(type), containerColor = MaterialTheme.colorScheme.primaryContainer)
            Text(
                text = checkInLabel(type),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "点击打卡",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun CheckInHistoryRow(
    checkIn: UnifiedCheckIn,
    onMoreClick: () -> Unit
) {
    AppCard(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = Dimens.CardPadding, vertical = Dimens.SectionSpacing)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Row 1: Name + Tag + More button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = checkIn.name,
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    // Show tag if available
                    checkIn.tag?.let { tag ->
                        StatusBadge(
                            text = tag,
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
                
                Text(
                    text = "更多",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.clickable { onMoreClick() }
                )
            }
            
            // Row 2: Type and Note
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "类型：${checkInLabel(checkIn.type)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                checkIn.note?.let { note ->
                    Text(
                        text = note,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
            
            // Row 3: Date and Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = checkIn.date,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                StatusBadge(
                    text = "完成",
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}

@Composable
private fun CheckInDetailSheet(
    checkIn: UnifiedCheckIn
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "打卡详情",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
        
        // Name and Tag row
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = checkIn.name,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            checkIn.tag?.let { tag ->
                StatusBadge(
                    text = tag,
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Details
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            DetailRow(label = "类型", value = checkInLabel(checkIn.type))
            DetailRow(label = "日期", value = checkIn.date)
            checkIn.note?.let { note ->
                DetailRow(label = "备注", value = note)
            }
            checkIn.moodType?.let { mood ->
                DetailRow(label = "心情", value = mood.displayName)
            }
            DetailRow(label = "状态", value = if (checkIn.isCompleted) "已完成" else "未完成")
        }
        
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

private fun performQuickCheckIn(checkInType: CheckInType, viewModel: CheckInViewModel) {
    when (checkInType) {
        CheckInType.LOVE_DIARY -> {
            viewModel.checkInLoveDiary(
                name = "恋爱日记",
                moodType = com.love.diary.data.model.MoodType.HAPPY,
                note = "美好的一天"
            )
        }

        else -> {
            viewModel.checkIn(
                name = checkInLabel(checkInType),
                type = checkInType
            )
        }
    }
}

private fun checkInLabel(checkInType: CheckInType): String {
    return when (checkInType) {
        CheckInType.LOVE_DIARY -> "恋爱时间记录"
        CheckInType.HABIT -> "习惯养成"
        CheckInType.EXERCISE -> "运动打卡"
        CheckInType.STUDY -> "学习打卡"
        CheckInType.WORKOUT -> "健身打卡"
        CheckInType.DIET -> "饮食打卡"
        CheckInType.MEDITATION -> "冥想打卡"
        CheckInType.READING -> "阅读打卡"
        CheckInType.WATER -> "喝水打卡"
        CheckInType.SLEEP -> "睡眠打卡"
        CheckInType.MILESTONE -> "里程碑事件"
        CheckInType.CUSTOM -> "自定义打卡"
    }
}

private fun checkInIcon(checkInType: CheckInType): String {
    return when (checkInType) {
        CheckInType.LOVE_DIARY -> "💕"
        CheckInType.HABIT -> "📌"
        CheckInType.EXERCISE -> "🏃‍♀️"
        CheckInType.STUDY -> "📖"
        CheckInType.WORKOUT -> "💪"
        CheckInType.DIET -> "🥗"
        CheckInType.MEDITATION -> "🧘"
        CheckInType.READING -> "📚"
        CheckInType.WATER -> "💧"
        CheckInType.SLEEP -> "🌙"
        CheckInType.MILESTONE -> "🎯"
        CheckInType.CUSTOM -> "✨"
    }
}
