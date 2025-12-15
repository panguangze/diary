package com.love.diary.presentation.screens.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Celebration
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import com.love.diary.data.database.entities.DailyMoodEntity
import com.love.diary.data.model.MoodType
import com.love.diary.presentation.viewmodel.HistoryViewModel
import com.love.diary.presentation.viewmodel.HomeViewModel
import com.love.diary.presentation.viewmodel.StatisticsViewModel
import com.love.diary.presentation.screens.statistics.MoodTrendCard
import com.love.diary.presentation.screens.statistics.StatisticsOverviewCard
import com.love.diary.presentation.screens.statistics.StatisticsScreen
import com.love.diary.util.ShareHelper
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val historyViewModel: HistoryViewModel = hiltViewModel()
    val statisticsViewModel: StatisticsViewModel = hiltViewModel()
    val historyRecords by historyViewModel.moodRecords.collectAsState()
    val statisticsUiState by statisticsViewModel.uiState.collectAsState()

    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    val commonMoods = remember { listOf(MoodType.HAPPY, MoodType.NORMAL, MoodType.SAD, MoodType.OTHER) }
    var showFullMoodGrid by rememberSaveable { mutableStateOf(false) }
    var showCalendarSheet by remember { mutableStateOf(false) }
    var showStatsSheet by remember { mutableStateOf(false) }
    var showMoodEditSheet by remember { mutableStateOf(false) }
    var selectedHistoryItem by remember { mutableStateOf<DailyMoodEntity?>(null) }

    val calendarSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val statsSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val detailSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val editMoodSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val todayString = uiState.todayDate.ifBlank { LocalDate.now().toString() }
    val hasTodayMood = uiState.todayMood != null && uiState.todayMoodDate == todayString

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 80.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(vertical = 12.dp)
        ) {
            item {
                RelationshipCard(uiState = uiState)
            }

            item {
                TodayOverviewBar(
                    dateDisplay = uiState.currentDateDisplay.ifBlank { "今天：$todayString" },
                    streak = uiState.currentStreak
                )
            }

            item {
                MoodTimelineCard(
                    uiState = uiState,
                    hasTodayMood = hasTodayMood,
                    commonMoods = commonMoods,
                    showFullMoodGrid = showFullMoodGrid,
                    onMoreToggle = { showFullMoodGrid = !showFullMoodGrid },
                    onMoodSelected = { mood ->
                        if (mood == MoodType.OTHER) {
                            viewModel.updateOtherMoodText("")
                            viewModel.showOtherMoodDialog()
                        } else {
                            viewModel.selectMood(mood)
                        }
                    },
                    onEdit = { showMoodEditSheet = true },
                    onShare = {
                        uiState.todayMood?.let { mood ->
                            ShareHelper(context).shareMoodAsText(
                                date = todayString,
                                moodType = mood,
                                moodText = uiState.todayMoodText,
                                dayIndex = uiState.dayIndex
                            )
                        }
                    },
                    onRecentMoodClick = { selectedHistoryItem = it },
                    onExpandCalendar = { showCalendarSheet = true }
                )
            }

            if (uiState.todayMood != null) {
                item {
                    FeedbackCard(mood = uiState.todayMood!!)
                }
            }

            item {
                MoodStatisticsPreviewSection(
                    uiState = statisticsUiState,
                    onRangeChange = statisticsViewModel::updateTimeRange,
                    onExpand = { showStatsSheet = true }
                )
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }

    if (uiState.showAnniversaryPopup) {
        Dialog(onDismissRequest = { viewModel.dismissAnniversaryPopup() }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Filled.Celebration,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = uiState.anniversaryMessage,
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        TextButton(onClick = { viewModel.dismissAnniversaryPopup() }) {
                            Text("知道啦")
                        }

                        Button(onClick = {
                            viewModel.dismissAnniversaryPopup()
                            viewModel.showOtherMoodDialog()
                        }) {
                            Text("写点想说的话")
                        }
                    }
                }
            }
        }
    }

    if (uiState.showOtherMoodDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.closeOtherMoodDialog() },
            title = { Text("想对我说点什么？") },
            text = {
                Column {
                    Text(
                        text = "写点今天的心情、想对我说的话，只有我会看。",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = uiState.otherMoodText,
                        onValueChange = { viewModel.updateOtherMoodText(it) },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("比如：今天看到你消息的时候，突然很安心……") },
                        maxLines = 5
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (uiState.otherMoodText.isNotBlank()) {
                        viewModel.saveOtherMood(uiState.otherMoodText)
                    }
                }) {
                    Text("保存")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.closeOtherMoodDialog() }) {
                    Text("取消")
                }
            }
        )
    }

    if (showCalendarSheet) {
        ModalBottomSheet(
            sheetState = calendarSheetState,
            onDismissRequest = { showCalendarSheet = false }
        ) {
            MoodCalendarBottomSheet(
                onDismiss = { showCalendarSheet = false },
                onDateClick = { date ->
                    // Check if this date has a mood record
                    val record = historyRecords.find { it.date == date }
                    if (record != null) {
                        selectedHistoryItem = record
                    }
                },
                moodRecords = historyRecords
            )
        }
    }

    selectedHistoryItem?.let { record ->
        ModalBottomSheet(
            sheetState = detailSheetState,
            onDismissRequest = { selectedHistoryItem = null }
        ) {
            HistoryDetailSheet(
                record = record,
                onShare = {
                    ShareHelper(context).shareMoodAsText(
                        date = record.date,
                        moodType = MoodType.fromCode(record.moodTypeCode),
                        moodText = record.moodText,
                        dayIndex = record.dayIndex
                    )
                },
                onCopy = {
                    record.moodText?.let { clipboardManager.setText(androidx.compose.ui.text.AnnotatedString(it)) }
                }
            )
        }
    }

    if (showStatsSheet) {
        ModalBottomSheet(
            sheetState = statsSheetState,
            onDismissRequest = { showStatsSheet = false }
        ) {
            StatisticsScreen(
                viewModel = statisticsViewModel,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 720.dp)
                    .padding(bottom = 24.dp)
            )
        }
    }

    if (showMoodEditSheet && !uiState.showOtherMoodDialog) {
        ModalBottomSheet(
            sheetState = editMoodSheetState,
            onDismissRequest = { showMoodEditSheet = false }
        ) {
            MoodSelectionSheet(
                selectedMood = uiState.todayMood,
                onMoodSelected = { mood ->
                    if (mood == MoodType.OTHER) {
                        viewModel.updateOtherMoodText("")
                        viewModel.showOtherMoodDialog()
                    } else {
                        viewModel.selectMood(mood)
                        showMoodEditSheet = false
                    }
                }
            )
        }
    }
}

