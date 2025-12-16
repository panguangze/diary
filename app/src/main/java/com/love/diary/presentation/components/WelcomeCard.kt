package com.love.diary.presentation.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * 简约风格的欢迎卡片
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WelcomeCard(
    title: String,
    subtitle: String,
    onContinue: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = ShapeTokens.Card,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimens.CardPadding),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Dimens.LargeSpacing)
        ) {
            // 标题部分
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Spacer(modifier = Modifier.height(Dimens.SectionSpacing))
                
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            // 继续按钮
            Button(
                onClick = onContinue,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(Dimens.ButtonHeight),
                shape = ShapeTokens.Button,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Text(
                    text = "开始记录",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

/**
 * 简约风格的输入卡片
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InputCard(
    title: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    onContinue: () -> Unit,
    modifier: Modifier = Modifier,
    isDateInput: Boolean = false
) {
    Card(
        modifier = modifier,
        shape = ShapeTokens.Card,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimens.CardPadding),
            verticalArrangement = Arrangement.spacedBy(Dimens.SectionSpacing)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                placeholder = { Text(placeholder) },
                modifier = Modifier.fillMaxWidth(),
                shape = ShapeTokens.Field
            )
            
            Button(
                onClick = onContinue,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(Dimens.ButtonHeight),
                shape = ShapeTokens.Button,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                enabled = value.isNotBlank()
            ) {
                Text(
                    text = "继续",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}