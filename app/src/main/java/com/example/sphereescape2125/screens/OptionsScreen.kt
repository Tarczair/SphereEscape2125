package com.example.sphereescape2125.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.text.style.TextAlign

@Composable
fun OptionsScreen(onBack: () -> Unit) {
    var musicVolume by remember { mutableStateOf(0.5f) }
    var soundVolume by remember { mutableStateOf(0.7f) }

    Column(
        modifier = Modifier
            // 1. ZMIANA: Używamy koloru tła z motywu
            .background(MaterialTheme.colorScheme.background)
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
            // 2. ZMIANA: Używamy koloru tekstu "na tle" z motywu
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(30.dp))

        Text("Głośność muzyki:",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontSize = 6.em,
                textAlign = TextAlign.Center,
                // 3. ZMIANA: Kolor tekstu "na tle"
                color = MaterialTheme.colorScheme.onBackground
            ),
            modifier = Modifier.fillMaxWidth()
        )
        Slider(
            value = musicVolume,
            colors = SliderDefaults.colors(
                // 4. ZMIANA: Używamy głównego koloru akcentu (primary)
                thumbColor = MaterialTheme.colorScheme.primary,
                activeTrackColor = MaterialTheme.colorScheme.primary,
                // 5. ZMIANA: Używamy stonowanego koloru dla nieaktywnej części
                inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant
            ),
            onValueChange = { musicVolume = it }
        )

        Text("Głośność dźwięków:",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontSize = 6.em,
                textAlign = TextAlign.Center,
                // 6. ZMIANA: Kolor tekstu "na tle"
                color = MaterialTheme.colorScheme.onBackground
            ),
            modifier = Modifier.fillMaxWidth(),
        )
        Slider(
            value = soundVolume,
            colors = SliderDefaults.colors(
                // 7. ZMIANA: Spójne kolory z motywu
                thumbColor = MaterialTheme.colorScheme.primary,
                activeTrackColor = MaterialTheme.colorScheme.primary,
                inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant
            ),
            onValueChange = { soundVolume = it }
        )

        Spacer(Modifier.height(32.dp))
        Button(
            onClick = { /* Reset logic */ },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                // 8. ZMIANA SEMANTYCZNA: Przycisk "Reset" to akcja niszcząca,
                //    więc używamy koloru 'error' z motywu (zazwyczaj czerwony).
                containerColor = MaterialTheme.colorScheme.error,
                contentColor = MaterialTheme.colorScheme.onError,
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
                // 9. ZMIANA: Dopasowujemy kolor ostrzeżenia do przycisku "Reset"
                color = MaterialTheme.colorScheme.error
            ),
        )

        Spacer(Modifier.height(32.dp))
        Button(
            onClick = onBack,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                // 10. ZMIANA: Przycisk "Wróć" używa głównego koloru akcentu
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        ) { Text("WRÓĆ",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontSize = 6.em,
                textAlign = TextAlign.Center
            ),
            // 11. USUNIĘCIE: Usunęliśmy 'color = Color.White',
            //    ponieważ przycisk sam zarządza kolorem tekstu (przez contentColor)
        )
        }
    }
}