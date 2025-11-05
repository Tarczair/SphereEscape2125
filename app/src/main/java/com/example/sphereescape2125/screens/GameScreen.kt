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
import com.example.sphereescape2125.screens.obstacle.WallObstacle
import com.example.sphereescape2125.screens.obstacle.drawRingWithGaps
import com.example.sphereescape2125.screens.obstacle.isCircleCollidingWithRing
import com.example.sphereescape2125.screens.obstacle.drawWalls
import com.example.sphereescape2125.screens.obstacle.generateWallsBetweenRings
import com.example.sphereescape2125.screens.obstacle.isCircleCollidingWithWall


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
            // 1. ZMIANA: Używamy koloru tła z motywu
            .background(MaterialTheme.colorScheme.background)
            .fillMaxSize()
    ) {
        // GameCanvas jest teraz w pełni oparty na motywie
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
                    // 2. ZMIANA: Kolor tekstu "na tle" z motywu
                    color = MaterialTheme.colorScheme.onBackground
                ),
            )
            Text(
                "POZOSTAŁY CZAS: ${String.format("%02d:%02d", time / 60, time % 60)}",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontSize = 6.em,
                    textAlign = TextAlign.Center,
                    // 3. ZMIANA: Kolor tekstu "na tle" z motywu
                    color = MaterialTheme.colorScheme.onBackground
                ),
            )
            Button(
                onClick = onBack,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    // 4. ZMIANA: Używamy głównego koloru akcentu
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                // 5. ZMIANA: Usunięto 'color = Color.White'
                // Przycisk sam zarządza kolorem tekstu poprzez 'contentColor'
                Text("WRÓĆ")
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
    val walls = remember { mutableStateListOf<WallObstacle>() }


    // 6. ZMIANA: Pobieramy kolory z motywu raz, na początku
    // Użyjemy 'error' dla przeszkód (zazwyczaj czerwony)
    val obstacleColor = MaterialTheme.colorScheme.error
    // Użyjemy 'secondary' dla kuli gracza (jako wyróżnienie)
    val ballColor = MaterialTheme.colorScheme.secondary

    // Inicjalizacja pierścieni
    LaunchedEffect(Unit) {
        if (rings.isEmpty()) {
            rings.add(
                RingObstacle(
                    center = Offset(600f, 800f),
                    outerRadius = 250f,
                    innerRadius = 200f,
                    // 7. ZMIANA: Używamy koloru z motywu
                    color = obstacleColor,
                )
            )
            prevStates.add(false to false)

            rings.add(
                RingObstacle(
                    center = Offset(600f, 800f),
                    outerRadius = 500f,
                    innerRadius = 450f,
                    // 8. ZMIANA: Używamy koloru z motywu
                    color = obstacleColor,
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
                )
                val prevState = prevStates.getOrNull(index) ?: (false to false)

                // kolizja (ściana pierścienia)
                if (currentState.first && !currentState.second) {
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
                            // 9. ZMIANA: Używamy koloru z motywu
                            color = obstacleColor,
                        )
                    }
                }

                if (index < prevStates.size) {
                    prevStates[index] = currentState
                }
            }

            for (wall in walls) {
                if (isCircleCollidingWithWall(Offset(ballX, ballY), ballRadius, wall)) {
                    // kolizja z ścianą
                        // odbicie — proste odwrócenie kierunku
                        ballX -= dx
                        ballY -= dy
                        targetX = ballX
                        targetY = ballY
                        Log.d("DEBUG_TAG", "Kula uderzyła w sciane")

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

    LaunchedEffect(rings.size) {
        if (rings.size > 1) {
            val newWalls = generateWallsBetweenRings(
                rings = rings,
                existingWalls = walls,
                wallsPerGap = 3
            )

            walls.addAll(newWalls)
        }
    }


    // Rysowanie
    Canvas(
        modifier = Modifier
            .fillMaxSize()
            // 10. ZMIANA: Usunięto .background(Color.Black)
            // Tło jest już dziedziczone z Box'a w 'GameScreen'
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    targetX = offset.x
                    targetY = offset.y
                }
            }
    ) {
        drawCircle(
            // 11. ZMIANA: Używamy koloru kuli z motywu
            color = ballColor,
            radius = ballRadius,
            center = Offset(ballX, ballY)
        )

        rings.forEach { drawRingWithGaps(it) }
        drawWalls(walls)
    }
}