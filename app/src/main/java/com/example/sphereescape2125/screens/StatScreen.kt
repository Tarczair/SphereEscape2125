package com.example.sphereescape2125.screens

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sphereescape2125.components.GlassButton
import com.example.sphereescape2125.ui.theme.AnimatedParticleBackground

@Composable
fun StatScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val sharedPrefs = remember { context.getSharedPreferences("SphereEscapePrefs", Context.MODE_PRIVATE) }
    val highScore = sharedPrefs.getInt("HighScore", 0)

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        AnimatedParticleBackground(modifier = Modifier.fillMaxSize())

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(24.dp)
        ) {
            Text(
                "STATYSTYKI",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Spacer(Modifier.height(40.dp))

            Surface(
                color = Color.White.copy(alpha = 0.1f),
                shape = MaterialTheme.shapes.large
            ) {
                Column(Modifier.padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("NAJLEPSZY WYNIK", color = Color.Cyan, fontSize = 14.sp)
                    Text("$highScore", color = Color.White, fontSize = 64.sp, fontWeight = FontWeight.Black)
                }
            }

            Spacer(Modifier.height(60.dp))
            GlassButton(text = "POWRÓT", onClick = onBack)
        }
    }
}