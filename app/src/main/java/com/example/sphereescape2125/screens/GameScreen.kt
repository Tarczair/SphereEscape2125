package com.example.sphereescape2125.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
// import androidx.compose.foundation.gestures.detectTapGestures // ZMIANA: Usunięte
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
// import androidx.compose.ui.input.pointer.pointerInput // ZMIANA: Usunięte
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import com.example.sphereescape2125.screens.obstacle.RingObstacle
import com.example.sphereescape2125.screens.obstacle.WallObstacle
import com.example.sphereescape2125.screens.obstacle.drawRingWithGaps
import com.example.sphereescape2125.screens.obstacle.isCircleCollidingWithRing
import com.example.sphereescape2125.screens.obstacle.drawWalls
import com.example.sphereescape2125.screens.obstacle.generateWallsBetweenRings
import kotlinx.coroutines.delay
import android.util.Log
import kotlin.math.hypot

// --- NOWE IMPORTY ---
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.platform.LocalContext
import com.example.sphereescape2125.sensors.TiltSensor // Upewnij się, że ścieżka jest poprawna
import com.example.sphereescape2125.screens.obstacle.getWallCollisionInfo // WAŻNY IMPORT!
// --- Koniec nowych importów ---


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
            .background(MaterialTheme.colorScheme.background)
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
                    color = MaterialTheme.colorScheme.onBackground
                )
            )
            Text(
                "POZOSTAŁY CZAS: ${String.format("%02d:%02d", time / 60, time % 60)}",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontSize = 6.em,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onBackground
                )
            )
            Button(
                onClick = onBack,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Text("WRÓĆ")
            }
        }
    }
}

