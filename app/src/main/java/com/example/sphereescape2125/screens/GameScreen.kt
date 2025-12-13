package com.example.sphereescape2125.screens

// --- IMPORTY SYSTEMOWE ---
import android.app.Activity
import android.view.WindowManager

// --- IMPORTY COMPOSE ---
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
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
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.floor
import kotlin.math.hypot
import kotlin.math.roundToInt

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
        onDispose { window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON) }
    }
}

@Composable
fun GameScreen(viewModel: MainViewModel, onBack: () -> Unit) {
    AndroidKeepScreenOn()

    // 1. Context potrzebny do sensora
    val context = LocalContext.current

    // 2. Definiujemy animację TUTAJ (Lokalnie, nie w ViewModelu)
    val shakeOffsetX = remember { Animatable(0f) }
    val shakeOffsetY = remember { Animatable(0f) }

    // 2. Nasłuchujemy sygnału z ViewModelu
    LaunchedEffect(Unit) {
        viewModel.shakeEvent.collect {
            // "Zmiękczona" animacja
            // Robimy tylko 2 szybkie cykle (zamiast 5), żeby nie zamulało
            for (i in 0..1) {
                // Przesunięcie tylko o 12 pikseli (było 50)
                shakeOffsetX.animateTo(12f, animationSpec = tween(40))
                shakeOffsetX.animateTo(-12f, animationSpec = tween(40))
                shakeOffsetY.animateTo(12f, animationSpec = tween(40))
                shakeOffsetY.animateTo(-12f, animationSpec = tween(40))
            }
            // Powrót do zera (szybki)
            shakeOffsetX.animateTo(0f, animationSpec = tween(40))
            shakeOffsetY.animateTo(0f, animationSpec = tween(40))
        }
    }

    // 4. Sensor
    val shakeDetector = remember {
        ShakeDetector(context) {
            // Wyświetl dymek dla pewności
            android.widget.Toast.makeText(context, "WSTRZĄS!", android.widget.Toast.LENGTH_SHORT).show()
            viewModel.onShakeDetected()
        }
    }

    DisposableEffect(Unit) {
        shakeDetector.start()
        onDispose { shakeDetector.stop() }
    }

    var bestScore by rememberSaveable { mutableStateOf(0) }
    var currentScore by remember { mutableStateOf(0) }
    var hasWon by remember { mutableStateOf(false) }
    var hasLost by remember { mutableStateOf(false) }
    var timeLeft by remember { mutableStateOf(60) }

    LaunchedEffect(Unit) {
        if (bestScore == 0) bestScore = 200
    }

    Box(
        Modifier
            .fillMaxSize()
            // ZMIANA KLUCZOWA: Usuwamy 'viewModel.', bo zmienne są teraz lokalne
            .graphicsLayer {
                translationX = shakeOffsetX.value
                translationY = shakeOffsetY.value
            }
            .background(MaterialTheme.colorScheme.background)
    ) {
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

    // FIZYKA
    val accelerationFactor = 0.1f
    val friction = 0.92f
    val maxSpeed = 10f
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
    val GROWTH_RATE = 15f
    val START_DELAY = 5f

    var bhRadius by remember { mutableFloatStateOf(INITIAL_RADIUS) }
    var bhDelay by remember { mutableFloatStateOf(START_DELAY) }
    var bhPause by remember { mutableFloatStateOf(0f) }

    val context = LocalContext.current
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
        while (true) {
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

    // ———————— GAME LOOP ———————— //
    LaunchedEffect(Unit) {
        var lastTime = System.currentTimeMillis()

        loop@ while (!stopLoop) {
            val now = System.currentTimeMillis()
            val dt = (now - lastTime) / 1000f
            lastTime = now

            if (hasWon || hasLost) break@loop

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

            if (bhDelay <= 0f && distBH <= bhRadius + ballRadius) {
                withContext(Dispatchers.Main) { onLost() }
                stopLoop = true
            }

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

                ballX += velocityX * dt * 60
                ballY += velocityY * dt * 60

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

                    // Sprawdzanie kolizji z gap walls
                    for (i in rings.indices) {
                        val ring = rings[i]
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

                            // GAP EFFECT
                            if (prev.second && !cur.second && !(isTriggered.getOrNull(i) ?: false)) {
                                val angle = Math.toDegrees(
                                    kotlin.math.atan2((ballY - ring.center.y).toDouble(), (ballX - ring.center.x).toDouble())
                                ).let { if (it < 0) it + 360 else it }

                                val effect = ring.gapEffects.minByOrNull { g -> abs(g.midAngle - angle) }
                                effect?.let { g ->
                                    when (g.effect.type) {
                                        EffectType.TIME -> {
                                            withContext(Dispatchers.Main) {
                                                localTimer += g.effect.value.toInt()
                                                if (g.effect.value > 0) bhPause += g.effect.value
                                                onTimeChange(localTimer)
                                            }
                                        }
                                        EffectType.POINTS -> {
                                            when (g.effect.operation) {
                                                Operation.MULTIPLY -> localHighScore = (localHighScore * g.effect.value.toInt()).coerceAtLeast(0)
                                                Operation.DIVIDE -> localHighScore = (localHighScore / g.effect.value.toInt()).coerceAtLeast(1)
                                                else -> pendingPointModifier += g.effect.value
                                            }
                                        }
                                        EffectType.WALLS -> {
                                            when (g.effect.operation) {
                                                Operation.MULTIPLY -> wallCountModifier = (wallCountModifier * g.effect.value).toInt().coerceAtLeast(0)
                                                Operation.DIVIDE -> wallCountModifier = (wallCountModifier.toFloat() / g.effect.value).roundToInt().coerceAtLeast(1)
                                                else -> wallCountModifier += g.effect.value.toInt()
                                            }
                                        }
                                        EffectType.GAPS -> {
                                            when (g.effect.operation) {
                                                Operation.MULTIPLY -> gapCountModifier = (gapCountModifier * g.effect.value).toInt().coerceAtLeast(0)
                                                Operation.DIVIDE -> gapCountModifier = (gapCountModifier.toFloat() / g.effect.value).roundToInt().coerceAtLeast(1)
                                                else -> gapCountModifier += g.effect.value.toInt()
                                            }
                                        }
                                    }
                                }

                                if (i < isTriggered.size) isTriggered[i] = true
                                ringCount++

                                val t = (now - timeSinceLastRing) / 1000f
                                localHighScore += maxOf(0, 50 - (t * 5).toInt())
                                timeSinceLastRing = now

                                if (rings.size < maxRings) {
                                    val lastRing = rings.last()
                                    val newInner = lastRing.outerRadius + 200f
                                    val newOuter = newInner + 50f

                                    val baseGaps = (floor(((PI.toFloat() * newInner) / gapSize) / 8)).toInt() + 1
                                    val ringsIndex = (ringCount - 2).coerceAtLeast(0)
                                    val reductionRate = 1
                                    val difficultyReduction = (ringsIndex / reductionRate).coerceAtMost(4)
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

                            if (i < prevStates.size) {
                                prevStates[i] = cur
                            } else {
                                prevStates.add(cur)
                            }
                        }
                    } else {
                        for (i in rings.indices) {
                            if (i < prevStates.size) prevStates[i] = curFirst[i] to curSecond[i]
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
                localHighScore += remainingTime * 5
                withContext(Dispatchers.Main) { onWin(localHighScore) }
                stopLoop = true
            }

            if (stopLoop) break@loop
            delay(16)
        }
        return@LaunchedEffect
    }

    // WALL GENERATOR
    LaunchedEffect(rings.size) {
        if (rings.size > 1) {
            val baseWalls = 6 + (10 * (rings.size - 2))
            val modifiedWalls = (baseWalls + wallCountModifier).toInt().coerceAtLeast(1)
            val newWalls = generateWallsBetweenRings(rings, modifiedWalls, obstacleColor)
            walls.addAll(newWalls)
        }
    }

    Canvas(modifier = Modifier.fillMaxSize()) {
        val center = Offset(size.width / 2f, size.height / 2f)
        val cam = center - Offset(ballX, ballY)

        drawContext.canvas.save()
        drawContext.canvas.translate(cam.x, cam.y)

        if (bhDelay > 0) {
            drawCircle(
                color = Color.Gray.copy(alpha = 0.3f),
                radius = INITIAL_RADIUS + 10f,
                center = BLACK_HOLE_CENTER
            )
        }

        rings.forEach { drawRingWithGaps(it) }
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