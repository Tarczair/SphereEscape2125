package com.example.sphereescape2125.screens

// --- IMPORTY SYSTEMOWE ---
import android.app.Activity
import android.view.WindowManager
import android.content.Context
import android.widget.Toast

// --- IMPORTY COMPOSE ---
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em

// --- IMPORTY TWOJEGO PROJEKTU ---
import com.example.sphereescape2125.MainViewModel
import com.example.sphereescape2125.screens.obstacle.*
import com.example.sphereescape2125.sensors.ShakeDetector
import com.example.sphereescape2125.sensors.TiltSensor

// --- IMPORTY MATEMATYCZNE I KORUTYNY ---
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlin.math.*

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
        Text("KONIEC GRY", style = MaterialTheme.typography.headlineLarge, color = MaterialTheme.colorScheme.error)
        Text("Skończył się czas / Wpadłeś w Czarną Dziurę!", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.error)
        Spacer(Modifier.height(24.dp))
        Button(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
            Text("WRÓĆ DO MENU")
        }
    }
}

@Composable
fun AndroidKeepScreenOn() {
    val context = LocalContext.current
    DisposableEffect(Unit) {
        val window = (context as Activity).window
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        onDispose { window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON) }
    }
}

@Composable
fun GameScreen(viewModel: MainViewModel, onBack: () -> Unit) {
    AndroidKeepScreenOn()
    val context = LocalContext.current

    // ================= SHAKE EVENT =================
    var shakeEvent by remember { mutableIntStateOf(0) }

    val shakeOffsetX = remember { Animatable(0f) }
    val shakeOffsetY = remember { Animatable(0f) }

    val shakeDetector = remember {
        ShakeDetector(context) {
            shakeEvent++
        }
    }

    DisposableEffect(Unit) {
        shakeDetector.start()
        onDispose { shakeDetector.stop() }
    }

    LaunchedEffect(shakeEvent) {

        if (shakeEvent > 0) {
            repeat(2) {
                shakeOffsetX.animateTo(15f, tween(50))
                shakeOffsetX.animateTo(-15f, tween(50))
                shakeOffsetY.animateTo(15f, tween(50))
                shakeOffsetY.animateTo(-15f, tween(50))
            }
            shakeOffsetX.animateTo(0f)
            shakeOffsetY.animateTo(0f)
        }
    }

    // ================= SCORE / STATE =================
    val prefs = remember { context.getSharedPreferences("SphereEscapePrefs", Context.MODE_PRIVATE) }
    var bestScore by remember { mutableIntStateOf(prefs.getInt("HighScore", 100)) }

    var currentScore by remember { mutableIntStateOf(0) }
    var hasWon by remember { mutableStateOf(false) }
    var hasLost by remember { mutableStateOf(false) }
    var timeLeft by remember { mutableIntStateOf(60) }

    fun win(score: Int) {
        if (score > bestScore) {
            bestScore = score
            prefs.edit().putInt("HighScore", score).apply()
        }
        hasWon = true
    }

    fun lose() {
        if (currentScore > bestScore) {
            bestScore = currentScore
            prefs.edit().putInt("HighScore", currentScore).apply()
        }
        hasLost = true
    }

    Box(
        Modifier
            .fillMaxSize()
            .graphicsLayer {
                translationX = shakeOffsetX.value
                translationY = shakeOffsetY.value
            }
            .background(MaterialTheme.colorScheme.background)
    ) {
        GameCanvas(
            shakeEvent = shakeEvent,
            hasWon = hasWon,
            hasLost = hasLost,
            remainingTime = timeLeft,
            onTimeChange = { timeLeft = it },
            onScoreChange = { currentScore = it },
            onWin = { win(it) },
            onLost = { lose() }
        )

        if (!hasWon && !hasLost) GameHUD(timeLeft, currentScore, onBack)
        if (hasLost) GameOverScreen(onBack)
        if (hasWon) VictoryScreen(currentScore, bestScore, onBack)
    }
}

