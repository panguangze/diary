package com.love.diary.presentation.screens.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.Image
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Celebration
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.FormatQuote
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Share
import androidx.compose.ui.res.painterResource
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Divider
import androidx.compose.material3.IconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import com.love.diary.data.database.entities.DailyMoodEntity
import com.love.diary.data.model.MoodType
import com.love.diary.presentation.components.AppCard
import com.love.diary.presentation.components.Dimens
import com.love.diary.presentation.components.SectionHeader
import com.love.diary.presentation.components.ShapeTokens
import com.love.diary.presentation.components.StatusBadge
import com.love.diary.presentation.viewmodel.HistoryViewModel
import com.love.diary.presentation.viewmodel.HomeViewModel
import com.love.diary.presentation.viewmodel.StatisticsViewModel
import com.love.diary.util.ShareHelper
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

private val MoodGridMaxHeight = 240.dp
private val StatsGridMinHeight = 160.dp
private val StatsGridMaxHeight = 320.dp
private const val RecentMoodIconTargetCount = 10
private val PrimaryPink = Color(0xFFFF557F)
private val AccentYellow = Color(0xFFFFD33D)
private val NeutralGray = Color(0xFF888888)
private val AccentGreen = Color(0xFF34C759)
private val AccentBlue = Color(0xFF007AFF)
private val AccentRed = Color(0xFFFF3B30)
private val BorderColor = Color(0xFFE5E7EB)
private val SubtitleGray = Color(0xFF666666)
private val BodyGray = Color(0xFF333333)
private val HeaderTextColor = Color(0xFF2D2D33)
private val SubTextColor = Color(0xFF999999)
private val ControlTextColor = Color(0xFF4A4A52)
private val LightSurfaceColor = Color(0xFFF2F2F5)
private val UploadBorderColor = Color(0xFFE5E5EA)
private val AccentPinkText = Color(0xFFFF7A90)
private val AccentGradientStart = Color(0xFFFF6B81)
private val AccentGradientEnd = Color(0xFFFF476F)
private val MoodSelectedStart = Color(0xFFFFE6E8)
private val MoodSelectedEnd = Color(0xFFFFC2C6)

private data class MoodOption(
    val label: String,
    val moodType: MoodType
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val historyViewModel: HistoryViewModel = hiltViewModel()
    val historyRecords by historyViewModel.moodRecords.collectAsState()
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    var showCalendarSheet by remember { mutableStateOf(false) }
    var selectedHistoryItem by remember { mutableStateOf<DailyMoodEntity?>(null) }

    val detailSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val todayString = uiState.todayDate.ifBlank { LocalDate.now().toString() }

    val moodOptions = remember {
        listOf(
            MoodOption("甜蜜", MoodType.SATISFIED),
            MoodOption("开心", MoodType.HAPPY),
            MoodOption("正常", MoodType.NORMAL),
            MoodOption("失落", MoodType.SAD),
            MoodOption("愤怒", MoodType.ANGRY),
            MoodOption("其他", MoodType.OTHER)
        )
    }

    val favoriteMood = historyRecords
        .takeLast(30)
        .groupBy { it.moodTypeCode }
        .maxByOrNull { it.value.size }
        ?.let { MoodType.fromCode(it.key) }

    val pageBackground = Brush.verticalGradient(listOf(Color(0xFFFAFAFC), Color(0xFFF5F5F8)))

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(pageBackground)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Dimens.ScreenPadding)
                .padding(top = 24.dp, bottom = 80.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            TodayHeader()

            Spacer(modifier = Modifier.height(20.dp))

            TopInfoCardRedesigned(
                title = "${uiState.coupleName ?: "小明 & 小红"}的第${if (uiState.dayIndex > 0) uiState.dayIndex else 16}天",
                subtitle = "From ${uiState.startDate.ifBlank { "2025 - 01 - 01" }} to ${uiState.todayDate.ifBlank { todayString }}",
                onAiClick = { viewModel.showOtherMoodDialog() }
            )

            Spacer(modifier = Modifier.height(16.dp))

            MoodRecordSection(
                moodOptions = moodOptions,
                selectedMood = uiState.todayMood,
                inputText = uiState.otherMoodText,
                onMoodSelected = { mood ->
                    val noteToSave = uiState.otherMoodText.ifBlank { null }
                    if (mood != uiState.todayMood) {
                        viewModel.updateOtherMoodText("")
                    }
                    viewModel.selectMood(mood, noteToSave)
                },
                onInputChange = viewModel::updateOtherMoodText,
                onSave = { viewModel.saveDescription(uiState.otherMoodText) }
            )

            Spacer(modifier = Modifier.height(16.dp))

            RecentMoodStatsSection(
                recentMoods = uiState.recentTenMoods,
                totalRecords = historyRecords.size,
                streak = uiState.currentStreak,
                favoriteMood = favoriteMood,
                moodQuote = uiState.todayMood?.feedbackText
                    ?: "无论今天心情如何，我都在你身边，爱你每一天。",
                onMoreClick = { showCalendarSheet = true },
                onMoodClick = { selectedHistoryItem = it }
            )
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
        MoodCalendarDialog(
            onDismiss = { showCalendarSheet = false },
            onDateClick = { date ->
                val record = historyRecords.find { it.date == date }
                if (record != null) {
                    selectedHistoryItem = record
                }
            },
            moodRecords = historyRecords
        )
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

}

}

