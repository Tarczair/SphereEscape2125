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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.text.style.TextAlign
import kotlinx.coroutines.delay
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.input.pointer.pointerInput
import android.util.Log

import com.example.sphereescape2125.screens.obstacle.RingObstacle
import com.example.sphereescape2125.screens.obstacle.drawRingWithGaps
import com.example.sphereescape2125.screens.obstacle.isCircleCollidingWithRing


@Composable
fun GameScreen(onBack: () -> Unit) {
    var time by remember { mutableIntStateOf(60) }

    // Odliczanie czasu
    LaunchedEffect(Unit) {
        while (time > 0) {
            delay(1000)
            time--
        }
    }

    Box(
        modifier = Modifier
            .background(Color(0xFF111111))
            .fillMaxSize()
    ) {
        GameCanvas()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                "POZIOM I",
                modifier = Modifier.padding(top = 24.dp),
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontSize = 6.em,
                    textAlign = TextAlign.Center,
                    color = Color.White
                ),
            )
            Text(
                "POZOSTAŁY CZAS: ${String.format("%02d:%02d", time / 60, time % 60)}",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontSize = 6.em,
                    textAlign = TextAlign.Center,
                    color = Color.White
                ),
            )
            Button(
                onClick = onBack,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Red,
                    contentColor = Color.White
                )
            ) {
                Text("WRÓĆ", color = Color.White)
            }
        }
    }
}

@Composable
fun GameCanvas() {
    val isTriggered = remember { BooleanArray(20) } // więcej miejsca na pierścienie
    var ringCount by remember { mutableIntStateOf(0) }
    val prevStates = remember { mutableStateListOf<Pair<Boolean, Boolean>>() }

    // Pozycja i ruch kuli
    var ballX by remember { mutableFloatStateOf(600f) }
    var ballY by remember { mutableFloatStateOf(800f) }
    var dx by remember { mutableFloatStateOf(0f) }
    var dy by remember { mutableFloatStateOf(0f) }
    val ballRadius = 40f

    var targetX by remember { mutableFloatStateOf(ballX) }
    var targetY by remember { mutableFloatStateOf(ballY) }

    val rings = remember { mutableStateListOf<RingObstacle>() }

    // Inicjalizacja pierścieni
    LaunchedEffect(Unit) {
        if (rings.isEmpty()) {
            rings.add(
                RingObstacle(
                    center = Offset(600f, 800f),
                    outerRadius = 250f,
                    innerRadius = 200f,
                    color = Color.Red,
                    gaps = listOf(0f to 60f, 180f to 30f)
                )
            )
            prevStates.add(false to false)

            rings.add(
                RingObstacle(
                    center = Offset(600f, 800f),
                    outerRadius = 500f,
                    innerRadius = 450f,
                    color = Color.Red,
                    gaps = listOf(0f to 60f, 180f to 30f)
                )
            )
            prevStates.add(false to false)
        }
    }

    // Główna pętla gry
    LaunchedEffect(Unit) {
        while (true) {
            val speed = 6f
            val dxTotal = targetX - ballX
            val dyTotal = targetY - ballY
            val dist = kotlin.math.hypot(dxTotal, dyTotal)

            // Ruch kuli
            if (dist > speed) {
                dx = (dxTotal / dist) * speed
                dy = (dyTotal / dist) * speed
                ballX += dx
                ballY += dy
            }

            var ringToAdd: RingObstacle? = null

            // Sprawdzenie kolizji z pierścieniami
            rings.forEachIndexed { index, ring ->
                val currentState = isCircleCollidingWithRing(
                    Offset(ballX, ballY),
                    ballRadius,
                    ring,
                    isTriggered[index]
                )
                val prevState = prevStates.getOrNull(index) ?: (false to false)

                // kolizja (ściana pierścienia)
                if (currentState.first && !currentState.second) {
                    // odbicie — proste odwrócenie kierunku
                    ballX -= dx
                    ballY -= dy
                    targetX = ballX
                    targetY = ballY
                    Log.d("DEBUG_TAG", "Kula uderzyła w pierścień $index")
                }

                // kula właśnie opuściła dziurę (gap)
                if (prevState.second && !currentState.second) {
                    Log.d("DEBUG_TAG", "Kula przeszła przez pierścień $index")

                    // dodaj nowy pierścień tylko raz
                    if (!isTriggered[index]) {
                        isTriggered[index] = true
                        ringCount++

                        ringToAdd = RingObstacle(
                            center = Offset(600f, 800f),
                            outerRadius = 500f + 250f * ringCount,
                            innerRadius = 450f + 250f * ringCount,
                            color = Color.Red,
                            gaps = listOf(0f to 60f, 180f to 30f)
                        )
                    }
                }

                if (index < prevStates.size) {
                    prevStates[index] = currentState
                }
            }

            // dodaj nowy pierścień po zakończeniu iteracji
            ringToAdd?.let {
                rings.add(it)
                prevStates.add(false to false)
            }

            delay(16L)
        }
    }

    // Rysowanie
    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    targetX = offset.x
                    targetY = offset.y
                }
            }
    ) {
        rings.forEach { drawRingWithGaps(it) }

        drawCircle(
            color = Color.Cyan,
            radius = ballRadius,
            center = Offset(ballX, ballY)
        )
    }
}
