// presentation/screens/history/HistoryScreen.kt
package com.love.diary.presentation.screens.history

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.hilt.navigation.compose.hiltViewModel
import com.love.diary.data.database.entities.DailyMoodEntity
import com.love.diary.data.model.MoodType
import com.love.diary.presentation.components.AppCard
import com.love.diary.presentation.components.AppScaffold
import com.love.diary.presentation.components.Dimens
import com.love.diary.presentation.components.EmptyState
import com.love.diary.presentation.components.LoadingState
import com.love.diary.presentation.components.SectionHeader
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

    AppScaffold(
        title = "心情日记",
        actions = {
            IconButton(onClick = { /* 筛选功能 */ }) {
                Icon(Icons.Default.FilterList, contentDescription = "筛选")
            }
        }
    ) { padding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = Dimens.ScreenPadding, vertical = Dimens.SectionSpacing),
            verticalArrangement = Arrangement.spacedBy(Dimens.SectionSpacing)
        ) {
            SectionHeader(
                title = "概览",
                subtitle = "共 ${moodRecords.size} 天记录"
            )

            when {
                isLoading -> LoadingState()
                moodRecords.isEmpty() -> EmptyState(
                    title = "还没有记录",
                    subtitle = "去首页记录今天的心情吧"
                )
                else -> MoodHistoryList(
                    moodRecords = moodRecords,
                    onItemClick = { /* TODO */ }
                )
            }
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
        verticalArrangement = Arrangement.spacedBy(Dimens.SectionSpacing),
        contentPadding = PaddingValues(vertical = Dimens.SectionSpacing)
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
    AppCard(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(Dimens.SectionSpacing)
        ) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
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

                MoodTypeBadge(moodType = MoodType.fromCode(record.moodTypeCode))
            }

            if (record.hasText && record.moodText != null) {
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
