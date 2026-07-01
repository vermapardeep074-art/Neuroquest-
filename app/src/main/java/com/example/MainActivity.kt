package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.components.ConfettiEffect
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.GameScreen
import com.example.ui.screens.SelectionScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.GameViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                val viewModel: GameViewModel = viewModel()
                val currentScreen by viewModel.currentScreen.collectAsState()
                val progress by viewModel.progress.collectAsState()
                
                var showConfetti by remember { mutableStateOf(false) }

                // Connect particle trigger
                LaunchedEffect(Unit) {
                    viewModel.confettiTrigger.collectLatest {
                        showConfetti = true
                        delay(2800)
                        showConfetti = false
                    }
                }

                Box(modifier = Modifier.fillMaxSize()) {
                    // Screen Router
                    when (currentScreen) {
                        "Dashboard" -> DashboardScreen(viewModel, progress)
                        "GameSelection" -> SelectionScreen(viewModel, progress)
                        "ActiveGame" -> GameScreen(viewModel, progress)
                        else -> DashboardScreen(viewModel, progress)
                    }

                    // Interactive overlay particle systems
                    ConfettiEffect(trigger = showConfetti)
                }
            }
        }
    }
}
