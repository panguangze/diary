package com.love.diary.presentation.screens.stats

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.love.diary.presentation.viewmodel.ChartPeriod
import com.love.diary.presentation.viewmodel.StatsViewModel
import com.patrykandpatrick.vico.compose.axis.horizontal.rememberBottomAxis
import com.patrykandpatrick.vico.compose.axis.vertical.rememberStartAxis
import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.compose.chart.line.lineChart
import com.patrykandpatrick.vico.core.entry.entryModelOf
import com.patrykandpatrick.vico.core.entry.entryOf

/**
 * Stats screen showing mood trends with charts and statistics
 */
@Composable
fun StatsScreen(
    modifier: Modifier = Modifier,
    viewModel: StatsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    if (uiState.isLoading) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    } else {
        StatsScreenContent(
            uiState = uiState,
            onPeriodSelected = viewModel::selectPeriod,
            onRefresh = viewModel::refresh,
            modifier = modifier
        )
    }
}

@Composable
private fun StatsScreenContent(
    uiState: com.love.diary.presentation.viewmodel.StatsUiState,
    onPeriodSelected: (ChartPeriod) -> Unit,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Period selector (Week/Month/Year)
        PeriodSelector(
            selectedPeriod = uiState.selectedPeriod,
            onPeriodSelected = onPeriodSelected
        )
        
        // Chart card
        ChartCard(
            chartData = uiState.chartData,
            selectedPeriod = uiState.selectedPeriod
        )
        
        // Stats cards section
        StatsSection(
            totalDays = uiState.totalRecordedDays,
            consecutiveDays = uiState.consecutiveDays,
            averageScore = uiState.averageMoodScore
        )
    }
}

@Composable
private fun PeriodSelector(
    selectedPeriod: ChartPeriod,
    onPeriodSelected: (ChartPeriod) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        ChartPeriod.values().forEach { period ->
            val isSelected = period == selectedPeriod
            val label = when (period) {
                ChartPeriod.WEEK -> "周"
                ChartPeriod.MONTH -> "月"
                ChartPeriod.YEAR -> "年"
            }
            
            FilterChip(
                selected = isSelected,
                onClick = { onPeriodSelected(period) },
                label = {
                    Text(
                        text = label,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = Color(0xFFFF6B81),
                    selectedLabelColor = Color.White
                )
            )
        }
    }
}

@Composable
private fun ChartCard(
    chartData: List<com.love.diary.data.model.MoodAggregation>,
    selectedPeriod: ChartPeriod
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text(
                text = "心情趋势",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            if (chartData.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "暂无数据",
                        color = Color.Gray
                    )
                }
            } else {
                // Convert data to Vico chart entries
                val entries = chartData.mapIndexed { index, data ->
                    entryOf(index.toFloat(), data.avgScore.toFloat())
                }
                
                val chartEntryModel = entryModelOf(entries)
                
                Chart(
                    chart = lineChart(),
                    model = chartEntryModel,
                    startAxis = rememberStartAxis(
                        title = "心情分数"
                    ),
                    bottomAxis = rememberBottomAxis(
                        title = when (selectedPeriod) {
                            ChartPeriod.WEEK -> "最近7天"
                            ChartPeriod.MONTH -> "最近30天"
                            ChartPeriod.YEAR -> "最近12个月"
                        }
                    )
                )
            }
        }
    }
}

@Composable
private fun StatsSection(
    totalDays: Int,
    consecutiveDays: Int,
    averageScore: Double
) {
    Column {
        Text(
            text = "统计概览",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatCard(
                title = "总天数",
                value = "$totalDays",
                modifier = Modifier.weight(1f)
            )
            
            StatCard(
                title = "连续天数",
                value = "$consecutiveDays",
                modifier = Modifier.weight(1f)
            )
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        StatCard(
            title = "平均心情",
            value = String.format("%.1f", averageScore),
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun StatCard(
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFF8F9FA)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                fontSize = 12.sp,
                color = Color.Gray
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = value,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFFF6B81)
            )
        }
    }
}
