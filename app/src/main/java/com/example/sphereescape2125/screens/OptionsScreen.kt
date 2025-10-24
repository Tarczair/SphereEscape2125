package com.example.sphereescape2125.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.text.style.TextAlign

@Composable
fun OptionsScreen(onBack: () -> Unit) {
    var musicVolume by remember { mutableStateOf(0.5f) }
    var soundVolume by remember { mutableStateOf(0.7f) }

    Column(
        modifier = Modifier
            .background(Color(0xFF333333))
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            "OPCJE",
            style = MaterialTheme.typography.headlineLarge.copy(
                fontSize = 8.em,
                textAlign = TextAlign.Center
            ),
            color = Color.White,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(30.dp))

        Text("Głośność muzyki:",
            style = MaterialTheme.typography.headlineMedium.copy(
                    fontSize = 6.em,
                    textAlign = TextAlign.Center,
                    color = Color.White
            ),
            modifier = Modifier.fillMaxWidth()
        )
        Slider(
            value = musicVolume,
            colors = SliderDefaults.colors(
                thumbColor = Color.Red,
                activeTrackColor = Color.Red,
                inactiveTrackColor = Color.Gray
            ),
            onValueChange = { musicVolume = it }
        )

        Text("Głośność dźwięków:",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontSize = 6.em,
                    textAlign = TextAlign.Center,
                    color = Color.White
                ),
            modifier = Modifier.fillMaxWidth(),
            )
        Slider(
            value = soundVolume,
            colors = SliderDefaults.colors(
                thumbColor = Color.Red,
                activeTrackColor = Color.Red,
                inactiveTrackColor = Color.Gray
            ),
            onValueChange = { soundVolume = it }
        )

        Spacer(Modifier.height(32.dp))
        Button(
            onClick = { /* Reset logic */ },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Red,     // kolor tła przycisku
                contentColor = Color.White,      // kolor tekstu/ikon w środku
                disabledContainerColor = Color.Gray, // kolor tła, gdy przycisk jest nieaktywny
                disabledContentColor = Color.LightGray // kolor tekstu, gdy nieaktywny
            )
        ) {
            Text("RESETUJ POSTĘP",
                    style = MaterialTheme.typography.headlineMedium.copy(
                    fontSize = 6.em,
                    textAlign = TextAlign.Center
            ),)

        }
        Text(
            "UWAGA! Ten przycisk usunie wszelkie zapisane rekordy!",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontSize = 6.em,
                textAlign = TextAlign.Center,
                color = Color.White
            ),
        )

        Spacer(Modifier.height(32.dp))
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