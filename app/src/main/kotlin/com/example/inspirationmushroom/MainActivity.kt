package com.example.inspirationmushroom

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.compose.runtime.remember
import androidx.work.WorkManager
import android.app.Application
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import android.Manifest
import android.os.Build
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberPermissionState
import com.example.inspirationmushroom.data.SettingsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Build Repository from Room
        val db = AppDatabase.getDatabase(applicationContext)
        val settingsRepository = SettingsRepository(applicationContext)
        val repository = RecordRepository(db.recordDao(), settingsRepository)

        // Automatically retry pending analysis on startup
        CoroutineScope(Dispatchers.IO).launch {
            // Use .first() to get the list only once and prevent an infinite loop from .collect()
            val pendingRecords = repository.getPendingAnalysisRecords().first()
            for (record in pendingRecords) {
                repository.retryAnalysis(record)
            }
        }
        
        cancelFollowUpWorkIfNeeded(intent)

        setContent { Note30App(this, repository) }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        setIntent(intent) // Update the activity's intent
        cancelFollowUpWorkIfNeeded(intent)
    }

    private fun cancelFollowUpWorkIfNeeded(intent: Intent?) {
        intent?.getStringExtra(ReminderWorker.FOLLOW_UP_TAG_KEY)?.let { tag ->
            WorkManager.getInstance(applicationContext).cancelAllWorkByTag(tag)
        }
    }
}

private enum class Dest(val route: String, val label: String) {
    Record("record", "记录"),
    History("history", "历史"),
    Settings("settings", "设置")
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun Note30App(activity: ComponentActivity, repository: RecordRepository) {
    val navController = rememberNavController()
    val items = listOf(Dest.Record, Dest.History, Dest.Settings)
    val scaffoldState = rememberScaffoldState()
    val scope = rememberCoroutineScope()

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        val permissionState = rememberPermissionState(permission = Manifest.permission.POST_NOTIFICATIONS)
        LaunchedEffect(Unit) {
            permissionState.launchPermissionRequest()
        }
    }

    Scaffold(
        scaffoldState = scaffoldState,
        bottomBar = {
            BottomNavigation(
                backgroundColor = MaterialTheme.colors.surface,
                contentColor = MaterialTheme.colors.primary,
                elevation = 8.dp
            ) {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route
                items.forEach { dest ->
                    BottomNavigationItem(
                        selected = currentRoute == dest.route,
                        onClick = {
                            navController.navigate(dest.route) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = {
                            Icon(
                                imageVector = when (dest) {
                                    Dest.Record -> Icons.Default.Create
                                    Dest.History -> Icons.Default.History
                                    Dest.Settings -> Icons.Default.Settings
                                },
                                contentDescription = dest.label
                            )
                        },
                        label = { Text(dest.label) },
                        selectedContentColor = MaterialTheme.colors.primary,
                        unselectedContentColor = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
                    )
                }
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = Dest.Record.route,
            modifier = androidx.compose.ui.Modifier.padding(paddingValues)
        ) {
            composable(Dest.Record.route) {
                val vm: RecordViewModel = viewModel(
                    factory = RecordViewModelFactory(activity.application, repository)
                )
                
                LaunchedEffect(vm) {
                    vm.recordSaved.collect {
                        scope.launch {
                            scaffoldState.snackbarHostState.showSnackbar("记录已保存")
                        }
                    }
                }

                RecordScreen(navController = navController, viewModel = vm)
            }
            composable(Dest.History.route) {
                val vm: HistoryViewModel = viewModel(
                    factory = HistoryViewModelFactory(activity.application, repository)
                )
                HistoryScreen(navController = navController, viewModel = vm)
            }
            composable(Dest.Settings.route) {
                val vm: SettingsViewModel = viewModel(
                    factory = SettingsViewModelFactory(activity.application)
                )
                SettingsScreen(viewModel = vm)
            }
        }
    }
}
class RecordViewModelFactory(
    private val application: Application,
    private val repository: RecordRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RecordViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return RecordViewModel(application, repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
class HistoryViewModelFactory(
    private val application: Application,
    private val repository: RecordRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HistoryViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return HistoryViewModel(application, repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

class SettingsViewModelFactory(
    private val application: Application
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SettingsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SettingsViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}