@Composable
private fun TodayHeader() {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = "恋爱日记",
            fontSize = 24.sp,
            lineHeight = 32.sp,
            fontWeight = FontWeight.Bold,
            color = HeaderTextColor
        )
        Spacer(modifier = Modifier.height(8.dp))
        Box(
            modifier = Modifier
                .width(80.dp)
                .height(1.dp)
                .background(AccentPinkText, shape = RoundedCornerShape(50))
        )
    }
}

@Composable
private fun TopInfoCardRedesigned(
    title: String,
    subtitle: String,
    onAiClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    lineHeight = 24.sp,
                    color = HeaderTextColor
                )
                Text(
                    text = subtitle,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Light,
                    lineHeight = 16.sp,
                    color = SubTextColor
                )
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(AccentPinkText.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Person,
                        contentDescription = null,
                        tint = AccentPinkText
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun MoodRecordSection(
    moodOptions: List<MoodOption>,
    selectedMood: MoodType?,
    inputText: String,
    onMoodSelected: (MoodType) -> Unit,
    onInputChange: (String) -> Unit,
    onSave: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 160.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "今天感觉如何？",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    lineHeight = 22.sp,
                    color = HeaderTextColor
                )

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    maxItemsInEachRow = 2
                ) {
                    moodOptions.forEach { option ->
                        MoodTag(
                            option = option,
                            selected = selectedMood == option.moodType,
                            onClick = { onMoodSelected(option.moodType) }
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    BasicTextField(
                        value = inputText,
                        onValueChange = onInputChange,
                        modifier = Modifier
                            .width(168.dp)
                            .height(40.dp)
                            .background(LightSurfaceColor, RoundedCornerShape(8.dp))
                            .border(1.dp, UploadBorderColor, RoundedCornerShape(8.dp))
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        textStyle = MaterialTheme.typography.bodyMedium.copy(
                            color = HeaderTextColor,
                            fontSize = 14.sp,
                            lineHeight = 20.sp
                        ),
                        singleLine = true,
                        decorationBox = { innerTextField ->
                            Box(
                                modifier = Modifier.fillMaxWidth(),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                if (inputText.isBlank()) {
                                    Text(
                                        text = "记录下你",
                                        color = SubTextColor,
                                        fontSize = 14.sp,
                                        lineHeight = 20.sp
                                    )
                                }
                                innerTextField()
                            }
                        }
                    )

                    Spacer(modifier = Modifier.weight(1f))

                    Button(
                        onClick = onSave,
                        enabled = selectedMood != null,
                        modifier = Modifier
                            .width(120.dp)
                            .height(40.dp)
                            .background(
                                brush = Brush.verticalGradient(
                                    colors = listOf(AccentGradientStart, AccentGradientEnd)
                                ),
                                shape = RoundedCornerShape(8.dp)
                            )
                            .drawBehind {
                                drawLine(
                                    color = Color.White.copy(alpha = 0.2f),
                                    start = Offset(0f, 0f),
                                    end = Offset(size.width, 0f),
                                    strokeWidth = 1.dp.toPx()
                                )
                            },
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Transparent,
                            contentColor = Color.White,
                            disabledContainerColor = AccentGradientStart.copy(alpha = 0.4f),
                            disabledContentColor = Color.White
                        ),
                        contentPadding = PaddingValues(horizontal = 12.dp)
                    ) {
                        Text(
                            text = "保存",
                            fontSize = 14.sp,
                            lineHeight = 20.sp
                        )
                    }
                }
            }

            Column(
                modifier = Modifier.width(120.dp),
                horizontalAlignment = Alignment.End
            ) {
                DashedUploadBox()
            }
        }
    }
}

