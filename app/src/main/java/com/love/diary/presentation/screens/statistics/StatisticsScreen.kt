package com.love.diary.presentation.screens.statistics

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.ui.res.stringResource
import com.love.diary.R
import com.love.diary.data.model.MoodType
import com.love.diary.presentation.components.AppCard
import com.love.diary.presentation.components.AppSegmentedTabs
import com.love.diary.presentation.components.AppScaffold
import com.love.diary.presentation.components.Dimens
import com.love.diary.presentation.components.EmptyState
import com.love.diary.presentation.components.ErrorState
import com.love.diary.presentation.components.SectionHeader
import com.love.diary.presentation.components.LoadingState
import com.love.diary.presentation.viewmodel.StatisticsViewModel
import kotlin.math.roundToInt

@Composable
fun StatisticsScreen(
    viewModel: StatisticsViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    when (uiState.contentState) {
        StatisticsViewModel.ContentState.LOADING -> {
            LoadingState()
        }

        StatisticsViewModel.ContentState.EMPTY -> {
            EmptyState(
                title = "最近${uiState.selectedDays}天还没有心情记录",
                subtitle = "去写一条吧",
                modifier = modifier
            )
        }

        StatisticsViewModel.ContentState.ERROR -> {
            ErrorState(
                message = uiState.errorMessage ?: "加载失败",
                modifier = modifier
            )
        }

        StatisticsViewModel.ContentState.CONTENT -> {
            StatisticsContent(uiState = uiState, viewModel = viewModel, modifier = modifier)
        }
    }
}

@Composable
private fun StatisticsContent(
    uiState: StatisticsViewModel.StatisticsUiState,
    viewModel: StatisticsViewModel,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(Dimens.SectionSpacing),
        contentPadding = PaddingValues(Dimens.ScreenPadding)
    ) {
        item {
            TimeRangeSelector(
                selectedDays = uiState.selectedDays,
                onDaysSelected = viewModel::updateTimeRange
            )
        }

        item {
            StatsBentoSection(
                uiState = uiState,
                modifier = Modifier.fillMaxWidth()
            )
        }

        item {
            MoodDistributionCard(uiState = uiState)
        }

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
    val rangeOptions = StatisticsViewModel.DEFAULT_RANGE_OPTIONS
    val options = listOf(
        stringResource(R.string.home_mood_trend_range_week),
        stringResource(R.string.home_mood_trend_range_month),
        stringResource(R.string.home_mood_trend_range_quarter),
        stringResource(R.string.home_mood_trend_range_year)
    )
    val selectedIndex = rangeOptions.indexOf(selectedDays).takeIf { it >= 0 } ?: 0

    AppCard(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(vertical = Dimens.SectionSpacing)
    ) {
        AppSegmentedTabs(
            options = options,
            selectedIndex = selectedIndex,
            onSelected = {
                val days = rangeOptions.getOrNull(it) ?: StatisticsViewModel.RANGE_WEEK
                onDaysSelected(days)
            }
        )
    }
}

@Composable
private fun StatsBentoSection(
    uiState: StatisticsViewModel.StatisticsUiState,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(Dimens.SectionSpacing)
    ) {
        SectionHeader(
            title = "最近 ${uiState.selectedDays} 天",
            subtitle = "一屏查看核心指标"
        )
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 260.dp, max = 520.dp),
            horizontalArrangement = Arrangement.spacedBy(Dimens.SectionSpacing),
            verticalArrangement = Arrangement.spacedBy(Dimens.SectionSpacing),
            userScrollEnabled = false
        ) {
            item {
                MiniStatCard(
                    title = "🔥 连续记录",
                    value = "${uiState.totalRecords} 天",
                    caption = "本阶段 streak"
                )
            }
            item {
                MiniStatCard(
                    title = "平均心情",
                    value = uiState.averageMood,
                    caption = uiState.topMood?.displayName ?: "暂无常见心情"
                )
            }
            item {
                MiniStatCard(
                    title = "最常心情",
                    value = uiState.topMood?.emoji ?: "-",
                    caption = uiState.topMood?.displayName ?: "等待记录"
                )
            }
            item(span = { GridItemSpan(2) }) {
                MoodTrendCard(
                    moodTrendData = uiState.moodTrend,
                    checkInTrendData = uiState.checkInTrend,
                    currentViewType = uiState.currentViewType
                )
            }
        }
    }
}

