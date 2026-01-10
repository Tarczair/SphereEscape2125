package com.example.sphereescape2125.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.activity.ComponentActivity
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.sphereescape2125.components.GlassButton
import com.example.sphereescape2125.components.PlayGlassBallButton
import com.example.sphereescape2125.ui.theme.AnimatedParticleBackground

/**
 * Ekran menu głównego gry.
 *
 * Stanowi centralny punkt nawigacji, umożliwiając rozpoczęcie rozgrywki,
 * przegląd statystyk, zmianę ustawień lub wyjście z aplikacji.
 *
 * Wizualnie komponuje dynamiczne tło cząsteczkowe [AnimatedParticleBackground]
 * z interfejsem w stylu "Glassmorphism". Kolorystyka tytułu adaptuje się
 * automatycznie do jasności tła (motyw jasny/ciemny), zapewniając optymalny kontrast.
 *
 * @param onPlay Funkcja wywoływana po naciśnięciu głównego przycisku startu (szklana kula).
 * @param onOptions Funkcja nawigująca do ekranu opcji.
 * @param onStats Funkcja nawigująca do ekranu statystyk.
 */
@Composable
fun MainMenu(
    onPlay: () -> Unit,
    onOptions: () -> Unit,
    onStats: () -> Unit,
) {
    val activity = LocalContext.current as? ComponentActivity

    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f

    val titleColor = if (isDark) {
        Color.Cyan.copy(alpha = 0.8f)
    } else {
        Color(0xFFFF1744).copy(alpha = 0.9f)
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        AnimatedParticleBackground(modifier = Modifier.fillMaxSize())

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(24.dp)
        ) {
            Text(
                text = "SPHERE ESCAPE",
                color = titleColor,
                fontSize = 40.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 2.sp,
                style = MaterialTheme.typography.headlineLarge.copy(
                    shadow = Shadow(
                        color = Color.Black,
                        offset = Offset(4f, 4f),
                        blurRadius = 8f
                    )
                )
            )

            Spacer(modifier = Modifier.height(60.dp))

            PlayGlassBallButton(
                onClick = onPlay,
                modifier = Modifier.size(160.dp)
            )

            Spacer(modifier = Modifier.height(60.dp))

            GlassButton(text = "Statystyki", onClick = onStats)
            Spacer(modifier = Modifier.height(16.dp))

            GlassButton(text = "Opcje", onClick = onOptions)
            Spacer(modifier = Modifier.height(16.dp))

            GlassButton(text = "Wyjście", onClick = { activity?.finish() })
        }
    }
}