package com.love.diary.presentation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
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
}

object ShapeTokens {
    val Card = RoundedCornerShape(Dimens.CardCorner)
    val Field = RoundedCornerShape(Dimens.FieldCorner)
}

@Composable
fun AppCard(
    modifier: Modifier = Modifier,
    shape: Shape = ShapeTokens.Card,
    contentPadding: PaddingValues = PaddingValues(Dimens.CardPadding),
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier,
        shape = shape,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(contentPadding), content = content)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppScaffold(
    title: String,
    modifier: Modifier = Modifier,
    actions: @Composable () -> Unit = {},
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
        floatingActionButton = floatingActionButton,
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
