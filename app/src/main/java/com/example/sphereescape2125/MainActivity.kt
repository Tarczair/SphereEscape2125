package com.example.sphereescape2125

import com.example.sphereescape2125.screens.OptionsScreen
import com.example.sphereescape2125.screens.GameScreen
import com.example.sphereescape2125.screens.MainMenu
import com.example.sphereescape2125.screens.StatScreen

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.example.mazeapp.ui.theme.MazeAppTheme

// Importy do ViewModel
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.viewmodel.compose.viewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            // 1. Pobieramy instancję MainViewModel
            val viewModel: MainViewModel = viewModel()

            // 2. Obserwujemy StateFlow dla motywu
            val isDark by viewModel.isDarkTheme.collectAsState()

            MazeAppTheme(darkTheme = isDark) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    // ZMIANA 1: Przekazujemy viewModel tutaj
                    SphereEscapeApp(viewModel)
                }
            }
        }
    }
}

@Composable
fun SphereEscapeApp(viewModel: MainViewModel) { // ZMIANA 2: Dodajemy parametr viewModel
    var screen by remember { mutableStateOf("menu") }

    when (screen) {
        // ZMIANA 3: Przekazujemy viewModel do GameScreen
        "game" -> GameScreen(viewModel = viewModel, onBack = { screen = "menu" })

        "options" -> OptionsScreen(onBack = { screen = "menu" })

        // POPRAWKA: Miałeś tu OptionsScreen, zmieniłem na StatScreen zgodnie z importem
        "stats" -> StatScreen(onBack = { screen = "menu" })

        "menu" -> MainMenu(
            onPlay = { screen = "game" },
            onOptions = { screen = "options" },
            onStats = { screen = "stats" }
        )
    }
}