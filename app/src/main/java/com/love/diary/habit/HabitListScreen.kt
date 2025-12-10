package com.love.diary.habit

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.clickable
import java.time.format.DateTimeFormatter
import androidx.compose.ui.ExperimentalComposeUiApi
import com.love.diary.data.model.Habit
import com.love.diary.data.model.HabitType
import java.time.LocalDate
import android.content.Context
import androidx.compose.ui.platform.LocalContext

@OptIn(androidx.compose.ui.ExperimentalComposeUiApi::class)
@Composable
fun HabitListScreen(
    modifier: Modifier = Modifier,
    context: Context = LocalContext.current,
    habitRepository: HabitRepository = remember { HabitRepository.getInstance(context) }
) {
    val habits by habitRepository.getAllHabits().collectAsState(initial = emptyList())
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "打卡事项",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
            
            FloatingActionButton(
                onClick = { /* 添加新习惯的逻辑 */ }
            ) {
                Icon(Icons.Default.Add, contentDescription = "添加打卡")
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        LazyColumn {
            items(habits) { habit ->
                HabitItemCard(
                    habit = habit,
                    onToggle = { 
                        habitRepository.toggleHabit(it)
                    },
                    onClick = { /* 导航到详情页 */ }
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
fun HabitItemCard(
    habit: Habit,
    onToggle: (Long) -> Unit,
    onClick: (Long) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick(habit.id) },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = habit.name,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Text(
                        text = when (habit.type) {
                            HabitType.POSITIVE -> "已打卡 ${habit.currentCount} 次"
                            HabitType.COUNTDOWN -> {
                                val targetDate = habit.targetDate?.let { LocalDate.parse(it) }
                                val today = LocalDate.now()
                                val daysLeft = if (targetDate != null) {
                                    java.time.temporal.ChronoUnit.DAYS.between(today, targetDate).toInt()
                                } else 0
                                "距离目标还有 $daysLeft 天"
                            }
                        },
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Text(
                        text = if (habit.isCompletedToday) "今日已完成" else "今日未完成",
                        fontSize = 12.sp,
                        color = if (habit.isCompletedToday) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                    )
                }
                
                Button(
                    onClick = { onToggle(habit.id) }
                ) {
                    Text(habit.buttonLabel)
                }
            }
        }
    }
}