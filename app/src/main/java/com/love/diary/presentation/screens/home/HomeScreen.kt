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
    var showHistorySheet by remember { mutableStateOf(false) }
    var showStatsSheet by remember { mutableStateOf(false) }
    var showMoodEditSheet by remember { mutableStateOf(false) }
    var selectedHistoryItem by remember { mutableStateOf<DailyMoodEntity?>(null) }
    var historyRange by rememberSaveable { mutableIntStateOf(7) }

    val historySheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val statsSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val detailSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val editMoodSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val todayString = uiState.todayDate.ifBlank { LocalDate.now().toString() }
    val hasTodayMood = uiState.todayMood != null && uiState.todayMoodDate == todayString

    val sortedHistory = remember(historyRecords) {
        historyRecords.sortedByDescending { it.date }
    }

    val filteredHistoryByRange = remember(sortedHistory, historyRange) {
        if (historyRange == 365) {
            sortedHistory
        } else {
            val endDate = LocalDate.now()
            val startDate = endDate.minusDays(historyRange.toLong() - 1)
            sortedHistory.filter {
                runCatching { LocalDate.parse(it.date) }.getOrNull()?.let { date ->
                    !date.isBefore(startDate) && !date.isAfter(endDate)
                } ?: false
            }
        }
    }

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
                TodayMoodSection(
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
                    }
                )
            }

            if (uiState.todayMood != null) {
                item {
                    FeedbackCard(mood = uiState.todayMood!!)
                }
            }

            item {
                RecentRecordsSection(
                    records = sortedHistory.take(7),
                    onItemClick = { selectedHistoryItem = it },
                    onSeeMore = { showHistorySheet = true }
                )
            }

            item {
                StatisticsPreviewSection(
                    uiState = statisticsUiState,
                    onRangeChange = statisticsViewModel::updateTimeRange,
                    onViewTypeChange = { type ->
                        when (type) {
                            StatisticsViewModel.ViewType.MOOD -> statisticsViewModel.switchToMoodTrend()
                            StatisticsViewModel.ViewType.CHECK_IN -> statisticsViewModel.switchToCheckInTrend(
                                uiState.coupleName ?: statisticsUiState.primaryCheckInName
                            )
                        }
                    },
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

    if (showHistorySheet) {
        ModalBottomSheet(
            sheetState = historySheetState,
            onDismissRequest = { showHistorySheet = false }
        ) {
            HistorySheetContent(
                records = filteredHistoryByRange,
                selectedRange = historyRange,
                onRangeChange = { historyRange = it },
                onItemClick = {
                    selectedHistoryItem = it
                },
                onClose = { showHistorySheet = false }
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

@Composable
private fun RecentRecordsSection(
    records: List<DailyMoodEntity>,
    onItemClick: (DailyMoodEntity) -> Unit,
    onSeeMore: () -> Unit
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
                text = "最近记录",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            TextButton(onClick = onSeeMore) { Text("查看更多") }
        }

        if (records.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.History,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(48.dp)
                )
                Text(text = "还没有记录", style = MaterialTheme.typography.titleMedium)
                Text(
                    text = "去上方记录今天的心情吧",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                records.forEach { record ->
                    RecentRecordItem(record = record, onClick = { onItemClick(record) })
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RecentRecordItem(
    record: DailyMoodEntity,
    onClick: () -> Unit
) {
    val dateFormatter = remember { DateTimeFormatter.ofPattern("MM-dd") }
    val weekFormatter = remember { TextStyle.SHORT }
    val date = runCatching { LocalDate.parse(record.date) }.getOrNull()
    val dayOfWeek = date?.dayOfWeek?.getDisplayName(weekFormatter, Locale.getDefault()) ?: ""

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp)),
        onClick = onClick
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "${date?.format(dateFormatter) ?: record.date} $dayOfWeek",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = "第 ${record.dayIndex} 天",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(text = MoodType.fromCode(record.moodTypeCode).emoji, style = MaterialTheme.typography.titleLarge)
                    Text(text = MoodType.fromCode(record.moodTypeCode).displayName, style = MaterialTheme.typography.bodyMedium)
                }
            }

            if (record.hasText && !record.moodText.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = record.moodText ?: "",
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
private fun HistorySheetContent(
    records: List<DailyMoodEntity>,
    selectedRange: Int,
    onRangeChange: (Int) -> Unit,
    onItemClick: (DailyMoodEntity) -> Unit,
    onClose: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "最近记录", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            TextButton(onClick = onClose) { Text("关闭") }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            listOf(7, 30, 90, 365).forEach { range ->
                FilterChip(
                    selected = selectedRange == range,
                    onClick = { onRangeChange(range) },
                    label = {
                        Text(
                            when (range) {
                                7 -> "7 天"
                                30 -> "30 天"
                                90 -> "90 天"
                                else -> "全年"
                            }
                        )
                    }
                )
            }
        }

        if (records.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.History,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(48.dp)
                )
                Text(text = "还没有记录", style = MaterialTheme.typography.titleMedium)
                Text(
                    text = "去上方记录今天的心情吧",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                items(records) { record ->
                    RecentRecordItem(record = record, onClick = { onItemClick(record) })
                }
            }
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
private fun StatisticsPreviewSection(
    uiState: StatisticsViewModel.StatisticsUiState,
    onRangeChange: (Int) -> Unit,
    onViewTypeChange: (StatisticsViewModel.ViewType) -> Unit,
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
                text = "统计",
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

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            FilterChip(
                selected = uiState.currentViewType == StatisticsViewModel.ViewType.MOOD,
                onClick = { onViewTypeChange(StatisticsViewModel.ViewType.MOOD) },
                label = { Text("心情") }
            )
            FilterChip(
                selected = uiState.currentViewType == StatisticsViewModel.ViewType.CHECK_IN,
                onClick = { onViewTypeChange(StatisticsViewModel.ViewType.CHECK_IN) },
                label = { Text("打卡") }
            )
        }

        StatisticsOverviewCard(uiState = uiState)

        if (uiState.currentViewType == StatisticsViewModel.ViewType.MOOD) {
            MoodTrendCard(
                moodTrendData = uiState.moodTrend,
                currentViewType = uiState.currentViewType
            )
        } else {
            MoodTrendCard(
                moodTrendData = emptyList(),
                checkInTrendData = uiState.checkInTrend,
                currentViewType = uiState.currentViewType
            )
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