@Composable
fun GameHUD(timeLeft: Int, currentScore: Int, onBack: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(top = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("POZIOM I", style = MaterialTheme.typography.headlineMedium.copy(fontSize = 6.em))
            Text("WYNIK: $currentScore", style = MaterialTheme.typography.headlineMedium.copy(fontSize = 6.em), modifier = Modifier.padding(top = 8.dp))

            val timeColor = if(timeLeft < 10) Color.Red else MaterialTheme.colorScheme.onBackground
            Text(
                "CZAS: ${String.format("%02d:%02d", timeLeft / 60, timeLeft % 60)}",
                style = MaterialTheme.typography.headlineMedium.copy(fontSize = 6.em),
                color = timeColor,
                modifier = Modifier.padding(top = 8.dp, bottom = 16.dp)
            )
        }
        Button(onClick = onBack, modifier = Modifier.fillMaxWidth()) { Text("WRÓĆ") }
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
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(20.dp)) {
            Text("ZWYCIĘSTWO!", style = MaterialTheme.typography.headlineLarge, color = MaterialTheme.colorScheme.primary)
            Text("Zdobyte punkty: $points", style = MaterialTheme.typography.headlineMedium)
            Text("Najlepszy wynik: $bestScore", style = MaterialTheme.typography.headlineMedium)
            Button(onClick = onBack, modifier = Modifier.fillMaxWidth().padding(horizontal = 40.dp)) { Text("WRÓĆ") }
        }
    }
}

fun calculateWallCount(
    ringIndex: Int,
    ring: RingObstacle,
    wallCountModifier: Int,
    wallMultiplier: Float
): Int {
    val radius = (ring.innerRadius + ring.outerRadius) / 2f
    val baseWalls = (radius / 120f).roundToInt() + (10 * ringIndex)

    return ((baseWalls + wallCountModifier) * wallMultiplier)
        .roundToInt()
        .coerceAtLeast(1)
}


// ————————————————————————————————————————————————
//                         GAME CANVAS
// ————————————————————————————————————————————————

