package com.example.sphereescape2125.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import com.example.sphereescape2125.screens.obstacle.*
import android.app.Activity
import android.view.WindowManager
import kotlinx.coroutines.delay
import android.util.Log
import kotlin.math.hypot
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.platform.LocalContext
import com.example.sphereescape2125.screens.obstacle.EffectType
import com.example.sphereescape2125.sensors.TiltSensor
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.floor
import kotlin.math.min

@Composable
fun AndroidKeepScreenOn() {
    val context = LocalContext.current
    DisposableEffect(Unit) {
        val window = (context as Activity).window
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        onDispose {
            window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }
}

@Composable
fun GameOverScreen(onBack: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background.copy(alpha = 0.9f))
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("KONIEC GRY", style = MaterialTheme.typography.headlineLarge, color = MaterialTheme.colorScheme.primary)
        Text("Skończył sie czas!", style = MaterialTheme.typography.headlineLarge, color = MaterialTheme.colorScheme.primary)

        Button(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
            Text("WRÓĆ DO MENU")
        }
    }
}

@Composable
fun GameScreen(onBack: () -> Unit) {
    AndroidKeepScreenOn()
    var bestScore by rememberSaveable { mutableStateOf(0) }
    var currentScore by remember { mutableStateOf(0) }
    var hasWon by remember { mutableStateOf(false) }
    var hasLost by remember { mutableStateOf(false) } // NOWY STAN
    var timeLeft by remember { mutableStateOf(60) }

    LaunchedEffect(Unit) {
        if (bestScore == 0) {
            bestScore = 200
        }
    }

    Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        GameCanvas(
            hasWon = hasWon,
            hasLost = hasLost, // PRZEKAZANIE NOWEGO STANU
            remainingTime = timeLeft,
            onTimeChange = { update ->
                timeLeft = update.coerceAtLeast(0)
                if (timeLeft == 0 && !hasWon) { // SPRAWDZANIE CZY CZAS SIE SKONCZYL
                    hasLost = true
                }
            },
            onScoreChange = { newScore -> currentScore = newScore },
            onWin = { finalScore ->
                currentScore = finalScore
                bestScore = maxOf(finalScore, bestScore)
                hasWon = true
            }
        )

        // Zmień warunek wyświetlania HUD
        if (!hasWon && !hasLost) {
            GameHUD(timeLeft = timeLeft, currentScore = currentScore, onBack = onBack)
        }

        // Dodaj ekran przegranej
        if (hasLost) {
            GameOverScreen(onBack = onBack)
        }

        // Ekran wygranej zostaje
        if (hasWon) {
            VictoryScreen(points = currentScore, bestScore = bestScore, onBack = onBack)
        }
    }
}

@Composable
fun GameHUD(timeLeft: Int, currentScore: Int, onBack: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Bottom    // <-- kluczowe
    ) {

        // Teksty nad przyciskiem
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "POZIOM I",
                style = MaterialTheme.typography.headlineMedium.copy(fontSize = 6.em)
            )

            Text(
                "WYNIK: $currentScore",
                style = MaterialTheme.typography.headlineMedium.copy(fontSize = 6.em),
                modifier = Modifier.padding(top = 8.dp)
            )

            Text(
                "POZOSTAŁY CZAS: ${String.format("%02d:%02d", timeLeft / 60, timeLeft % 60)}",
                style = MaterialTheme.typography.headlineMedium.copy(fontSize = 6.em),
                modifier = Modifier.padding(top = 8.dp, bottom = 16.dp)
            )
        }

        // Przycisk na samym dole
        Button(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
            Text("WRÓĆ")
        }
    }

}

@Composable
fun VictoryScreen(points: Int, bestScore: Int, onBack: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background.copy(alpha = 0.9f)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Text("ZWYCIĘSTWO!", style = MaterialTheme.typography.headlineLarge, color = MaterialTheme.colorScheme.primary)
            Text("Zdobyte punkty: $points", style = MaterialTheme.typography.headlineMedium)
            Text("Najlepszy wynik: $bestScore", style = MaterialTheme.typography.headlineMedium)
            Button(onClick = onBack, modifier = Modifier.fillMaxWidth().padding(horizontal = 40.dp)) {
                Text("WRÓĆ")
            }
        }
    }
}

fun getLineSegmentCollision(
    circleCenter: Offset,
    circleRadius: Float,
    start: Offset,
    end: Offset,
    normal: Offset
): Triple<Offset, Float, Offset>? {
    val dx = end.x - start.x
    val dy = end.y - start.y
    val lenSq = dx*dx + dy*dy
    if (lenSq == 0f) return null

    val t = ((circleCenter.x - start.x) * dx + (circleCenter.y - start.y) * dy) / lenSq
    val clampedT = t.coerceIn(0f, 1f)
    val closest = Offset(start.x + clampedT * dx, start.y + clampedT * dy)
    val dist = hypot(circleCenter.x - closest.x, circleCenter.y - closest.y)

    if (dist <= circleRadius) {
        return Triple(closest, dist, normal)
    }
    return null
}

