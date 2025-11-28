package com.example.sphereescape2125.screens.obstacle

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.sin
import kotlin.random.Random


enum class EffectType {
    WALLS,      // liczba ścian
    GAPS,       // liczba przerw
    TIME,       // czas
    POINTS      // punkty
}


fun generateRandomEffect(): RingEffect {

    val effectType = EffectType.entries.random()

    val isDebuff = Random.nextFloat() < 0.7f   // 70% debuff / 30% buff

    val value = when (effectType) {
        EffectType.WALLS -> Random.nextInt(1, 4).toFloat()
        EffectType.GAPS -> Random.nextInt(1, 3).toFloat()
        EffectType.TIME -> Random.nextInt(2, 6).toFloat()
        EffectType.POINTS -> Random.nextInt(5, 21).toFloat()
    }

    val op = if (isDebuff) Operation.SUB else Operation.ADD

    val finalValue = if (op == Operation.SUB) -value else value

    val icon = when(effectType) {
        EffectType.WALLS -> "🧱"
        EffectType.GAPS -> "🚪"
        EffectType.TIME -> "⏱️"
        EffectType.POINTS -> "⭐"
    }

    val label = "$icon ${if (finalValue > 0) "+${finalValue.toInt()}" else finalValue.toInt()}"

    return RingEffect(
        operation = op,
        value = finalValue,
        label = label,
        type = effectType
    )
}


const val gapSize = 120f

data class RingEffect(
    val operation: Operation,
    val value: Float,
    val label: String,
    val type: EffectType
)


data class GapWithEffect(
    val startAngle: Float,
    val endAngle: Float,
    val effect: RingEffect
) {
    val midAngle: Float
        get() = (startAngle + endAngle) / 2f
}

enum class Operation { ADD, SUB, MUL, DIV }

data class RingObstacle(
    val center: Offset,
    val outerRadius: Float,
    val innerRadius: Float,
    val color: Color = Color.Red,
    var wallsGenerated: Boolean = false,
) {
    var totalExits: Int = 0
    var gaps: MutableList<Float> = mutableListOf()
    var gapEffects: MutableList<GapWithEffect> = mutableListOf()
    val gapAngle = (gapSize / innerRadius) * (180f / PI.toFloat())

    init {
        totalExits = (floor(((PI.toFloat() * innerRadius) / gapSize) / 6)).toInt() + 2

        for (i in 0 until totalExits) {
            val randStart = gapAngle + (360f / totalExits) * i
            val randStop = (360f / totalExits) - gapAngle + (360f / totalExits) * i

            val randomAngle = (Random.nextInt(
                (randStart * 10).toInt(),
                (randStop * 10).toInt() + 1
            )) / 10f

            val gapStart = randomAngle
            val gapEnd = (gapStart + gapAngle) % 360f
            val effect = generateRandomEffect()

            gapEffects.add(GapWithEffect(startAngle = gapStart, endAngle = gapEnd, effect = effect))

            gaps.add(gapStart)

            gapEffects.add(
                GapWithEffect(
                    startAngle = gapStart,
                    endAngle = gapEnd,
                    effect = effect
                )
            )
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
        var gapEnd = (gapStart + gapAngle) % 360f

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

    for (g in obstacle.gapEffects) {
        val midAngleDeg = g.midAngle

        val textRadius = (obstacle.innerRadius + obstacle.outerRadius) / 2f
        val textAngleRad = Math.toRadians(midAngleDeg.toDouble())
        val textX = obstacle.center.x + (textRadius * cos(textAngleRad)).toFloat()
        val textY = obstacle.center.y + (textRadius * sin(textAngleRad)).toFloat()

        val paint = android.graphics.Paint().apply {
            textAlign = android.graphics.Paint.Align.CENTER
            textSize = 40f
            color = android.graphics.Color.WHITE
            isAntiAlias = true
        }

        drawContext.canvas.nativeCanvas.apply {
            save()
            translate(textX, textY)
            rotate(midAngleDeg + 90f)
            val yCentered = - (paint.descent() + paint.ascent()) / 2f
            drawText(g.effect.label, 0f, yCentered, paint)
            restore()
        }
    }

}