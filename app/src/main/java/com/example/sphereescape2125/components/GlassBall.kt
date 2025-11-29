package com.example.sphereescape2125.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * To jest CZYSTA grafika kulki.
 * Przyjmuje 'baseColor', więc można ją łatwo przemalować.
 * Użyj tego w grze jako postać gracza (Player) lub jako element dekoracyjny.
 */
@Composable
fun GlassBall(
    modifier: Modifier = Modifier,
    baseColor: Color = Color.Cyan
) {
    Canvas(modifier = modifier) {
        val radius = size.minDimension / 2
        val centerOffset = Offset(size.width / 2, size.height / 2)

        // 1. Cień pod kulką (żeby lewitowała)
        drawCircle(
            color = Color.Black.copy(alpha = 0.3f),
            radius = radius * 0.9f,
            center = centerOffset + Offset(x = radius * 0.15f, y = radius * 0.2f)
        )

        // 2. Główna bryła (Gradient - efekt szkła)
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    baseColor.copy(alpha = 0.1f), // Środek bardzo jasny/przezroczysty
                    baseColor.copy(alpha = 0.6f)  // Brzegi nasycone
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
            style = Stroke(width = radius * 0.05f)
        )

        // 4. BŁYSK (Refleks świetlny - kluczowy dla efektu 3D)
        drawOval(
            color = Color.White.copy(alpha = 0.9f),
            topLeft = Offset(
                x = centerOffset.x - radius * 0.5f,
                y = centerOffset.y - radius * 0.6f
            ),
            size = Size(width = radius * 0.5f, height = radius * 0.3f)
        )

        // 5. Odbicie od dołu (światło odbite od podłogi)
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
 * Tu decydujemy o dynamicznych kolorach w zależności od motywu (światła).
 */
@Composable
fun PlayGlassBallButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    // --- LOGIKA KOLORÓW ---
    // 1. Sprawdzamy jasność tła z aktualnego MaterialTheme (zmienianego przez czujnik)
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f

    // 2. Definiujemy kolor kulki:
    // Ciemny motyw -> Cyan (Neonowy błękit)
    // Jasny motyw -> Intensywna Czerwień (Red Accent - wygląda jak rubin)
    val ballColor = if (isDark) {
        Color.Cyan
    } else {
        Color(0xFFFF1744)
    }


    val textColor = if (isDark) Color.White else Color(0xFF1C1B1F)

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
                indication = null // Wyłączamy standardowy cień Androida
            ) { onClick() }
    ) {
        // Rysujemy kulkę z dynamicznym kolorem
        GlassBall(
            modifier = Modifier.matchParentSize(),
            baseColor = ballColor
        )

        // Rysujemy tekst na wierzchu
        Text(
            text = "GRAJ",
            color = textColor,
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp
        )
    }
}