@Composable
fun GameCanvas() {
    val isTriggered = remember { mutableStateListOf<Boolean>().apply { repeat(20) { add(false) } } }
    var ringCount by remember { mutableIntStateOf(0) }
    val prevStates = remember { mutableStateListOf<Pair<Boolean, Boolean>>() }

    // --- Pozycja kulki w 'remember' ---
    var ballX by remember { mutableFloatStateOf(600f) }
    var ballY by remember { mutableFloatStateOf(800f) }
    val ballRadius = 40f

    // --- Usunięto targetX i targetY ---

    val rings = remember { mutableStateListOf<RingObstacle>() }
    val walls = remember { mutableStateListOf<WallObstacle>() }

    val obstacleColor = MaterialTheme.colorScheme.error
    val ballColor = MaterialTheme.colorScheme.secondary

    // --- Integracja TiltSensor (bez zmian) ---
    val context = LocalContext.current
    val tiltSensor = remember { TiltSensor(context) }

    DisposableEffect(Unit) {
        tiltSensor.startListening()
        onDispose {
            tiltSensor.stopListening()
        }
    }
    val gravityData by tiltSensor.gravityData.collectAsState() // DOBRZE
    // --- Koniec integracji ---

    LaunchedEffect(Unit) {
        if (rings.isEmpty()) {
            rings.add(
                RingObstacle(
                    center = Offset(600f, 800f),
                    outerRadius = 250f,
                    innerRadius = 200f,
                    color = obstacleColor
                )
            )
            prevStates.add(false to false)

            rings.add(
                RingObstacle(
                    center = Offset(600f, 800f),
                    outerRadius = 500f,
                    innerRadius = 450f,
                    color = obstacleColor
                )
            )
            prevStates.add(false to false)
        }
    }

    // --- Prędkość w 'remember' ---
    var velocityX by remember { mutableFloatStateOf(0f) }
    var velocityY by remember { mutableFloatStateOf(0f) }



// --- NOWE STAŁE FIZYKI ---
    val accelerationFactor = 0.5f // Jeszcze mniejsza czułość (mniej "rwie")
    val friction = 0.96f          // Wyraźnie większe tarcie (kulka "walczy" z ruchem)
    val maxSpeed = 4f             // Niska prędkość maksymalna (powinna być "spacerowa")

    // ---  NOWA LINIA: KALIBRACJA POZYCJI "ZERO" ---
    // Ustawia "zero" na lekki przechył. 0.0f = idealnie płasko. 9.8f = idealnie pionowo.
    // Musisz poeksperymentować z tą wartością!
    val CALIBRATION_OFFSET_Y = 4f

    LaunchedEffect(Unit) {
        while (true) {

            // --- NOWA, BEZPOŚREDNIA LOGIKA RUCHU ---

            // 1. Użyj danych z sensora jako bezpośredniego przyspieszenia.
            val ax = -gravityData.x * accelerationFactor
            // --- MODYFIKACJA ---
            // Odczyt z grawitacji 'Y' jest korygowany o nasz offset
            val ay = (gravityData.y - CALIBRATION_OFFSET_Y) * accelerationFactor
            // -------------------

            // 2. Dodaj przyspieszenie do aktualnej prędkości
            velocityX += ax
            velocityY += ay

            // 3. Zastosuj tarcie (opór)
            velocityX *= friction
            velocityY *= friction

            // 4. Ogranicz maksymalną prędkość
            val speed = hypot(velocityX, velocityY)
            if (speed > maxSpeed) {
                velocityX = (velocityX / speed) * maxSpeed
                velocityY = (velocityY / speed) * maxSpeed
            }

            // 5. Zaktualizuj pozycję kulki na podstawie finalnej prędkości
            ballX += velocityX
            ballY += velocityY

            // --- KONIEC NOWEJ LOGIKI RUCHU ---


            // --- Logika kolizji z pierścieniami (bez zmian) ---
            var ringToAdd: RingObstacle? = null

            rings.forEachIndexed { index, ring ->
                val currentState = isCircleCollidingWithRing(Offset(ballX, ballY), ballRadius, ring)
                val prevState = prevStates.getOrNull(index) ?: (false to false)

                if (currentState.first && !currentState.second) {
                    // Odbicie od pierścienia z utratą energii (0.8f)
                    velocityX = -velocityX * 0.8f
                    velocityY = -velocityY * 0.8f
                    Log.d("DEBUG_TAG", "Kula uderzyła w pierścień $index")

                    ballX += velocityX // Lekkie wypchnięcie
                    ballY += velocityY
                }
                if (prevState.second && !currentState.second && !isTriggered[index]) {
                    isTriggered[index] = true
                    ringCount++
                    ringToAdd = RingObstacle(
                        center = Offset(600f, 800f),
                        outerRadius = 500f + 250f * ringCount,
                        innerRadius = 450f + 250f * ringCount,
                        color = obstacleColor
                    )
                }
                if (index < prevStates.size) {
                    prevStates[index] = currentState
                }
            }

            // W pliku: GameScreen.kt

            // --- NOWA, OSTATECZNA FIZYKA KOLIZJI ZE ŚCIANĄ ---
            for (wall in walls) {
                // Używamy naszej nowej, stabilnej funkcji detekcji
                val collisionInfo = getWallCollisionInfo(
                    circleCenter = Offset(ballX, ballY),
                    circleRadius = ballRadius,
                    wall = wall
                )

                if (collisionInfo != null) {
                    // Mamy kolizję!
                    val (closestPoint, distance, normal) = collisionInfo // Teraz mamy 'normal'
                    val wallHalfWidth = 25f
                    val collisionThreshold = ballRadius + wallHalfWidth

                    // 1. Oblicz głębokość penetracji
                    // Dodajemy mały bufor 0.01f, aby uniknąć "drżenia"
                    val penetrationDepth = (collisionThreshold - distance) + 0.01f

                    // 2. Wypchnij kulkę ze ściany (zapobiega utknięciu)
                    // Ta część jest kluczowa i musi być zrobiona NAJPIERW
                    ballX += normal.x * penetrationDepth
                    ballY += normal.y * penetrationDepth

                    // 3. Oblicz PRAWIDŁOWĄ reakcję na prędkość (Model "Constraint")

                    // 3a. Oblicz iloczyn skalarny prędkości i normalnej
                    val dot = (velocityX * normal.x) + (velocityY * normal.y)

                    // 3b. Reaguj TYLKO jeśli kulka leci W STRONĘ ściany (dot < 0)
                    if (dot < 0) {
                        // 3c. Anuluj prędkość prostopadłą i zastosuj odbicie (bounciness)
                        // To jest wzór na pełną reakcję (odbicie + ślizg)
                        // v_nowe = v - (1 + bounciness) * dot(v, n) * n

                        val bounciness = 0.6f // Zmniejszyłem odbicie, aby było mniej "sprężyste"
                        val restitution = 1.0f + bounciness

                        val reflectVx = velocityX - (restitution * dot * normal.x)
                        val reflectVy = velocityY - (restitution * dot * normal.y)

                        velocityX = reflectVx
                        velocityY = reflectVy
                    }
                    // Jeśli dot >= 0, kulka już się oddala (lub ślizga).
                    // Samo wypchnięcie pozycyjne wystarczy. Nie ruszamy prędkości.
                }
            } // --- Koniec pętli for (wall in walls) ---


            ringToAdd?.let {
                rings.add(it)
                prevStates.add(false to false)
            }

            delay(16L) // Dąży do ~60 klatek na sekundę
        }
    }

    LaunchedEffect(rings.size) {
        if (rings.size > 1) {
            // Twoja logika generowania ścian - zostaje
            val newWalls = generateWallsBetweenRings(
                rings = rings,
                existingWalls = walls,
                wallsPerGap = 6 + (4 * (rings.size - 2)),
                color = obstacleColor
            )
            walls.addAll(newWalls)
        }
    }

    Canvas(
        modifier = Modifier
            .fillMaxSize()
    ) {
        // Twoja logika kamery - zostaje
        val canvasCenter = Offset(size.width / 2f, size.height / 2f)
        val cameraOffset = canvasCenter - Offset(ballX, ballY)
        drawContext.canvas.save()
        drawContext.canvas.translate(cameraOffset.x, cameraOffset.y)

        drawCircle(
            color = ballColor,
            radius = ballRadius,
            center = Offset(ballX, ballY)
        )

        rings.forEach { drawRingWithGaps(it) }
        drawWalls(walls)

        drawContext.canvas.restore()
    }
}