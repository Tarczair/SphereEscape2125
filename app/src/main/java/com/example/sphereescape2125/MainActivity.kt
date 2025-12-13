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


/**
 * Główna aktywność startowa aplikacji Sphere Escape 2125.
 *
 * Klasa ta pełni rolę punktu wejścia (Entry Point) dla interfejsu użytkownika.
 * Odpowiada za:
 * 1. Inicjalizację głównego [MainViewModel].
 * 2. Obserwowanie globalnego stanu motywu (Ciemny/Jasny) sterowanego czujnikiem światła.
 * 3. Ustawienie głównego kontenera nawigacji [SphereEscapeApp].
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {

            val viewModel: MainViewModel = viewModel()


            val isDark by viewModel.isDarkTheme.collectAsState()

            MazeAppTheme(darkTheme = isDark) {
                Surface(modifier = Modifier.fillMaxSize()) {

                    SphereEscapeApp(viewModel)
                }
            }
        }
    }
}

/**
 * Główny kompozyt (Composable) zarządzający nawigacją w aplikacji.
 *
 * Funkcja pełni rolę prostego routera, przełączającego widoki na podstawie
 * lokalnego stanu `screen`. Przekazuje również instancję [MainViewModel]
 * do ekranów, które tego wymagają (np. [GameScreen]).
 *
 * Dostępne ekrany:
 * - "menu": Menu główne.
 * - "game": Właściwa rozgrywka.
 * - "options": Ekran opcji.
 * - "stats": Ekran statystyk.
 *
 * @param viewModel Główny ViewModel aplikacji, współdzielony między ekranami.
 */
@Composable
fun SphereEscapeApp(viewModel: MainViewModel) {
    var screen by remember { mutableStateOf("menu") }

    when (screen) {

        "game" -> GameScreen(viewModel = viewModel, onBack = { screen = "menu" })

        "options" -> OptionsScreen(onBack = { screen = "menu" })


        "stats" -> StatScreen(onBack = { screen = "menu" })

        "menu" -> MainMenu(
            onPlay = { screen = "game" },
            onOptions = { screen = "options" },
            onStats = { screen = "stats" }
        )
    }
}