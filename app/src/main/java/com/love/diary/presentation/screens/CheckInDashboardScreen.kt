package com.love.diary.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import com.love.diary.data.model.CheckInType
import com.love.diary.data.model.UnifiedCheckIn
import com.love.diary.presentation.viewmodel.CheckInViewModel
import com.love.diary.presentation.components.AppCard
import com.love.diary.presentation.components.AppScaffold
import com.love.diary.presentation.components.Dimens
import com.love.diary.presentation.components.SectionHeader
import com.love.diary.presentation.components.StatusBadge
import java.time.LocalDate
import java.time.format.DateTimeFormatter


@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun CheckInDashboardScreen(
    viewModel: CheckInViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    var selectedCheckIn by remember { mutableStateOf<UnifiedCheckIn?>(null) }
    var showCheckInCalendar by remember { mutableStateOf(false) }
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
                SectionHeader(
                    title = "最近打卡", 
                    subtitle = "历史记录一目了然"
                )
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
                    CheckInHistoryRow(checkIn = checkIn)
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
    
    // Show check-in calendar dialog
    if (showCheckInCalendar) {
        CheckInCalendarDialog(
            onDismiss = { showCheckInCalendar = false },
            onDateClick = { date ->
                val record = uiState.checkInRecords.find { it.date == date }
                if (record != null) {
                    selectedCheckIn = record
                }
            },
            checkInRecords = uiState.checkInRecords
        )
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
    checkIn: UnifiedCheckIn
) {
    AppCard(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = Dimens.CardPadding, vertical = Dimens.SectionSpacing)
    ) {
        Box(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Row 1: Name + Tag
                Row(
                    modifier = Modifier.fillMaxWidth(),
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
        CheckInType.DAY_COUNTDOWN -> "天数倒计时"
        CheckInType.CHECKIN_COUNTDOWN -> "打卡倒计时"
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
        CheckInType.DAY_COUNTDOWN -> "⏰"
        CheckInType.CHECKIN_COUNTDOWN -> "📅"
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CheckInCalendarDialog(
    onDismiss: () -> Unit,
    onDateClick: (String) -> Unit,
    checkInRecords: List<UnifiedCheckIn>
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("本月", "年历")

    val today = java.time.LocalDate.now()
    var currentMonth by remember { mutableStateOf(today) }

    // Create a map of date -> check-ins for quick lookup
    val checkInMap = remember(checkInRecords) {
        checkInRecords.groupBy { it.date }
    }

    val scrollState = rememberScrollState()

    androidx.compose.ui.window.Dialog(
        onDismissRequest = onDismiss,
        properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .padding(top = 64.dp),
            contentAlignment = Alignment.TopCenter
        ) {
            androidx.compose.material3.Surface(
                modifier = Modifier
                    .fillMaxWidth(0.92f)
                    .widthIn(max = 360.dp),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
                tonalElevation = 0.dp,
                shadowElevation = 24.dp,
                color = MaterialTheme.colorScheme.surface
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(scrollState)
                        .padding(horizontal = 16.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "打卡日历",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                        )
                        androidx.compose.material3.IconButton(onClick = onDismiss) {
                            Icon(
                                imageVector = androidx.compose.material.icons.Icons.Filled.Close,
                                contentDescription = "关闭日历"
                            )
                        }
                    }

                    // Tabs
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        tabs.forEachIndexed { index, tab ->
                            androidx.compose.material3.FilterChip(
                                selected = selectedTab == index,
                                onClick = { selectedTab = index },
                                label = { Text(tab) }
                            )
                        }
                    }

                    when (selectedTab) {
                        0 -> CheckInMonthCalendarView(
                            currentMonth = currentMonth,
                            onMonthChange = { currentMonth = it },
                            checkInMap = checkInMap,
                            onDateClick = onDateClick,
                            today = today
                        )
                        1 -> CheckInYearCalendarView(
                            year = today.year,
                            checkInMap = checkInMap,
                            onMonthClick = { month ->
                                currentMonth = java.time.LocalDate.of(today.year, month, 1)
                                selectedTab = 0
                            },
                            today = today
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CheckInMonthCalendarView(
    currentMonth: java.time.LocalDate,
    onMonthChange: (java.time.LocalDate) -> Unit,
    checkInMap: Map<String, List<UnifiedCheckIn>>,
    onDateClick: (String) -> Unit,
    today: java.time.LocalDate
) {
    val dateFormatter = remember { java.time.format.DateTimeFormatter.ofPattern("yyyy年MM月") }
    
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Month navigation
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            androidx.compose.material3.TextButton(onClick = { onMonthChange(currentMonth.minusMonths(1)) }) {
                Text("< 上月")
            }
            Text(
                text = currentMonth.format(dateFormatter),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
            )
            androidx.compose.material3.TextButton(onClick = { onMonthChange(currentMonth.plusMonths(1)) }) {
                Text("下月 >")
            }
        }

        // Day of week headers
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            listOf("日", "一", "二", "三", "四", "五", "六").forEach { day ->
                Text(
                    text = day,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.weight(1f),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Calendar grid (7x6)
        val firstDayOfMonth = currentMonth.withDayOfMonth(1)
        val daysInMonth = currentMonth.lengthOfMonth()
        val firstDayOfWeek = firstDayOfMonth.dayOfWeek.value % 7 // Sunday = 0
        
        androidx.compose.foundation.lazy.grid.LazyVerticalGrid(
            columns = androidx.compose.foundation.lazy.grid.GridCells.Fixed(7),
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 400.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Empty cells before first day
            items(firstDayOfWeek) {
                Box(modifier = Modifier.size(48.dp))
            }
            
            // Days of the month
            items(daysInMonth) { dayIndex ->
                val day = dayIndex + 1
                val date = currentMonth.withDayOfMonth(day)
                val dateStr = date.toString()
                val checkInsForDate = checkInMap[dateStr]
                val isToday = date == today
                
                CheckInDayCell(
                    day = day,
                    checkInsForDate = checkInsForDate,
                    isToday = isToday,
                    onClick = {
                        if (!checkInsForDate.isNullOrEmpty()) {
                            onDateClick(dateStr)
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun CheckInYearCalendarView(
    year: Int,
    checkInMap: Map<String, List<UnifiedCheckIn>>,
    onMonthClick: (Int) -> Unit,
    today: java.time.LocalDate
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "${year}年",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
        )

        androidx.compose.foundation.lazy.grid.LazyVerticalGrid(
            columns = androidx.compose.foundation.lazy.grid.GridCells.Fixed(3),
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 600.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(12) { monthIndex ->
                val month = monthIndex + 1
                CheckInMiniMonthGrid(
                    year = year,
                    month = month,
                    checkInMap = checkInMap,
                    onClick = { onMonthClick(month) },
                    today = today
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CheckInDayCell(
    day: Int,
    checkInsForDate: List<UnifiedCheckIn>?,
    isToday: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = when {
        isToday -> MaterialTheme.colorScheme.primaryContainer
        !checkInsForDate.isNullOrEmpty() -> MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp)
        else -> MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp)
    }

    androidx.compose.material3.Card(
        onClick = onClick,
        modifier = Modifier
            .size(48.dp)
            .border(
                width = if (isToday) 2.dp else 0.dp,
                color = if (isToday) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp)
            ),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp),
        colors = androidx.compose.material3.CardDefaults.cardColors(containerColor = backgroundColor)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            if (!checkInsForDate.isNullOrEmpty()) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Show icon of first check-in
                    Text(
                        text = checkInIcon(checkInsForDate.first().type),
                        fontSize = 16.sp
                    )
                    Text(
                        text = day.toString(),
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 10.sp
                    )
                }
            } else {
                Text(
                    text = day.toString(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun CheckInMiniMonthGrid(
    year: Int,
    month: Int,
    checkInMap: Map<String, List<UnifiedCheckIn>>,
    onClick: () -> Unit,
    today: java.time.LocalDate
) {
    val monthName = remember(month) {
        val date = java.time.LocalDate.of(year, month, 1)
        date.format(java.time.format.DateTimeFormatter.ofPattern("M月"))
    }

    androidx.compose.material3.Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
        colors = androidx.compose.material3.CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp)
        )
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = monthName,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                modifier = Modifier.padding(bottom = 4.dp)
            )

            // Mini grid showing check-in icons
            val firstDay = java.time.LocalDate.of(year, month, 1)
            val daysInMonth = firstDay.lengthOfMonth()
            val firstDayOfWeek = firstDay.dayOfWeek.value % 7

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(2.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                // Empty cells
                repeat(firstDayOfWeek) {
                    Box(modifier = Modifier.size(12.dp))
                }

                // Days
                repeat(daysInMonth) { dayIndex ->
                    val day = dayIndex + 1
                    val date = java.time.LocalDate.of(year, month, day)
                    val dateStr = date.toString()
                    val checkInsForDate = checkInMap[dateStr]

                    Box(
                        modifier = Modifier.size(12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (!checkInsForDate.isNullOrEmpty()) {
                            // Use colored circle for check-ins
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .background(
                                        MaterialTheme.colorScheme.primary,
                                        shape = androidx.compose.foundation.shape.CircleShape
                                    )
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(
                                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f),
                                        shape = androidx.compose.foundation.shape.RoundedCornerShape(2.dp)
                                    )
                            )
                        }
                    }
                }
            }
        }
    }
}