@Composable
fun GameCanvas(
    shakeEvent: Int,
    hasWon: Boolean,
    hasLost: Boolean,
    remainingTime: Int,
    onTimeChange: (Int) -> Unit,
    onScoreChange: (Int) -> Unit,
    onWin: (Int) -> Unit,
    onLost: () -> Unit
) {
    val maxRings = 15
    var stopLoop = false

    val rings = remember { mutableStateListOf<RingObstacle>() }
    val walls = remember { mutableStateListOf<WallObstacle>() }
    val prevStates = remember { mutableStateListOf<Pair<Boolean, Boolean>>() }
    val isTriggered = remember { mutableStateListOf<Boolean>().apply { repeat(50) { add(false) } } }

    val obstacleColor = MaterialTheme.colorScheme.error
    val ballColor = MaterialTheme.colorScheme.secondary

    var ballX by remember { mutableFloatStateOf(600f) }
    var ballY by remember { mutableFloatStateOf(800f) }
    val ballRadius = 40f
    val center = Offset(600f, 800f)

    var velocityX by remember { mutableFloatStateOf(0f) }
    var velocityY by remember { mutableFloatStateOf(0f) }

    val accelerationFactor = 0.1f
    val friction = 0.92f
    val maxSpeed = 10f
    val CALIBRATION_OFFSET_Y = 4f

    var ringCount by remember { mutableIntStateOf(0) }
    var localTimer by remember { mutableIntStateOf(remainingTime) }
    var localHighScore by remember { mutableIntStateOf(0) }
    var timeSinceLastRing by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var pendingPointModifier by remember { mutableFloatStateOf(0f) }

    // ————— CZARNA DZIURA ————— //
    val BLACK_HOLE_CENTER = Offset(600f, 800f)
    val INITIAL_RADIUS = 40f
    val GROWTH_RATE = 15f
    val START_DELAY = 5f

    var bhRadius by remember { mutableFloatStateOf(INITIAL_RADIUS) }
    var bhDelay by remember { mutableFloatStateOf(START_DELAY) }
    var bhPause by remember { mutableFloatStateOf(0f) }

    val context = LocalContext.current
    val tiltSensor = remember { TiltSensor(context) }

    var wallCountModifier by remember { mutableIntStateOf(0) }
    var gapCountModifier by remember { mutableIntStateOf(0) }

    var wallMultiplier by remember { mutableFloatStateOf(1f) }
    var gapMultiplier by remember { mutableFloatStateOf(1f) }

    // Inicjalizacja początkowych pierścieni
    LaunchedEffect(Unit) {
        if (rings.isEmpty()) {
            rings.add(RingObstacle(BLACK_HOLE_CENTER, 250f, 200f, obstacleColor, ringCount = 0))
            prevStates.add(false to false)
            rings.add(RingObstacle(BLACK_HOLE_CENTER, 500f, 450f, obstacleColor, ringCount = 1))
            prevStates.add(false to false)
        }
    }

    // ================= SHAKE HANDLER =================
    var lastHandledShake by remember { mutableIntStateOf(0) }

    LaunchedEffect(shakeEvent) {
        if (shakeEvent <= lastHandledShake) return@LaunchedEffect
        lastHandledShake = shakeEvent

        // ⏱️ KARA ZA WSTRZĄS
        localTimer = (localTimer - 8).coerceAtLeast(0)
        withContext(Dispatchers.Main) {
            onTimeChange(localTimer)
        }

        if (localTimer <= 0) {
            withContext(Dispatchers.Main) {
                onLost()
            }
            return@LaunchedEffect
        }


        val dist = hypot(ballX - center.x, ballY - center.y)
        var ringIndex = -1
        for (i in 0 until rings.size - 1) {
            val epsilon  = ballRadius * 0.5f

            if (dist >= rings[i].outerRadius - epsilon && dist <= rings[i + 1].innerRadius + epsilon) {
                ringIndex = i
                break
            }
        }
        if (ringIndex == -1) return@LaunchedEffect

        val angle = Math.toDegrees(
            atan2(
                (ballY - center.y).toDouble(),
                (ballX - center.x).toDouble()
            )
        ).toFloat().let { if (it < 0) it + 360f else it }

        val wallCount = calculateWallCount(
            ringIndex = ringIndex,
            ring = rings[ringIndex],
            wallCountModifier = wallCountModifier,
            wallMultiplier = wallMultiplier
        )

        regenerateWallsForSpecificRing(
            ringIndex = ringIndex,
            rings = rings,
            walls = walls,
            wallsPerGap = wallCount,
            color = rings[ringIndex].color,
            playerAngle = angle
        )
    }

    // TIMER
    LaunchedEffect(Unit) {
        while (!hasWon && !hasLost) {
            delay(1000)
            localTimer--
            withContext(Dispatchers.Main) { onTimeChange(localTimer) }

            if (localTimer <= 0) {
                withContext(Dispatchers.Main) { onLost() }
                stopLoop = true // <-- zatrzymuje główny game loop
                break
            }
        }

    }

    DisposableEffect(Unit) {
        tiltSensor.startListening()
        onDispose { tiltSensor.stopListening() }
    }
    val gravityData by tiltSensor.gravityData.collectAsState()

    // ———————— GAME LOOP ———————— //
    LaunchedEffect(Unit) {
        var lastTime = System.currentTimeMillis()
        val physicsSteps = 4
        if(lastTime <= 0f) {
            onLost()
        }

        loop@ while (!stopLoop) {
            val now = System.currentTimeMillis()
            var dt = (now - lastTime) / 1000f
            lastTime = now
            if (dt > 0.1f) dt = 0.1f

            if (hasWon || hasLost) break@loop

            val subDt = dt / physicsSteps

            repeat(physicsSteps) { _ ->
                val gravity = gravityData

                // ————— CZARNA DZIURA ————— //
                if (bhDelay > 0f) {
                    bhDelay -= subDt
                } else if (bhPause > 0f) {
                    bhPause -= subDt
                } else {
                    val distToPlayer = hypot(ballX - BLACK_HOLE_CENTER.x, ballY - BLACK_HOLE_CENTER.y)
                    val gapBetweenRings = 250f
                    val thresholdDist = bhRadius + (5 * gapBetweenRings)

                    var currentGrowthRate = GROWTH_RATE
                    if (distToPlayer > thresholdDist) {
                        val extraDistance = distToPlayer - thresholdDist
                        val speedBoost = extraDistance * 0.08f
                        currentGrowthRate += speedBoost
                    }
                    bhRadius += currentGrowthRate * subDt
                }

                // Sprawdzenie przegranej przez dziurę
                val distBH = hypot(ballX - BLACK_HOLE_CENTER.x, ballY - BLACK_HOLE_CENTER.y)
                if (bhDelay <= 0f && distBH <= bhRadius + ballRadius) {
                    withContext(Dispatchers.Main) {
                        if (!hasLost) onLost()
                    }
                    stopLoop = true
                    return@repeat
                }

                // ————— RUCH ————— //
                val ax = -gravity.x * accelerationFactor
                val ay = (gravity.y - CALIBRATION_OFFSET_Y) * accelerationFactor
                velocityX = (velocityX + ax) * friction
                velocityY = (velocityY + ay) * friction

                val speed = hypot(velocityX, velocityY)
                if (speed > maxSpeed) {
                    velocityX = (velocityX / speed) * maxSpeed
                    velocityY = (velocityY / speed) * maxSpeed
                }

                ballX += velocityX * subDt * 60
                ballY += velocityY * subDt * 60

                // ————— KOLIZJE ————— //
                if (rings.isNotEmpty()) {
                    val curFirst = BooleanArray(rings.size)
                    val curSecond = BooleanArray(rings.size)

                    for (i in rings.indices) {
                        val coll = isCircleCollidingWithRing(Offset(ballX, ballY), ballRadius, rings[i])
                        curFirst[i] = coll.first
                        curSecond[i] = coll.second
                    }

                    var hitGap = false
                    // Gap walls
                    for (i in rings.indices) {
                        val gapWalls = rings[i].generateGapWalls()
                        for (gw in gapWalls) {
                            val res = getLineSegmentCollision(Offset(ballX, ballY), ballRadius, gw.start, gw.end, gw.normal)
                            if (res != null) {
                                hitGap = true
                                val (_, d, n) = res
                                val pen = ballRadius - d
                                if (pen > 0f) {
                                    ballX += n.x * (pen + 0.1f); ballY += n.y * (pen + 0.1f)
                                }
                                val dot = velocityX * n.x + velocityY * n.y
                                if (dot < 0f) { velocityX -= dot * n.x; velocityY -= dot * n.y }
                            }
                        }
                    }

                    // Zwykłe ściany (Walls)
                    for (w in walls) {
                        val info = getWallCollisionInfo(Offset(ballX, ballY), ballRadius, w)
                        if (info != null) {
                            val (_, d, n) = info
                            val pen = (ballRadius + 25f) - d
                            if (pen > 0f) {
                                ballX += n.x * (pen + 0.5f); ballY += n.y * (pen + 0.5f)
                            }
                            val dot = velocityX * n.x + velocityY * n.y
                            if (dot < 0f) { velocityX -= dot * n.x; velocityY -= dot * n.y }
                        }
                    }

                    var ringToAdd: RingObstacle? = null

                    if (!hitGap) {
                        for (i in rings.indices) {
                            val ring = rings[i]
                            val cur = curFirst[i] to curSecond[i]
                            val prev = if (i < prevStates.size) prevStates[i] else (false to false)

                            // 1. Logika fizyki (Kolizja z pierścieniem) - bez zmian, działa poprawnie
                            if (cur.first && !cur.second) {
                                val dx = ballX - ring.center.x
                                val dy = ballY - ring.center.y
                                val dist = hypot(dx, dy)
                                if (dist > 0f) {
                                    val innerD = abs(dist - ring.innerRadius)
                                    val outerD = abs(dist - ring.outerRadius)
                                    var nx = dx / dist
                                    var ny = dy / dist
                                    val pen = if (innerD < outerD) (ring.innerRadius - ballRadius) - dist else dist - (ring.outerRadius + ballRadius)
                                    if (innerD < outerD) { nx = -nx; ny = -ny }
                                    val push = abs(pen) + 0.5f
                                    ballX += nx * push
                                    ballY += ny * push
                                    val dot = velocityX * nx + velocityY * ny
                                    if (dot < 0f) { velocityX -= dot * nx; velocityY -= dot * ny }
                                }
                            }

                            // 2. Logika przejścia (SCORE I BUFFY)
                            // Zmieniony warunek: prev.second (był w obrębie pierścienia) -> !cur.second (wyleciał poza)
                            if (prev.second && !cur.second && !(isTriggered.getOrNull(i) ?: false)) {
                                val angle = Math.toDegrees(atan2((ballY - ring.center.y).toDouble(), (ballX - ring.center.x).toDouble()))
                                    .let { if (it < 0) it + 360 else it }.toFloat()

                                val effect = ring.gapEffects.minByOrNull { g -> abs(g.midAngle - angle) }

                                var pointEffectApplied = false

                                effect?.let { g ->
                                    when (g.effect.type) {
                                        EffectType.POINTS -> {
                                            // OBLICZANIE PUNKTÓW: Operujemy na Double dla precyzji, potem zaokrąglamy
                                            val currentBase = localHighScore.toDouble()
                                            val newScore = when (g.effect.operation) {
                                                Operation.MULTIPLY -> currentBase * g.effect.value
                                                Operation.DIVIDE ->
                                                    if (g.effect.value != 0f) currentBase / g.effect.value else currentBase
                                                Operation.ADD -> currentBase + g.effect.value
                                                Operation.SUB -> currentBase + g.effect.value
                                            }
                                            localHighScore = newScore.toInt().coerceAtLeast(0)
                                            pointEffectApplied = true // Flaga: użyliśmy efektu z luki
                                        }
                                        EffectType.TIME -> {
                                            localTimer = (localTimer + g.effect.value.toInt()).coerceAtLeast(0)
                                            if (g.effect.value > 0) bhPause += g.effect.value
                                        }
                                        EffectType.WALLS -> {
                                            when (g.effect.operation) {
                                                Operation.ADD, Operation.SUB -> {
                                                    wallCountModifier =
                                                        (wallCountModifier + g.effect.value.toInt())
                                                }
                                                Operation.MULTIPLY, Operation.DIVIDE -> {
                                                    wallMultiplier *= g.effect.value
                                                    wallMultiplier = wallMultiplier.coerceIn(0.25f, 3f)
                                                }
                                            }
                                        }

                                        EffectType.GAPS -> {
                                            when (g.effect.operation) {
                                                Operation.ADD, Operation.SUB -> {
                                                    gapCountModifier =
                                                        (gapCountModifier + g.effect.value.toInt())
                                                }
                                                Operation.MULTIPLY, Operation.DIVIDE -> {
                                                    gapMultiplier *= g.effect.value
                                                    gapMultiplier = gapMultiplier.coerceIn(0.25f, 3f)
                                                }
                                            }
                                        }
                                    }
                                }

                                // DODAWANIE PUNKTÓW ZA CZAS (Tylko jeśli nie było efektu punktowego w luce)
                                if (!pointEffectApplied) {
                                    val timeBonus = maxOf(0, 50 - ((System.currentTimeMillis() - timeSinceLastRing) / 1000f * 5).toInt())
                                    localHighScore += timeBonus
                                }

                                // Rejestrujemy przejście pierścienia
                                if (i < isTriggered.size) isTriggered[i] = true
                                ringCount++
                                timeSinceLastRing = System.currentTimeMillis()

                                // Dodawanie nowego pierścienia (logika zasięgu)
                                if (rings.size < maxRings) {
                                    val lastRing = rings.last()
                                    val newInner = lastRing.outerRadius + 200f
                                    val newOuter = newInner + 50f
                                    val baseGaps = (floor(((PI.toFloat() * newInner) / gapSizeCollision) / 8)).toInt() + 1
                                    val modGaps =     ((baseGaps + gapCountModifier) * gapMultiplier)
                                        .roundToInt()
                                        .coerceAtLeast(1)

                                    ringToAdd = RingObstacle(BLACK_HOLE_CENTER, newOuter, newInner, obstacleColor, false, ringCount).apply {
                                        totalExits = modGaps
                                    }
                                }

                                // Synchronizacja UI
                                withContext(Dispatchers.Main) {
                                    onScoreChange(localHighScore)
                                    onTimeChange(localTimer)
                                }
                            }

                            if (i < prevStates.size) prevStates[i] = cur
                            else prevStates.add(cur)
                        }
                    }

                    ringToAdd?.let {
                        withContext(Dispatchers.Main) {
                            rings.add(it)
                            prevStates.add(false to false)
                            isTriggered.add(false)
                        }
                    }
                }
            }

            if (ringCount >= maxRings && !hasWon) {
                localHighScore += localTimer * 5

                withContext(Dispatchers.Main) {
                    onScoreChange(localHighScore)
                    onWin(localHighScore)
                }

                stopLoop = true
            }

            if (stopLoop) break@loop
            delay(16)
        }
    }

    // GENERATOR ŚCIAN
    LaunchedEffect(rings.size) {
        if (rings.size > 1) {
            val i = rings.size - 2 // indeks "bieżącego" pierścienia (ostatni dodany to size-1)
            // Używamy tego samego algorytmu co przy wstrząsie, ale losowo
            // Tu po prostu dodajemy nowe ściany dla NOWEGO poziomu
            val current = rings[i]
            val next = rings[i+1]

            // Sprawdźmy czy już są ściany między tymi pierścieniami (żeby nie dublować)
            val hasWalls = walls.any { it.startRing == current }

            if (!hasWalls) {
                val radius = (current.innerRadius + current.outerRadius) / 2f

                val baseWalls =
                    (radius / 120f).roundToInt() + (10 * i)  // gęstość ~1 ściana / 120px

                val modWalls = calculateWallCount(
                    ringIndex = i,
                    ring = current,
                    wallCountModifier = wallCountModifier,
                    wallMultiplier = wallMultiplier
                )

                // Używamy nowej bezpiecznej funkcji
                val newWalls = generateWallsBetweenRings(
                    listOf(current, next), // Przekazujemy tylko parę
                    modWalls,
                    obstacleColor
                )
                walls.addAll(newWalls)
            }
        }
    }

    Canvas(modifier = Modifier.fillMaxSize()) {
        val center = Offset(size.width / 2f, size.height / 2f)
        val cam = center - Offset(ballX, ballY)

        drawContext.canvas.save()
        drawContext.canvas.translate(cam.x, cam.y)

        if (bhDelay > 0) {
            drawCircle(Color.Gray.copy(alpha = 0.3f), INITIAL_RADIUS + 10f, BLACK_HOLE_CENTER)
        }

        rings.forEach { drawRingWithGaps(it) }
        drawWalls(walls)

        drawCircle(Color.Black, bhRadius, BLACK_HOLE_CENTER, alpha = 0.9f)
        drawCircle(ballColor, ballRadius, Offset(ballX, ballY))

        drawContext.canvas.restore()
    }
}