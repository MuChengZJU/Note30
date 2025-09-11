package com.example.note30

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun HistoryScreen(navController: NavController, viewModel: HistoryViewModel = viewModel()) {
    val records by viewModel.getAllRecords().collectAsState(initial = emptyList())
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("历史记录", style = MaterialTheme.typography.h5)
        
        // Timeline View
        LazyColumn {
            items(records) { record ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    elevation = 4.dp
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "${SimpleDateFormat("HH:mm", Locale.getDefault()).format(record.timestamp)} - " +
                                   "${SimpleDateFormat("HH:mm", Locale.getDefault()).format(record.timestamp.time + 30 * 60 * 1000)}",
                            style = MaterialTheme.typography.subtitle1
                        )
                        Text("效率: ${record.efficiency}", style = MaterialTheme.typography.body2)
                        record.mood?.let { mood ->
                            Text("情绪: $mood", style = MaterialTheme.typography.body2)
                        }
                        Text(record.content, style = MaterialTheme.typography.body1)
                    }
                }
            }
        }
    }
}