package com.love.diary.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.love.diary.data.model.CountdownMode
import com.love.diary.data.model.UnifiedCheckInConfig

/**
 * Card component for displaying countdown check-in
 * Shows countdown progress, remaining days/times, and status
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CountdownCard(
    config: UnifiedCheckInConfig,
    daysRemaining: Int?,
    countdownRemaining: Int?,
    progress: Float,
    onCheckIn: (() -> Unit)? = null,
    onClick: () -> Unit = {}
) {
    val isCompleted = when (config.countdownMode) {
        CountdownMode.DAY_COUNTDOWN -> daysRemaining != null && daysRemaining <= 0
        CountdownMode.CHECKIN_COUNTDOWN -> countdownRemaining != null && countdownRemaining <= 0
        null -> false
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header with icon and name
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = config.icon,
                        fontSize = 32.sp
                    )
                    Column {
                        Text(
                            text = config.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        config.description?.let {
                            Text(
                                text = it,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
                
                // Status badge
                if (isCompleted) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Text(
                            text = "✓ 已完成",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }

            // Countdown display
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    when (config.countdownMode) {
                        CountdownMode.DAY_COUNTDOWN -> {
                            Text(
                                text = if (daysRemaining != null && daysRemaining > 0) {
                                    "还剩 $daysRemaining 天"
                                } else if (daysRemaining == 0) {
                                    "今天就是目标日"
                                } else {
                                    "目标日期已过"
                                },
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (isCompleted) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.onSurface
                                }
                            )
                            config.targetDate?.let {
                                Text(
                                    text = "目标日期：$it",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        CountdownMode.CHECKIN_COUNTDOWN -> {
                            Text(
                                text = if (countdownRemaining != null && countdownRemaining > 0) {
                                    "还需打卡 $countdownRemaining 次"
                                } else {
                                    "打卡目标已达成"
                                },
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (isCompleted) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.onSurface
                                }
                            )
                            Text(
                                text = "已打卡：${config.countdownProgress}/${config.countdownTarget}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            config.tag?.let {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.secondaryContainer,
                                    modifier = Modifier.padding(top = 4.dp)
                                ) {
                                    Text(
                                        text = it,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer
                                    )
                                }
                            }
                        }
                        null -> {
                            Text(
                                text = "未配置倒计时",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Progress bar
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                LinearProgressIndicator(
                    progress = progress / 100f,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp),
                    color = if (isCompleted) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.secondary
                    },
                )
                Text(
                    text = "进度：${String.format("%.1f", progress)}%",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Action button (only for check-in countdown)
            if (config.countdownMode == CountdownMode.CHECKIN_COUNTDOWN && !isCompleted && onCheckIn != null) {
                Button(
                    onClick = onCheckIn,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = countdownRemaining != null && countdownRemaining > 0
                ) {
                    Text(config.buttonLabel)
                }
            }
        }
    }
}
