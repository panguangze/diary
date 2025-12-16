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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp

object Dimens {
    val ScreenPadding = 16.dp
    val CardPadding = 16.dp
    val SectionSpacing = 12.dp
    val LargeSpacing = 24.dp
    val CardCorner = 16.dp
    val FieldCorner = 12.dp
    val TopBarHeight = 56.dp
    val ButtonHeight = 48.dp
    val IconSize = 24.dp
}

object ShapeTokens {
    val Card = RoundedCornerShape(Dimens.CardCorner)
    val Field = RoundedCornerShape(Dimens.FieldCorner)
    val Pill = RoundedCornerShape(percent = 50) // 更圆润的pill形状
    val Button = RoundedCornerShape(12.dp) // 按钮圆角
}

@Composable
fun AppCard(
    modifier: Modifier = Modifier,
    bordered: Boolean = false,
    shape: Shape = ShapeTokens.Card,
    contentPadding: PaddingValues = PaddingValues(Dimens.CardPadding),
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val border = if (bordered) BorderStroke(1.dp, MaterialTheme.colorScheme.outline) else null
    val elevation = if (onClick != null) CardDefaults.cardElevation(defaultElevation = 2.dp) else CardDefaults.cardElevation(defaultElevation = 0.dp)
    val colors = CardDefaults.cardColors(
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface
    )

    if (onClick != null) {
        Card(
            modifier = modifier.clickable { onClick() },
            shape = shape,
            border = border,
            colors = colors,
            elevation = elevation
        ) {
            Column(modifier = Modifier.padding(contentPadding), content = content)
        }
    } else {
        Card(
            modifier = modifier,
            shape = shape,
            border = border,
            colors = colors,
            elevation = elevation
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
    content: @Composable (PaddingValues) -> Unit
) {
    Scaffold(
        modifier = modifier,
        topBar = {
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
            .padding(horizontal = 12.dp, vertical = 6.dp),
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
            .background(MaterialTheme.colorScheme.surfaceVariant, ShapeTokens.Pill)
            .padding(4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
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
                        .height(48.dp)
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
