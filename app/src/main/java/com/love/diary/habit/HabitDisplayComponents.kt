package com.love.diary.habit

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.love.diary.data.model.Habit
import com.love.diary.data.model.PositiveDisplayType
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import kotlin.math.roundToInt

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
            progress = progress,
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
    val today = LocalDate.now()
    val weekStart = today.minusDays(today.dayOfWeek.value.toLong() - 1) // 从周一算起
    
    val weekDays = (0..6).map { day ->
        weekStart.plusDays(day)
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        weekDays.forEach { day ->
            val isToday = day == today
            val isChecked = isToday && habit.isCompletedToday
            
            DayCheckInBox(
                day = day,
                isChecked = isChecked,
                isToday = isToday,
                modifier = Modifier.weight(1f)
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
    val currentMonth = YearMonth.now()
    
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Text(
            text = "${currentMonth.year}年${currentMonth.month.value}月",
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        
        // 星期标题行
        WeekdayHeaders()
        
        // 日历网格
        val daysInMonth = currentMonth.lengthOfMonth()
        val firstDayOfWeek = currentMonth.atDay(1).dayOfWeek.value
        
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
            
            items(daysInMonth) { day ->
                val date = currentMonth.atDay(day)
                val isToday = date == LocalDate.now()
                val isChecked = isToday && habit.isCompletedToday
                
                DayCheckInBox(
                    day = date,
                    isChecked = isChecked,
                    isToday = isToday,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(2.dp)
                )
            }
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
    modifier: Modifier = Modifier
) {
    val formatter = DateTimeFormatter.ofPattern("d")
    val dayNumber = day.format(formatter)
    
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .border(
                width = if (isToday) 2.dp else 1.dp,
                color = if (isToday) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                shape = RoundedCornerShape(4.dp)
            )
            .background(
                color = if (isChecked) MaterialTheme.colorScheme.primaryContainer else Color.Transparent
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = dayNumber,
            style = MaterialTheme.typography.bodySmall.copy(
                color = if (isChecked) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
            )
        )
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