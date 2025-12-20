package com.love.diary.presentation.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp

object Dimens {
    val ScreenPadding = 16.dp
    val CardPadding = 16.dp
    val SectionSpacing = 12.dp
    val LargeSpacing = 24.dp
    val MediumSpacing = 16.dp
    val SmallSpacing = 8.dp
    val CardCorner = 20.dp              // 16 → 20dp，更柔和的圆角
    val FieldCorner = 14.dp             // 12 → 14dp
    val TopBarHeight = 56.dp
    val CardElevation = 2.dp            // 新增：统一的卡片阴影高度
    val CardBorderWidth = 0.5.dp        // 新增：更精致的边框宽度
}

object ShapeTokens {
    val Card = RoundedCornerShape(Dimens.CardCorner)
    val Field = RoundedCornerShape(Dimens.FieldCorner)
    val Pill = CircleShape
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppCard(
    modifier: Modifier = Modifier,
    bordered: Boolean = false,
    shape: Shape = ShapeTokens.Card,
    contentPadding: PaddingValues = PaddingValues(Dimens.CardPadding),
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    // 优化的卡片样式：纯白背景，精致边框，subtle阴影
    val border = if (bordered) {
        BorderStroke(
            Dimens.CardBorderWidth,
            MaterialTheme.colorScheme.outlineVariant  // 使用主题色，更统一
        )
    } else null
    
    val colors = CardDefaults.cardColors(
        containerColor = MaterialTheme.colorScheme.surface  // 使用主题表面色
    )

    if (onClick != null) {
        Card(
            modifier = modifier,
            shape = shape,
            border = border,
            colors = colors,
            elevation = CardDefaults.cardElevation(
                defaultElevation = Dimens.CardElevation,
                pressedElevation = 4.dp,
                hoveredElevation = 3.dp
            ),
            onClick = onClick
        ) {
            Column(modifier = Modifier.padding(contentPadding), content = content)
        }
    } else {
        Card(
            modifier = modifier,
            shape = shape,
            border = border,
            colors = colors,
            elevation = CardDefaults.cardElevation(
                defaultElevation = Dimens.CardElevation
            )
        ) {
            Column(modifier = Modifier.padding(contentPadding), content = content)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppScaffold(
    title: String,
    modifier: Modifier = Modifier,
    actions: @Composable RowScope.() -> Unit = {},
    floatingActionButton: @Composable (() -> Unit)? = null,
    showTopBar: Boolean = true,
    backgroundBrush: Brush? = null,
    content: @Composable (PaddingValues) -> Unit
) {
    // 优化的背景渐变：更精致的色彩过渡
    val appliedBrush = backgroundBrush ?: Brush.verticalGradient(
        colors = listOf(
            MaterialTheme.colorScheme.background,
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),  // 0.6 → 0.4f，更柔和
            MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)  // 添加第三级，更自然
        ),
        startY = 0f,
        endY = 1500f  // 控制渐变范围，更自然
    )
    Scaffold(
        modifier = modifier.background(appliedBrush),
        topBar = {
            if (showTopBar) {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    },
                    actions = actions,
                )
            } else null
        },
        floatingActionButton = { floatingActionButton?.invoke() },
        content = content
    )
}

@Composable
fun SectionHeader(
    title: String,
    subtitle: String? = null,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        if (!subtitle.isNullOrBlank()) {
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun StatusBadge(
    text: String,
    modifier: Modifier = Modifier,
    containerColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.primaryContainer,
    contentColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onPrimaryContainer
    ) {
    Box(
        modifier = modifier
            .background(containerColor, ShapeTokens.Pill)
            .padding(horizontal = 14.dp, vertical = 7.dp),  // 12dp, 6dp → 14dp, 7dp，更舒适的内边距
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            color = contentColor
        )
    }
}

@Composable
fun AppSegmentedTabs(
    options: List<String>,
    selectedIndex: Int,
    onSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),  // 更subtle的背景
                ShapeTokens.Pill
            )
            .padding(4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)  // 4dp → 6dp，更宽松
        ) {
            options.forEachIndexed { index, label ->
                val selected = index == selectedIndex
                val background by animateColorAsState(
                    targetValue = if (selected) MaterialTheme.colorScheme.primary else Color.Transparent,
                    label = "tab_background"
                )
                val contentColor by animateColorAsState(
                    targetValue = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                    label = "tab_content"
                )

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)  // 48 → 44dp，更精致的比例
                        .background(background, ShapeTokens.Pill)
                        .clickable { onSelected(index) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelLarge,
                        color = contentColor
                    )
                }
            }
        }
    }
}
