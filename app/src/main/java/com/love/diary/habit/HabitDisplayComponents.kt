package com.love.diary.habit

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.love.diary.data.model.Habit
import com.love.diary.data.model.PositiveDisplayType
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import kotlin.math.roundToInt
import kotlinx.coroutines.flow.first

/**
 * 根据打卡类型展示不同的打卡组件
 */
@Composable
fun HabitDisplayView(
    habit: Habit,
    modifier: Modifier = Modifier
) {
    when {
        habit.type == com.love.diary.data.model.HabitType.COUNTDOWN -> {
            CountdownProgressView(habit, modifier)
        }
        habit.displayType == PositiveDisplayType.WEEKLY -> {
            WeeklyDisplayView(habit, modifier)
        }
        habit.displayType == PositiveDisplayType.MONTHLY -> {
            MonthlyDisplayView(habit, modifier)
        }
        else -> {
            WeeklyDisplayView(habit, modifier) // 默认使用周展示
        }
    }
}

/**
 * 倒计时打卡进度条
 */
@Composable
fun CountdownProgressView(
    habit: Habit,
    modifier: Modifier = Modifier
) {
    val targetDate = habit.targetDate?.let { LocalDate.parse(it) }
    val startDate = LocalDate.parse(habit.startDate)
    val currentDate = LocalDate.now()

    val totalDays = if (targetDate != null) {
        ChronoUnit.DAYS.between(startDate, targetDate).toInt()
    } else 0
    
    val elapsedDays = if (targetDate != null) {
        ChronoUnit.DAYS.between(startDate, currentDate).toInt()
    } else 0

    val progress = if (totalDays > 0) {
        (elapsedDays.toFloat() / totalDays).coerceIn(0f, 1f)
    } else 0f

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
        )
        
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "开始: ${formatDate(startDate)}",
                style = MaterialTheme.typography.bodySmall
            )
            
            Text(
                text = "目标: ${formatDate(targetDate ?: startDate)}",
                style = MaterialTheme.typography.bodySmall
            )
        }
        
        // Add progress percentage text
        Text(
            text = "进度: ${(progress * 100).toInt()}% (已过 $elapsedDays / 共 $totalDays 天)",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

/**
 * 周展示视图 - 显示一周的打卡情况
 */
@Composable
fun WeeklyDisplayView(
    habit: Habit,
    modifier: Modifier = Modifier
) {
    var showMore by remember { mutableStateOf(false) }
    val context = androidx.compose.ui.platform.LocalContext.current
    val database = com.love.diary.data.database.LoveDatabase.getInstance(context)
    
    // 获取历史打卡记录
    val checkInRecords = remember(habit.id, showMore) {
        mutableStateOf<List<String>>(emptyList())
    }
    
    LaunchedEffect(habit.id, showMore) {
        val records = database.habitDao().getHabitRecordsFlow(habit.id).first()
        checkInRecords.value = records.map { it.date }
    }
    
    val today = LocalDate.now()
    val currentWeekStart = today.minusDays(today.dayOfWeek.value.toLong() - 1) // 从周一算起
    
    // 显示当前周或最近4周
    val weeksToShow = if (showMore) 4 else 1
    
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        repeat(weeksToShow) { weekIndex ->
            val weekStart = currentWeekStart.minusWeeks(weekIndex.toLong())
            val weekDays = (0..6).map { day ->
                weekStart.plusDays(day.toLong())
            }
            
            // Show week label for multiple weeks
            if (weeksToShow > 1) {
                Text(
                    text = if (weekIndex == 0) "本周" else "${weekIndex}周前",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 4.dp)
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                weekDays.forEach { day ->
                    val isToday = day == today
                    val isChecked = checkInRecords.value.contains(day.toString())
                    
                    // Get tag color if available
                    val tagColor = if (habit.tags.isNotEmpty() && isChecked) {
                        val tags = habit.tags.split(",").filter { it.isNotEmpty() }
                        if (tags.isNotEmpty()) {
                            com.love.diary.ui.theme.TagColors.getOrNull(0) // Use first tag color
                        } else null
                    } else null
                    
                    DayCheckInBox(
                        day = day,
                        isChecked = isChecked,
                        isToday = isToday,
                        showWeekday = true,  // Show weekday in weekly view
                        tagColor = tagColor,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
        
        // "更多" button
        TextButton(
            onClick = { showMore = !showMore },
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            Text(if (showMore) "收起" else "更多")
            Icon(
                imageVector = if (showMore) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                contentDescription = if (showMore) "收起" else "更多"
            )
        }
    }
}

/**
 * 月展示视图 - 显示当月日历和打卡情况
 */
@Composable
fun MonthlyDisplayView(
    habit: Habit,
    modifier: Modifier = Modifier
) {
    var showMore by remember { mutableStateOf(false) }
    val context = androidx.compose.ui.platform.LocalContext.current
    val database = com.love.diary.data.database.LoveDatabase.getInstance(context)
    
    // 获取历史打卡记录
    val checkInRecords = remember(habit.id, showMore) {
        mutableStateOf<List<String>>(emptyList())
    }
    
    LaunchedEffect(habit.id, showMore) {
        val records = database.habitDao().getHabitRecordsFlow(habit.id).first()
        checkInRecords.value = records.map { it.date }
    }
    
    val currentMonth = YearMonth.now()
    val monthsToShow = if (showMore) 3 else 1
    
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        repeat(monthsToShow) { monthIndex ->
            val displayMonth = currentMonth.minusMonths(monthIndex.toLong())
            
            Column {
                Text(
                    text = if (monthIndex == 0) 
                        "${displayMonth.year}年${displayMonth.month.value}月（本月）" 
                    else 
                        "${displayMonth.year}年${displayMonth.month.value}月",
                    style = MaterialTheme.typography.titleMedium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                
                // 星期标题行
                WeekdayHeaders()
                
                // 日历网格
                val daysInMonth = displayMonth.lengthOfMonth()
                val firstDayOfWeek = displayMonth.atDay(1).dayOfWeek.value
                
                LazyVerticalGrid(
                    columns = GridCells.Fixed(7),
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(7f / 5f)
                ) {
                    // 添加空白占位符以对齐第一个日期
                    repeat(firstDayOfWeek - 1) {
                        item {
                            Spacer(modifier = Modifier.fillMaxSize())
                        }
                    }
                    
                    items(daysInMonth) { dayIndex ->
                        val day = dayIndex + 1  // Convert 0-based index to 1-based day
                        val date = displayMonth.atDay(day)
                        val isToday = date == LocalDate.now()
                        val isChecked = checkInRecords.value.contains(date.toString())
                        
                        // Get tag color if available
                        val tagColor = if (habit.tags.isNotEmpty() && isChecked) {
                            val tags = habit.tags.split(",").filter { it.isNotEmpty() }
                            if (tags.isNotEmpty()) {
                                com.love.diary.ui.theme.TagColors.getOrNull(0) // Use first tag color
                            } else null
                        } else null
                        
                        DayCheckInBox(
                            day = date,
                            isChecked = isChecked,
                            isToday = isToday,
                            tagColor = tagColor,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(2.dp)
                        )
                    }
                }
            }
        }
        
        // "更多" button
        TextButton(
            onClick = { showMore = !showMore },
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            Text(if (showMore) "收起" else "更多")
            Icon(
                imageVector = if (showMore) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                contentDescription = if (showMore) "收起" else "更多"
            )
        }
    }
}

/**
 * 显示星期标题
 */
@Composable
private fun WeekdayHeaders() {
    val weekdays = listOf("一", "二", "三", "四", "五", "六", "日")
    
    Row(
        modifier = Modifier.fillMaxWidth()
    ) {
        weekdays.forEach { day ->
            Text(
                text = day,
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .weight(1f)
                    .padding(vertical = 4.dp)
            )
        }
    }
}

/**
 * 单个日期打卡框
 */
@Composable
fun DayCheckInBox(
    day: LocalDate,
    isChecked: Boolean,
    isToday: Boolean,
    showWeekday: Boolean = false,  // Whether to show weekday name instead of date
    tagColor: Color? = null,  // Tag color to fill when checked
    modifier: Modifier = Modifier
) {
    val displayText = if (showWeekday) {
        // Show weekday name (一, 二, 三, etc.)
        when (day.dayOfWeek) {
            DayOfWeek.MONDAY -> "一"
            DayOfWeek.TUESDAY -> "二"
            DayOfWeek.WEDNESDAY -> "三"
            DayOfWeek.THURSDAY -> "四"
            DayOfWeek.FRIDAY -> "五"
            DayOfWeek.SATURDAY -> "六"
            DayOfWeek.SUNDAY -> "日"
        }
    } else {
        // Show day number
        val formatter = DateTimeFormatter.ofPattern("d")
        day.format(formatter)
    }
    
    val backgroundColor = when {
        isChecked && tagColor != null -> tagColor.copy(alpha = 0.8f)
        isChecked -> MaterialTheme.colorScheme.primaryContainer
        else -> Color.Transparent
    }
    
    val textColor = when {
        isChecked && tagColor != null -> Color.White
        isChecked -> MaterialTheme.colorScheme.onPrimaryContainer
        else -> MaterialTheme.colorScheme.onSurface
    }
    
    val borderColor = when {
        isToday && tagColor != null -> tagColor
        isToday -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.outline
    }
    
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .border(
                width = if (isToday) 2.dp else 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(8.dp)
            )
            .background(
                color = backgroundColor,
                shape = RoundedCornerShape(8.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = displayText,
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = textColor,
                    fontWeight = if (isToday) androidx.compose.ui.text.font.FontWeight.Bold else androidx.compose.ui.text.font.FontWeight.Normal
                )
            )
            if (showWeekday && isToday) {
                Text(
                    text = day.format(DateTimeFormatter.ofPattern("d")),
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = textColor.copy(alpha = 0.8f)
                    )
                )
            }
        }
    }
}

/**
 * 显示打卡统计信息：当前连续天数、最长连续天数、累计打卡次数
 */
@Composable
fun HabitStatsView(
    habit: Habit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        StatItem(
            label = "当前连续",
            value = habit.currentStreak.toString(),
            color = MaterialTheme.colorScheme.primary
        )
        
        Divider(
            modifier = Modifier
                .fillMaxHeight()
                .width(1.dp),
            color = MaterialTheme.colorScheme.outline
        )
        
        StatItem(
            label = "最长连续",
            value = habit.longestStreak.toString(),
            color = MaterialTheme.colorScheme.secondary
        )
        
        Divider(
            modifier = Modifier
                .fillMaxHeight()
                .width(1.dp),
            color = MaterialTheme.colorScheme.outline
        )
        
        StatItem(
            label = "累计打卡",
            value = habit.totalCheckIns.toString(),
            color = MaterialTheme.colorScheme.tertiary
        )
    }
}

/**
 * 统计项组件
 */
@Composable
private fun StatItem(
    label: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.headlineSmall.copy(color = color),
            textAlign = TextAlign.Center
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * 格式化日期显示
 */
private fun formatDate(date: LocalDate?): String {
    return date?.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) ?: ""
}