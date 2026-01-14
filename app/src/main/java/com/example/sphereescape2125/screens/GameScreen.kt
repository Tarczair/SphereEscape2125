package com.example.sphereescape2125.screens

import android.app.Activity
import android.content.Context
import android.view.WindowManager
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
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import com.example.sphereescape2125.MainViewModel
import com.example.sphereescape2125.screens.obstacle.*
import com.example.sphereescape2125.sensors.ShakeDetector
import com.example.sphereescape2125.sensors.TiltSensor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlin.math.*

/**
 * Ekran wyświetlany po przegranej rozgrywce (Game Over).
 *
 * Blokuje interakcję z grą, wyświetla komunikat o przyczynie porażki (czas lub czarna dziura)
 * i umożliwia powrót do menu głównego. Tło jest półprzezroczyste, aby gracz widział moment porażki.
 *
 * @param onBack Funkcja wywoływana po kliknięciu przycisku powrotu.
 */
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
        Text("KONIEC GRY", style = MaterialTheme.typography.headlineLarge, color = MaterialTheme.colorScheme.onBackground)
        Text("Skończył się czas / Wpadłeś w Czarną Dziurę!", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.onBackground)
        Spacer(Modifier.height(24.dp))
        Button(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
            Text("WRÓĆ DO MENU")
        }
    }
}

/**
 * Komponent narzędziowy zapobiegający wygaszaniu ekranu.
 *
 * Wykorzystuje flagę systemową [WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON].
 * Jest to kluczowe w grach sterowanych ruchem, gdzie użytkownik nie dotyka ekranu,
 * co system mógłby zinterpretować jako bezczynność.
 */
@Composable
fun AndroidKeepScreenOn() {
    val context = LocalContext.current
    DisposableEffect(Unit) {
        val window = (context as Activity).window
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        onDispose { window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON) }
    }
}

/**
 * Główny kontener ekranu rozgrywki.
 *
 * Odpowiada za:
 * 1. Inicjalizację sensorów (w tym [ShakeDetector] dla mechaniki wstrząsów).
 * 2. Zarządzanie globalnym stanem gry (zwycięstwo, porażka, wynik, czas).
 * 3. Obsługę efektu wizualnego trzęsienia ekranem (Shake Animation) przy wykryciu wstrząsu.
 * 4. Wyświetlanie warstwy HUD oraz ekranów końcowych (Victory/GameOver).
 *
 * Renderowanie właściwej rozgrywki delegowane jest do [GameCanvas].
 *
 * @param viewModel ViewModel aplikacji (obecnie nieużywany bezpośrednio w logice, ale przekazywany dla spójności).
 * @param onBack Callback nawigacyjny powrotu do menu.
 */
