package com.example.sphereescape2125.ui.theme


import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.luminance
import kotlin.random.Random


// --- PALETA KOLORÓW TŁA ---

/** Górny kolor gradientu dla motywu ciemnego (przestrzeń kosmiczna). */
val DarkBgTop = Color(0xFF1E232E)
/** Dolny kolor gradientu dla motywu ciemnego. */
val DarkBgBottom = Color(0xFF0A0C10)
/** Kolor cząsteczek w motywie ciemnym (gwiazdy/pył). */
val DarkParticle = Color.White


/** Górny kolor gradientu dla motywu jasnego (błękit nieba). */
val LightBgTop = Color(0xFFE3F2FD)
/** Dolny kolor gradientu dla motywu jasnego. */
val LightBgBottom = Color(0xFFBBDEFB)
/** Kolor cząsteczek w motywie jasnym. */
val LightParticle = Color(0xFF1565C0)

/**
 * Model danych reprezentujący pojedynczą cząsteczkę w animacji tła.
 *
 * @property x Aktualna pozycja pozioma na ekranie.
 * @property y Aktualna pozycja pionowa na ekranie.
 * @property size Promień cząsteczki.
 * @property speedY Prędkość wznoszenia się cząsteczki.
 * @property color Kolor (wraz z kanałem alfa).
 * @property initialDelay Opóźnienie startowe, aby cząsteczki nie pojawiały się jednocześnie.
 */
data class Particle(
    var x: Float,
    var y: Float,
    val size: Float,
    val speedY: Float,
    val color: Color,
    val initialDelay: Long
)

/**
 * Komponent tła renderujący dynamiczny gradient z animowanymi cząsteczkami.
 *
 * Tworzy efekt atmosferyczny dostosowany do aktualnego motywu:
 * - W trybie ciemnym: Efekt kosmosu/głębi z jasnymi drobinami.
 * - W trybie jasnym: Efekt nieba z ciemniejszymi drobinami.
 *
 * Animacja wykorzystuje [Canvas] do rysowania dużej liczby obiektów bez obciążania układu kompozycji.
 *
 * @param modifier Modyfikator układu (domyślnie wypełnia dostępną przestrzeń).
 * @param isDark Flaga określająca tryb kolorystyczny. Domyślnie obliczana na podstawie luminancji tła motywu.
 */
@Composable
fun AnimatedParticleBackground(
    modifier: Modifier = Modifier,

    isDark: Boolean = MaterialTheme.colorScheme.background.luminance() < 0.5f
) {
    val particles = remember { mutableStateListOf<Particle>() }


    val particleBaseColor = if (isDark) DarkParticle else LightParticle
    // Inicjalizacja lub reset cząsteczek przy zmianie motywu
    LaunchedEffect(isDark) {
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

        // 1. Rysowanie TŁA (Gradient)
        drawRect(
            brush = Brush.verticalGradient(
                colors = if (isDark) listOf(DarkBgTop, DarkBgBottom)
                else listOf(LightBgTop, LightBgBottom),
                startY = 0f,
                endY = canvasHeight
            )
        )

        // 2. Symulacja i rysowanie CZĄSTECZEK
        particles.forEach { particle ->
            if (System.currentTimeMillis() - particle.initialDelay > 0) {
                // Aktualizacja pozycji (ruch w górę)
                particle.y -= particle.speedY * elapsedTime
                // Reset cząsteczki, gdy wyleci poza górną krawędź
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
