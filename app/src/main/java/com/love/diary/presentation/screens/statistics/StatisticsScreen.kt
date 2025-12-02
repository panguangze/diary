package com.love.diary.presentation.screens.statistics

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.love.diary.data.model.MoodType
import com.love.diary.presentation.viewmodel.StatisticsViewModel
import kotlin.math.roundToInt

@Composable
fun StatisticsScreen(
    viewModel: StatisticsViewModel = viewModel(),
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    if (uiState.isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    } else {
        StatisticsContent(uiState = uiState, viewModel = viewModel, modifier = modifier)
    }
}

@Composable
private fun StatisticsContent(
    uiState: StatisticsViewModel.StatisticsUiState,
    viewModel: StatisticsViewModel,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(16.dp)
    ) {
        // 标题
        item {
            Text(
                text = "心情统计",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "最近 ${uiState.selectedDays} 天的记录",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // 时间范围选择
        item {
            TimeRangeSelector(
                selectedDays = uiState.selectedDays,
                onDaysSelected = viewModel::updateTimeRange
            )
        }

        // 统计概览卡片
        item {
            StatisticsOverviewCard(uiState = uiState)
        }

        // 心情分布
        item {
            MoodDistributionCard(
                moodStats = uiState.moodStats,
                totalRecords = uiState.totalRecords
            )
        }

        // 心情趋势
        item {
            MoodTrendCard(trendData = uiState.moodTrend)
        }

        // 统计总结
        item {
            StatisticsSummaryCard(uiState = uiState)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimeRangeSelector(
    selectedDays: Int,
    onDaysSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val timeRanges = listOf(7, 30, 90, 365)

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            timeRanges.forEach { days ->
                FilterChip(
                    selected = selectedDays == days,
                    onClick = { onDaysSelected(days) },
                    label = {
                        Text(
                            text = when (days) {
                                7 -> "最近7天"
                                30 -> "最近30天"
                                90 -> "最近90天"
                                365 -> "全年"
                                else -> "$days 天"
                            }
                        )
                    },
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
            }
        }
    }
}

@Composable
fun StatisticsOverviewCard(
    uiState: StatisticsViewModel.StatisticsUiState,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "统计概览",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                StatItem(
                    title = "记录天数",
                    value = uiState.totalRecords.toString(),
                    icon = Icons.Default.DateRange
                )

                StatItem(
                    title = "平均心情",
                    value = uiState.averageMood,
                    icon = Icons.Default.TrendingUp
                )

                StatItem(
                    title = "最常心情",
                    value = uiState.topMood?.emoji ?: "-",
                    icon = Icons.Default.EmojiEmotions
                )
            }
        }
    }
}

@Composable
fun StatItem(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onPrimaryContainer,
            modifier = Modifier.size(24.dp)
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = value,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )

        Text(
            text = title,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
        )
    }
}

@Composable
fun MoodDistributionCard(
    moodStats: Map<MoodType, Int>,
    totalRecords: Int,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "心情分布",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(12.dp))

            MoodType.values().forEach { moodType ->
                val count = moodStats[moodType] ?: 0
                val percentage = if (totalRecords > 0) {
                    (count.toFloat() / totalRecords * 100).roundToInt()
                } else 0

                if (count > 0) {
                    MoodDistributionItem(
                        moodType = moodType,
                        count = count,
                        percentage = percentage,
                        totalRecords = totalRecords
                    )

                    if (moodType != MoodType.values().last()) {
                        Divider(modifier = Modifier.padding(vertical = 4.dp))
                    }
                }
            }

            if (totalRecords == 0) {
                Text(
                    text = "暂无记录",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 16.dp)
                )
            }
        }
    }
}

@Composable
fun MoodDistributionItem(
    moodType: MoodType,
    count: Int,
    percentage: Int,
    totalRecords: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 心情图标
        Text(
            text = moodType.emoji,
            fontSize = MaterialTheme.typography.titleLarge.fontSize,
            modifier = Modifier.size(32.dp)
        )

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = moodType.displayName,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )

                Text(
                    text = "$count 次 ($percentage%)",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // 进度条
            LinearProgressIndicator(
                progress = if (totalRecords > 0) count.toFloat() / totalRecords else 0f,
                modifier = Modifier.fillMaxWidth(),
                color = getMoodColor(moodType),
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
        }
    }
}

@Composable
fun MoodTrendCard(
    trendData: List<Pair<String, Int>>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "心情趋势",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(12.dp))

            if (trendData.isNotEmpty()) {
                // 简单的趋势图表
                SimpleTrendChart(trendData = trendData)
            } else {
                Text(
                    text = "暂无趋势数据",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 16.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "记录每一天的心情，有起伏才像真实的生活。",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
            )
        }
    }
}

