package com.example.sphereescape2125.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import com.example.sphereescape2125.screens.obstacle.RingObstacle
import com.example.sphereescape2125.screens.obstacle.WallObstacle
import com.example.sphereescape2125.screens.obstacle.drawRingWithGaps
import com.example.sphereescape2125.screens.obstacle.isCircleCollidingWithRing
import com.example.sphereescape2125.screens.obstacle.drawWalls
import com.example.sphereescape2125.screens.obstacle.generateWallsBetweenRings
import com.example.sphereescape2125.screens.obstacle.isCircleCollidingWithWall
import kotlinx.coroutines.delay
import android.util.Log
import kotlin.math.hypot

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

    var ballX by remember { mutableFloatStateOf(600f) }
    var ballY by remember { mutableFloatStateOf(800f) }
    val ballRadius = 40f
    var targetX by remember { mutableFloatStateOf(ballX) }
    var targetY by remember { mutableFloatStateOf(ballY) }

    val rings = remember { mutableStateListOf<RingObstacle>() }
    val walls = remember { mutableStateListOf<WallObstacle>() }

    val obstacleColor = MaterialTheme.colorScheme.error
    val ballColor = MaterialTheme.colorScheme.secondary

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

    var velocityX = 0f
    var velocityY = 0f
    val acceleration = 0.5f
    val maxSpeed = 6f
    val brakeMultiplier = 2f
    var previousTargetX = targetX
    var previousTargetY = targetY

    LaunchedEffect(Unit) {
        while (true) {
            var dxTotal = targetX - ballX
            var dyTotal = targetY - ballY
            val dist = kotlin.math.hypot(dxTotal, dyTotal)

            if (dist > 1f) {
                // Sprawdzenie zmiany celu
                val targetChanged = targetX != previousTargetX || targetY != previousTargetY
                if (targetChanged) {
                    velocityX /= brakeMultiplier
                    velocityY /= brakeMultiplier
                    previousTargetX = targetX
                    previousTargetY = targetY
                }

                // Kierunek i przyspieszenie
                val dirX = dxTotal / dist
                val dirY = dyTotal / dist
                val ax = dirX * acceleration
                val ay = dirY * acceleration

                velocityX += ax
                velocityY += ay

                // Ograniczenie prędkości
                var speed = kotlin.math.hypot(velocityX, velocityY)
                if (speed > maxSpeed) {
                    velocityX = (velocityX / speed) * maxSpeed
                    velocityY = (velocityY / speed) * maxSpeed
                    speed = maxSpeed
                }

                // Hamowanie przy zbliżaniu się do celu
                if (dist < 50f) {
                    val brakeFactor = dist / 50f
                    velocityX *= brakeFactor
                    velocityY *= brakeFactor
                }

                // Aktualizacja pozycji
                ballX += velocityX
                ballY += velocityY
            } else {
                // Gdy kula dotrze do celu
                ballX = targetX
                ballY = targetY
                velocityX = 0f
                velocityY = 0f
                Log.d("DEBUG_TAG", "Kula stoi w miejscu")
            }

            var ringToAdd: RingObstacle? = null

            rings.forEachIndexed { index, ring ->
                val currentState = isCircleCollidingWithRing(Offset(ballX, ballY), ballRadius, ring)
                val prevState = prevStates[index]
                if (currentState.first && !currentState.second) {
                    velocityX = -velocityX
                    velocityY = -velocityY
                    Log.d("DEBUG_TAG", "Kula uderzyła w pierścień $index")
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
                prevStates[index] = currentState
            }

            for (wall in walls) {
                if (isCircleCollidingWithWall(Offset(ballX, ballY), ballRadius, wall)) {
                    velocityX = -velocityX
                    velocityY = -velocityY
                }
            }

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
                wallsPerGap = 6 + (4 * (rings.size - 2)),
                color = obstacleColor
            )
            walls.addAll(newWalls)
        }
    }

    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    val canvasCenter = Offset(size.width / 2f, size.height / 2f)
                    val worldOffset = offset - canvasCenter + Offset(ballX, ballY)
                    targetX = worldOffset.x
                    targetY = worldOffset.y
                }
            }
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
