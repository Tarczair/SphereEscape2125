package com.example.sphereescape2125

import com.example.sphereescape2125.screens.OptionsScreen
import com.example.sphereescape2125.screens.GameScreen
import com.example.sphereescape2125.screens.MainMenu
import com.example.sphereescape2125.screens.StatScreen


import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.mazeapp.ui.theme.MazeAppTheme

// NOWE IMPORTY:
import androidx.compose.runtime.collectAsState        // Do zamiany Flow na State
import androidx.lifecycle.viewmodel.compose.viewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            // 1. Pobieramy instancję MainViewModel
            val viewModel: MainViewModel = viewModel()

            // 2. Obserwujemy StateFlow i zamieniamy go na State,
            //    który Compose może odczytać.
            //    'isDark' będzie automatycznie aktualizowane (true/false)
            val isDark by viewModel.isDarkTheme.collectAsState()

            // 3. Przekazujemy dynamiczną wartość 'isDark' zamiast 'true'
            MazeAppTheme(darkTheme = isDark) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    SphereEscapeApp()
                }
            }
        }
    }
}
@Composable
fun SphereEscapeApp() {
    var screen by remember { mutableStateOf("menu") }

    when (screen) {
        "game" -> GameScreen(onBack = { screen = "menu" })
        "options" -> OptionsScreen(onBack = { screen = "menu" })
        "stats" -> OptionsScreen(onBack = { screen = "menu" })
        "menu" -> MainMenu(onPlay = { screen = "game" },  onOptions = { screen = "options" }, onStats = { screen = "stats" })
    }
}