@Composable
private fun RelationshipCard(uiState: com.love.diary.presentation.viewmodel.HomeUiState) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp)
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "「${uiState.coupleName ?: "我们"}」已经在一起",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "第 ${uiState.dayIndex} 天",
                style = MaterialTheme.typography.displaySmall.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "= ${uiState.dayDisplay}",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center
            )

            if (uiState.dayIndex % 100 == 0 && uiState.dayIndex > 0) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer)
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Celebration,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = "🎉 今天是我们在一起的第 ${uiState.dayIndex} 天！",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
private fun TodayOverviewBar(
    dateDisplay: String,
    streak: Int
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(12.dp),
        tonalElevation = 1.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = dateDisplay,
                style = MaterialTheme.typography.titleMedium
            )

            Text(
                text = "连续记录：${streak}天",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MoodTimelineCard(
    uiState: com.love.diary.presentation.viewmodel.HomeUiState,
    hasTodayMood: Boolean,
    commonMoods: List<MoodType>,
    showFullMoodGrid: Boolean,
    onMoreToggle: () -> Unit,
    onMoodSelected: (MoodType) -> Unit,
    onEdit: () -> Unit,
    onShare: () -> Unit,
    onRecentMoodClick: (DailyMoodEntity) -> Unit,
    onExpandCalendar: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Section 1: Today's Mood
            Text(
                text = "今天的心情",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            if (hasTodayMood && uiState.todayMood != null) {
                TodayMoodDisplay(
                    mood = uiState.todayMood,
                    moodText = uiState.todayMoodText,
                    onEdit = onEdit,
                    onShare = onShare
                )
            } else {
                TodayMoodInput(
                    commonMoods = commonMoods,
                    showFullMoodGrid = showFullMoodGrid,
                    onMoreToggle = onMoreToggle,
                    onMoodSelected = onMoodSelected,
                    selectedMood = uiState.todayMood
                )
            }

            // Section 2: Recent 10 Moods
            RecentMoodsRow(
                recentMoods = uiState.recentTenMoods,
                onMoodClick = onRecentMoodClick
            )

            // Section 3: Expand Calendar Button
            Button(
                onClick = onExpandCalendar,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("展开日历")
            }
        }
    }
}

