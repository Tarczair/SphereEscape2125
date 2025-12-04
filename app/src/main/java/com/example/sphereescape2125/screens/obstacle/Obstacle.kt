package com.example.sphereescape2125.screens.obstacle

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import com.example.sphereescape2125.screens.GapWall
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.sin
import kotlin.random.Random
import kotlin.math.abs
import kotlin.math.sqrt
import kotlin.math.min

enum class EffectType {
    WALLS,      // liczba ścian
    GAPS,       // liczba przerw
    TIME,       // czas
    POINTS      // punkty
}

enum class Operation { ADD, SUB, MULTIPLY, DIVIDE }

// Funkcja generująca losowy efekt, skalowany względem aktualnego numeru pierścienia
fun generateRandomEffect(ringCount: Int): RingEffect {
    val effectType = EffectType.entries.random()
    val isMultiplier = Random.nextFloat() < 0.2f // 20% szansy na mnożenie/dzielenie
    val isDebuff = Random.nextFloat() < 0.7f     // 70% debuff / 30% buff

    // DYNAMICZNE SKALOWANIE: Wartość max Add/Sub rośnie pierwiastkowo z ringCount
    // Bazowa wartość: 3. Maksymalna wartość wzrasta o pierwiastek z ringCount (ograniczone do +5).
    val maxAddSub = 3 + (sqrt(ringCount.toFloat())).toInt().coerceAtMost(5)

    val value = when (effectType) {
        EffectType.WALLS, EffectType.GAPS ->
            if (isMultiplier) {
                Random.nextInt(2, 4).toFloat() // x2 lub x3
            } else {
                Random.nextInt(1, maxAddSub + 1).toFloat() // +1 do maxAddSub
            }
        EffectType.TIME -> Random.nextInt(2, 6).toFloat() // Tylko ADD/SUB
        EffectType.POINTS ->
            if (isMultiplier) {
                Random.nextInt(2, 4).toFloat() // x2 lub x3
            } else {
                Random.nextInt(5, 21).toFloat()
            }
    }

    val op = when (effectType) {
        EffectType.WALLS -> {
            // WALLS: ADD/MUL to DEBUFF (więcej ścian), SUB/DIV to BUFF (mniej ścian)
            if (isMultiplier) {
                if (isDebuff) Operation.MULTIPLY else Operation.DIVIDE
            } else {
                if (isDebuff) Operation.ADD else Operation.SUB
            }
        }
        EffectType.GAPS -> {
            // GAPS: SUB/DIV to DEBUFF (mniej przerw), ADD/MUL to BUFF (więcej przerw)
            if (isMultiplier) {
                if (isDebuff) Operation.DIVIDE else Operation.MULTIPLY
            } else {
                if (isDebuff) Operation.SUB else Operation.ADD
            }
        }
        EffectType.POINTS, EffectType.TIME -> {
            // POINTS/TIME: SUB/DIV to DEBUFF, ADD/MUL to BUFF
            if (isMultiplier) {
                if (isDebuff) Operation.DIVIDE else Operation.MULTIPLY
            } else {
                if (isDebuff) Operation.SUB else Operation.ADD
            }
        }
    }

    // finalValue: Dla SUB i DIV musi być ujemne.
    val finalValue = if (op == Operation.SUB || op == Operation.DIVIDE) -value else value

    val icon = when(effectType) {
        EffectType.WALLS -> "🧱"
        EffectType.GAPS -> "🚪"
        EffectType.TIME -> "⏱️"
        EffectType.POINTS -> "⭐"
    }

    val effectLabel = when (op) {
        Operation.ADD -> "+${value.toInt()}"
        Operation.SUB -> "-${value.toInt()}"
        Operation.MULTIPLY -> "x${value.toInt()}"
        Operation.DIVIDE -> "÷${value.toInt()}"
    }

    val label = "$icon $effectLabel"

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

data class RingObstacle(
    val center: Offset,
    val outerRadius: Float,
    val innerRadius: Float,
    val color: Color = Color.Red,
    var wallsGenerated: Boolean = false,
    val ringCount: Int
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

            val safeStart = minOf(randStart, randStop)
            val safeStop = maxOf(randStart, randStop)

            val randomAngle = (Random.nextInt(
                (safeStart * 10).toInt(),
                (safeStop * 10).toInt() + 1
            )) / 10f

            val gapStart = randomAngle
            val gapEnd = (gapStart + gapAngle) % 360f

            val effect = generateRandomEffect(ringCount)

            gapEffects.add(GapWithEffect(startAngle = gapStart, endAngle = gapEnd, effect = effect))
            gaps.add(gapStart)
        }
    }
}

