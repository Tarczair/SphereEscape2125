package com.example.sphereescape2125.ui.theme


import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import kotlin.random.Random

// W pliku MainMenu.kt (lub osobno, jeśli chcesz to reutilizować)

// --- DEFINICJA KOLORÓW DLA TŁA ---
// Ciemny motyw (Space)
val DarkBgTop = Color(0xFF1E232E)
val DarkBgBottom = Color(0xFF0A0C10)
val DarkParticle = Color.White

// Jasny motyw (Sky)
val LightBgTop = Color(0xFFE3F2FD) // Bardzo jasny błękit
val LightBgBottom = Color(0xFFBBDEFB) // Niebieski
val LightParticle = Color(0xFF1565C0) // Ciemny niebieski (żeby było widać kropki)

data class Particle(
    var x: Float,
    var y: Float,
    val size: Float,
    val speedY: Float,
    val color: Color,
    val initialDelay: Long
)

@Composable
fun AnimatedParticleBackground(
    modifier: Modifier = Modifier,
    // To automatycznie wykryje ustawienie z Twojego czujnika światła!
    isDark: Boolean = isSystemInDarkTheme()
) {
    val particles = remember { mutableStateListOf<Particle>() }

    // Ustawiamy kolory w zależności od motywu
    val particleBaseColor = if (isDark) DarkParticle else LightParticle

    LaunchedEffect(isDark) { // Resetujemy cząsteczki jak zmieni się motyw
        particles.clear()
        repeat(50) {
            particles.add(
                Particle(
                    x = Random.nextFloat() * 1000f,
                    y = Random.nextFloat() * 2000f,
                    size = Random.nextFloat() * 3f + 1f,
                    speedY = Random.nextFloat() * 2f + 0.5f,
                    // Losujemy alpha, ale bazowy kolor bierzemy z motywu
                    color = particleBaseColor.copy(alpha = Random.nextFloat() * 0.3f + 0.1f),
                    initialDelay = Random.nextLong(1000L)
                )
            )
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "particleAnim")
    val elapsedTime by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 100000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ), label = "particleTime"
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        val canvasWidth = size.width
        val canvasHeight = size.height

        // Rysowanie TŁA (Gradient) zależnie od motywu
        drawRect(
            brush = Brush.verticalGradient(
                colors = if (isDark) listOf(DarkBgTop, DarkBgBottom)
                else listOf(LightBgTop, LightBgBottom),
                startY = 0f,
                endY = canvasHeight
            )
        )

        // Rysowanie CZĄSTECZEK
        particles.forEach { particle ->
            if (System.currentTimeMillis() - particle.initialDelay > 0) {
                particle.y -= particle.speedY * elapsedTime
                if (particle.y < 0f) {
                    particle.y = canvasHeight + Random.nextFloat() * 100f
                    particle.x = Random.nextFloat() * canvasWidth
                }
                drawCircle(
                    color = particle.color,
                    radius = particle.size,
                    center = Offset(particle.x, particle.y)
                )
            }
        }
    }
}
