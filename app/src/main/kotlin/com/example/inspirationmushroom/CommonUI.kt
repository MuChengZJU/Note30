package com.example.inspirationmushroom

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun PauseBanner() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        Color(0xFFFF6B6B), // 珊瑚红
                        Color(0xFFFFE66D)  // 阳光黄
                    )
                )
            )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "⏸️",
                style = MaterialTheme.typography.h5
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    "提醒已暂停",
                    style = MaterialTheme.typography.subtitle1.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                )
                Text(
                    "点击右上角恢复 ▶️",
                    style = MaterialTheme.typography.caption.copy(
                        color = Color.White.copy(alpha = 0.9f)
                    )
                )
            }
        }
    }
}
