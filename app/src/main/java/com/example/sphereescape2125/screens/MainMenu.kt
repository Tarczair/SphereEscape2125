package com.example.sphereescape2125.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.activity.ComponentActivity
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.sphereescape2125.components.GlassButton
import com.example.sphereescape2125.components.PlayGlassBallButton
import com.example.sphereescape2125.ui.theme.AnimatedParticleBackground


@Composable
fun MainMenu(
    onPlay: () -> Unit,
    onOptions: () -> Unit,
    onStats: () -> Unit,
) {
    val activity = LocalContext.current as? ComponentActivity

    // Główny kontener (Box pozwala nakładać elementy na siebie - tło pod spodem)
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        // 1. WARSTWA TŁA: Cząsteczki (z pliku Particles.kt)
        // Upewnij się, że Particles.kt jest w pakiecie ui.theme
        AnimatedParticleBackground(modifier = Modifier.fillMaxSize())

        // 2. WARSTWA TREŚCI: Napisy i przyciski
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(24.dp)
        ) {
            // TYTUŁ GRY
            Text(
                text = "SPHERE ESCAPE",
                color = Color.Cyan.copy(alpha = 0.8f), // Lekki neonowy błękit
                fontSize = 40.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 2.sp,
                style = androidx.compose.material3.MaterialTheme.typography.headlineLarge.copy(
                    shadow = Shadow(
                        color = Color.Black,
                        offset = Offset(4f, 4f),
                        blurRadius = 8f
                    )
                )
            )

            Spacer(modifier = Modifier.height(60.dp))

            // SZKLANA KULKA "GRAJ"
            PlayGlassBallButton(
                onClick = onPlay,
                modifier = Modifier.size(160.dp) // Rozmiar kulki w menu
            )

            Spacer(modifier = Modifier.height(60.dp))

            // LISTA PRZYCISKÓW (te prostokątne)
            GlassButton(text = "Statystyki", onClick = onStats)
            Spacer(modifier = Modifier.height(16.dp))

            GlassButton(text = "Opcje", onClick = onOptions)
            Spacer(modifier = Modifier.height(16.dp))

            GlassButton(text = "Wyjście", onClick = { activity?.finish() })
        }
    }
}