@Composable
private fun TodayMoodDisplay(
    mood: MoodType,
    moodText: String?,
    onEdit: () -> Unit,
    onShare: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(text = mood.emoji, style = MaterialTheme.typography.displaySmall)
                Column {
                    Text(
                        text = mood.displayName,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "今天的记录",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                TextButton(onClick = onEdit) { Text("改一下") }
                TextButton(onClick = onShare) { Text("分享") }
            }
        }

        if (!moodText.isNullOrBlank()) {
            Text(
                text = moodText,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun TodayMoodInput(
    commonMoods: List<MoodType>,
    showFullMoodGrid: Boolean,
    onMoreToggle: () -> Unit,
    onMoodSelected: (MoodType) -> Unit,
    selectedMood: MoodType?
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "选择今天的心情吧",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            commonMoods.forEach { mood ->
                ElevatedCard(
                    onClick = { onMoodSelected(mood) },
                    modifier = Modifier
                        .weight(1f)
                        .height(72.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(text = mood.emoji, style = MaterialTheme.typography.titleLarge)
                        Text(text = mood.displayName, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }

            OutlinedCard(
                onClick = onMoreToggle,
                modifier = Modifier
                    .width(88.dp)
                    .height(72.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "更多", style = MaterialTheme.typography.bodyMedium)
                }
            }
        }

        AnimatedVisibility(visible = showFullMoodGrid) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 420.dp)
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(MoodType.values()) { mood ->
                    MoodButton(
                        mood = mood,
                        isSelected = selectedMood == mood,
                        onClick = { onMoodSelected(mood) }
                    )
                }
            }
        }
    }
}

