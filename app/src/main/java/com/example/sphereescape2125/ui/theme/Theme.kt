package com.example.mazeapp.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/**
 * Paleta kolorów dla trybu ciemnego.
 *
 * Składa się z ciemnoszarego tła oraz czerwonego koloru wiodącego (Primary),
 * zapewniając wysoki kontrast w słabych warunkach oświetleniowych.
 */
private val DarkColors = darkColorScheme(
    primary = Color.Red,
    secondary = Color(0xFF2196F3),
    background = Color(0xFF222222),
    surface = Color(0xFF333333),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = Color.White,
    onSurface = Color.White
)

/**
 * Paleta kolorów dla trybu jasnego.
 *
 * Wykorzystuje chłodne odcienie (turkus, błękit) oraz bardzo jasne tło,
 * nadając interfejsowi nowoczesny i przejrzysty charakter.
 */
private val LightColors = lightColorScheme(
    primary = Color(0xFF00897B),
    secondary = Color(0xFF0277BD),
    background = Color(0xFFF4F9FF),
    surface = Color(0xFFFFFFFF),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = Color(0xFF1A1C1E),
    onSurface = Color(0xFF1A1C1E)
)
/**
 * Główny kontener motywu aplikacji (Theme Wrapper).
 *
 * Funkcja ta aplikuje zdefiniowane schematy kolorów oraz typografię do drzewa widoków Jetpack Compose.
 * Pozwala na dynamiczne przełączanie trybu (jasny/ciemny) w zależności od przekazanego parametru,
 * co umożliwia sterowanie wyglądem aplikacji np. za pomocą czujnika światła.
 *
 * @param darkTheme Flaga określająca, czy ma zostać użyty tryb ciemny.
 * Domyślnie przyjmuje wartość z ustawień systemowych.
 * @param content   Treść interfejsu użytkownika (funkcja Composable), która ma zostać objęta motywem.
 */
@Composable
fun MazeAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = androidx.compose.material3.Typography(),
        content = content
    )
}