fun RingObstacle.generateGapWalls(): List<GapWall> {
    val walls = mutableListOf<GapWall>()
    val gapAngle = (gapSize / innerRadius) * (180f / PI.toFloat())

    for (gapStart in gaps) {
        val gapEnd = (gapStart + gapAngle) % 360f

        // --- ŚCIANA 1: Na początku przerwy (zamyka materiał pierścienia) ---
        val startRad = Math.toRadians(gapStart.toDouble())
        val innerStart = Offset(
            center.x + innerRadius * cos(startRad).toFloat(),
            center.y + innerRadius * sin(startRad).toFloat()
        )
        val outerStart = Offset(
            center.x + outerRadius * cos(startRad).toFloat(),
            center.y + outerRadius * sin(startRad).toFloat()
        )

        val dx1 = outerStart.x - innerStart.x
        val dy1 = outerStart.y - innerStart.y
        val len1 = kotlin.math.hypot(dx1, dy1)
        val normal1 = if (len1 != 0f) Offset(-dy1/len1, dx1/len1) else Offset.Zero

        walls.add(GapWall(innerStart, outerStart, normal1))


        // --- ŚCIANA 2: Na końcu przerwy ---
        val endRad = Math.toRadians(gapEnd.toDouble())
        val innerEnd = Offset(
            center.x + innerRadius * cos(endRad).toFloat(),
            center.y + innerRadius * sin(endRad).toFloat()
        )
        val outerEnd = Offset(
            center.x + outerRadius * cos(endRad).toFloat(),
            center.y + outerRadius * sin(endRad).toFloat()
        )

        val dx2 = outerEnd.x - innerEnd.x
        val dy2 = outerEnd.y - innerEnd.y
        val len2 = kotlin.math.hypot(dx2, dy2)
        val normal2 = if (len2 != 0f) Offset(dy2/len2, -dx2/len2) else Offset.Zero

        walls.add(GapWall(innerEnd, outerEnd, normal2))
    }

    return walls
}

fun DrawScope.drawRingWithGaps(obstacle: RingObstacle) {
    val visualOffset = 20f // Wartość przesunięcia
    val visualOuterRadius = obstacle.outerRadius - visualOffset
    val visualInnerRadius = obstacle.innerRadius - visualOffset
    val strokeWidth = visualOuterRadius - visualInnerRadius
    val totalAngles = mutableListOf<Pair<Float, Float>>()
    val gapAngle = (gapSize / obstacle.innerRadius) * (180f / PI.toFloat())

    for (gap in obstacle.gaps) {
        val gapStart = gap
        var gapEnd = (gapStart + gapAngle) % 360f
        if (gapEnd > 360f) gapEnd = 360f
        totalAngles.add(gapStart to gapEnd)
    }

    val filledAngles = mutableListOf<Pair<Float, Float>>()
    var currentStartAngle = 0f

    val sortedGaps = totalAngles.sortedBy { it.first }

    for ((startAngle, endAngle) in sortedGaps) {
        if (currentStartAngle < startAngle) {
            filledAngles.add(currentStartAngle to startAngle)
        }
        currentStartAngle = endAngle
    }

    if (currentStartAngle < 360f) {
        filledAngles.add(currentStartAngle to 360f)
    }

    for ((startAngle, endAngle) in filledAngles) {
        drawArc(
            color = obstacle.color,
            startAngle = startAngle,
            sweepAngle = endAngle - startAngle,
            useCenter = false,
            // Użycie visualOuterRadius do określenia rozmiaru i pozycji
            topLeft = Offset(obstacle.center.x - visualOuterRadius, obstacle.center.y - visualOuterRadius),
            size = androidx.compose.ui.geometry.Size(visualOuterRadius * 2, visualOuterRadius * 2),
            style = Stroke(width = strokeWidth)
        )
    }


    // WIZUALNA KOREKTA PRZESUNIĘCIA TEKSTU
    for (g in obstacle.gapEffects) {
        val midAngleDeg = g.midAngle
        val textRadius = (visualInnerRadius + visualOuterRadius) / 2f
        val textAngleRad = Math.toRadians(midAngleDeg.toDouble())
        val textX = obstacle.center.x + (textRadius * cos(textAngleRad)).toFloat()
        val textY = obstacle.center.y + (textRadius * sin(textAngleRad)).toFloat()

        val paint = android.graphics.Paint().apply {
            textAlign = android.graphics.Paint.Align.CENTER
            textSize = 40f
            color = android.graphics.Color.WHITE
            isAntiAlias = true
        }

        val Y_OFFSET_CORRECTION = -5f // Przesunięcie tekstu o 5px do góry (w stronę środka luki)

        drawContext.canvas.nativeCanvas.apply {
            save()
            translate(textX, textY)
            rotate(midAngleDeg + 90f)
            val yCentered = - (paint.descent() + paint.ascent()) / 2f

            drawText(g.effect.label, 0f, yCentered + Y_OFFSET_CORRECTION, paint)

            restore()
        }
    }
}