package com.example.sphereescape2125.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// --- IMPORTY TWOICH KOMPONENTÓW ---
import com.example.sphereescape2125.ui.theme.AnimatedParticleBackground
import com.example.sphereescape2125.components.GlassButton

@Composable
fun OptionsScreen(onBack: () -> Unit) {
    var musicVolume by remember { mutableStateOf(0.5f) }
    var soundVolume by remember { mutableStateOf(0.7f) }

    // 1. Box jako główny kontener (warstwy)
    Box(modifier = Modifier.fillMaxSize()) {

        // 2. TŁO: Cząsteczki (tak samo jak w Menu)
        AnimatedParticleBackground(modifier = Modifier.fillMaxSize())

        // 3. TREŚĆ
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // TYTUŁ
            Text(
                text = "OPCJE",
                color = Color.White,
                fontSize = 40.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 2.sp,
                style = MaterialTheme.typography.headlineLarge.copy(
                    shadow = Shadow(
                        color = Color.Cyan.copy(alpha = 0.5f),
                        offset = Offset(0f, 0f),
                        blurRadius = 20f // Neonowy blask
                    )
                ),
                modifier = Modifier.padding(bottom = 40.dp)
            )

            // SEKCJA: MUZYKA
            Text(
                text = "Głośność muzyki: ${(musicVolume * 100).toInt()}%",
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
            )

            // Stylowany Slider (Suwak)
            Slider(
                value = musicVolume,
                onValueChange = { musicVolume = it },
                colors = SliderDefaults.colors(
                    thumbColor = Color.Cyan,
                    activeTrackColor = Color.Cyan.copy(alpha = 0.8f),
                    inactiveTrackColor = Color.White.copy(alpha = 0.2f) // Wygląda jak szkło
                )
            )

            Spacer(modifier = Modifier.height(24.dp))

            // SEKCJA: DŹWIĘKI
            Text(
                text = "Głośność efektów: ${(soundVolume * 100).toInt()}%",
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
            )
            Slider(
                value = soundVolume,
                onValueChange = { soundVolume = it },
                colors = SliderDefaults.colors(
                    thumbColor = Color.Cyan,
                    activeTrackColor = Color.Cyan.copy(alpha = 0.8f),
                    inactiveTrackColor = Color.White.copy(alpha = 0.2f)
                )
            )

            Spacer(Modifier.height(50.dp))

            // PRZYCISK RESET (Szklany, ale Czerwony)
            // Tworzymy go ręcznie tutaj, bo GlassButton jest domyślnie biały/szary.
            // To doda fajny efekt niebezpieczeństwa.
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .height(50.dp)
                    .shadow(10.dp, shape = RoundedCornerShape(30.dp), spotColor = Color.Red)
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(Color(0xFFD32F2F).copy(alpha = 0.3f), Color(0xFFB71C1C).copy(alpha = 0.5f))
                        ),
                        shape = RoundedCornerShape(30.dp)
                    )
                    .border(1.dp, Color.Red.copy(alpha = 0.5f), RoundedCornerShape(30.dp))
                // Tutaj można dodać clickable { } z logiką resetu
            ) {
                Text(
                    text = "RESETUJ POSTĘP",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }

            Text(
                text = "Tej operacji nie można cofnąć.",
                color = Color.Red.copy(alpha = 0.8f),
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 8.dp)
            )

            Spacer(Modifier.height(40.dp))

            // PRZYCISK POWROTU (Nasz standardowy GlassButton)
            GlassButton(
                text = "WRÓĆ",
                onClick = onBack
            )
        }
    }
}