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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sphereescape2125.ui.theme.AnimatedParticleBackground
import com.example.sphereescape2125.components.GlassButton

/**
 * Ekran ustawień aplikacji.
 *
 * Umożliwia użytkownikowi:
 * - Regulację głośności muzyki i efektów dźwiękowych za pomocą suwaków.
 * - Zresetowanie postępów gry (funkcja niszcząca).
 *
 * Komponent w pełni adaptuje się do aktualnego motywu (Jasny/Ciemny),
 * dynamicznie zmieniając kolory tekstów, suwaków oraz styl przycisku resetowania
 * (czerwona poświata w trybie ciemnym, granatowa w jasnym).
 *
 * @param onBack Funkcja wywoływana po naciśnięciu przycisku powrotu.
 */
@Composable
fun OptionsScreen(onBack: () -> Unit) {
    var musicVolume by remember { mutableStateOf(0.5f) }
    var soundVolume by remember { mutableStateOf(0.7f) }

    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f

    val mainTextColor = if (isDark) Color.White else Color(0xFF1C1B1F)
    val secondaryTextColor = if (isDark) Color.White.copy(alpha = 0.8f) else Color(0xFF1C1B1F).copy(alpha = 0.8f)

    val sliderInactiveColor = if (isDark) Color.White.copy(alpha = 0.2f) else Color.Black.copy(alpha = 0.1f)
    val sliderActiveColor = if (isDark) Color.Cyan else Color(0xFF00897B)

    Box(modifier = Modifier.fillMaxSize()) {

        AnimatedParticleBackground(modifier = Modifier.fillMaxSize())

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
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

            // --- SEKCJA AUDIO ---

            Text(
                text = "Głośność muzyki: ${(musicVolume * 100).toInt()}%",
                color = secondaryTextColor,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
            )

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

            // --- SEKCJA RESETOWANIA DANYCH ---

            val resetButtonBrush = if (isDark) {
                Brush.horizontalGradient(
                    colors = listOf(Color(0xFFD32F2F).copy(alpha = 0.3f), Color(0xFFB71C1C).copy(alpha = 0.5f))
                )
            } else {
                Brush.horizontalGradient(
                    colors = listOf(
                        Color(0xFF1A237E).copy(alpha = 0.7f),
                        Color(0xFF0D47A1).copy(alpha = 0.8f)
                    )
                )
            }

            val resetButtonBorder = if (isDark) Color.Red.copy(alpha = 0.5f) else Color(0xFF283593).copy(alpha = 0.5f)
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
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }

            Text(
                text = "Tej operacji nie można cofnąć.",
                color = if (isDark) Color.Red.copy(alpha = 0.8f) else Color(0xFF1A237E).copy(alpha = 0.8f),
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 8.dp)
            )

            Spacer(Modifier.height(40.dp))

            GlassButton(
                text = "WRÓĆ",
                onClick = onBack
            )
        }
    }
}