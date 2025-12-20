package com.love.diary.presentation.components

import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * Unified Date Picker Dialog Component
 * Standardized date picker to be used across the entire app
 *
 * @param onDismiss Callback when dialog is dismissed
 * @param onDateSelected Callback with selected date in yyyy-MM-dd format
 * @param initialDate Initial date string in yyyy-MM-dd format, defaults to today
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UnifiedDatePickerDialog(
    onDismiss: () -> Unit,
    onDateSelected: (String) -> Unit,
    initialDate: String? = null
) {
    val formatter = remember { DateTimeFormatter.ofPattern("yyyy-MM-dd") }
    
    // Parse initial date or use today
    val initialMillis = remember(initialDate) {
        try {
            val date = if (!initialDate.isNullOrEmpty()) {
                LocalDate.parse(initialDate, formatter)
            } else {
                LocalDate.now()
            }
            date.atStartOfDay()
                .atZone(ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli()
        } catch (e: Exception) {
            LocalDate.now()
                .atStartOfDay()
                .atZone(ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli()
        }
    }

    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = initialMillis
    )

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val selectedDate = Instant.ofEpochMilli(millis)
                            .atZone(ZoneId.systemDefault())
                            .toLocalDate()
                            .format(formatter)
                        onDateSelected(selectedDate)
                    }
                    onDismiss()
                }
            ) {
                Text("确定")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    ) {
        DatePicker(state = datePickerState)
    }
}
