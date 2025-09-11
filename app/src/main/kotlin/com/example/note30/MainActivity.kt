package com.example.note30

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Note30App()
        }
    }
}

@Composable
fun Note30App() {
    MaterialTheme {
        Surface {
            Text("Welcome to Note30!")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    Note30App()
}