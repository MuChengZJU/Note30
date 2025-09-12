package com.example.note30

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

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Build Repository from Room
        val db = AppDatabase.getDatabase(applicationContext)
        val repository = RecordRepository(db.recordDao())
        
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
    History("history", "历史")
}

@Composable
fun Note30App(activity: ComponentActivity, repository: RecordRepository) {
    val navController = rememberNavController()
    val items = listOf(Dest.Record, Dest.History)
    val scaffoldState = rememberScaffoldState()
    val scope = rememberCoroutineScope()

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