@Composable
fun GameScreen(viewModel: MainViewModel, onBack: () -> Unit) {
    AndroidKeepScreenOn()
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("SphereEscapePrefs", Context.MODE_PRIVATE) }

    val shockSens = prefs.getFloat("ShockSens", 1.0f)

    var shakeEvent by remember { mutableIntStateOf(0) }
    val shakeOffsetX = remember { Animatable(0f) }
    val shakeOffsetY = remember { Animatable(0f) }

    // Inicjalizacja detektora z uwzględnieniem progu (używając nowej wersji klasy ShakeDetector)
    val shakeDetector = remember {
        ShakeDetector(context, threshold = 4.5f - shockSens) {
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

/**
 * Wyświetlacz przezierny (Head-Up Display) prezentujący kluczowe parametry w trakcie gry.
 *
 * Pokazuje:
 * - Aktualny poziom.
 * - Bieżący wynik punktowy.
 * - Pozostały czas (zmienia kolor na czerwony, gdy zostało < 10 sekund).
 */
@Composable
fun GameHUD(timeLeft: Int, currentScore: Int, onBack: () -> Unit) {
    val textColor = MaterialTheme.colorScheme.onBackground

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

            Text("WYNIK: $currentScore", style = MaterialTheme.typography.headlineMedium.copy(fontSize = 6.em), color = textColor, modifier = Modifier.padding(top = 8.dp))

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

/**
 * Ekran zwycięstwa.
 *
 * Wyświetlany po pomyślnym ukończeniu wszystkich pierścieni.
 * Prezentuje wynik końcowy oraz zapisany najlepszy wynik.
 */
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

/**
 * Oblicza liczbę ścian do wygenerowania w luce między pierścieniami.
 *
 * Algorytm bazuje na:
 * 1. Promieniu pierścienia (im dalej, tym więcej miejsca na ściany).
 * 2. Indeksie pierścienia (im wyższy poziom, tym trudniej).
 * 3. Modyfikatorach dynamicznych [wallCountModifier] i [wallMultiplier] zdobywanych w trakcie gry.
 *
 * @param ringIndex Numer kolejny pierścienia (0-indexed).
 * @param ring Obiekt pierścienia [RingObstacle].
 * @param wallCountModifier Addytywny modyfikator liczby ścian (Buff/Debuff).
 * @param wallMultiplier Mnożnikowy modyfikator liczby ścian (Buff/Debuff).
 * @return Całkowita liczba ścian do wygenerowania.
 */
fun calculateWallCount(
    ringIndex: Int,
    ring: RingObstacle,
    wallCountModifier: Int,
    wallMultiplier: Float
): Int {
    val radius = (ring.innerRadius + ring.outerRadius) / 2f

    // Zwiększamy bazę: np. (5 * ringIndex) zamiast poprzedniej logiki
    // Dodatkowo dodajemy mnożnik trudności zależny od etapu
    val difficultyFactor = 1.5f // Możesz tu wstawić np. 1.0f + (ringIndex * 0.1f)

    val baseWalls = (radius / 100f).roundToInt() + (12 * ringIndex)

    return (((baseWalls + wallCountModifier) * wallMultiplier) * difficultyFactor)
        .roundToInt()
        .coerceAtLeast(1)
}

/**
 * Serce silnika gry (Game Engine) - komponent odpowiedzialny za logikę fizyki, renderowanie i stan świata.
 *
 * Funkcja ta implementuje:
 * - **Pętlę gry (Game Loop):** Opartą na korutynach, działającą z częstotliwością ~60 FPS.
 * - **Silnik fizyczny:** Całkowanie ruchu piłki z obsługą tarcia i przyspieszenia z akcelerometru.
 * Zastosowano technikę **sub-steppingu** (4 kroki na klatkę) dla precyzyjnej detekcji kolizji przy dużych prędkościach.
 * - **Obsługę kolizji:**
 * - Z pierścieniami (odbicia sprężyste).
 * - Ze ścianami (ścianki przeszkód [WallObstacle]).
 * - Z "lukami" (wyjścia z pierścieni).
 * - **Mechanikę Czarnej Dziury:** Obiekt w centrum, który powiększa się w czasie. Jeśli gracz dotknie horyzontu zdarzeń - przegrywa.
 * - **System Modyfikatorów (Buff/Debuff):**
 * Przelatując przez luki, gracz może aktywować efekty zmieniające:
 * - Punkty (mnożenie, dzielenie, dodawanie).
 * - Czas (dodanie/odjęcie).
 * - Strukturę poziomu (zwiększenie/zmniejszenie liczby ścian lub luk w kolejnych pierścieniach).
 * - **Obsługę wstrząsu (Shake):**
 * Wykrycie wstrząsu powoduje przetasowanie układu ścian w bieżącym pierścieniu, ale nakłada karę czasową.
 *
 * Renderowanie odbywa się na natywnym [Canvas] z zastosowaniem transformacji kamery (kamera śledzi gracza).
 *
 * @param shakeEvent Licznik zdarzeń wstrząsu (zmienia się przy wykryciu potrząśnięcia).
 * @param hasWon Flaga stanu zwycięstwa.
 * @param hasLost Flaga stanu porażki.
 * @param remainingTime Pozostały czas w sekundach.
 * @param onTimeChange Callback aktualizujący czas w nadrzędnym komponencie.
 * @param onScoreChange Callback aktualizujący wynik.
 * @param onWin Callback wywoływany przy zwycięstwie.
 * @param onLost Callback wywoływany przy porażce.
 */
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

    val visualEffects = remember { mutableStateListOf<VisualEffect>() }

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

    val friction = 0.92f
    val maxSpeed = 10f
    val CALIBRATION_OFFSET_Y = 4f

    var ringCount by remember { mutableIntStateOf(0) }
    var localTimer by remember { mutableIntStateOf(remainingTime) }
    var localHighScore by remember { mutableIntStateOf(0) }
    var timeSinceLastRing by remember { mutableLongStateOf(System.currentTimeMillis()) }

    val context = LocalContext.current

    // Parametry Czarnej Dziury
    val BLACK_HOLE_CENTER = Offset(600f, 800f)
    val INITIAL_RADIUS = 40f
    val GROWTH_RATE = 15f
    val START_DELAY = 5f

    var bhRadius by remember { mutableFloatStateOf(INITIAL_RADIUS) }
    var bhDelay by remember { mutableFloatStateOf(START_DELAY) }
    var bhPause by remember { mutableFloatStateOf(0f) }

    val tiltSensor = remember { TiltSensor(context) }

    var wallCountModifier by remember { mutableIntStateOf(0) }
    var gapCountModifier by remember { mutableIntStateOf(0) }

    var wallMultiplier by remember { mutableFloatStateOf(1f) }
    var gapMultiplier by remember { mutableFloatStateOf(1f) }

    val prefs = remember { context.getSharedPreferences("SphereEscapePrefs", Context.MODE_PRIVATE) }
    // Pobieramy czułość (używamy klucza "BallSpeed" tak jak w Twoim sliderze)
    val controlSensitivity = prefs.getFloat("BallSpeed", 1.0f)


    LaunchedEffect(Unit) {
        if (rings.isEmpty()) {
            rings.add(RingObstacle(BLACK_HOLE_CENTER, 250f, 200f, obstacleColor, ringCount = 0))
            prevStates.add(false to false)
            rings.add(RingObstacle(BLACK_HOLE_CENTER, 500f, 450f, obstacleColor, ringCount = 1))
            prevStates.add(false to false)
        }
    }

    // Logika obsługi wstrząsu (Shake Handler)
    var lastHandledShake by remember { mutableIntStateOf(0) }

    LaunchedEffect(shakeEvent) {
        if (shakeEvent <= lastHandledShake) return@LaunchedEffect
        lastHandledShake = shakeEvent

        // Kara czasowa za użycie wstrząsu
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

    // Główny Timer odliczający czas gry
    LaunchedEffect(Unit) {
        while (!hasWon && !hasLost) {
            delay(1000)
            localTimer--
            withContext(Dispatchers.Main) { onTimeChange(localTimer) }

            if (localTimer <= 0) {
                withContext(Dispatchers.Main) { onLost() }
                stopLoop = true
                break
            }
        }
    }

    DisposableEffect(Unit) {
        tiltSensor.startListening()
        onDispose { tiltSensor.stopListening() }
    }
    val gravityData by tiltSensor.gravityData.collectAsState()

    // --- GŁÓWNA PĘTLA GRY (PHYSICS & GAME LOOP) ---
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

            val iterator = visualEffects.iterator()
            while (iterator.hasNext()) {
                val ev = iterator.next()
                ev.y -= 1.5f       // Ruch w górę
                ev.alpha -= 0.02f  // Znikanie
                ev.lifetime--
                if (ev.lifetime <= 0) iterator.remove()
            }

            val subDt = dt / physicsSteps

            repeat(physicsSteps) { _ ->
                val gravity = gravityData

                // Aktualizacja logiki Czarnej Dziury
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

                // Sprawdzenie kolizji z Czarną Dziurą
                val distBH = hypot(ballX - BLACK_HOLE_CENTER.x, ballY - BLACK_HOLE_CENTER.y)
                if (bhDelay <= 0f && distBH <= bhRadius + ballRadius) {
                    withContext(Dispatchers.Main) {
                        if (!hasLost) onLost()
                    }
                    stopLoop = true
                    return@repeat
                }

                // Fizyka ruchu kulki
                val ax = -gravity.x * controlSensitivity
                val ay = (gravity.y - CALIBRATION_OFFSET_Y) * controlSensitivity

                velocityX = (velocityX + ax) * friction
                velocityY = (velocityY + ay) * friction

                val speed = hypot(velocityX, velocityY)
                if (speed > maxSpeed) {
                    velocityX = (velocityX / speed) * maxSpeed
                    velocityY = (velocityY / speed) * maxSpeed
                }

                ballX += velocityX * subDt * 60
                ballY += velocityY * subDt * 60

                // Detekcja i obsługa kolizji
                if (rings.isNotEmpty()) {
                    val curFirst = BooleanArray(rings.size)
                    val curSecond = BooleanArray(rings.size)
                    for (i in rings.indices) {
                        val coll = isCircleCollidingWithRing(Offset(ballX, ballY), ballRadius, rings[i])
                        curFirst[i] = coll.first
                        curSecond[i] = coll.second
                    }

                    var hitGap = false
                    // Kolizje ze ściankami w lukach (Gap Walls)
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

                    // Kolizje ze zwykłymi ścianami przeszkód (Obstacle Walls)
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
                        }
                    }

                    // 3. NOWA LOGIKA PUNKTÓW I PRZEJŚĆ (Niezależna od fizyki)
                    for (i in rings.indices) {
                        val ring = rings[i]
                        val dist = hypot(ballX - ring.center.x, ballY - ring.center.y)

                        // Sprawdzamy czy środek kulki wszedł w obszar obręczy
                        val isInside = dist > ring.innerRadius && dist < ring.outerRadius
                        val wasTriggered = isTriggered.getOrNull(i) ?: false

                        if (isInside && !wasTriggered) {
                            isTriggered[i] = true // Blokada, aby efekt odpalił się tylko raz

                            val angle = Math.toDegrees(atan2((ballY - ring.center.y).toDouble(), (ballX - ring.center.x).toDouble()))
                                .let { if (it < 0) it + 360 else it }.toFloat()

                            // Znalezienie najbliższego efektu w luce
                            val effect = ring.gapEffects.minByOrNull { g -> abs(g.midAngle - angle) }
                            var pointEffectApplied = false

                            effect?.let { g ->
                                // --- LOGIKA POWIADOMIENIA (Z TWOJEGO WKLEJONEGO KODU) ---
                                val isPositive = when (g.effect.type) {
                                    EffectType.POINTS -> g.effect.operation != Operation.SUB && g.effect.operation != Operation.DIVIDE
                                    EffectType.TIME -> g.effect.value > 0
                                    EffectType.WALLS -> g.effect.value < 0
                                    EffectType.GAPS -> g.effect.value > 0
                                }

                                val valueAbs = if (g.effect.value < 0) -g.effect.value else g.effect.value
                                val valueInt = valueAbs.toInt()

                                val operator = when (g.effect.operation) {
                                    Operation.MULTIPLY -> "x"
                                    Operation.DIVIDE -> "/"
                                    Operation.ADD -> "+"
                                    else -> if (g.effect.value < 0) "-" else "+"
                                }

                                val message = when (g.effect.type) {
                                    EffectType.POINTS -> {
                                        if (g.effect.operation == Operation.MULTIPLY || g.effect.operation == Operation.DIVIDE) "PUNKTY $operator$valueInt!"
                                        else if (isPositive) "ZDOBYTO $valueInt PKT!" else "UTRACONO $valueInt PKT!"
                                    }
                                    EffectType.TIME -> {
                                        if (g.effect.operation == Operation.MULTIPLY || g.effect.operation == Operation.DIVIDE) "$operator$valueInt CZASU"
                                        else "$operator$valueInt SEK"
                                    }
                                    EffectType.WALLS -> {
                                        if (g.effect.operation == Operation.MULTIPLY || g.effect.operation == Operation.DIVIDE) "ŚCIANY $operator$valueAbs"
                                        else if (isPositive) "MNIEJ ŚCIAN ($valueInt)" else "WIĘCEJ ŚCIAN ($valueInt)"
                                    }
                                    EffectType.GAPS -> {
                                        if (g.effect.operation == Operation.MULTIPLY || g.effect.operation == Operation.DIVIDE) "WYJŚCIA $operator$valueAbs"
                                        else if (isPositive) "+$valueInt WYJŚCIA" else "-$valueInt WYJŚCIA"
                                    }
                                }

                                val feedbackColor = if (isPositive) Color.Green else Color.Red
                                visualEffects.add(VisualEffect(message, feedbackColor, ballX, ballY + 60f))

                                // --- LOGIKA OBLICZEŃ (Z TWOJEGO WKLEJONEGO KODU) ---
                                when (g.effect.type) {
                                    EffectType.POINTS -> {
                                        val currentBase = localHighScore.toDouble()
                                        val newScore = when (g.effect.operation) {
                                            Operation.MULTIPLY -> currentBase * g.effect.value
                                            Operation.DIVIDE -> if (g.effect.value != 0f) currentBase / g.effect.value else currentBase
                                            Operation.ADD -> currentBase + g.effect.value
                                            Operation.SUB -> currentBase + g.effect.value
                                        }
                                        localHighScore = newScore.toInt().coerceAtLeast(0)
                                        pointEffectApplied = true
                                    }
                                    EffectType.TIME -> {
                                        localTimer = (localTimer + g.effect.value.toInt()).coerceAtLeast(0)
                                        if (g.effect.value > 0) bhPause += g.effect.value
                                    }
                                    EffectType.WALLS -> {
                                        if (g.effect.operation == Operation.MULTIPLY || g.effect.operation == Operation.DIVIDE) {
                                            wallMultiplier = (wallMultiplier * g.effect.value).coerceIn(0.25f, 3f)
                                        } else {
                                            wallCountModifier += g.effect.value.toInt()
                                        }
                                    }
                                    EffectType.GAPS -> {
                                        if (g.effect.operation == Operation.MULTIPLY || g.effect.operation == Operation.DIVIDE) {
                                            gapMultiplier = (gapMultiplier * g.effect.value).coerceIn(0.25f, 3f)
                                        } else {
                                            gapCountModifier += g.effect.value.toInt()
                                        }
                                    }
                                }
                            }

                            // Bonus za czas, jeśli nie było efektu punktowego
                            if (!pointEffectApplied) {
                                val timeBonus = maxOf(0, 50 - ((System.currentTimeMillis() - timeSinceLastRing) / 1000f * 5).toInt())
                                localHighScore += timeBonus
                            }

                            ringCount++
                            timeSinceLastRing = System.currentTimeMillis()

                            // --- GENEROWANIE NASTĘPNEGO PIERŚCIENIA ---
                            if (rings.size < maxRings) {
                                val lastRing = rings.last()
                                val newInner = lastRing.outerRadius + 200f
                                val newOuter = newInner + 50f

                                // gapSizeCollision to stała, którą masz w kodzie (upewnij się, że jest dostępna)
                                val baseGaps = (floor(((PI.toFloat() * newInner) / 40f) / 8)).toInt() + 1
                                val modGaps = ((baseGaps + gapCountModifier) * gapMultiplier).roundToInt().coerceAtLeast(1)

                                val nextRing = RingObstacle(BLACK_HOLE_CENTER, newOuter, newInner, obstacleColor, false, ringCount).apply {
                                    totalExits = modGaps
                                }

                                // Dodanie do list musi odbyć się na wątku głównym UI, ponieważ rings i isTriggered to mutableStateListOf
                                withContext(Dispatchers.Main) {
                                    rings.add(nextRing)
                                    isTriggered.add(false)
                                    onScoreChange(localHighScore)
                                    onTimeChange(localTimer)
                                }
                            } else {
                                // Jeśli to ostatni pierścień, zaktualizuj tylko wyniki
                                withContext(Dispatchers.Main) {
                                    onScoreChange(localHighScore)
                                    onTimeChange(localTimer)
                                }
                            }
                        }
                    }

                    // 4. Finalizacja dodania pierścienia
                    ringToAdd?.let {
                        withContext(Dispatchers.Main) {
                            rings.add(it)
                            isTriggered.add(false)
                        }
                    }
                }

            }

            // Warunek zwycięstwa (ukończenie wszystkich pierścieni)
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

    // Generator ścian między pierścieniami
    LaunchedEffect(rings.size) {
        if (rings.size > 1) {
            val i = rings.size - 2
            val current = rings[i]
            val next = rings[i+1]

            val hasWalls = walls.any { it.startRing == current }

            if (!hasWalls) {
                val radius = (current.innerRadius + current.outerRadius) / 2f
                val baseWalls = (radius / 120f).roundToInt() + (10 * i)

                val modWalls = calculateWallCount(
                    ringIndex = i,
                    ring = current,
                    wallCountModifier = wallCountModifier,
                    wallMultiplier = wallMultiplier
                )

                val newWalls = generateWallsBetweenRings(
                    listOf(current, next),
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

        visualEffects.forEach { ev ->
            drawContext.canvas.nativeCanvas.drawText(
                ev.text,
                ev.x,
                ev.y,
                android.graphics.Paint().apply {
                    color = ev.color.toArgb()
                    textSize = 42f
                    textAlign = android.graphics.Paint.Align.CENTER
                    alpha = (ev.alpha * 255).toInt()
                    isFakeBoldText = true
                    // Cień, aby tekst był widoczny na każdym tle
                    setShadowLayer(12f, 0f, 0f, android.graphics.Color.BLACK)
                }
            )
        }

        drawContext.canvas.restore()
    }
}