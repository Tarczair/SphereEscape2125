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
import androidx.compose.ui.graphics.luminance
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

    // --- LOGIKA KOLORÓW ---
    // 1. Sprawdzamy jasność tła
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f

    // 2. Definiujemy dynamiczne kolory tekstów
    val mainTextColor = if (isDark) Color.White else Color(0xFF1C1B1F) // Biały vs Ciemny Grafit
    val secondaryTextColor = if (isDark) Color.White.copy(alpha = 0.8f) else Color(0xFF1C1B1F).copy(alpha = 0.8f)

    // 3. Kolory slidera
    val sliderInactiveColor = if (isDark) Color.White.copy(alpha = 0.2f) else Color.Black.copy(alpha = 0.1f)
    val sliderActiveColor = if (isDark) Color.Cyan else Color(0xFF00897B) // Turkusowy w jasnym

    // 1. Box jako główny kontener (warstwy)
    Box(modifier = Modifier.fillMaxSize()) {

        // 2. TŁO: Cząsteczki
        AnimatedParticleBackground(modifier = Modifier.fillMaxSize())

        // 3. TREŚĆ
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // TYTUŁ "OPCJE"
            Text(
                text = "OPCJE",
                color = mainTextColor,
                fontSize = 40.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 2.sp,
                style = MaterialTheme.typography.headlineLarge.copy(
                    shadow = if (isDark) {
                        Shadow(
                            color = Color.Cyan.copy(alpha = 0.5f),
                            offset = Offset(0f, 0f),
                            blurRadius = 20f
                        )
                    } else {
                        Shadow(
                            color = Color.Black.copy(alpha = 0.1f),
                            offset = Offset(2f, 2f),
                            blurRadius = 4f
                        )
                    }
                ),
                modifier = Modifier.padding(bottom = 40.dp)
            )

            // SEKCJA: MUZYKA
            Text(
                text = "Głośność muzyki: ${(musicVolume * 100).toInt()}%",
                color = secondaryTextColor,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
            )

            // Stylowany Slider (Suwak)
            Slider(
                value = musicVolume,
                onValueChange = { musicVolume = it },
                colors = SliderDefaults.colors(
                    thumbColor = sliderActiveColor,
                    activeTrackColor = sliderActiveColor.copy(alpha = 0.8f),
                    inactiveTrackColor = sliderInactiveColor
                )
            )

            Spacer(modifier = Modifier.height(24.dp))

            // SEKCJA: DŹWIĘKI
            Text(
                text = "Głośność efektów: ${(soundVolume * 100).toInt()}%",
                color = secondaryTextColor,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
            )
            Slider(
                value = soundVolume,
                onValueChange = { soundVolume = it },
                colors = SliderDefaults.colors(
                    thumbColor = sliderActiveColor,
                    activeTrackColor = sliderActiveColor.copy(alpha = 0.8f),
                    inactiveTrackColor = sliderInactiveColor
                )
            )

            Spacer(Modifier.height(50.dp))

            // --- PRZYCISK RESET (DYNAMICZNY) ---
            val resetButtonBrush = if (isDark) {
                // Ciemny motyw: Czerwone, "żarzące się" szkło
                Brush.horizontalGradient(
                    colors = listOf(Color(0xFFD32F2F).copy(alpha = 0.3f), Color(0xFFB71C1C).copy(alpha = 0.5f))
                )
            } else {
                // Jasny motyw: Ciemnogranatowe szkło (Navy Blue)
                // Używamy alpha, żeby zachować efekt szkła, ale kolory są ciemne, żeby kontrastowały z tłem
                Brush.horizontalGradient(
                    colors = listOf(
                        Color(0xFF1A237E).copy(alpha = 0.7f), // Ciemny Indygo
                        Color(0xFF0D47A1).copy(alpha = 0.8f)  // Ciemny Niebieski
                    )
                )
            }

            // Kolor ramki
            val resetButtonBorder = if (isDark) Color.Red.copy(alpha = 0.5f) else Color(0xFF283593).copy(alpha = 0.5f)
            // Kolor cienia (poświaty)
            val resetButtonShadow = if (isDark) Color.Red else Color(0xFF1A237E).copy(alpha = 0.5f)

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .height(50.dp)
                    .shadow(8.dp, shape = RoundedCornerShape(30.dp), spotColor = resetButtonShadow)
                    .background(
                        brush = resetButtonBrush,
                        shape = RoundedCornerShape(30.dp)
                    )
                    .border(1.dp, resetButtonBorder, RoundedCornerShape(30.dp))
            ) {
                Text(
                    text = "RESETUJ POSTĘP",
                    color = Color.White, // Biały tekst wygląda świetnie i na czerwonym, i na granatowym
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }

            // Tekst ostrzegawczy pod przyciskiem
            Text(
                text = "Tej operacji nie można cofnąć.",
                // W ciemnym motywie czerwony, w jasnym granatowy (dopasowany do przycisku)
                color = if (isDark) Color.Red.copy(alpha = 0.8f) else Color(0xFF1A237E).copy(alpha = 0.8f),
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 8.dp)
            )

            Spacer(Modifier.height(40.dp))

            // PRZYCISK POWROTU
            GlassButton(
                text = "WRÓĆ",
                onClick = onBack
            )
        }
    }
}