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
import com.example.sphereescape2125.screens.obstacle.RingObstacle
import com.example.sphereescape2125.screens.obstacle.WallObstacle
import com.example.sphereescape2125.screens.obstacle.drawRingWithGaps
import com.example.sphereescape2125.screens.obstacle.isCircleCollidingWithRing
import com.example.sphereescape2125.screens.obstacle.drawWalls
import com.example.sphereescape2125.screens.obstacle.generateWallsBetweenRings
import kotlinx.coroutines.delay
import android.util.Log
import kotlin.math.hypot

import androidx.compose.runtime.collectAsState
import androidx.compose.ui.platform.LocalContext
import com.example.sphereescape2125.sensors.TiltSensor // Upewnij się, że ścieżka jest poprawna
import com.example.sphereescape2125.screens.obstacle.getWallCollisionInfo // WAŻNY IMPORT!
import kotlin.math.abs


@Composable
fun GameScreen(onBack: () -> Unit) {
    var time by remember { mutableIntStateOf(60) }
    var hasWon by remember { mutableStateOf(false) }
    var bestScore by remember { mutableStateOf(200) }

    // Timer działa tylko gdy gra trwa
    LaunchedEffect(hasWon) {
        if (!hasWon) {
            while (time > 0) {
                delay(1000)
                time--
            }
        }
    }

    Box(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.background)
            .fillMaxSize()
    ) {
        var highScore by remember { mutableStateOf(0) }

        GameCanvas(
            hasWon = hasWon,
            remainingTime = time,
            onWin = { score ->
                highScore = score
                hasWon = true
            }
        )


        // Jeśli gra NIE jest wygrana → pokazujemy UI gry
        if (!hasWon) {
            GameHUD(time = time, onBack = onBack)
        }

        // Jeśli gra JEST wygrana → pokazujemy ekran zwycięstwa
        if (hasWon) {
            VictoryScreen(
                points = highScore,
                bestScore = maxOf(highScore, bestScore),
                onBack = onBack
            )
        }
    }
}

@Composable
fun GameHUD(time: Int, onBack: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Bottom
    ) {
        Text(
            "POZIOM I",
            modifier = Modifier.padding(top = 24.dp),
            style = MaterialTheme.typography.headlineMedium.copy(fontSize = 6.em)
        )
        Text(
            "POZOSTAŁY CZAS: ${String.format("%02d:%02d", time / 60, time % 60)}",
            style = MaterialTheme.typography.headlineMedium.copy(fontSize = 6.em),
            modifier = Modifier.padding(bottom = 16.dp)
        )
        Button(
            onClick = onBack,
            modifier = Modifier.fillMaxWidth()
        ) {
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
            Text(
                "ZWYCIĘSTWO!",
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                "Zdobyte punkty: $points",
                style = MaterialTheme.typography.headlineMedium
            )
            Text(
                "Najlepszy wynik: $bestScore",
                style = MaterialTheme.typography.headlineMedium
            )

            Button(
                onClick = onBack,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 40.dp)
            ) {
                Text("WRÓĆ")
            }
        }
    }
}


