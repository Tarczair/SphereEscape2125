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
 * Komponent graficzny renderujący stylizowaną, szklaną kulę.
 *
 * Funkcja wykorzystuje natywny [Canvas] do narysowania obiektu z efektem pseudo-3D.
 * Składa się z kilku warstw rysowanych w następującej kolejności:
 * 1. Cień pod obiektem (dla efektu lewitacji).
 * 2. Główna bryła z gradientem radialnym (efekt szkła).
 * 3. Obrys (krawędź).
 * 4. Refleks świetlny (błysk) i odbicie dolne.
 *
 * @param modifier Modyfikator układu dla tego elementu.
 * @param baseColor Kolor bazowy kuli. Domyślnie [Color.Cyan].
 */
@Composable
fun GlassBall(
    modifier: Modifier = Modifier,
    baseColor: Color = Color.Cyan
) {
    Canvas(modifier = modifier) {
        val radius = size.minDimension / 2
        val centerOffset = Offset(size.width / 2, size.height / 2)


        drawCircle(
            color = Color.Black.copy(alpha = 0.3f),
            radius = radius * 0.9f,
            center = centerOffset + Offset(x = radius * 0.15f, y = radius * 0.2f)
        )


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


        drawCircle(
            color = baseColor.copy(alpha = 0.8f),
            radius = radius,
            center = centerOffset,
            style = Stroke(width = radius * 0.05f)
        )


        drawOval(
            color = Color.White.copy(alpha = 0.9f),
            topLeft = Offset(
                x = centerOffset.x - radius * 0.5f,
                y = centerOffset.y - radius * 0.6f
            ),
            size = Size(width = radius * 0.5f, height = radius * 0.3f)
        )


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
 * Interaktywny przycisk w kształcie szklanej kuli z etykietą tekstową.
 *
 * Komponent ten dostosowuje swój kolor do aktualnego motywu aplikacji (jasny/ciemny)
 * na podstawie luminancji tła. Zawiera animację skalowania (zmniejszania)
 * w momencie wciśnięcia.
 *
 * @param modifier Modyfikator układu.
 * @param onClick Funkcja wywoływana po kliknięciu przycisku.
 */
@Composable
fun PlayGlassBallButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()


    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f


    val ballColor = if (isDark) {
        Color.Cyan
    } else {
        Color(0xFFFF1744)
    }


    val textColor = if (isDark) Color.White else Color(0xFF1C1B1F)


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
                indication = null
            ) { onClick() }
    ) {

        GlassBall(
            modifier = Modifier.matchParentSize(),
            baseColor = ballColor
        )


        Text(
            text = "GRAJ",
            color = textColor,
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp
        )
    }
}