@Composable
fun GameCanvas(
    hasWon: Boolean,
    hasLost: Boolean,
    remainingTime: Int,
    onTimeChange: (Int) -> Unit,
    onScoreChange: (Int) -> Unit,
    onWin: (score: Int) -> Unit
) {
    val isTriggered = remember { mutableStateListOf<Boolean>().apply { repeat(20) { add(false) } } }
    var ringCount by remember { mutableIntStateOf(0) }
    val prevStates = remember { mutableStateListOf<Pair<Boolean, Boolean>>() }

    val maxRings = 15
    var ballX by remember { mutableFloatStateOf(600f) }
    var ballY by remember { mutableFloatStateOf(800f) }
    val ballRadius = 40f

    val rings = remember { mutableStateListOf<RingObstacle>() }
    val walls = remember { mutableStateListOf<WallObstacle>() }

    val obstacleColor = MaterialTheme.colorScheme.error
    val ballColor = MaterialTheme.colorScheme.secondary

    val context = LocalContext.current
    val tiltSensor = remember { TiltSensor(context) }
    val ringTimes = remember { mutableStateListOf<Long>() }
    var localHighScore by remember { mutableIntStateOf(0) }
    var localTimer by remember { mutableIntStateOf(remainingTime) }
    val gapWalls = remember { mutableStateListOf<GapWall>() }
    var timeSinceLastRing by remember { mutableLongStateOf(System.currentTimeMillis()) }

    LaunchedEffect(Unit) {
        while (true) {
            if (!hasWon && localTimer > 0) {
                delay(1000)
                localTimer--
                onTimeChange(localTimer)
            } else {
                delay(1000)
            }
        }
    }

    DisposableEffect(Unit) {
        tiltSensor.startListening()
        onDispose { tiltSensor.stopListening() }
    }
    val gravityData by tiltSensor.gravityData.collectAsState()

    LaunchedEffect(Unit) {
        if (rings.isEmpty()) {
            // POPRAWKA 2: Poprawne przekazanie ringCount
            rings.add(RingObstacle(Offset(600f, 800f), 250f, 200f, obstacleColor, ringCount = 0))
            prevStates.add(false to false)
            rings.add(RingObstacle(Offset(600f, 800f), 500f, 450f, obstacleColor, ringCount = 1))
            prevStates.add(false to false)
        }
    }

    var velocityX by remember { mutableFloatStateOf(0f) }
    var velocityY by remember { mutableFloatStateOf(0f) }

    var wallCountModifier by remember { mutableFloatStateOf(0f) }
    var gapCountModifier by remember { mutableFloatStateOf(0f) }
    var pendingPointModifier by remember { mutableFloatStateOf(0f) }

    val accelerationFactor = 0.5f
    val friction = 0.96f
    val maxSpeed = 15f
    val CALIBRATION_OFFSET_Y = 4f

    LaunchedEffect(Unit) {
        var lastUpdateTime = System.currentTimeMillis() // Dodanie śledzenia czasu
        while (true) {
            val currentTime = System.currentTimeMillis()

            if (hasWon || hasLost) {
                velocityX = 0f
                velocityY = 0f
                delay(16L)
                lastUpdateTime = currentTime // Aktualizacja czasu
                continue
            }

            val delta = (currentTime - lastUpdateTime).toFloat() / 1000f // Obliczenie delty

            val ax = -gravityData.x * accelerationFactor
            val ay = (gravityData.y - CALIBRATION_OFFSET_Y) * accelerationFactor

            velocityX += ax
            velocityY += ay
            velocityX *= friction
            velocityY *= friction

            val speed = hypot(velocityX, velocityY)
            if (speed > maxSpeed) {
                velocityX = (velocityX / speed) * maxSpeed
                velocityY = (velocityY / speed) * maxSpeed
            }

            // POPRAWKA 1: Użycie delty w aktualizacji pozycji
            ballX += velocityX * delta * 60
            ballY += velocityY * delta * 60

            var ringToAdd: RingObstacle? = null

            // ... (reszta logiki GameCanvas jest poprawna i pozostaje bez zmian) ...
            // UWAGA: Logika efektów jest już poprawna w Twoim wklejonym kodzie.

            // 1. Obliczanie stanów
            val currentStates = mutableListOf<Pair<Boolean, Boolean>>()
            rings.forEach { r ->
                currentStates.add(isCircleCollidingWithRing(Offset(ballX, ballY), ballRadius, r))
            }

            // 2. GapWalls
            gapWalls.clear()
            rings.forEach { ring -> gapWalls.addAll(ring.generateGapWalls()) }

            // 3. Kolizja z GapWalls (ścianki przerw)
            var collidedWithGap = false
            for (gw in gapWalls) {
                val result = getLineSegmentCollision(Offset(ballX, ballY), ballRadius, gw.start, gw.end, gw.normal)
                if (result != null) {
                    collidedWithGap = true
                    val (_, dist, normal) = result
                    val penetration = ballRadius - dist
                    if (penetration > 0f) {
                        ballX += normal.x * (penetration + 0.1f)
                        ballY += normal.y * (penetration + 0.1f)
                    }
                    val dot = velocityX * normal.x + velocityY * normal.y
                    if (dot < 0f) {
                        velocityX -= dot * normal.x
                        velocityY -= dot * normal.y
                    }
                }
            }

            // 4. Kolizja ze ścianami promienistymi (czerwone linie)
            for (wall in walls) {
                val info = getWallCollisionInfo(Offset(ballX, ballY), ballRadius, wall)
                if (info != null) {
                    val (_, dist, normal) = info
                    val wallHalfWidth = 25f
                    val penetration = (ballRadius + wallHalfWidth) - dist

                    if (penetration > 0f) {
                        ballX += normal.x * (penetration + 0.5f)
                        ballY += normal.y * (penetration + 0.5f)
                    }

                    val dot = velocityX * normal.x + velocityY * normal.y
                    if (dot < 0f) {
                        velocityX -= dot * normal.x
                        velocityY -= dot * normal.y
                    }
                }
            }

            // 5. Kolizja z pierścieniami (jeśli nie w gapie i nie w gapWall)
            if (!collidedWithGap) {
                for (index in rings.indices) {
                    val ring = rings[index]
                    val currentState = currentStates.getOrNull(index) ?: (false to false)
                    val prevState = prevStates.getOrNull(index) ?: (false to false)

                    // currentState.first == true oznacza "fizycznie dotykam pierścienia"
                    if (currentState.first && !currentState.second) {
                        val dx = ballX - ring.center.x
                        val dy = ballY - ring.center.y
                        val distance = hypot(dx, dy)

                        if (distance > 0f) {
                            // Sprawdzamy, czy jesteśmy bliżej wewnętrznej czy zewnętrznej krawędzi
                            val distToInner = abs(distance - ring.innerRadius)
                            val distToOuter = abs(distance - ring.outerRadius)

                            var normalX = dx / distance
                            var normalY = dy / distance
                            var penetration = 0f

                            if (distToInner < distToOuter) {
                                // Odbicie od WEWNĘTRZNEJ ściany (do środka planszy)
                                // Chcemy być na pozycji: innerRadius - ballRadius
                                penetration = (ring.innerRadius - ballRadius) - distance
                                // Normalna powinna wpychać nas do środka (przeciwnie do dx,dy)
                                normalX = -normalX
                                normalY = -normalY
                            } else {
                                // Odbicie od ZEWNĘTRZNEJ ściany (na zewnątrz planszy)
                                // Chcemy być na pozycji: outerRadius + ballRadius
                                penetration = distance - (ring.outerRadius + ballRadius)
                                // Normalna wypycha na zewnątrz (zgodnie z dx,dy)
                                // (tu normalX/Y są już ustawione na zewnątrz)
                            }

                            // Korekta pozycji (push out) - abs() bo penetration może wyjść ujemne przy złych założeniach, ale tutaj kierunek normalnej załatwia sprawę
                            // Dla bezpieczeństwa używamy abs(penetration) i kierunku normalnej
                            val pushAmount = abs(penetration) + 0.5f

                            ballX += normalX * pushAmount
                            ballY += normalY * pushAmount

                            val dot = velocityX * normalX + velocityY * normalY
                            if (dot < 0f) {
                                velocityX -= dot * normalX
                                velocityY -= dot * normalY
                            }
                        }
                    }

                    // Trigger efektów i dodawanie nowych ringów
                    if (prevState.second && !currentState.second && !isTriggered.getOrNull(index).let { it == true }) {
                        val angle = Math.toDegrees(kotlin.math.atan2((ballY - ring.center.y).toDouble(), (ballX - ring.center.x).toDouble())).let { if (it < 0) it + 360 else it }

                        val effect = ring.gapEffects.minByOrNull { g -> abs(g.midAngle - angle) }
                        effect?.let { g ->
                            when (g.effect.type) {
                                EffectType.WALLS -> {
                                    when (g.effect.operation) {
                                        Operation.ADD, Operation.SUB -> wallCountModifier += g.effect.value
                                        Operation.MULTIPLY -> wallCountModifier = (wallCountModifier * g.effect.value).coerceAtMost(3f).coerceAtLeast(1f)
                                        Operation.DIVIDE -> wallCountModifier = (wallCountModifier / abs(g.effect.value)).coerceAtMost(3f).coerceAtLeast(1f)
                                    }
                                }
                                EffectType.GAPS -> {
                                    when (g.effect.operation) {
                                        Operation.ADD, Operation.SUB -> gapCountModifier += g.effect.value
                                        Operation.MULTIPLY -> gapCountModifier = (gapCountModifier * g.effect.value).coerceAtMost(3f).coerceAtLeast(1f)
                                        Operation.DIVIDE -> gapCountModifier = (gapCountModifier / abs(g.effect.value)).coerceAtMost(3f).coerceAtLeast(1f)
                                    }
                                }
                                EffectType.TIME -> {
                                    localTimer += g.effect.value.toInt()
                                    if (localTimer < 0) localTimer = 0
                                    onTimeChange(localTimer)
                                }
                                EffectType.POINTS -> {
                                    when (g.effect.operation) {
                                        Operation.ADD, Operation.SUB -> pendingPointModifier += g.effect.value
                                        // NOWA LOGIKA: Mnożenie/Dzielenie całego wyniku (localHighScore)
                                        Operation.MULTIPLY -> localHighScore = (localHighScore * g.effect.value).toInt().coerceAtMost(999999) // Limit max score
                                        Operation.DIVIDE -> localHighScore = (localHighScore / abs(g.effect.value)).toInt().coerceAtLeast(0) // Minimum 0
                                    }
                                }
                            }
                        }

                        while (isTriggered.size <= index) isTriggered.add(false)
                        isTriggered[index] = true
                        ringCount++

                        val timeElapsedSeconds = (currentTime - timeSinceLastRing) / 1000f
                        val speedBonus = maxOf(0, (50 - (timeElapsedSeconds * 5).toInt())) // 50 pkt za 0 sek, -5 za sek
                        localHighScore += speedBonus
                        timeSinceLastRing = currentTime

                        if (ringCount == maxRings) {
                            onWin(localHighScore)
                        }

                        val newOuter = 500f + 250f * ringCount
                        val newInner = 450f + 250f * ringCount
                        val baseGaps = (floor(((PI.toFloat() * newInner) / gapSize) / 6)).toInt() + 2
                        val modifiedGaps = (baseGaps + gapCountModifier).toInt().coerceAtLeast(1)

                        val newRing = RingObstacle(
                            center = Offset(600f, 800f),
                            outerRadius = newOuter,
                            innerRadius = newInner,
                            color = obstacleColor,
                            wallsGenerated = false,
                            ringCount = ringCount
                        ).apply {
                            totalExits = modifiedGaps
                        }

                        localHighScore += pendingPointModifier.toInt()
                        pendingPointModifier = 0f
                        ringToAdd = newRing

                        onScoreChange(localHighScore)
                    }

                    while (prevStates.size <= index) prevStates.add(false to false)
                    prevStates[index] = currentState
                }
            } else {
                for (i in rings.indices) {
                    while (prevStates.size <= i) prevStates.add(false to false)
                    prevStates[i] = currentStates.getOrNull(i) ?: (false to false)
                }
            }

            ringToAdd?.let {
                if (rings.size < maxRings) {
                    rings.add(it)
                    prevStates.add(false to false)
                } else {
                    localHighScore += remainingTime * 5
                    onWin(localHighScore)
                }
            }
            lastUpdateTime = currentTime // Dodanie aktualizacji czasu
            delay(16L)
        }
    }

    LaunchedEffect(rings.size) {
        if (rings.size > 1) {
            val baseWalls = 6 + (4 * (rings.size - 2))
            val modifiedWalls = (baseWalls + wallCountModifier).toInt().coerceAtLeast(1) // Użyj globalnego stanu

            val newWalls = generateWallsBetweenRings(rings, modifiedWalls, obstacleColor)
            walls.addAll(newWalls)
        }
    }

    Canvas(modifier = Modifier.fillMaxSize()) {
        val canvasCenter = Offset(size.width / 2f, size.height / 2f)
        val cameraOffset = canvasCenter - Offset(ballX, ballY)
        drawContext.canvas.save()
        drawContext.canvas.translate(cameraOffset.x, cameraOffset.y)

        drawCircle(color = ballColor, radius = ballRadius, center = Offset(ballX, ballY))
        rings.forEach { drawRingWithGaps(it) }
        drawWalls(walls)

        drawContext.canvas.restore()
    }
}