@Composable
private fun MiniStatCard(
    title: String,
    value: String,
    caption: String
) {
    AppCard(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(16.dp)
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = caption,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun StatisticsOverviewCard(
    uiState: StatisticsViewModel.StatisticsUiState,
    modifier: Modifier = Modifier
) {
    AppCard(
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(Dimens.SectionSpacing)
        ) {
            Text(
                text = "统计概览",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )

            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (uiState.currentViewType == StatisticsViewModel.ViewType.MOOD) {
                    // 心情统计概览
                    StatItem(
                        title = "记录天数",
                        value = uiState.totalRecords.toString(),
                        icon = Icons.Default.DateRange,
                        insight = "记录越多，故事越完整"
                    )

                    StatItem(
                        title = "平均心情",
                        value = uiState.averageMood,
                        icon = Icons.Default.TrendingUp,
                        insight = "最常心情：${uiState.topMood?.displayName ?: "-"}"
                    )

                    StatItem(
                        title = "最常心情",
                        value = uiState.topMood?.emoji ?: "-",
                        icon = Icons.Default.EmojiEmotions,
                        insight = uiState.topMood?.displayName
                    )
                } else {
                    // 打卡统计概览
                    StatItem(
                        title = "打卡次数",
                        value = uiState.totalRecords.toString(),
                        icon = Icons.Default.CheckCircle,
                        insight = "保持节奏"
                    )

                    StatItem(
                        title = "打卡天数",
                        value = uiState.checkInTrend.distinctBy { it.date }.size.toString(),
                        icon = Icons.Default.DateRange,
                        insight = "坚持是最好的习惯"
                    )

                    StatItem(
                        title = "最近打卡",
                        value = uiState.checkInTrend.lastOrNull()?.date?.substring(5)?.replace("-", "/") ?: "-",
                        icon = Icons.Default.AccessTime,
                        insight = "保持今日也打卡"
                    )
                }
            }

            Text(
                text = if (uiState.currentViewType == StatisticsViewModel.ViewType.MOOD) {
                    "最常见的心情：${uiState.topMood?.displayName ?: "-"}"
                } else {
                    "最近的打卡记录让节奏更稳定"
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun StatItem(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    insight: String? = null,
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
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )

        Text(
            text = title,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
        )

        if (!insight.isNullOrBlank()) {
            Text(
                text = insight,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

@Composable
fun MoodDistributionCard(
    uiState: StatisticsViewModel.StatisticsUiState,
    modifier: Modifier = Modifier
) {
    if (uiState.currentViewType == StatisticsViewModel.ViewType.MOOD) {
        AppCard(
            modifier = modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(Dimens.SectionSpacing)
            ) {
                Text(
                    text = "心情分布",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )

                MoodType.values().forEach { moodType ->
                    val count = uiState.moodStats[moodType] ?: 0
                    val percentage = if (uiState.totalRecords > 0) {
                        (count.toFloat() / uiState.totalRecords * 100).roundToInt()
                    } else 0

                    if (count > 0) {
                        MoodDistributionItem(
                            moodType = moodType,
                            count = count,
                            percentage = percentage,
                            totalRecords = uiState.totalRecords
                        )

                        if (moodType != MoodType.values().last()) {
                            Divider(modifier = Modifier.padding(vertical = 4.dp))
                        }
                    }
                }

                if (uiState.totalRecords == 0) {
                    Text(
                        text = "暂无记录",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
    // 打卡视图中不显示此卡片
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
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp),
                color = getMoodColor(moodType),
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
                strokeCap = StrokeCap.Round
            )
        }
    }
}

@Composable
fun MoodTrendCard(
    moodTrendData: List<Pair<String, Int>>,
    checkInTrendData: List<com.love.diary.data.model.CheckInTrend> = emptyList(),
    currentViewType: StatisticsViewModel.ViewType = StatisticsViewModel.ViewType.MOOD,
    modifier: Modifier = Modifier
) {
    AppCard(
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(Dimens.SectionSpacing)
        ) {
            Text(
                text = if (currentViewType == StatisticsViewModel.ViewType.MOOD) "心情趋势" else "打卡趋势",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium
            )

            if (currentViewType == StatisticsViewModel.ViewType.MOOD) {
                if (moodTrendData.isNotEmpty()) {
                    // 心情趋势图表
                    SimpleTrendChart(trendData = moodTrendData)
                } else {
                    Text(
                        text = "暂无心情趋势数据",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 16.dp)
                    )
                }
            } else {
                if (checkInTrendData.isNotEmpty()) {
                    // 打卡趋势图表 - 将 CheckInTrend 转换为 Pair<String, Int> 以兼容图表
                    val chartData = checkInTrendData.map { it.date to it.count }
                    SimpleTrendChart(trendData = chartData)
                } else {
                    Text(
                        text = "暂无打卡趋势数据",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = if (currentViewType == StatisticsViewModel.ViewType.MOOD) 
                    "记录每一天的心情，有起伏才像真实的生活。" 
                    else "坚持打卡，见证自己的成长与进步。",
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

    val lineColor = MaterialTheme.colorScheme.primary
    val textColor = MaterialTheme.colorScheme.onSurfaceVariant

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(200.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            if (trendData.size > 1 && scoreRange > 0) {
                val points = trendData.mapIndexed { index, (_, score) ->
                    val x = size.width * index / (trendData.size - 1).toFloat()
                    val y = size.height * (maxScore - score).toFloat() / scoreRange.toFloat()
                    Offset(x, y)
                }

                val fillPath = androidx.compose.ui.graphics.Path().apply {
                    moveTo(points.first().x, size.height)
                    points.forEach { lineTo(it.x, it.y) }
                    lineTo(points.last().x, size.height)
                    close()
                }

                drawPath(
                    path = fillPath,
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            lineColor.copy(alpha = 0.35f),
                            Color.Transparent
                        )
                    )
                )

                for (i in 0 until points.size - 1) {
                    drawLine(
                        color = lineColor,
                        start = points[i],
                        end = points[i + 1],
                        strokeWidth = 3.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                }

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
    AppCard(
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(Dimens.SectionSpacing)
        ) {
            Text(
                text = "小总结",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium
            )

            Text(
                text = generateSummaryText(uiState),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

// 注意：这不是 @Composable 函数，只是普通函数
private fun generateSummaryText(uiState: StatisticsViewModel.StatisticsUiState): String {
    return buildString {
        if (uiState.currentViewType == StatisticsViewModel.ViewType.MOOD) {
            // 心情统计总结
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
        } else {
            // 打卡统计总结
            append("在最近 ${uiState.selectedDays} 天里，")

            if (uiState.totalRecords == 0) {
                append("还没有打卡记录哦。")
                return@buildString
            }

            append("你一共完成了 ${uiState.totalRecords} 次打卡。\n\n")
            
            append("坚持打卡，见证自己的成长与进步。")
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
