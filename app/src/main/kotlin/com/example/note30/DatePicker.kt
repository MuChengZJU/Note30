package com.example.note30

import android.app.DatePickerDialog
import android.widget.DatePicker
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun DateRangePicker(
    onDateRangeSelected: (Long, Long) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val calendar = Calendar.getInstance()

    var startDate by remember { mutableStateOf<Long?>(null) }
    var endDate by remember { mutableStateOf<Long?>(null) }

    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    val startDatePickerDialog = DatePickerDialog(
        context,
        { _: DatePicker, year: Int, month: Int, dayOfMonth: Int ->
            calendar.set(year, month, dayOfMonth)
            startDate = calendar.timeInMillis
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )

    val endDatePickerDialog = DatePickerDialog(
        context,
        { _: DatePicker, year: Int, month: Int, dayOfMonth: Int ->
            calendar.set(year, month, dayOfMonth)
            endDate = calendar.timeInMillis
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )

    Column(
        modifier = Modifier.padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text("选择日期范围")
        
        Button(onClick = { startDatePickerDialog.show() }) {
            Text(text = startDate?.let { dateFormat.format(Date(it)) } ?: "选择开始日期")
        }

        Button(onClick = { endDatePickerDialog.show() }) {
            Text(text = endDate?.let { dateFormat.format(Date(it)) } ?: "选择结束日期")
        }

        Row {
            Button(onClick = onDismiss, modifier = Modifier.padding(end = 8.dp)) {
                Text("取消")
            }
            Button(
                onClick = {
                    if (startDate != null && endDate != null) {
                        onDateRangeSelected(startDate!!, endDate!!)
                    }
                },
                enabled = startDate != null && endDate != null
            ) {
                Text("确认")
            }
        }
    }
}
