package org.easydictionary.app.view.pickers

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DateRangePicker
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDateRangePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.easydictionary.app.R
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun DateRangePickerDialog(
    initialStart: Long?,
    initialEnd: Long?,
    formatter: DateTimeFormatter,
    onDismiss: () -> Unit,
    onDateRangeSelected: (start: String?, end: String?) -> Unit
) {
    val pickerState = rememberDateRangePickerState(
        initialSelectedStartDateMillis = initialStart,
        initialSelectedEndDateMillis = initialEnd
    )
    val formatter = remember { formatter }

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    val startStr = pickerState.selectedStartDateMillis?.toLocalDateString(formatter)
                    val endStr = pickerState.selectedEndDateMillis?.toLocalDateString(formatter)
                    onDateRangeSelected(startStr, endStr)
                    onDismiss()
                }
            ) {
                Text(stringResource(R.string.ok))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    ) {
        DateRangePicker(
            state = pickerState,
            title = {
                Text(
                    modifier = Modifier.padding(16.dp),
                    text = stringResource(R.string.select_period)
                )
            },
            headline = {
                Column(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    val start = pickerState.selectedStartDateMillis
                    val end = pickerState.selectedEndDateMillis
                    val text = when {
                        start != null && end != null ->
                            "${start.toLocalDateString(formatter)} – ${
                                end.toLocalDateString(
                                    formatter
                                )
                            }"

                        start != null ->
                            "${start.toLocalDateString(formatter)} – …"

                        else -> stringResource(R.string.select_dates)
                    }
                    Text(text)
                }
            }
        )
    }
}

private fun Long.toLocalDateString(formatter: DateTimeFormatter): String {
    val instant = Instant.ofEpochMilli(this)
    val zoneId = ZoneId.systemDefault()
    val date = instant.atZone(zoneId).toLocalDate()
    return date.format(formatter)
}
