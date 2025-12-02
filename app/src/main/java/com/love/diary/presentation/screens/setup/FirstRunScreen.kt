// presentation/screens/setup/FirstRunScreen.kt
package com.love.diary.presentation.screens.setup

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.style.TextAlign
import com.love.diary.data.repository.AppRepository
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.launch

@Composable
fun FirstRunScreen(
    repository: AppRepository,
    onSetupComplete: () -> Unit,
    modifier: Modifier = Modifier
) {
    var startDate by remember { mutableStateOf(LocalDate.now().toString()) }
    var coupleName by remember { mutableStateOf("") }
    var yourName by remember { mutableStateOf("") }
    var partnerName by remember { mutableStateOf("") }

    val coroutineScope = rememberCoroutineScope()
    val dateFormatter = remember { DateTimeFormatter.ofPattern("yyyy-MM-dd") }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 标题
            Text(
                text = "欢迎使用恋爱日记 💕",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(32.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp)
                ) {
                    Text(
                        text = "让我们开始记录吧",
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    // 恋爱开始日期
                    OutlinedTextField(
                        value = startDate,
                        onValueChange = { startDate = it },
                        label = { Text("恋爱开始日期") },
                        placeholder = { Text("例如：2023-06-01") },
                        leadingIcon = {
                            Icon(Icons.Default.DateRange, contentDescription = null)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // 组合名字
                    OutlinedTextField(
                        value = coupleName,
                        onValueChange = { coupleName = it },
                        label = { Text("给我们的组合起个名字") },
                        placeholder = { Text("例如：小猫和大熊") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // 个人昵称
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = yourName,
                            onValueChange = { yourName = it },
                            label = { Text("你的昵称") },
                            placeholder = { Text("例如：小猫") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = partnerName,
                            onValueChange = { partnerName = it },
                            label = { Text("TA的昵称") },
                            placeholder = { Text("例如：大熊") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // 说明文字
            Text(
                text = "这些信息将用于计算恋爱天数，生成专属的恋爱日记。",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            // 开始按钮
            Button(
                onClick = {
                    coroutineScope.launch {
                        repository.initializeFirstRun(
                            startDate = startDate,
                            coupleName = if (coupleName.isNotBlank()) coupleName else null,
                            partnerNickname = if (partnerName.isNotBlank()) partnerName else null
                        )
                        onSetupComplete()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = startDate.isNotBlank()
            ) {
                Text(
                    text = "开始记录我们的爱情",
                    fontSize = 18.sp,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
        }
    }
}