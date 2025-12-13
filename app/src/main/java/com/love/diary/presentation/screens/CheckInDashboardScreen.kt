package com.love.diary.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.love.diary.data.model.CheckInType
import com.love.diary.data.model.UnifiedCheckIn
import com.love.diary.presentation.viewmodel.CheckInViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckInDashboardScreen(
    viewModel: CheckInViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // 标题
        Text(
            text = "打卡应用",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        // 打卡类型选择
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            items(uiState.checkInTypes) { checkInType ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = when(checkInType) {
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
                                CheckInType.CUSTOM -> "自定义打卡"
                            },
                            style = MaterialTheme.typography.titleMedium
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Button(
                            onClick = {
                                // 根据类型执行相应的打卡操作
                                when(checkInType) {
                                    CheckInType.LOVE_DIARY -> {
                                        viewModel.checkInLoveDiary(
                                            name = "恋爱日记",
                                            moodType = com.love.diary.data.model.MoodType.HAPPY,
                                            note = "美好的一天"
                                        )
                                    }
                                    else -> {
                                        viewModel.checkIn(
                                            name = when(checkInType) {
                                                CheckInType.HABIT -> "习惯打卡"
                                                CheckInType.EXERCISE -> "运动"
                                                CheckInType.STUDY -> "学习"
                                                CheckInType.WORKOUT -> "健身"
                                                CheckInType.DIET -> "饮食"
                                                CheckInType.MEDITATION -> "冥想"
                                                CheckInType.READING -> "阅读"
                                                CheckInType.WATER -> "喝水"
                                                CheckInType.SLEEP -> "睡眠"
                                                else -> "自定义打卡"
                                            },
                                            type = checkInType
                                        )
                                    }
                                }
                            }
                        ) {
                            Text("打卡")
                        }
                    }
                }
            }
        }
        
        // 打卡记录显示
        LazyColumn(
            modifier = Modifier
                .height(200.dp)
                .fillMaxWidth()
        ) {
            items(uiState.checkInRecords) { checkIn ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = checkIn.name,
                            style = MaterialTheme.typography.titleSmall
                        )
                        Text(
                            text = "类型: ${checkIn.type.name}",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text(
                            text = "日期: ${checkIn.date}",
                            style = MaterialTheme.typography.bodySmall
                        )
                        checkIn.note?.let { note ->
                            Text(
                                text = "备注: $note",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }
        }
    }
}