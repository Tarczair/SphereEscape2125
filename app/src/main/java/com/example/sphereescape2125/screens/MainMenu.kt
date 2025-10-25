package com.example.sphereescape2125.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.activity.ComponentActivity
import androidx.compose.ui.platform.LocalContext
import com.example.sphereescape2125.ui.theme.Orange


@Composable
fun MainMenu(
    onPlay: () -> Unit,
    onOptions: () -> Unit,
    onStats: () -> Unit,
) {
    val activity = LocalContext.current as? ComponentActivity
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Button(
            onClick = onPlay,
            shape = CircleShape,
            colors = ButtonDefaults.buttonColors(containerColor = Orange),
            modifier = Modifier.size(100.dp)
        ) {}

        Spacer(Modifier.height(32.dp))

        Button(onClick = {}, modifier = Modifier.fillMaxWidth()) { Text("STATYSTYKI") }
        Spacer(Modifier.height(8.dp))
        Button(onClick = onOptions, modifier = Modifier.fillMaxWidth()) { Text("OPCJE") }
        Spacer(Modifier.height(8.dp))
        Button(onClick = { activity?.finish() }, modifier = Modifier.fillMaxWidth()) { Text("WYJŚCIE") }
    }
}
