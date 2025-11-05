package com.example.sphereescape2125.screens.obstacle

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import kotlin.math.PI
import kotlin.math.floor
import kotlin.random.Random

const val gapSize = 80f

data class RingObstacle(
    val center: Offset,
    val outerRadius: Float,
    val innerRadius: Float,
    val color: Color = Color.Red,
) {
    var totalExits: Int = 0
    var gaps: MutableList<Float> = mutableListOf()
    val gapAngle = (gapSize / innerRadius) * (180f / PI.toFloat())

    init {
        totalExits = (floor(((PI.toFloat() * innerRadius) / gapSize) / 3)).toInt()
        for (i in 0 until totalExits) {
            val randStart = gapAngle + (360f / totalExits) * i
            val randStop = (360f / totalExits) - gapAngle + (360f / totalExits) * i

            val random: Float = randStart + Random.nextFloat() * (randStop - randStart)
            gaps.add(random)
        }
    }
}

fun DrawScope.drawRingWithGaps(obstacle: RingObstacle) {
    val totalAngles = mutableListOf<Pair<Float, Float>>()
    val innerRadius = obstacle.innerRadius
    val gapAngle = (gapSize / innerRadius) * (180f / PI.toFloat())




    // Dodanie przerw do listy segmentów
    for (gap in obstacle.gaps) {

        val gapStart = gap
        var gapEnd = gapStart + gapAngle

        // Jeśli przerwa wychodzi poza 360°, dostosowujemy ją
        if (gapEnd > 360f) {
            gapEnd = 360f
        }

        totalAngles.add(gapStart to gapEnd)
    }

    // Teraz musimy dodać segmenty wypełniające
    val filledAngles = mutableListOf<Pair<Float, Float>>()

    var currentStartAngle = 0f  // Początek segmentu
    for ((startAngle, endAngle) in totalAngles) {
        // Dodajemy segment przed każdą przerwą, jeśli istnieje przestrzeń
        if (currentStartAngle < startAngle) {
            filledAngles.add(currentStartAngle to startAngle)
        }
        currentStartAngle = endAngle // Przesuwamy początek na koniec przerwy
    }

    // Jeśli ostatni kąt kończy się przed 360°, dodajemy resztę okręgu
    if (currentStartAngle < 360f) {
        filledAngles.add(currentStartAngle to 360f)
    }

    // Rysowanie segmentów wypełniających okrąg
    for ((startAngle, endAngle) in filledAngles) {
        drawArc(
            color = obstacle.color,
            startAngle = startAngle,
            sweepAngle = endAngle - startAngle,  // Długość segmentu
            useCenter = false,
            topLeft = Offset(obstacle.center.x - obstacle.outerRadius, obstacle.center.y - obstacle.outerRadius),
            size = androidx.compose.ui.geometry.Size(obstacle.outerRadius * 2, obstacle.outerRadius * 2),
            style = Stroke(width = obstacle.outerRadius - obstacle.innerRadius)
        )
    }
}