@Composable
fun GameCanvas(
    hasWon: Boolean,
    remainingTime: Int,
    onWin: (score: Int) -> Unit
)
 {
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

     val ringTimes = remember { mutableStateListOf<Long>() } // czas wejścia w pierścień
     var localHighScore by remember { mutableIntStateOf(0) }

    DisposableEffect(Unit) {
        tiltSensor.startListening()
        onDispose {
            tiltSensor.stopListening()
        }
    }
    val gravityData by tiltSensor.gravityData.collectAsState()

    LaunchedEffect(Unit) {
        if (rings.isEmpty()) {
            rings.add(
                RingObstacle(
                    center = Offset(600f, 800f),
                    outerRadius = 250f,
                    innerRadius = 200f,
                    color = obstacleColor,
                )
            )
            prevStates.add(false to false)

            rings.add(
                RingObstacle(
                    center = Offset(600f, 800f),
                    outerRadius = 500f,
                    innerRadius = 450f,
                    color = obstacleColor,
                )
            )
            prevStates.add(false to false)
        }
    }

    var velocityX by remember { mutableFloatStateOf(0f) }
    var velocityY by remember { mutableFloatStateOf(0f) }



    val accelerationFactor = 0.5f
    val friction = 0.96f
    val maxSpeed = 4f

    val CALIBRATION_OFFSET_Y = 4f

    LaunchedEffect(Unit) {
        while (true) {
            if (hasWon) {
                // Zatrzymaj kulkę
                velocityX = 0f
                velocityY = 0f
                delay(16L)
                continue
            }
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

            ballX += velocityX
            ballY += velocityY


            var ringToAdd: RingObstacle? = null

            rings.forEachIndexed { index, ring ->
                val currentState = isCircleCollidingWithRing(Offset(ballX, ballY), ballRadius, ring)
                val prevState = prevStates.getOrNull(index) ?: (false to false)

                if (currentState.first && !currentState.second) {
                    val normalX_raw = ballX - ring.center.x
                    val normalY_raw = ballY - ring.center.y
                    val distance = hypot(normalX_raw, normalY_raw)

                    if (distance == 0f) {
                        Log.d("DEBUG_TAG", "Kolizja w centrum pierścienia, pomijam")
                        return@forEachIndexed
                    }

                    // Znormalizowany wektor (zawsze wskazuje OD środka pierścienia na zewnątrz)
                    val normalX = normalX_raw / distance
                    val normalY = normalY_raw / distance

                    // Obliczamy "bliskość" kulki do obu ścian pierścienia.
                    val proximityToInner = abs(distance - ring.innerRadius)
                    val proximityToOuter = abs(distance - ring.outerRadius)

                    // Iloczyn skalarny
                    val dot = (velocityX * normalX) + (velocityY * normalY)

                    if (proximityToInner < proximityToOuter) {
                        // Wypchnij kulkę DO ŚRODKA
                        val penetrationDepth = (distance + ballRadius) - ring.innerRadius

                        if (penetrationDepth > 0) {
                            val correction = (penetrationDepth + 0.01f)
                            ballX -= normalX * correction
                            ballY -= normalY * correction
                        }

                        if (dot > 0) {
                            val bounciness = 0.0f

                            val normalVelocityX = dot * normalX
                            val normalVelocityY = dot * normalY
                            val tangentVelocityX = velocityX - normalVelocityX
                            val tangentVelocityY = velocityY - normalVelocityY

                            // Odbijamy tylko składową normalną
                            val reflectedNormalVx = -normalVelocityX * bounciness
                            val reflectedNormalVy = -normalVelocityY * bounciness

                            velocityX = tangentVelocityX + reflectedNormalVx
                            velocityY = tangentVelocityY + reflectedNormalVy
                        }

                    } else {
                        //Kulka jest na zewnątrz pierścienia i leci do środka

                        // Wypchnij kulkę NA ZEWNĄTRZ
                        val penetrationDepth = (ring.outerRadius + ballRadius) - distance

                        if (penetrationDepth > 0) {
                            val correction = (penetrationDepth + 0.01f)
                            ballX += normalX * correction
                            ballY += normalY * correction
                        }

                        if (dot < 0) {
                            val bounciness = 0.0f

                            val normalVelocityX = dot * normalX
                            val normalVelocityY = dot * normalY
                            val tangentVelocityX = velocityX - normalVelocityX
                            val tangentVelocityY = velocityY - normalVelocityY

                            val reflectedNormalVx = -normalVelocityX * bounciness
                            val reflectedNormalVy = -normalVelocityY * bounciness

                            velocityX = tangentVelocityX + reflectedNormalVx
                            velocityY = tangentVelocityY + reflectedNormalVy
                        }
                    }
                }


                if (prevState.second && !currentState.second && !isTriggered[index]) {
                    isTriggered[index] = true
                    ringCount++

                    val currentTime = System.currentTimeMillis()
                    val timeSpent = if (ringTimes.isEmpty()) 0 else (currentTime - ringTimes.last()) / 1000
                    ringTimes.add(currentTime)

                    if (timeSpent < 10) localHighScore += (10 - timeSpent).toInt()

                    if (ringCount == maxRings) {
                        localHighScore += remainingTime * 5
                        onWin(localHighScore)
                    }

                    ringToAdd = RingObstacle(
                        center = Offset(600f, 800f),
                        outerRadius = 500f + 250f * ringCount,
                        innerRadius = 450f + 250f * ringCount,
                        color = obstacleColor,
                    )
                }

                if (index < prevStates.size) {
                    prevStates[index] = currentState
                }
            }

            for (wall in walls) {
                val collisionInfo = getWallCollisionInfo(
                    circleCenter = Offset(ballX, ballY),
                    circleRadius = ballRadius,
                    wall = wall
                )

                if (collisionInfo != null) {
                    val (closestPoint, distance, normal) = collisionInfo
                    val wallHalfWidth = 25f
                    val collisionThreshold = ballRadius + wallHalfWidth

                    val penetrationDepth = (collisionThreshold - distance) + 0.01f

                    // Wypchnij kulkę ze ściany (zapobiega utknięciu)
                    ballX += normal.x * penetrationDepth
                    ballY += normal.y * penetrationDepth

                    val dot = (velocityX * normal.x) + (velocityY * normal.y)

                    if (dot < 0) {
                        // Anuluj prędkość prostopadłą i zastosuj odbicie

                        val bounciness = 0.6f
                        val restitution = 1.0f + bounciness

                        val reflectVx = velocityX - (restitution * dot * normal.x)
                        val reflectVy = velocityY - (restitution * dot * normal.y)

                        velocityX = reflectVx
                        velocityY = reflectVy
                    }
                }
            }


            ringToAdd?.let {
                if (rings.size < maxRings) {
                    rings.add(it)
                    prevStates.add(false to false)
                } else {
                    onWin(localHighScore)
                }
            }

            delay(16L)
        }
    }

    LaunchedEffect(rings.size) {
        if (rings.size > 1) {
            val newWalls = generateWallsBetweenRings(
                rings = rings,
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