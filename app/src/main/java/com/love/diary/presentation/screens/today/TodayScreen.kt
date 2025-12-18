package com.love.diary.presentation.screens.today

import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.love.diary.data.model.MoodType
import com.love.diary.presentation.components.ImagePicker
import com.love.diary.presentation.viewmodel.TodayViewModel

@Composable
fun TodayScreen(
    modifier: Modifier = Modifier,
    viewModel: TodayViewModel = hiltViewModel()
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
        TodayScreenContent(
            uiState = uiState,
            onMoodSelected = viewModel::selectMood,
            onNoteChanged = viewModel::updateMoodNote,
            onImageSelected = { viewModel.selectImage(it) },
            onImageCleared = viewModel::clearImage,
            onSave = viewModel::save,
            onErrorDismissed = viewModel::clearError,
            modifier = modifier
        )
    }
}

@Composable
private fun TodayScreenContent(
    uiState: com.love.diary.presentation.viewmodel.TodayUiState,
    onMoodSelected: (MoodType) -> Unit,
    onNoteChanged: (String) -> Unit,
    onImageSelected: (Uri) -> Unit,
    onImageCleared: () -> Unit,
    onSave: () -> Unit,
    onErrorDismissed: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    
    uiState.saveError?.let { error ->
        AlertDialog(
            onDismissRequest = onErrorDismissed,
            title = { Text("错误") },
            text = { Text(error) },
            confirmButton = {
                TextButton(onClick = onErrorDismissed) {
                    Text("确定")
                }
            }
        )
    }
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        CoupleHeaderCard(
            coupleName = uiState.coupleName ?: "我们",
            loveDays = uiState.loveDays,
            dateRangeText = uiState.dateRangeText
        )
        
        MoodSelectorSection(
            selectedMood = uiState.selectedMood,
            onMoodSelected = onMoodSelected
        )
        
        if (uiState.comfortingMessage.isNotEmpty()) {
            ComfortingMessageCard(message = uiState.comfortingMessage)
        }
        
        MoodNoteInput(
            note = uiState.moodNote,
            onNoteChanged = onNoteChanged
        )
        
        ImagePickerSection(
            imageUri = uiState.selectedImageUri,
            onImageSelected = onImageSelected,
            onImageCleared = onImageCleared
        )
        
        Button(
            onClick = onSave,
            modifier = Modifier.fillMaxWidth(),
            enabled = !uiState.isSaving && uiState.selectedMood != null,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFFF6B81)
            )
        ) {
            if (uiState.isSaving) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = Color.White
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(
                text = if (uiState.isSaving) "保存中..." else "保存",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }
        
        RecentMoodsSection(recentMoods = uiState.recentMoods)
        
        StatsCardsSection(
            totalDays = uiState.totalRecordedDays,
            consecutiveDays = uiState.consecutiveDays,
            mostCommonMood = uiState.mostCommonMood
        )
    }
}

@Composable
private fun CoupleHeaderCard(
    coupleName: String,
    loveDays: Int,
    dateRangeText: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFFFF5F7)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = coupleName,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFFF6B81)
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "在一起的第 $loveDays 天",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFFF6B81)
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = dateRangeText,
                fontSize = 12.sp,
                color = Color.Gray
            )
        }
    }
}

@Composable
private fun MoodSelectorSection(
    selectedMood: MoodType?,
    onMoodSelected: (MoodType) -> Unit
) {
    Column {
        Text(
            text = "今天的心情",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        
        val moods = listOf(
            MoodType.SWEET, MoodType.HAPPY, MoodType.NEUTRAL,
            MoodType.SAD, MoodType.ANGRY, MoodType.OTHER
        )
        
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            moods.chunked(3).forEach { rowMoods ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    rowMoods.forEach { mood ->
                        MoodButton(
                            mood = mood,
                            isSelected = mood == selectedMood,
                            onClick = { onMoodSelected(mood) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MoodButton(
    mood: MoodType,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedCard(
        modifier = modifier
            .aspectRatio(1f)
            .clickable(onClick = onClick),
        border = if (isSelected) {
            BorderStroke(2.dp, Color(0xFFFF6B81))
        } else {
            BorderStroke(1.dp, Color.LightGray)
        },
        colors = CardDefaults.outlinedCardColors(
            containerColor = if (isSelected) Color(0xFFFFF5F7) else Color.White
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = mood.emoji,
                fontSize = 36.sp
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = mood.displayName,
                fontSize = 12.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = if (isSelected) Color(0xFFFF6B81) else Color.Gray
            )
        }
    }
}

@Composable
private fun ComfortingMessageCard(message: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFFFF9E6)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Text(
            text = message,
            fontSize = 14.sp,
            color = Color(0xFF8B7355),
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        )
    }
}

@Composable
private fun MoodNoteInput(
    note: String,
    onNoteChanged: (String) -> Unit
) {
    Column {
        Text(
            text = "记录今天",
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        OutlinedTextField(
            value = note,
            onValueChange = onNoteChanged,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 100.dp),
            placeholder = { Text("写下今天的心情...") },
            maxLines = 5,
            shape = RoundedCornerShape(12.dp)
        )
    }
}

@Composable
private fun ImagePickerSection(
    imageUri: String?,
    onImageSelected: (Uri) -> Unit,
    onImageCleared: () -> Unit
) {
    Column {
        Text(
            text = "添加图片",
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        ImagePicker(
            currentImageUri = imageUri?.let { Uri.parse(it) },
            onImageSelected = { uri ->
                if (uri != null) {
                    onImageSelected(uri)
                } else {
                    onImageCleared()
                }
            }
        )
    }
}

@Composable
private fun RecentMoodsSection(recentMoods: List<com.love.diary.data.model.UnifiedCheckIn>) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "最近30天",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            
            TextButton(onClick = { /* More action - placeholder */ }) {
                Text("更多", color = Color(0xFFFF6B81))
            }
        }
        
        if (recentMoods.isEmpty()) {
            Text(
                text = "还没有记录",
                fontSize = 14.sp,
                color = Color.Gray,
                modifier = Modifier.padding(vertical = 16.dp)
            )
        } else {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(vertical = 8.dp)
            ) {
                items(recentMoods) { checkIn ->
                    checkIn.moodType?.let { mood ->
                        MoodIconItem(mood = mood)
                    }
                }
            }
        }
    }
}

@Composable
private fun MoodIconItem(mood: MoodType) {
    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(Color(0xFFF5F5F5)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = mood.emoji,
            fontSize = 24.sp
        )
    }
}

@Composable
private fun StatsCardsSection(
    totalDays: Int,
    consecutiveDays: Int,
    mostCommonMood: MoodType?
) {
    Column {
        Text(
            text = "统计",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatCard(
                title = "总记录",
                value = "$totalDays 天",
                modifier = Modifier.weight(1f)
            )
            
            StatCard(
                title = "连续",
                value = "$consecutiveDays 天",
                modifier = Modifier.weight(1f)
            )
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        mostCommonMood?.let { mood ->
            StatCard(
                title = "最常见心情（30天）",
                value = "${mood.emoji} ${mood.displayName}",
                modifier = Modifier.fillMaxWidth()
            )
        }
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
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFFF6B81)
            )
        }
    }
}
