package com.example.mazeapp.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

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

// NOWA paleta jasna (Turkus / Błękit)
private val LightColors = lightColorScheme(
    primary = Color(0xFF00897B), // Główny akcent: Miętowy/Turkusowy (np. przyciski)
    secondary = Color(0xFF0277BD), // Drugi akcent: Przyjemny błękit
    background = Color(0xFFF4F9FF), // Tło: Bardzo jasny, chłodny błękit
    surface = Color(0xFFFFFFFF), // Powierzchnia (np. karty): Czysta biel dla kontrastu
    onPrimary = Color.White, // Tekst na kolorze głównym (biały dla czytelności)
    onSecondary = Color.White, // Tekst na kolorze dodatkowym
    onBackground = Color(0xFF1A1C1E), // Tekst na tle (ciemnoszary)
    onSurface = Color(0xFF1A1C1E) // Tekst na powierzchniach
)

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
