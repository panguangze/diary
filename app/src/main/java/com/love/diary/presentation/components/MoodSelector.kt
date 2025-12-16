package com.love.diary.presentation.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.love.diary.data.model.MoodType

/**
 * 简约风格的心情选择按钮
 */
@Composable
fun MoodButton(
    mood: MoodType,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.1f else 1f,
        label = "scale"
    )
    
    val backgroundColor by animateColorAsState(
        targetValue = if (isSelected) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            MaterialTheme.colorScheme.surfaceVariant
        },
        label = "background_color"
    )
    
    val contentColor by animateColorAsState(
        targetValue = if (isSelected) {
            MaterialTheme.colorScheme.onPrimaryContainer
        } else {
            MaterialTheme.colorScheme.onSurface
        },
        label = "content_color"
    )
    
    val borderColor by animateColorAsState(
        targetValue = if (isSelected) {
            MaterialTheme.colorScheme.primary
        } else {
            Color.Transparent
        },
        label = "border_color"
    )

    Card(
        modifier = modifier
            .scale(scale)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor,
            contentColor = contentColor
        ),
        border = if (isSelected) BorderStroke(2.dp, borderColor) else null,
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 4.dp else 0.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(
                        if (isSelected) MaterialTheme.colorScheme.primary 
                        else MaterialTheme.colorScheme.surface
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = mood.emoji,
                    style = MaterialTheme.typography.headlineMedium
                )
            }
            Spacer(modifier = Modifier.padding(4.dp))
            Text(
                text = mood.displayName,
                style = MaterialTheme.typography.labelMedium,
                textAlign = TextAlign.Center,
                fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal
            )
        }
    }
}

/**
 * 水平排列的心情选择行
 */
@Composable
fun MoodSelectorRow(
    selectedMood: MoodType?,
    onMoodSelected: (MoodType) -> Unit,
    modifier: Modifier = Modifier,
    moods: List<MoodType> = MoodType.values().toList()
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        moods.forEach { mood ->
            MoodButton(
                mood = mood,
                isSelected = selectedMood == mood,
                onClick = { onMoodSelected(mood) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

/**
 * 网格布局的心情选择器
 */
@Composable
fun MoodSelectorGrid(
    selectedMood: MoodType?,
    onMoodSelected: (MoodType) -> Unit,
    modifier: Modifier = Modifier,
    moods: List<MoodType> = MoodType.values().toList()
) {
    val columns = 3
    val rows = (moods.size + columns - 1) / columns // 向上取整
    
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        repeat(rows) { rowIndex ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                repeat(columns) { colIndex ->
                    val index = rowIndex * columns + colIndex
                    if (index < moods.size) {
                        val mood = moods[index]
                        MoodButton(
                            mood = mood,
                            isSelected = selectedMood == mood,
                            onClick = { onMoodSelected(mood) },
                            modifier = Modifier.weight(1f)
                        )
                    } else {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}