@Composable
private fun MoodTag(
    option: MoodOption,
    selected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .width(80.dp)
            .height(40.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(8.dp),
        color = Color.Transparent,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
        border = null
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = if (selected) {
                        Brush.verticalGradient(listOf(MoodSelectedStart, MoodSelectedEnd))
                    } else {
                        Brush.verticalGradient(listOf(LightSurfaceColor, LightSurfaceColor))
                    },
                    shape = RoundedCornerShape(8.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Image(
                    painter = painterResource(id = option.moodType.getDrawableResourceId()),
                    contentDescription = option.label,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = option.label,
                    fontSize = 14.sp,
                    fontWeight = if (selected) FontWeight.Medium else FontWeight.Normal,
                    lineHeight = 20.sp,
                    color = if (selected) AccentPinkText else ControlTextColor
                )
            }
        }
    }
}

@Composable
private fun DashedUploadBox() {
    Box(
        modifier = Modifier
            .size(120.dp)
            .clip(RoundedCornerShape(8.dp))
            .drawBehind {
                drawRoundRect(
                    color = UploadBorderColor,
                    cornerRadius = CornerRadius(8.dp.toPx()),
                    style = Stroke(
                        width = 2.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 8f))
                    )
                )
            }
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.AddPhotoAlternate,
                contentDescription = null,
                tint = SubTextColor,
                modifier = Modifier.size(32.dp)
            )
            Text(
                text = "点击上传图片",
                fontSize = 12.sp,
                fontWeight = FontWeight.Normal,
                lineHeight = 16.sp,
                color = SubTextColor
            )
        }
    }
}

@Composable
private fun RecentMoodStatsSection(
    recentMoods: List<DailyMoodEntity>,
    totalRecords: Int,
    streak: Int,
    favoriteMood: MoodType?,
    moodQuote: String,
    onMoreClick: () -> Unit,
    onMoodClick: (DailyMoodEntity) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 240.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "最近心情",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    lineHeight = 22.sp,
                    color = HeaderTextColor
                )
                Text(
                    text = "更多",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    lineHeight = 16.sp,
                    color = AccentPinkText,
                    modifier = Modifier.clickable { onMoreClick() }
                )
            }

            MoodIconRow(
                recentMoods = recentMoods,
                onMoodClick = onMoodClick
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatItem(title = "已经记录", value = totalRecords.toString(), unit = "天")
                StatItem(title = "连续记录", value = streak.toString(), unit = "天")
                StatItem(
                    title = "最近30天常见心情",
                    value = favoriteMood?.displayName ?: "-",
                    unit = null,
                    highlight = true
                )
            }

            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "心情寄语",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    lineHeight = 22.sp,
                    color = HeaderTextColor
                )
                Text(
                    text = moodQuote,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Normal,
                    lineHeight = 20.sp,
                    color = ControlTextColor
                )
            }
        }
    }
}

