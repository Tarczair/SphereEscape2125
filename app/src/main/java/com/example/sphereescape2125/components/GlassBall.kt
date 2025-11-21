package com.example.sphereescape2125.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke

/**
 * To jest CZYSTA grafika kulki.
 * Użyj tego w grze jako postać gracza (Player) lub jako element dekoracyjny.
 * Rozmiar kontrolujesz przez modifier (np. Modifier.size(50.dp)).
 */
@Composable
fun GlassBall(
    modifier: Modifier = Modifier,
    baseColor: Color = Color.Cyan
) {
    Canvas(modifier = modifier) {
        // Pobieramy najmniejszy wymiar, żeby kulka zawsze była kołem, nawet jak modifier jest prostokątem
        val radius = size.minDimension / 2

        // Obliczamy środek rysowania
        val centerOffset = Offset(size.width / 2, size.height / 2)

        // 1. Cień pod kulką (żeby lewitowała)
        drawCircle(
            color = Color.Black.copy(alpha = 0.3f),
            radius = radius * 0.9f,
            center = centerOffset + Offset(x = radius * 0.15f, y = radius * 0.2f) // Skalowany offset cienia
        )

        // 2. Główna bryła (Półprzezroczysta)
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    baseColor.copy(alpha = 0.1f),
                    baseColor.copy(alpha = 0.6f)
                ),
                center = centerOffset,
                radius = radius
            ),
            radius = radius,
            center = centerOffset
        )

        // 3. Krawędź (Obrys)
        drawCircle(
            color = baseColor.copy(alpha = 0.8f),
            radius = radius,
            center = centerOffset,
            style = Stroke(width = radius * 0.05f) // Grubość obrysu zależna od rozmiaru
        )

        // 4. BŁYSK (Refleks świetlny)
        drawOval(
            color = Color.White.copy(alpha = 0.9f),
            topLeft = Offset(
                x = centerOffset.x - radius * 0.5f,
                y = centerOffset.y - radius * 0.6f
            ),
            size = Size(width = radius * 0.5f, height = radius * 0.3f)
        )

        // 5. Mały odblask na dole (odbicie od podłogi)
        drawOval(
            color = Color.White.copy(alpha = 0.4f),
            topLeft = Offset(
                x = centerOffset.x - radius * 0.2f,
                y = centerOffset.y + radius * 0.5f
            ),
            size = Size(width = radius * 0.4f, height = radius * 0.2f)
        )
    }
}

/**
 * To jest interaktywny PRZYCISK w kształcie kulki.
 * Użyj tego w Menu Głównym.
 */
@Composable
fun PlayGlassBallButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    // Animacja "wciśnięcia" przycisku
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.9f else 1f,
        animationSpec = tween(durationMillis = 100),
        label = "ButtonScaleAnimation"
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .scale(scale)
            .clickable(
                interactionSource = interactionSource,
                indication = null // Wyłączamy standardowy cień Androida, bo mamy własną animację
            ) { onClick() }
    ) {
        // Tutaj używamy naszej osobnej komponenty graficznej
        // Domyślny rozmiar to 150.dp, ale modifier z zewnątrz może to nadpisać
        GlassBall(
            modifier = Modifier.matchParentSize(), // Wypełnij Box
            baseColor = Color.Cyan
        )

        // Opcjonalnie: Tekst "GRAJ" w środku (jeśli chcesz)

        androidx.compose.material3.Text(
            text = "GRAJ",
            color = Color.White,
            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
        )

    }
}