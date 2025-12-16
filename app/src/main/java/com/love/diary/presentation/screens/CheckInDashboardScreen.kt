package com.love.diary.presentation.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import com.love.diary.data.model.CheckInType
import com.love.diary.data.model.UnifiedCheckIn
import com.love.diary.presentation.viewmodel.CheckInViewModel
import com.love.diary.presentation.components.AppCard
import com.love.diary.presentation.components.AppScaffold
import com.love.diary.presentation.components.Dimens

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckInDashboardScreen(
    viewModel: CheckInViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    AppScaffold(title = "打卡") { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(Dimens.ScreenPadding),
            verticalArrangement = Arrangement.spacedBy(Dimens.SectionSpacing)
        ) {
            // 打卡类型选择
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(Dimens.SectionSpacing)
            ) {
                items(uiState.checkInTypes) { checkInType ->
                    AppCard(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(Dimens.SectionSpacing)
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
                                    CheckInType.MILESTONE -> "里程碑事件"
                                    CheckInType.CUSTOM -> "自定义打卡"
                                },
                                style = MaterialTheme.typography.titleMedium
                            )
                            
                            Button(
                                onClick = {
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
                                                    CheckInType.MILESTONE -> "里程碑事件"
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
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(Dimens.SectionSpacing)
            ) {
                items(uiState.checkInRecords) { checkIn ->
                    AppCard(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(Dimens.SectionSpacing / 2)
                        ) {
                            Text(
                                text = checkIn.name,
                                style = MaterialTheme.typography.titleSmall
                            )
                            Text(
                                text = "类型: ${checkIn.type.name}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "日期: ${checkIn.date}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            checkIn.note?.let { note ->
                                Text(
                                    text = "备注: $note",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