@Composable
fun SimpleTrendChart(
    trendData: List<Pair<String, Int>>,
    modifier: Modifier = Modifier
) {
    val maxScore = trendData.maxOfOrNull { it.second } ?: 2
    val minScore = trendData.minOfOrNull { it.second } ?: -2
    val scoreRange = maxScore - minScore

    // 在 Canvas 外部获取颜色值
    val gridColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
    val lineColor = MaterialTheme.colorScheme.primary
    val textColor = MaterialTheme.colorScheme.onSurfaceVariant

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(200.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = 1.dp.toPx()

            // 水平网格线
            for (i in 0..4) {
                val y = size.height * i / 4f
                drawLine(
                    color = gridColor,
                    start = Offset(0f, y),
                    end = Offset(size.width, y),
                    strokeWidth = strokeWidth
                )
            }

            // 垂直网格线（如果需要）
            if (trendData.size > 1) {
                for (i in trendData.indices) {
                    val x = size.width * i / (trendData.size - 1).toFloat()
                    drawLine(
                        color = gridColor,
                        start = Offset(x, 0f),
                        end = Offset(x, size.height),
                        strokeWidth = strokeWidth
                    )
                }
            }

            // 绘制趋势线
            if (trendData.size > 1 && scoreRange > 0) {
                val points = trendData.mapIndexed { index, (_, score) ->
                    val x = size.width * index / (trendData.size - 1).toFloat()
                    val y = size.height * (maxScore - score).toFloat() / scoreRange.toFloat()
                    Offset(x, y)
                }

                // 绘制连接线
                for (i in 0 until points.size - 1) {
                    drawLine(
                        color = lineColor,
                        start = points[i],
                        end = points[i + 1],
                        strokeWidth = 3.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                }

                // 绘制点
                points.forEach { point ->
                    drawCircle(
                        color = lineColor,
                        radius = 4.dp.toPx(),
                        center = point
                    )
                }
            }
        }

        // 如果没有足够的数据点，显示提示
        if (trendData.size < 2) {
            Text(
                text = "📈 心情趋势图\n(需要更多记录)",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = textColor
            )
        }
    }
}

@Composable
fun StatisticsSummaryCard(
    uiState: StatisticsViewModel.StatisticsUiState,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "小总结",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = generateSummaryText(uiState),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
    }
}

// 注意：这不是 @Composable 函数，只是普通函数
private fun generateSummaryText(uiState: StatisticsViewModel.StatisticsUiState): String {
    return buildString {
        append("在最近 ${uiState.selectedDays} 天里，")

        if (uiState.totalRecords == 0) {
            append("还没有记录过心情哦。")
            return@buildString
        }

        append("你一共记录了 ${uiState.totalRecords} 天的心情。\n\n")

        val topMood = uiState.topMood
        val topCount = if (topMood != null) uiState.moodStats[topMood] ?: 0 else 0

        if (topMood != null && topCount > 0) {
            append("「${topMood.displayName}」出现了 ${topCount} 次。\n\n")

            val summary = when (topMood) {
                MoodType.HAPPY, MoodType.SATISFIED ->
                    "在大多数时间里，你是开心而满足的。继续保持这种好心态～"
                MoodType.NORMAL ->
                    "平平淡淡才是真，细水长流的爱情最是珍贵。"
                MoodType.SAD ->
                    "最近你的状态有点低落，要记得内心也需要休息，我可以随时陪你聊聊。"
                MoodType.ANGRY ->
                    "你曾表达了一些愤怒，将情绪记录在此，说明你正在认真对待这段关系。"
                MoodType.OTHER ->
                    "每一天的心情都是独特的，感谢你愿意和我分享这些无法分类的时刻。"
                else -> "感谢你认真记录每一天的心情。"
            }
            append(summary)
        } else {
            append("你的心情记录丰富多彩，每一天都是独特的体验。")
        }
    }
}

@Composable
private fun getMoodColor(moodType: MoodType): Color {
    return when (moodType) {
        MoodType.HAPPY -> MaterialTheme.colorScheme.primary
        MoodType.SATISFIED -> MaterialTheme.colorScheme.secondary
        MoodType.NORMAL -> MaterialTheme.colorScheme.tertiary
        MoodType.SAD -> MaterialTheme.colorScheme.onSurfaceVariant
        MoodType.ANGRY -> MaterialTheme.colorScheme.error
        MoodType.OTHER -> MaterialTheme.colorScheme.outline
    }
}