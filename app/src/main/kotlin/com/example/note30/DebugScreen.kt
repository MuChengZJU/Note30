package com.example.note30

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.delay
import java.util.concurrent.TimeUnit

@Composable
fun DebugScreen(viewModel: DebugViewModel = viewModel()) {
    val workInfo by viewModel.reminderWorkInfo.collectAsState()
    val nextReminderTime by viewModel.nextReminderTime.collectAsState()
    val context = LocalContext.current

    var timeToNext by remember { mutableStateOf<Long?>(null) }

    LaunchedEffect(nextReminderTime) {
        while (true) {
            timeToNext = if (nextReminderTime != null) {
                val remaining = nextReminderTime!! - System.currentTimeMillis()
                if (remaining > 0) remaining else 0
            } else {
                null
            }
            delay(1000)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("调试与状态", style = MaterialTheme.typography.h5.copy(fontWeight = FontWeight.Bold))

        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = 4.dp
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("后台提醒任务", style = MaterialTheme.typography.subtitle1)
                Spacer(modifier = Modifier.height(8.dp))
                Text("状态: ${workInfo?.state?.name ?: "未知"}", color = Color.Gray)

                if (timeToNext != null) {
                    val minutes = TimeUnit.MILLISECONDS.toMinutes(timeToNext!!)
                    val seconds = TimeUnit.MILLISECONDS.toSeconds(timeToNext!!) % 60
                    Text(
                        "下次提醒倒计时: ${String.format("%02d:%02d", minutes, seconds)}",
                        color = MaterialTheme.colors.primary,
                        fontWeight = FontWeight.Bold
                    )
                } else {
                    Text("下次提醒倒计时: 未计划", color = Color.Gray)
                }
            }
        }

        Button(
            onClick = { viewModel.triggerReminderNow() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("立即触发一次提醒")
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            "ColorOS / RealmeUI / OxygenOS 专用设置",
            style = MaterialTheme.typography.subtitle2,
            fontWeight = FontWeight.Bold
        )

        OutlinedButton(
            onClick = {
                val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                    putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                }
                context.startActivity(intent)
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("1. 打开应用通知设置")
        }

        OutlinedButton(
            onClick = {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    val intent = Intent(Settings.ACTION_CHANNEL_NOTIFICATION_SETTINGS).apply {
                        putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                        putExtra(Settings.EXTRA_CHANNEL_ID, Note30Application.CHANNEL_ID)
                    }
                    context.startActivity(intent)
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("2. 打开提醒渠道设置")
        }

        OutlinedButton(
            onClick = {
                val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                    data = Uri.parse("package:${context.packageName}")
                }
                context.startActivity(intent)
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("3. 请求忽略电池优化")
        }

        Text(
            "操作指南: \n" +
            "1. 在'应用通知设置'中，确保'允许通知'、'横幅'、'铃声'和'振动'都已开启。\n" +
            "2. 在'提醒渠道设置'中，再次确认上述开关都已开启。\n" +
            "3. 在'电池优化'中，选择'不允许'或'不优化'。\n" +
            "4. 还需要手动开启'自启动'和'后台锁定'。",
            style = MaterialTheme.typography.caption
        )
    }
}