@Composable
private fun StatItem(
    title: String,
    value: String,
    unit: String?,
    highlight: Boolean = false
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        val valueText = unit?.let { "$value$it" } ?: value
        val valueColor = if (highlight) AccentPinkText else ControlTextColor
        val valueSize = if (highlight) 18.sp else 16.sp
        val valueWeight = if (highlight) FontWeight.Bold else FontWeight.Normal

        Text(
            text = valueText,
            fontSize = valueSize,
            fontWeight = valueWeight,
            lineHeight = if (highlight) 24.sp else 22.sp,
            color = valueColor
        )
        Text(
            text = title,
            fontSize = 12.sp,
            fontWeight = FontWeight.Light,
            lineHeight = 16.sp,
            color = SubTextColor
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun MoodIconRow(
    recentMoods: List<DailyMoodEntity>,
    onMoodClick: (DailyMoodEntity) -> Unit
) {
    if (recentMoods.isEmpty()) {
        Text(
            text = "还没有心情记录，去写下第一条吧",
            fontSize = 14.sp,
            fontWeight = FontWeight.Normal,
            lineHeight = 20.sp,
            color = SubtitleGray
        )
    } else {
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            recentMoods.take(10).forEach { mood ->
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(AccentPinkText.copy(alpha = 0.08f))
                        .clickable { onMoodClick(mood) },
                    contentAlignment = Alignment.Center
                ) {
                    val moodType = MoodType.fromCode(mood.moodTypeCode)
                    Image(
                        painter = painterResource(id = moodType.getDrawableResourceId()),
                        contentDescription = moodType.displayName,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun HeroHeader(
    uiState: com.love.diary.presentation.viewmodel.HomeUiState,
    dateDisplay: String
) {
    AppCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Dimens.ScreenPadding),
        contentPadding = PaddingValues(Dimens.CardPadding)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(Dimens.SectionSpacing)
        ) {
            // Avatar row with couple names
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left avatar
                AvatarPlaceholder(
                    modifier = Modifier.semantics {
                        contentDescription = "用户头像"
                    }
                )
                
                // Center content
                Column(
                    modifier = Modifier.weight(1f).padding(horizontal = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "Day ${uiState.dayIndex}",
                        style = MaterialTheme.typography.displaySmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = uiState.coupleName?.let { "与 $it 的第 ${uiState.dayIndex} 天" } 
                            ?: "Days Together",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                    if (uiState.dayIndex % 100 == 0 && uiState.dayIndex > 0) {
                        StatusBadge(
                            text = "🎉 里程碑",
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
                
                // Right avatar
                AvatarPlaceholder(
                    modifier = Modifier.semantics {
                        contentDescription = "伴侣头像"
                    }
                )
            }
            
            // Date display
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Surface(
                    shape = ShapeTokens.Pill,
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                    tonalElevation = 0.dp,
                    shadowElevation = 0.dp
                ) {
                    Text(
                        text = dateDisplay,
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun AvatarPlaceholder(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(60.dp)
            .clip(CircleShape)
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primaryContainer,
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                    )
                )
            )
            .border(2.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Filled.Person,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(32.dp)
        )
    }
}

@Composable
private fun MoodNoteViewer(
    note: String?,
    onEdit: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(ShapeTokens.Field)
            .clickable { onEdit() },
        shape = ShapeTokens.Field,
        color = MaterialTheme.colorScheme.surfaceVariant,
        tonalElevation = 0.dp,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimens.SectionSpacing),
            verticalArrangement = Arrangement.spacedBy(Dimens.SectionSpacing)
        ) {
            Text(
                text = "今天的心情描述",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = note?.takeIf { it.isNotBlank() } ?: "点击添加一些描述吧",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onEdit) {
                    Text("修改")
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
    AppCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Dimens.ScreenPadding),
        contentPadding = PaddingValues(horizontal = Dimens.CardPadding, vertical = Dimens.SectionSpacing)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = dateDisplay,
                style = MaterialTheme.typography.titleMedium
            )

            StatusBadge(
                text = "🔥 ${streak} 天",
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MoodTimelineCard(
    uiState: com.love.diary.presentation.viewmodel.HomeUiState,
    onMoodSelected: (MoodType) -> Unit,
    onRecentMoodClick: (DailyMoodEntity) -> Unit,
    onExpandCalendar: () -> Unit,
    onNoteChange: (String) -> Unit,
    onSaveNote: (String) -> Unit,
    onEnterEdit: () -> Unit,
    onCancelEdit: () -> Unit
) {
    val noteText = uiState.otherMoodText.ifBlank { uiState.todayMoodText.orEmpty() }

    AppCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Dimens.ScreenPadding)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(Dimens.SectionSpacing)
        ) {
            SectionHeader(
                title = "今天感觉如何？",
                subtitle = "点击表情即可切换心情"
            )

            MoodSelectorRow(
                selectedMood = uiState.todayMood,
                onMoodSelected = onMoodSelected
            )

            MoodPromptText(selectedMood = uiState.todayMood)

            val shouldShowEditor = shouldShowDescriptionEditor(uiState)

            if (shouldShowEditor) {
                AnimatedVisibility(
                    visible = true,
                    enter = fadeIn() + expandVertically()
                ) {
                    MoodNoteInput(
                        note = noteText,
                        onNoteChange = onNoteChange,
                        onSave = { onSaveNote(noteText) },
                        onCancel = if (!uiState.todayMoodText.isNullOrBlank()) onCancelEdit else null,
                        isSaveEnabled = uiState.todayMood != null,
                        errorMessage = uiState.descriptionError
                    )
                }
            } else {
                MoodNoteViewer(
                    note = uiState.todayMoodText,
                    onEdit = onEnterEdit
                )
            }
            
            // Photo upload placeholder
            if (uiState.todayMood != null) {
                PhotoUploadPlaceholder(
                    modifier = Modifier.semantics {
                        contentDescription = "上传今天的照片"
                    },
                    onClick = {
                        // TODO: Implement photo picker
                    }
                )
            }

            RecentMoodsList(
                recentMoods = uiState.recentTenMoods,
                onMoodClick = onRecentMoodClick,
                onMoreClick = onExpandCalendar
            )
        }
    }
}

private fun shouldShowDescriptionEditor(
    uiState: com.love.diary.presentation.viewmodel.HomeUiState
): Boolean {
    return uiState.isDescriptionEditing ||
        (uiState.todayMood != null && uiState.todayMoodText.isNullOrBlank())
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
            horizontalArrangement = Arrangement.Start,
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun MoodSelectorRow(
    selectedMood: MoodType?,
    onMoodSelected: (MoodType) -> Unit
) {
    // Use FlowRow to wrap moods on smaller screens
    FlowRow(
        modifier = Modifier
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        maxItemsInEachRow = 3
    ) {
        MoodType.values().forEach { mood ->
            MoodButton(
                mood = mood,
                isSelected = selectedMood == mood,
                onClick = { onMoodSelected(mood) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun MoodNoteInput(
    note: String,
    onNoteChange: (String) -> Unit,
    onSave: () -> Unit,
    onCancel: (() -> Unit)? = null,
    isSaveEnabled: Boolean = true,
    errorMessage: String? = null,
    saveLabel: String = "保存记录"
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = ShapeTokens.Field,
        color = MaterialTheme.colorScheme.surfaceVariant,
        tonalElevation = 0.dp,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimens.SectionSpacing),
            verticalArrangement = Arrangement.spacedBy(Dimens.SectionSpacing)
        ) {
            Text(
                text = "今天的心情描述（可选）",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            OutlinedTextField(
                value = note,
                onValueChange = onNoteChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 96.dp),
                placeholder = { Text("写下一句话，直接保存在今天的记录里") },
                shape = ShapeTokens.Field
            )

            if (!errorMessage.isNullOrBlank()) {
                Text(
                    text = errorMessage,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                onCancel?.let {
                    TextButton(onClick = it) {
                        Text("取消")
                    }
                }
                TextButton(
                    onClick = onSave,
                    enabled = isSaveEnabled
                ) {
                    Text(saveLabel)
                }
            }
        }
    }
}

@Composable
private fun MoodPromptText(selectedMood: MoodType?) {
    val prompt = selectedMood?.feedbackText ?: "选一个心情，记录今天的状态"

    Text(
        text = prompt,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.fillMaxWidth(),
        maxLines = 2
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RecentMoodsList(
    recentMoods: List<DailyMoodEntity>,
    onMoodClick: (DailyMoodEntity) -> Unit,
    onMoreClick: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(Dimens.SectionSpacing)
    ) {
        SectionHeader(title = "最近心情")

        AppCard(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = Dimens.SectionSpacing, vertical = Dimens.SectionSpacing)
        ) {
            if (recentMoods.isEmpty()) {
                Text(
                    text = "还没有心情记录，去写下第一条吧",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RecentMoodIconsRow(
                        recentMoods = recentMoods,
                        onMoodClick = onMoodClick,
                        modifier = Modifier.weight(1f)
                    )

                    Spacer(modifier = Modifier.width(Dimens.SectionSpacing))

                    MoreMoodsButton(
                        onClick = onMoreClick,
                        modifier = Modifier.width(88.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun RecentMoodIconsRow(
    recentMoods: List<DailyMoodEntity>,
    onMoodClick: (DailyMoodEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(modifier = modifier) {
        val density = LocalDensity.current
        val iconSize = 36.dp
        val iconSpacing = 8.dp
        val targetCount = minOf(RecentMoodIconTargetCount, recentMoods.size)
        val availablePx = with(density) { maxWidth.toPx() }
        val iconPx = with(density) { iconSize.toPx() }
        val spacingPx = with(density) { iconSpacing.toPx() }
        val maxIconsFit = if (targetCount == 0) {
            0
        } else {
            // Calculate how many fixed-size icon slots fit into the single available row (no wrap)
            ((availablePx + spacingPx) / (iconPx + spacingPx)).toInt().coerceIn(1, targetCount)
        }
        val moodsToShow = recentMoods.take(maxIconsFit)

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(iconSpacing),
            verticalAlignment = Alignment.CenterVertically
        ) {
            moodsToShow.forEach { moodRecord ->
                Box(
                    modifier = Modifier
                        .size(iconSize)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer)
                        .clickable { onMoodClick(moodRecord) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = MoodType.fromCode(moodRecord.moodTypeCode).emoji,
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
        }
    }
}

@Composable
private fun RecentMoodListItem(
    moodRecord: DailyMoodEntity,
    onClick: () -> Unit
) {
    val dateFormatter = remember { DateTimeFormatter.ofPattern("MM-dd") }
    val date = runCatching { LocalDate.parse(moodRecord.date) }.getOrNull()
    val formattedDate = date?.format(dateFormatter) ?: moodRecord.date

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = formattedDate,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
            )
            Text(
                text = MoodType.fromCode(moodRecord.moodTypeCode).displayName,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        Box(
            modifier = Modifier
                .size(36.dp)
                .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = MoodType.fromCode(moodRecord.moodTypeCode).emoji,
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}

@Composable
private fun MoreMoodsButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    OutlinedButton(
        onClick = onClick,
        modifier = modifier
            .heightIn(min = 40.dp),
        shape = ShapeTokens.Field,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = if (isPressed) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.primary
        ),
        contentPadding = PaddingValues(horizontal = Dimens.SectionSpacing, vertical = Dimens.SectionSpacing / 2),
        interactionSource = interactionSource
    ) {
        Text(
            text = "更多",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary
        )
        Icon(
            imageVector = Icons.Filled.ChevronRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .padding(start = 4.dp)
                .size(16.dp)
        )
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
                        .padding(top = 8.dp)
                        .heightIn(max = MoodGridMaxHeight),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    userScrollEnabled = false
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

@OptIn(ExperimentalMaterial3Api::class)
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
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(text = mood.emoji, style = MaterialTheme.typography.headlineMedium)
                    Column {
                        Text(text = mood.displayName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text(text = "今天的记录", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
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

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
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
                fontWeight = FontWeight.SemiBold
            )
            TextButton(onClick = onExpand) { Text("更多统计") }
        }

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
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

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = StatsGridMinHeight, max = StatsGridMaxHeight),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            userScrollEnabled = false
        ) {
            items(
                listOf(
                    "记录天数" to uiState.totalRecords.toString(),
                    "平均心情" to uiState.averageMood,
                    "最常心情" to (uiState.topMood?.displayName ?: "-"),
                    "统计范围" to "最近${uiState.selectedDays}天"
                )
            ) { (title, value) ->
                StatPreviewCard(
                    title = title,
                    value = value
                )
            }
        }
    }
}

@Composable
private fun StatPreviewCard(
    title: String,
    value: String
) {
    val shape = RoundedCornerShape(12.dp)
    ElevatedCard(
        modifier = Modifier
            .heightIn(min = 140.dp)
            .border(BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant), shape),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 8.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Divider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 1.dp)
            Text(
                text = "默认展开，数据实时更新",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MoodCalendarDialog(
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

    val scrollState = rememberScrollState()

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .padding(top = 64.dp),
            contentAlignment = Alignment.TopCenter
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth(0.92f)
                    .widthIn(max = 360.dp),
                shape = RoundedCornerShape(16.dp),
                tonalElevation = 0.dp,
                shadowElevation = 24.dp,
                color = MaterialTheme.colorScheme.surface
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(scrollState)
                        .padding(horizontal = 16.dp, vertical = 16.dp),
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
                        IconButton(onClick = onDismiss) {
                            Icon(
                                imageVector = Icons.Filled.Close,
                                contentDescription = "关闭日历"
                            )
                        }
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

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
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
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val targetScale = when {
        isPressed -> 0.98f
        isSelected -> 1.2f
        else -> 1f
    }
    val scale by animateFloatAsState(
        targetValue = targetScale,
        animationSpec = androidx.compose.animation.core.tween(durationMillis = 140),
        label = "mood_button_scale"
    )

    Card(
        modifier = modifier
            .height(76.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .semantics {
                contentDescription =
                    "心情-${mood.displayName}${if (isSelected) "，已选择" else ""}"
            },
        shape = CircleShape,
        colors = CardDefaults.cardColors(
            containerColor = when {
                isPressed -> MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                isSelected -> MaterialTheme.colorScheme.primaryContainer
                else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.9f)
            }
        ),
        border = BorderStroke(
            1.dp,
            if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        onClick = onClick,
        interactionSource = interactionSource
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = mood.emoji,
                fontSize = 28.sp,
                color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface.copy(
                    alpha = 0.7f
                )
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = mood.displayName,
                style = MaterialTheme.typography.labelLarge,
                color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun PhotoUploadPlaceholder(
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(160.dp)
            .clip(ShapeTokens.Field)
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
            .border(
                width = 2.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                shape = ShapeTokens.Field
            )
            .clickable { onClick() }
            .semantics {
                contentDescription = "添加照片"
            },
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.padding(Dimens.SectionSpacing),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Filled.AddPhotoAlternate,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "添加照片记录这一刻",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "点击上传",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
private fun MoodQuoteCard(
    selectedMood: MoodType?,
    modifier: Modifier = Modifier
) {
    if (selectedMood != null) {
        AppCard(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = Dimens.ScreenPadding),
            contentPadding = PaddingValues(Dimens.CardPadding)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Dimens.SectionSpacing),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Filled.FormatQuote,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.size(32.dp)
                )
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "心情寄语",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = selectedMood.feedbackText,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        lineHeight = 20.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun StatsRow(
    totalRecords: Int,
    continuousRecords: Int,
    favoriteMood: MoodType?,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = Dimens.ScreenPadding),
        verticalArrangement = Arrangement.spacedBy(Dimens.SectionSpacing)
    ) {
        SectionHeader(
            title = "记录统计",
            subtitle = "你的心情变化"
        )
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(Dimens.SectionSpacing)
        ) {
            StatsCard(
                title = "总记录",
                value = "$totalRecords 天",
                icon = "📊",
                modifier = Modifier.weight(1f)
            )
            StatsCard(
                title = "连续打卡",
                value = "$continuousRecords 天",
                icon = "🔥",
                modifier = Modifier.weight(1f)
            )
        }
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(Dimens.SectionSpacing)
        ) {
            StatsCard(
                title = "最近30天",
                value = favoriteMood?.displayName ?: "-",
                icon = favoriteMood?.emoji ?: "💭",
                subtitle = "最常心情",
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun StatsCard(
    title: String,
    value: String,
    icon: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null
) {
    ElevatedCard(
        modifier = modifier,
        shape = ShapeTokens.Card,
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimens.SectionSpacing),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = icon,
                    style = MaterialTheme.typography.titleMedium
                )
            }
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }
        }
    }
}