@Composable
private fun RecentMoodsRow(
    recentMoods: List<DailyMoodEntity>,
    onMoodClick: (DailyMoodEntity) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "最近10次心情",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium
        )

        if (recentMoods.isEmpty()) {
            Text(
                text = "还没有历史记录",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        } else {
            androidx.compose.foundation.lazy.LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(recentMoods) { moodRecord ->
                    RecentMoodItem(
                        moodRecord = moodRecord,
                        onClick = { onMoodClick(moodRecord) }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RecentMoodItem(
    moodRecord: DailyMoodEntity,
    onClick: () -> Unit
) {
    val dateFormatter = remember { DateTimeFormatter.ofPattern("MM-dd") }
    val date = runCatching { LocalDate.parse(moodRecord.date) }.getOrNull()
    val formattedDate = date?.format(dateFormatter) ?: moodRecord.date

    Card(
        onClick = onClick,
        modifier = Modifier.size(60.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp)
        )
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = MoodType.fromCode(moodRecord.moodTypeCode).emoji,
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = formattedDate,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TodayMoodSection(
    uiState: com.love.diary.presentation.viewmodel.HomeUiState,
    hasTodayMood: Boolean,
    commonMoods: List<MoodType>,
    showFullMoodGrid: Boolean,
    onMoreToggle: () -> Unit,
    onMoodSelected: (MoodType) -> Unit,
    onEdit: () -> Unit,
    onShare: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "今天的心情",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }

        if (hasTodayMood && uiState.todayMood != null) {
            TodayMoodCard(
                mood = uiState.todayMood,
                moodText = uiState.todayMoodText,
                onEdit = onEdit,
                onShare = onShare
            )
        } else {
            Text(
                text = "选择今天的心情吧",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                commonMoods.forEach { mood ->
                    ElevatedCard(
                        onClick = { onMoodSelected(mood) },
                        modifier = Modifier
                            .weight(1f)
                            .height(72.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(text = mood.emoji, style = MaterialTheme.typography.titleLarge)
                            Text(text = mood.displayName, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }

                OutlinedCard(
                    onClick = onMoreToggle,
                    modifier = Modifier
                        .width(88.dp)
                        .height(72.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "更多", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }

            AnimatedVisibility(visible = showFullMoodGrid) {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(MoodType.values()) { mood ->
                        MoodButton(
                            mood = mood,
                            isSelected = uiState.todayMood == mood,
                            onClick = { onMoodSelected(mood) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TodayMoodCard(
    mood: MoodType,
    moodText: String?,
    onEdit: () -> Unit,
    onShare: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(text = mood.emoji, style = MaterialTheme.typography.headlineMedium)
                    Column {
                        Text(text = mood.displayName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text(text = "今天的记录", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(onClick = onEdit) { Text("改一下") }
                    TextButton(onClick = onShare) { Text("分享") }
                }
            }

            if (!moodText.isNullOrBlank()) {
                Text(
                    text = moodText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
private fun FeedbackCard(mood: MoodType) {
    AnimatedVisibility(
        visible = true,
        enter = fadeIn() + expandVertically()
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer
            )
        ) {
            Text(
                text = mood.feedbackText,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(16.dp),
                textAlign = TextAlign.Center
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HistoryDetailSheet(
    record: DailyMoodEntity,
    onShare: () -> Unit,
    onCopy: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "记录详情", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(text = MoodType.fromCode(record.moodTypeCode).emoji, style = MaterialTheme.typography.headlineMedium)
            Column {
                Text(text = record.date, style = MaterialTheme.typography.titleMedium)
                Text(text = MoodType.fromCode(record.moodTypeCode).displayName, style = MaterialTheme.typography.bodyMedium)
            }
        }

        if (!record.moodText.isNullOrBlank()) {
            OutlinedTextField(
                value = record.moodText ?: "",
                onValueChange = {},
                readOnly = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 120.dp),
                label = { Text("一句话") }
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = onShare,
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Filled.Share, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text("分享")
            }

            OutlinedCard(
                modifier = Modifier.weight(1f),
                onClick = onCopy
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Filled.ContentCopy, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("复制文本")
                }
            }
        }
    }
}

@Composable
private fun MoodStatisticsPreviewSection(
    uiState: StatisticsViewModel.StatisticsUiState,
    onRangeChange: (Int) -> Unit,
    onExpand: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "心情统计",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            TextButton(onClick = onExpand) { Text("展开") }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            listOf(7, 30, 90, 365).forEach { days ->
                FilterChip(
                    selected = uiState.selectedDays == days,
                    onClick = { onRangeChange(days) },
                    label = {
                        Text(
                            when (days) {
                                7 -> "最近7天"
                                30 -> "最近30天"
                                90 -> "最近90天"
                                else -> "全年"
                            }
                        )
                    }
                )
            }
        }

        // Only show mood statistics
        StatisticsOverviewCard(uiState = uiState)

        MoodTrendCard(
            moodTrendData = uiState.moodTrend,
            currentViewType = StatisticsViewModel.ViewType.MOOD
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MoodCalendarBottomSheet(
    onDismiss: () -> Unit,
    onDateClick: (String) -> Unit,
    moodRecords: List<DailyMoodEntity>
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("本月", "年历")
    
    val today = LocalDate.now()
    var currentMonth by remember { mutableStateOf(today) }
    
    // Create a map of date -> mood for quick lookup
    val moodMap = remember(moodRecords) {
        moodRecords.associateBy { it.date }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "心情日历",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            TextButton(onClick = onDismiss) { Text("关闭") }
        }

        // Tabs
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            tabs.forEachIndexed { index, tab ->
                FilterChip(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    label = { Text(tab) }
                )
            }
        }

        when (selectedTab) {
            0 -> MonthCalendarView(
                currentMonth = currentMonth,
                onMonthChange = { currentMonth = it },
                moodMap = moodMap,
                onDateClick = onDateClick,
                today = today
            )
            1 -> YearCalendarView(
                year = today.year,
                moodMap = moodMap,
                onMonthClick = { month ->
                    currentMonth = LocalDate.of(today.year, month, 1)
                    selectedTab = 0
                },
                today = today
            )
        }
    }
}

@Composable
private fun MonthCalendarView(
    currentMonth: LocalDate,
    onMonthChange: (LocalDate) -> Unit,
    moodMap: Map<String, DailyMoodEntity>,
    onDateClick: (String) -> Unit,
    today: LocalDate
) {
    val dateFormatter = remember { DateTimeFormatter.ofPattern("yyyy年MM月") }
    
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Month navigation
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = { onMonthChange(currentMonth.minusMonths(1)) }) {
                Text("< 上月")
            }
            Text(
                text = currentMonth.format(dateFormatter),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            TextButton(onClick = { onMonthChange(currentMonth.plusMonths(1)) }) {
                Text("下月 >")
            }
        }

        // Day of week headers
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            listOf("日", "一", "二", "三", "四", "五", "六").forEach { day ->
                Text(
                    text = day,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Calendar grid (7x6)
        val firstDayOfMonth = currentMonth.withDayOfMonth(1)
        val daysInMonth = currentMonth.lengthOfMonth()
        val firstDayOfWeek = firstDayOfMonth.dayOfWeek.value % 7 // Sunday = 0
        
        LazyVerticalGrid(
            columns = GridCells.Fixed(7),
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 400.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Empty cells before first day
            items(firstDayOfWeek) {
                Box(modifier = Modifier.size(48.dp))
            }
            
            // Days of the month
            items(daysInMonth) { dayIndex ->
                val day = dayIndex + 1
                val date = currentMonth.withDayOfMonth(day)
                val dateStr = date.toString()
                val moodRecord = moodMap[dateStr]
                val isToday = date == today
                
                CalendarDayCell(
                    day = day,
                    moodRecord = moodRecord,
                    isToday = isToday,
                    onClick = {
                        if (moodRecord != null) {
                            onDateClick(dateStr)
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun YearCalendarView(
    year: Int,
    moodMap: Map<String, DailyMoodEntity>,
    onMonthClick: (Int) -> Unit,
    today: LocalDate
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "${year}年",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 600.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(12) { monthIndex ->
                val month = monthIndex + 1
                MiniMonthGrid(
                    year = year,
                    month = month,
                    moodMap = moodMap,
                    onClick = { onMonthClick(month) },
                    today = today
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CalendarDayCell(
    day: Int,
    moodRecord: DailyMoodEntity?,
    isToday: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = when {
        isToday -> MaterialTheme.colorScheme.primaryContainer
        moodRecord != null -> MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp)
        else -> MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp)
    }

    Card(
        onClick = onClick,
        modifier = Modifier
            .size(48.dp)
            .border(
                width = if (isToday) 2.dp else 0.dp,
                color = if (isToday) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(8.dp)
            ),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            if (moodRecord != null) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = MoodType.fromCode(moodRecord.moodTypeCode).emoji,
                        fontSize = 16.sp
                    )
                    Text(
                        text = day.toString(),
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 10.sp
                    )
                }
            } else {
                Text(
                    text = day.toString(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MiniMonthGrid(
    year: Int,
    month: Int,
    moodMap: Map<String, DailyMoodEntity>,
    onClick: () -> Unit,
    today: LocalDate
) {
    val monthName = remember(month) {
        val date = LocalDate.of(year, month, 1)
        date.format(DateTimeFormatter.ofPattern("M月"))
    }

    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp)
        )
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = monthName,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 4.dp)
            )

            // Mini grid showing mood icons
            val firstDay = LocalDate.of(year, month, 1)
            val daysInMonth = firstDay.lengthOfMonth()
            val firstDayOfWeek = firstDay.dayOfWeek.value % 7

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(2.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                // Empty cells
                repeat(firstDayOfWeek) {
                    Box(modifier = Modifier.size(12.dp))
                }

                // Days
                repeat(daysInMonth) { dayIndex ->
                    val day = dayIndex + 1
                    val date = LocalDate.of(year, month, day)
                    val dateStr = date.toString()
                    val moodRecord = moodMap[dateStr]

                    Box(
                        modifier = Modifier.size(12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (moodRecord != null) {
                            // Use colored circle instead of tiny emoji for better accessibility
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .background(
                                        when (MoodType.fromCode(moodRecord.moodTypeCode)) {
                                            MoodType.HAPPY -> MaterialTheme.colorScheme.primary
                                            MoodType.SATISFIED -> MaterialTheme.colorScheme.tertiary
                                            MoodType.NORMAL -> MaterialTheme.colorScheme.secondary
                                            MoodType.SAD -> MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
                                            MoodType.ANGRY -> MaterialTheme.colorScheme.error
                                            MoodType.OTHER -> MaterialTheme.colorScheme.outline
                                        },
                                        shape = CircleShape
                                    )
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(
                                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f),
                                        shape = RoundedCornerShape(2.dp)
                                    )
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MoodSelectionSheet(
    selectedMood: MoodType?,
    onMoodSelected: (MoodType) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "改一下心情",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 420.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(MoodType.values()) { mood ->
                MoodButton(
                    mood = mood,
                    isSelected = selectedMood == mood,
                    onClick = { onMoodSelected(mood) }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoodButton(
    mood: MoodType,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = if (isSelected) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.surface
    }

    val borderColor = if (isSelected) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.outline
    }

    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.05f else 1f,
        label = "mood_button_scale"
    )

    Card(
        modifier = Modifier
            .height(100.dp)
            .aspectRatio(1f)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(12.dp)
            )
            .semantics {
                contentDescription =
                    "${mood.displayName}心情按钮${if (isSelected) "，已选择" else ""}"
            },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor
        ),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = mood.emoji,
                fontSize = 32.sp
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = mood.displayName,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}
