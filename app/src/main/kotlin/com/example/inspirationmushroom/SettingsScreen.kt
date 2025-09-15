package com.example.inspirationmushroom

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(viewModel: SettingsViewModel = viewModel()) {
    val scaffoldState = rememberScaffoldState()
    val scope = rememberCoroutineScope()

    val apiUrl by viewModel.apiUrl.collectAsState()
    val apiKey by viewModel.apiKey.collectAsState()
    val selectedModel by viewModel.selectedModel.collectAsState()
    val availableModels by viewModel.availableModels.collectAsState()
    val isLoadingModels by viewModel.isLoadingModels.collectAsState()

    Scaffold(
        scaffoldState = scaffoldState,
        topBar = {
            TopAppBar(
                title = { Text("AI 设置") },
                backgroundColor = MaterialTheme.colors.primary,
                contentColor = MaterialTheme.colors.onPrimary
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // API URL 输入
            OutlinedTextField(
                value = apiUrl,
                onValueChange = { viewModel.updateApiUrl(it) },
                label = { Text("API 地址") },
                placeholder = { Text("例如: https://api.openai.com") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                singleLine = true
            )

            // API Key 输入
            OutlinedTextField(
                value = apiKey,
                onValueChange = { viewModel.updateApiKey(it) },
                label = { Text("API 密钥") },
                placeholder = { Text("输入你的 API Key") },
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                singleLine = true
            )

            // 模型选择区域
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = 4.dp
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "模型选择",
                        style = MaterialTheme.typography.h6
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // 获取模型列表按钮
                        Button(
                            onClick = {
                                scope.launch {
                                    viewModel.loadAvailableModels()
                                }
                            },
                            enabled = apiUrl.isNotBlank() && apiKey.isNotBlank(),
                            modifier = Modifier.weight(1f)
                        ) {
                            if (isLoadingModels) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = MaterialTheme.colors.onPrimary
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = "刷新"
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("获取模型")
                            }
                        }

                        // 保存配置按钮
                        Button(
                            onClick = {
                                scope.launch {
                                    val success = viewModel.saveConfiguration()
                                    if (success) {
                                        scaffoldState.snackbarHostState.showSnackbar("配置已保存")
                                    } else {
                                        scaffoldState.snackbarHostState.showSnackbar("保存失败")
                                    }
                                }
                            },
                            enabled = apiUrl.isNotBlank() && apiKey.isNotBlank()
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "保存"
                            )
                        }
                    }

                    // 模型选择下拉菜单
                    if (availableModels.isNotEmpty()) {
                        var expanded by remember { mutableStateOf(false) }

                        ExposedDropdownMenuBox(
                            expanded = expanded,
                            onExpandedChange = { expanded = !expanded }
                        ) {
                            OutlinedTextField(
                                value = selectedModel ?: "选择模型",
                                onValueChange = {},
                                readOnly = true,
                                trailingIcon = {
                                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                                },
                                modifier = Modifier.fillMaxWidth()
                            )

                            ExposedDropdownMenu(
                                expanded = expanded,
                                onDismissRequest = { expanded = false }
                            ) {
                                availableModels.forEach { model ->
                                    DropdownMenuItem(
                                        onClick = {
                                            viewModel.updateSelectedModel(model)
                                            expanded = false
                                        }
                                    ) {
                                        Text(text = model)
                                    }
                                }
                            }
                        }
                    } else if (apiUrl.isNotBlank() && apiKey.isNotBlank()) {
                        Text(
                            text = "点击\"获取模型\"按钮加载可用模型",
                            style = MaterialTheme.typography.body2,
                            color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }
            }

            // 说明文本
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = 2.dp
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "使用说明",
                        style = MaterialTheme.typography.h6,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Text(
                        text = "1. 输入兼容 OpenAI API 的服务地址\n" +
                              "2. 输入对应的 API 密钥\n" +
                              "3. 点击\"获取模型\"加载可用模型列表\n" +
                              "4. 选择你想要使用的模型\n" +
                              "5. 点击保存配置\n\n" +
                              "你的配置将安全地存储在本地设备上。",
                        style = MaterialTheme.typography.body2
                    )
                }
            }
        }
    }
}
