package com.example.inspirationmushroom

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Save
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@Composable
fun RecordScreen(navController: NavController, viewModel: RecordViewModel) {
    var text by remember { mutableStateOf("") }
    val scaffoldState = rememberScaffoldState()
    val scope = rememberCoroutineScope()

    LaunchedEffect(viewModel) {
        viewModel.recordSaved.collect {
            scope.launch {
                scaffoldState.snackbarHostState.showSnackbar("记录已保存，AI分析中...")
            }
        }
    }

    Scaffold(
        scaffoldState = scaffoldState,
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "记录", 
                        style = MaterialTheme.typography.h6.copy(fontWeight = FontWeight.Bold)
                    ) 
                },
                backgroundColor = MaterialTheme.colors.primary,
                contentColor = MaterialTheme.colors.onPrimary,
                elevation = 8.dp
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    if (text.isNotBlank()) {
                        viewModel.saveRecord(content = text)
                        text = "" // 清空输入框
                    }
                },
                backgroundColor = MaterialTheme.colors.primary,
                contentColor = MaterialTheme.colors.onPrimary
            ) {
                Icon(
                    imageVector = Icons.Default.Save,
                    contentDescription = "保存记录"
                )
            }
        },
        backgroundColor = Color(0xFFF8F9FF)
    ) { paddingValues ->
        Column(
        modifier = Modifier
            .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 主文本输入区域
                Card(
                    modifier = Modifier.fillMaxWidth(),
                elevation = 4.dp,
                shape = RoundedCornerShape(16.dp),
                    backgroundColor = Color.White
                ) {
                    Column(modifier = Modifier.padding(24.dp)) {
                            Text(
                        text = "✍️ 记录你的灵感",
                        style = MaterialTheme.typography.h5.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colors.primary,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    OutlinedTextField(
                        value = text,
                        onValueChange = { text = it },
                        placeholder = {
                            Text(
                                "在这里记录你的想法、感受或经历...\n\nAI将自动帮你分析情绪、提取关键词并生成摘要。",
                                color = Color.Gray
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp),
                        colors = TextFieldDefaults.outlinedTextFieldColors(
                            focusedBorderColor = MaterialTheme.colors.primary,
                            cursorColor = MaterialTheme.colors.primary,
                            backgroundColor = Color(0xFFF8F9FF)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        maxLines = 12
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                                Text(
                        text = "💡 提示：写下任何让你有感触的事情，AI会帮你洞察其中的模式和意义。",
                                style = MaterialTheme.typography.body2,
                        color = Color.Gray
                    )
                }
            }
        }
    }
}