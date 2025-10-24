package com.example.sphereescape2125.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import kotlinx.coroutines.delay

@Composable
fun GameScreen(onBack: () -> Unit) {
    var time by remember { mutableStateOf(60) }

    LaunchedEffect(Unit) {
        while (time > 0) {
            delay(1000)
            time--
        }
    }

    Column(
        modifier = Modifier
            .background(Color(0xFF333333))
            .fillMaxSize()
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Text("POZIOM I",
            modifier = Modifier.padding(top = 24.dp),
            style = MaterialTheme.typography.headlineMedium.copy(
            fontSize = 6.em,
            textAlign = TextAlign.Center,
            color = Color.White
        ),)
        Canvas(modifier = Modifier.size(300.dp)) {
            drawCircle(Color.Black, radius = size.minDimension / 2)
            drawCircle(Color.DarkGray, radius = size.minDimension / 3)
            drawCircle(Color.Red, radius = 10f, center = Offset(size.width / 2, size.height / 2))
        }
        Text("POZOSTAŁY CZAS: ${String.format("%02d:%02d", time / 60, time % 60)}",
            style = MaterialTheme.typography.headlineMedium.copy(
            fontSize = 6.em,
            textAlign = TextAlign.Center,
            color = Color.White
        ),)
        Button(
            onClick = onBack,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Red,     // kolor tła przycisku
                contentColor = Color.White,      // kolor tekstu/ikon w środku
                disabledContainerColor = Color.Gray, // kolor tła, gdy przycisk jest nieaktywny
                disabledContentColor = Color.LightGray // kolor tekstu, gdy nieaktywny
            )
        ) { Text("WRÓĆ",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontSize = 6.em,
                textAlign = TextAlign.Center
            ),
            color = Color.White
        )
        }
    }
}