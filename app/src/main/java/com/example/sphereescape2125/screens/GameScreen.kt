package com.example.sphereescape2125.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import com.example.sphereescape2125.screens.obstacle.*
import android.app.Activity
import android.view.WindowManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.hypot
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.Color
import com.example.sphereescape2125.screens.obstacle.EffectType
import com.example.sphereescape2125.sensors.TiltSensor
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.floor
import kotlin.math.roundToInt
import kotlin.math.min

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
    var hasLost by remember { mutableStateOf(false) }
    var timeLeft by remember { mutableStateOf(60) }

    LaunchedEffect(Unit) {
        if (bestScore == 0) bestScore = 200
    }

    Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        GameCanvas(
            hasWon = hasWon,
            hasLost = hasLost,
            remainingTime = timeLeft,
            onTimeChange = { update ->
                timeLeft = update.coerceAtLeast(0)
                if (timeLeft == 0 && !hasWon) hasLost = true
            },
            onScoreChange = { currentScore = it },
            onWin = { finalScore ->
                currentScore = finalScore
                bestScore = maxOf(bestScore, finalScore)
                hasWon = true
            },
            onLost = { hasLost = true }
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
            Text(
                "POZOSTAŁY CZAS: ${String.format("%02d:%02d", timeLeft / 60, timeLeft % 60)}",
                style = MaterialTheme.typography.headlineMedium.copy(fontSize = 6.em),
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

// ————————————————————————————————————————————————
//                         GAME CANVAS
// ————————————————————————————————————————————————

@Composable
fun GameCanvas(
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

    // Inicjalizujemy listy jako puste, ale wypełnimy je w LaunchedEffect
    val rings = remember { mutableStateListOf<RingObstacle>() }
    val walls = remember { mutableStateListOf<WallObstacle>() }
    val prevStates = remember { mutableStateListOf<Pair<Boolean, Boolean>>() }
    val isTriggered = remember { mutableStateListOf<Boolean>().apply { repeat(50) { add(false) } } }

    val obstacleColor = MaterialTheme.colorScheme.error
    val ballColor = MaterialTheme.colorScheme.secondary

    // STARTING POSITION
    var ballX by remember { mutableFloatStateOf(600f) }
    var ballY by remember { mutableFloatStateOf(800f) }
    val ballRadius = 40f

    var velocityX by remember { mutableFloatStateOf(0f) }
    var velocityY by remember { mutableFloatStateOf(0f) }

    val accelerationFactor = 0.5f
    val friction = 0.96f
    val maxSpeed = 15f
    val CALIBRATION_OFFSET_Y = 4f

    var ringCount by remember { mutableIntStateOf(0) }
    var localTimer by remember { mutableIntStateOf(remainingTime) }
    var localHighScore by remember { mutableIntStateOf(0) }
    var timeSinceLastRing by remember { mutableLongStateOf(System.currentTimeMillis()) }

    var wallCountModifier by remember { mutableIntStateOf(0) }
    var gapCountModifier by remember { mutableIntStateOf(0) }
    var pendingPointModifier by remember { mutableFloatStateOf(0f) }

    // ————— CZARNA DZIURA ————— //
    val BLACK_HOLE_CENTER = Offset(600f, 800f)
    val INITIAL_RADIUS = 40f
    val GROWTH_RATE = 15f // Zmniejszyłem trochę tempo wzrostu dla lepszego balansu
    val START_DELAY = 5f

    var bhRadius by remember { mutableFloatStateOf(INITIAL_RADIUS) }
    var bhDelay by remember { mutableFloatStateOf(START_DELAY) }
    var bhPause by remember { mutableFloatStateOf(0f) }

    val context = LocalContext.current
    // Pamiętaj, aby klasa TiltSensor istniała w odpowiednim pakiecie
    val tiltSensor = remember { TiltSensor(context) }

    //Inicjalizacja początkowych pierścieni
    LaunchedEffect(Unit) {
        if (rings.isEmpty()) {
            rings.add(RingObstacle(Offset(600f, 800f), 250f, 200f, obstacleColor, ringCount = 0))
            prevStates.add(false to false)
            rings.add(RingObstacle(Offset(600f, 800f), 500f, 450f, obstacleColor, ringCount = 1))
            prevStates.add(false to false)
        }
    }

    // TIMER
    LaunchedEffect(Unit) {
        var lastUpdateTime = System.currentTimeMillis() // Dodanie śledzenia czasu
        while (true) {
            // Timer działa tylko jeśli gra trwa
            if (!hasWon && !hasLost && localTimer > 0) {
                delay(1000)
                localTimer--
                withContext(Dispatchers.Main) { onTimeChange(localTimer) }
            }
        }
    }

    DisposableEffect(Unit) {
        tiltSensor.startListening()
        onDispose { tiltSensor.stopListening() }
    }
    val gravityData by tiltSensor.gravityData.collectAsState()

    // ———————— GAME LOOP (OPTYMALIZOWANY) ———————— //
    LaunchedEffect(Unit) {
        var lastTime = System.currentTimeMillis()

        loop@ while (!stopLoop) {
            val now = System.currentTimeMillis()
            val dt = (now - lastTime) / 1000f
            lastTime = now

            // Jeżeli gra zakończona, przerywamy pętlę - to pozwala UI natychmiast się przebudować
            if (hasWon || hasLost) break@loop

            // Lokalna kopia gravityData (bez odwołań z Compose) — bezpiecznie do pracy w tle
            val gravity = gravityData

            // ————— CZARNA DZIURA LOGIC ————— //
            if (bhDelay > 0f) {
                bhDelay -= dt
            } else if (bhPause > 0f) {
                bhPause -= dt
            } else {
                bhRadius += GROWTH_RATE * dt
            }

            val distBH = hypot(ballX - BLACK_HOLE_CENTER.x, ballY - BLACK_HOLE_CENTER.y)

            // Jeżeli kolizja z czarną dziurą -> zgłoś przegraną i przerwij pętlę
            if (bhDelay <= 0f && distBH <= bhRadius + ballRadius) {
                withContext(Dispatchers.Main) { onLost() }
                stopLoop = true
            }

            // Wykonaj fizykę i detekcję kolizji poza wątkiem UI
            withContext(Dispatchers.Default) {
                // ————— FIZYKA KULKI ————— //
                val ax = -gravity.x * accelerationFactor
                val ay = (gravity.y - CALIBRATION_OFFSET_Y) * accelerationFactor

                velocityX = (velocityX + ax) * friction
                velocityY = (velocityY + ay) * friction

                val speed = hypot(velocityX, velocityY)
                if (speed > maxSpeed) {
                    velocityX = (velocityX / speed) * maxSpeed
                    velocityY = (velocityY / speed) * maxSpeed
                }
            }

                ballX += velocityX * dt * 60
                ballY += velocityY * dt * 60

                // ————— KOLIZJE ————— //
                if (rings.isNotEmpty()) {
                    // Reuse arrays to avoid alocations co klatke
                    val curFirst = BooleanArray(rings.size)
                    val curSecond = BooleanArray(rings.size)

                    for (i in rings.indices) {
                        val coll = isCircleCollidingWithRing(Offset(ballX, ballY), ballRadius, rings[i])
                        curFirst[i] = coll.first
                        curSecond[i] = coll.second
                    }
                }
            }

                    var hitGap = false

                    // Sprawdzanie kolizji z gap walls
                    for (i in rings.indices) {
                        val ring = rings[i]
                        // generateGapWalls może tworzyć nową listę — staraj się nie robić tego zbyt często
                        val gapWalls = ring.generateGapWalls()
                        if (gapWalls.isNotEmpty()) {
                            for (gw in gapWalls) {
                                val res = getLineSegmentCollision(Offset(ballX, ballY), ballRadius, gw.start, gw.end, gw.normal)
                                if (res != null) {
                                    hitGap = true
                                    val (_, d, n) = res
                                    val pen = ballRadius - d
                                    if (pen > 0f) {
                                        ballX += n.x * (pen + 0.1f)
                                        ballY += n.y * (pen + 0.1f)
                                    }
                                    val dot = velocityX * n.x + velocityY * n.y
                                    if (dot < 0f) {
                                        velocityX -= dot * n.x
                                        velocityY -= dot * n.y
                                    }
                                }
                            }
                        }
                    }

                    // Ściany
                    for (w in walls) {
                        val info = getWallCollisionInfo(Offset(ballX, ballY), ballRadius, w)
                        if (info != null) {
                            val (_, d, n) = info
                            val pen = (ballRadius + 25f) - d
                            if (pen > 0f) {
                                ballX += n.x * (pen + 0.5f)
                                ballY += n.y * (pen + 0.5f)
                            }
                            val dot = velocityX * n.x + velocityY * n.y
                            if (dot < 0f) {
                                velocityX -= dot * n.x
                                velocityY -= dot * n.y
                            }
                        }
                    }

                    var ringToAdd: RingObstacle? = null

                    if (!hitGap) {
                        for (i in rings.indices) {
                            val ring = rings[i]
                            val cur = curFirst[i] to curSecond[i]
                            val prev = if (i < prevStates.size) prevStates[i] else (false to false)

                            if (cur.first && !cur.second) {
                                val dx = ballX - ring.center.x
                                val dy = ballY - ring.center.y
                                val dist = hypot(dx, dy)
                                if (dist > 0f) {
                                    val innerD = abs(dist - ring.innerRadius)
                                    val outerD = abs(dist - ring.outerRadius)
                                    var nx = dx / dist
                                    var ny = dy / dist
                                    var pen = 0f

                                    if (innerD < outerD) {
                                        pen = (ring.innerRadius - ballRadius) - dist
                                        nx = -nx
                                        ny = -ny
                                    } else pen = dist - (ring.outerRadius + ballRadius)

                                    val push = abs(pen) + 0.5f
                                    ballX += nx * push
                                    ballY += ny * push

                                    val dot = velocityX * nx + velocityY * ny
                                    if (dot < 0f) {
                                        velocityX -= dot * nx
                                        velocityY -= dot * ny
                                    }
                                }
                            }

                            // GAP EFFECT (Przejście przez lukę)
                            if (prev.second && !cur.second && !(isTriggered.getOrNull(i) ?: false)) {
                                val angle = Math.toDegrees(
                                    kotlin.math.atan2((ballY - ring.center.y).toDouble(), (ballX - ring.center.x).toDouble())
                                ).let { if (it < 0) it + 360 else it }

                                val effect = ring.gapEffects.minByOrNull { g -> abs(g.midAngle - angle) }
                                effect?.let { g ->
                                    when (g.effect.type) {
                                        EffectType.TIME -> {
                                            // Callbacky wykonujemy na głównym wątku
                                            withContext(Dispatchers.Main) {
                                                localTimer += g.effect.value.toInt()
                                                if (g.effect.value > 0) bhPause += g.effect.value
                                                onTimeChange(localTimer)
                                            }
                                        }
                                        EffectType.POINTS -> {
                                            // ZMIANA: Przetwarzanie mnożenia/dzielenia dla punktów
                                            when (g.effect.operation) {
                                                Operation.MULTIPLY -> localHighScore = (localHighScore * g.effect.value.toInt()).coerceAtLeast(0)
                                                Operation.DIVIDE -> localHighScore = (localHighScore / g.effect.value.toInt()).coerceAtLeast(1) // Nigdy nie dzielimy przez 0 i nigdy mniej niż 1
                                                // Dla ADD/SUB (które mają ujemne/dodatnie value) używamy pendingPointModifier
                                                else -> pendingPointModifier += g.effect.value
                                            }
                                        }
                                        EffectType.WALLS -> {
                                            when (g.effect.operation) {
                                                Operation.MULTIPLY -> wallCountModifier = (wallCountModifier * g.effect.value).toInt().coerceAtLeast(0)
                                                // POPRAWKA: Dzielenie z zaokrąglaniem
                                                Operation.DIVIDE -> wallCountModifier = (wallCountModifier.toFloat() / g.effect.value).roundToInt().coerceAtLeast(1)
                                                else -> wallCountModifier += g.effect.value.toInt()
                                            }
                                        }
                                        EffectType.GAPS -> {
                                            when (g.effect.operation) {
                                                Operation.MULTIPLY -> gapCountModifier = (gapCountModifier * g.effect.value).toInt().coerceAtLeast(0)
                                                // POPRAWKA: Dzielenie z zaokrąglaniem
                                                Operation.DIVIDE -> gapCountModifier = (gapCountModifier.toFloat() / g.effect.value).roundToInt().coerceAtLeast(1)
                                                else -> gapCountModifier += g.effect.value.toInt()
                                            }
                                        }
                                    }
                                }

                                if (i < isTriggered.size) isTriggered[i] = true
                                ringCount++ // Zwiększamy licznik ukończonych pierścieni

                                val t = (now - timeSinceLastRing) / 1000f
                                localHighScore += maxOf(0, 50 - (t * 5).toInt())
                                timeSinceLastRing = now

                                // Generowanie KOLEJNEGO pierścienia
                                if (rings.size < maxRings) {
                                    val lastRing = rings.last()
                                    val newInner = lastRing.outerRadius + 200f // Odstęp między pierścieniami
                                    val newOuter = newInner + 50f

                                    // ZMIANA: Mniej przerw - Zmniejszamy stałą dodawaną z 2 na 1
                                    val baseGaps = (floor(((PI.toFloat() * newInner) / gapSize) / 8)).toInt() + 1

                                    // Zaczynamy liczenie "postępu" od 2. ukończonego pierścienia.
                                    val ringsIndex = (ringCount - 2).coerceAtLeast(0)

                                    // Redukcja: 1 luka mniej co 1 ukończony pierścień (agresywnie)
                                    val reductionRate = 1

                                    // Całkowita liniowa redukcja, maksymalnie 4 luki.
                                    val difficultyReduction = (ringsIndex / reductionRate).coerceAtMost(4)

                                    // ZMIANA: Mniej przerw - Upewniamy się, że zostanie co najmniej 1 przerwa
                                    val baseGapsAdjusted = (baseGaps - difficultyReduction).coerceAtLeast(1)

                                    val modGaps = (baseGapsAdjusted + gapCountModifier).toInt().coerceAtLeast(1)

                                    ringToAdd = RingObstacle(
                                        center = BLACK_HOLE_CENTER,
                                        outerRadius = newOuter,
                                        innerRadius = newInner,
                                        color = obstacleColor,
                                        wallsGenerated = false,
                                        ringCount = ringCount
                                    ).apply { totalExits = modGaps }
                                }
                                localHighScore += pendingPointModifier.toInt()
                                pendingPointModifier = 0f

                                withContext(Dispatchers.Main) { onScoreChange(localHighScore) }
                            }

                            // Aktualizacja historii stanów (robimy to po obliczeniach)
                            if (i < prevStates.size) {
                                prevStates[i] = cur
                            } else {
                                prevStates.add(cur)
                            }
                        }
                    } else {
                        // Jeśli uderzyliśmy w ścianę luki (boki), aktualizujemy tylko stan
                        for (i in rings.indices) {
                            if (i < prevStates.size) prevStates[i] = curFirst[i] to curSecond[i]
                        }
                    }

                    // Dodajemy nowy pierścień do listy (tylko jeśli został wygenerowany)
                    ringToAdd?.let {
                        withContext(Dispatchers.Main) {
                            rings.add(it)
                            prevStates.add(false to false)
                            isTriggered.add(false)
                        }
                    }
                } // Koniec bloku "if (rings.isNotEmpty())"
            } // Koniec bloku outer withContext(Dispatchers.Default)

            // POPRAWKA #1: LOGIKA ZWYCIĘSTWA PRZENIESIONA TUTAJ
            if (ringCount >= maxRings && !hasWon) {
                localHighScore += remainingTime * 5
                withContext(Dispatchers.Main) { onWin(localHighScore) }
                stopLoop = true
            }

            if (stopLoop) break@loop

            // zminimalizuj alokacje między klatkami
            delay(16)
        } // koniec pętli loop

        // Pętla zakończona — ewentualne sprzątanie (jeśli potrzebne)
        return@LaunchedEffect
    }

    // WALL GENERATOR
    LaunchedEffect(rings.size) {
        if (rings.size > 1) {
            // POPRAWKA #2: Zwiększony współczynnik wzrostu z 8 na 10 (ściany rosną BARDZIEJ agresywnie)
            val baseWalls = 6 + (10 * (rings.size - 2))
            val modifiedWalls = (baseWalls + wallCountModifier).toInt().coerceAtLeast(1) // Użyj globalnego stanu

            val newWalls = generateWallsBetweenRings(rings, modifiedWalls, obstacleColor)
            walls.addAll(newWalls)
        }
    }

    Canvas(modifier = Modifier.fillMaxSize()) {
        val center = Offset(size.width / 2f, size.height / 2f)
        val cam = center - Offset(ballX, ballY)

        drawContext.canvas.save()
        drawContext.canvas.translate(cam.x, cam.y)

        // RYSOWANIE CZARNEJ DZIURY
        // Rysujemy "bezpieczną strefę" jeśli jest czas ochronny
        if (bhDelay > 0) {
            drawCircle(
                color = Color.Gray.copy(alpha = 0.3f),
                radius = INITIAL_RADIUS + 10f,
                center = BLACK_HOLE_CENTER
            )
        }


        // PIERŚCIENIE
        rings.forEach {
            drawRingWithGaps(it)
        }

        // ŚCIANY
        drawWalls(walls)

        drawCircle(
            color = Color.Black,
            radius = bhRadius,
            center = BLACK_HOLE_CENTER,
            alpha = 0.9f
        )

        drawCircle(
            color = ballColor,
            radius = ballRadius,
            center = Offset(ballX, ballY)
        )

        drawContext.canvas.restore()
    }
}