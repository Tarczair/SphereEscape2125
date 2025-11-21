package com.example.sphereescape2125.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun GlassButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(targetValue = if (isPressed) 0.95f else 1f, label = "scale")

    // LOGIKA KOLORÓW DLA MOTYWU
    val isDark = isSystemInDarkTheme()

    val textColor = if (isDark) Color.White else Color(0xFF1C1B1F) // Biały vs Prawie Czarny
    val borderColor = if (isDark) Color.White.copy(alpha = 0.3f) else Color.White.copy(alpha = 0.6f) // W jasnym mocniejsza ramka
    val containerGradient = if (isDark) {
        listOf(Color.White.copy(alpha = 0.15f), Color.White.copy(alpha = 0.05f))
    } else {
        // W jasnym motywie szkło musi być bardziej "mleczne" (więcej bieli)
        listOf(Color.White.copy(alpha = 0.6f), Color.White.copy(alpha = 0.3f))
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .scale(scale)
            .width(250.dp)
            .background(
                brush = Brush.verticalGradient(colors = containerGradient),
                shape = RoundedCornerShape(30.dp)
            )
            .border(
                width = 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(30.dp)
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) { onClick() }
            .padding(vertical = 16.dp)
    ) {
        Text(
            text = text.uppercase(),
            color = textColor, // Dynamiczny kolor tekstu
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )
    }
}