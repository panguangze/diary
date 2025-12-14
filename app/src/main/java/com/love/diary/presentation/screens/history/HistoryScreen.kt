// presentation/screens/history/HistoryScreen.kt
package com.love.diary.presentation.screens.history

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.style.TextOverflow
import androidx.hilt.navigation.compose.hiltViewModel
import com.love.diary.data.database.entities.DailyMoodEntity
import com.love.diary.data.model.MoodType
import com.love.diary.presentation.viewmodel.HistoryViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.*

@Composable
fun HistoryScreen(
    viewModel: HistoryViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    val moodRecords by viewModel.moodRecords.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    Column(modifier = modifier.fillMaxSize()) {
        // 顶部栏
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "心情日记",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = "共 ${moodRecords.size} 天记录",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                IconButton(onClick = { /* 筛选功能 */ }) {
                    Icon(Icons.Default.FilterList, contentDescription = "筛选")
                }
            }
        }

        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (moodRecords.isEmpty()) {
            EmptyHistoryView()
        } else {
            MoodHistoryList(
                moodRecords = moodRecords,
                onItemClick = { date ->
                    // TODO: 打开详情页
                }
            )
        }
    }
}

@Composable
fun EmptyHistoryView() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.History,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Text(
                text = "还没有记录",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Text(
                text = "去首页记录今天的心情吧",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun MoodHistoryList(
    moodRecords: List<DailyMoodEntity>,
    onItemClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val dateFormatter = remember { DateTimeFormatter.ofPattern("yyyy-MM-dd") }
    val weekFormatter = remember {
        TextStyle.FULL_STANDALONE
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
    ) {
        itemsIndexed(moodRecords) { index, record ->
            AnimatedVisibility(
                visible = true,
                enter = fadeIn(animationSpec = tween(300, delayMillis = index * 50)) +
                        slideInVertically(
                            initialOffsetY = { it / 4 },
                            animationSpec = tween(300, delayMillis = index * 50)
                        )
            ) {
                MoodHistoryItem(
                    record = record,
                    dateFormatter = dateFormatter,
                    weekFormatter = weekFormatter,
                    onClick = { onItemClick(record.date) }
                )
            }
        }
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoodHistoryItem(
    record: DailyMoodEntity,
    dateFormatter: DateTimeFormatter,
    weekFormatter: TextStyle,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp)
        ),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    // 日期和星期几
                    val date = LocalDate.parse(record.date)
                    val dayOfWeek = date.dayOfWeek.getDisplayName(weekFormatter, Locale.getDefault())

                    Text(
                        text = "${record.date} ($dayOfWeek)",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    )

                    Text(
                        text = "第 ${record.dayIndex} 天",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // 心情标签
                MoodTypeBadge(moodType = MoodType.fromCode(record.moodTypeCode))
            }

            // 心情文字预览
            if (record.hasText && record.moodText != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = record.moodText!!,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun MoodTypeBadge(moodType: MoodType) {
    val backgroundColor = when (moodType) {
        MoodType.HAPPY -> MaterialTheme.colorScheme.primaryContainer
        MoodType.SATISFIED -> MaterialTheme.colorScheme.secondaryContainer
        MoodType.NORMAL -> MaterialTheme.colorScheme.surfaceVariant
        MoodType.SAD -> MaterialTheme.colorScheme.tertiaryContainer
        MoodType.ANGRY -> MaterialTheme.colorScheme.errorContainer
        MoodType.OTHER -> MaterialTheme.colorScheme.surfaceVariant
    }

    val textColor = when (moodType) {
        MoodType.HAPPY -> MaterialTheme.colorScheme.onPrimaryContainer
        MoodType.SATISFIED -> MaterialTheme.colorScheme.onSecondaryContainer
        MoodType.NORMAL -> MaterialTheme.colorScheme.onSurfaceVariant
        MoodType.SAD -> MaterialTheme.colorScheme.onTertiaryContainer
        MoodType.ANGRY -> MaterialTheme.colorScheme.onErrorContainer
        MoodType.OTHER -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    Surface(
        shape = RoundedCornerShape(8.dp),
        color = backgroundColor,
        contentColor = textColor
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = moodType.emoji,
                fontSize = MaterialTheme.typography.bodySmall.fontSize
            )

            Text(
                text = moodType.displayName,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium
            )
        }
    }
}