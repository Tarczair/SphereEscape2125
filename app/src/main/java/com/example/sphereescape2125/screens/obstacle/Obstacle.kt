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
import kotlin.math.sqrt

/**
 * Reprezentuje fizyczną ścianę boczną wewnątrz luki (przerwy) w pierścieniu.
 *
 * Służy do obsługi kolizji, gdy gracz uderzy w bok otwarcia pierścienia.
 * Wektor normalny jest kluczowy do obliczenia kierunku odbicia piłki.
 *
 * @property start Punkt początkowy odcinka ściany (od strony wewnętrznej pierścienia).
 * @property end Punkt końcowy odcinka ściany (od strony zewnętrznej pierścienia).
 * @property normal Znormalizowany wektor prostopadły do ściany, wskazujący "wnętrze" przeszkody (kierunek odbicia).
 */
data class GapWall(
    val start: Offset,
    val end: Offset,
    val normal: Offset
)


data class VisualEffect(
    val text: String,
    val color: Color,
    var x: Float,
    var y: Float,
    var alpha: Float = 1f,
    var lifetime: Int = 45 // ok. 0.75 sekundy przy 60 FPS
)


/**
 * Typ efektu (modyfikatora), jaki gracz może otrzymać przelatując przez lukę.
 */
enum class EffectType {
    /** Modyfikuje liczbę ścian w następnych pierścieniach. */
    WALLS,
    /** Modyfikuje liczbę przerw (wyjść) w następnych pierścieniach. */
    GAPS,
    /** Dodaje lub odejmuje czas gry. */
    TIME,
    /** Modyfikuje wynik punktowy. */
    POINTS
}

/**
 * Operacja matematyczna wykonywana przez efekt.
 */
enum class Operation { ADD, SUB, MULTIPLY, DIVIDE }

/**
 * Stała szerokość luki w pikselach, używana do obliczeń geometrycznych.
 */
const val gapSize = 120f

/**
 * Model danych pojedynczego efektu (Buff/Debuff).
 *
 * @property operation Rodzaj operacji (np. mnożenie punktów, dodawanie czasu).
 * @property value Wartość liczbowa efektu.
 * @property label Etykieta tekstowa wyświetlana w grze (np. "⭐ x2").
 * @property type Kategoria efektu.
 */
data class RingEffect(
    val operation: Operation,
    val value: Float,
    val label: String,
    val type: EffectType
)

/**
 * Struktura wiążąca efekt z konkretną luką w pierścieniu.
 *
 * @property startAngle Kąt początkowy luki.
 * @property endAngle Kąt końcowy luki.
 * @property effect Efekt przypisany do tej luki.
 * @property midAngle Kąt środkowy luki (używany do pozycjonowania tekstu).
 */
data class GapWithEffect(
    val startAngle: Float,
    val endAngle: Float,
    val effect: RingEffect
) {
    val midAngle: Float
        get() = (startAngle + endAngle) / 2f
}

/**
 * Generuje losowy efekt (Buff lub Debuff) skalowany poziomem trudności.
 *
 * Algorytm:
 * 1. Losuje typ efektu.
 * 2. Określa czy efekt jest mnożnikiem (20% szans) czy modyfikatorem addytywnym.
 * 3. Określa czy jest to Debuff (70% szans) czy Buff (30% szans).
 * 4. Skaluje siłę efektu używając pierwiastka z numeru pierścienia ([ringCount]),
 * dzięki czemu gra staje się trudniejsza (lub bardziej ryzykowna) z czasem.
 *
 * @param ringCount Numer aktualnego pierścienia (poziom trudności).
 * @return Wygenerowany obiekt [RingEffect].
 */
fun generateRandomEffect(ringCount: Int): RingEffect {
    val effectType = EffectType.entries.random()
    val isMultiplier = Random.nextFloat() < 0.2f && effectType != EffectType.TIME
    val isDebuff = Random.nextFloat() < 0.7f

    // Skalowanie trudności: Im dalej, tym większe wartości dodawania/odejmowania
    val maxAddSub = 3 + (sqrt(ringCount.toFloat())).toInt().coerceAtMost(5)

    val value = when (effectType) {
        EffectType.WALLS, EffectType.GAPS ->
            if (isMultiplier) {
                Random.nextInt(2, 4).toFloat()
            } else {
                Random.nextInt(1, maxAddSub + 1).toFloat()
            }
        EffectType.TIME -> Random.nextInt(2, 6).toFloat()
        EffectType.POINTS ->
            if (isMultiplier) {
                Random.nextInt(2, 4).toFloat()
            } else {
                Random.nextInt(5, 21).toFloat()
            }
    }

    // Logika mapowania operacji na Buff/Debuff w zależności od typu
    val op = when (effectType) {
        EffectType.WALLS -> {
            // Dla ścian: Mnożenie/Dodawanie to utrudnienie (Debuff)
            if (isMultiplier) {
                if (isDebuff) Operation.MULTIPLY else Operation.DIVIDE
            } else {
                if (isDebuff) Operation.ADD else Operation.SUB
            }
        }
        EffectType.GAPS -> {
            // Dla przerw: Dzielenie/Odejmowanie to utrudnienie (mniej wyjść)
            if (isMultiplier) {
                if (isDebuff) Operation.DIVIDE else Operation.MULTIPLY
            } else {
                if (isDebuff) Operation.SUB else Operation.ADD
            }
        }
        EffectType.POINTS, EffectType.TIME -> {
            // Dla punktów/czasu: Dzielenie/Odejmowanie to kara
            if (isMultiplier) {
                if (isDebuff) Operation.DIVIDE else Operation.MULTIPLY
            } else {
                if (isDebuff) Operation.SUB else Operation.ADD
            }
        }
    }

    val finalValue = if (op == Operation.SUB) -value else value

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

    return RingEffect(
        operation = op,
        value = finalValue,
        label = "$icon $effectLabel",
        type = effectType
    )
}

/**
 * Główna klasa reprezentująca przeszkodę w postaci pierścienia.
 *
 * Odpowiada za:
 * 1. Przechowywanie geometrii (promienie, środek).
 * 2. Generowanie losowych luk (wyjść) w momencie inicjalizacji.
 * 3. Przypisywanie losowych efektów do każdej luki.
 *
 * @param center Środek pierścienia.
 * @param outerRadius Promień zewnętrzny.
 * @param innerRadius Promień wewnętrzny.
 * @param color Kolor pierścienia.
 * @param ringCount Numer porządkowy pierścienia (wpływa na trudność efektów).
 */
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
        // Obliczanie maksymalnej liczby wyjść w zależności od obwodu
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

/**
 * Oblicza geometrię ścian bocznych dla wszystkich luk w pierścieniu.
 *
 * Dla każdej luki generowane są dwa odcinki (ściany):
 * 1. Na początku luki (kąt startowy).
 * 2. Na końcu luki (kąt końcowy).
 *
 * Obliczane są również wektory normalne, które muszą "odpychać" gracza do wnętrza pierścienia,
 * a nie do wnętrza luki.
 *
 * @return Lista obiektów [GapWall] gotowa do detekcji kolizji.
 */
fun RingObstacle.generateGapWalls(): List<GapWall> {
    val walls = mutableListOf<GapWall>()
    val gapAngle = (gapSize / innerRadius) * (180f / PI.toFloat())

    for (gapStart in gaps) {
        val gapEnd = (gapStart + gapAngle) % 360f

        // Ściana 1 (Początek luki)
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

        // Ściana 2 (Koniec luki)
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



/**
 * Funkcja rysująca pierścień wraz z przerwami i etykietami efektów.
 *
 * Rysowanie odbywa się poprzez składanie łuków ([drawArc]) w miejscach, gdzie NIE ma przerw.
 * Dodatkowo funkcja wykorzystuje natywny Canvas Androida do narysowania obróconego tekstu
 * z etykietą efektu dokładnie w środku luki.
 *
 * @param obstacle Obiekt pierścienia do narysowania.
 */
fun DrawScope.drawRingWithGaps(obstacle: RingObstacle) {
    // Offset wizualny, aby collider był nieco "głębiej" niż grafika (lepsze odczucie gry)
    val visualOffset = 20f
    val visualOuterRadius = obstacle.outerRadius - visualOffset
    val visualInnerRadius = obstacle.innerRadius - visualOffset
    val strokeWidth = visualOuterRadius - visualInnerRadius
    val totalAngles = mutableListOf<Pair<Float, Float>>()
    val gapAngle = (gapSize / obstacle.innerRadius) * (180f / PI.toFloat())

    // 1. Przygotowanie listy kątów, gdzie są dziury
    for (gap in obstacle.gaps) {
        val gapStart = gap
        var gapEnd = (gapStart + gapAngle) % 360f
        if (gapEnd > 360f) gapEnd = 360f
        totalAngles.add(gapStart to gapEnd)
    }

    // 2. Odwrócenie logiki: Obliczenie kątów, gdzie JEST ściana (wypełnienie)
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

    // 3. Rysowanie łuków
    for ((startAngle, endAngle) in filledAngles) {
        drawArc(
            color = obstacle.color,
            startAngle = startAngle,
            sweepAngle = endAngle - startAngle,
            useCenter = false,
            topLeft = Offset(obstacle.center.x - visualOuterRadius, obstacle.center.y - visualOuterRadius),
            size = androidx.compose.ui.geometry.Size(visualOuterRadius * 2, visualOuterRadius * 2),
            style = Stroke(width = strokeWidth)
        )
    }

    // 4. Rysowanie tekstów efektów w lukach
    for (g in obstacle.gapEffects) {
        val midAngleDeg = g.midAngle
        val textRadius = (visualInnerRadius + visualOuterRadius) / 2f
        val textAngleRad = Math.toRadians(midAngleDeg.toDouble())
        val textX = obstacle.center.x + (textRadius * cos(textAngleRad)).toFloat()
        val textY = obstacle.center.y + (textRadius * sin(textAngleRad)).toFloat()

        // POBIERZ KOLOR Z MOTYWU (Ciemny w Light Mode, Biały w Dark Mode)
        val themeTextColor = android.graphics.Color.parseColor(
            if (obstacle.color == Color.Red) "#FFFFFF" else "#1A1C1E"
        )

        val paint = android.graphics.Paint().apply {
            textAlign = android.graphics.Paint.Align.CENTER
            textSize = 45f
            color = android.graphics.Color.WHITE
            isAntiAlias = true
            isFakeBoldText = true
            // ...ALE DODAJEMY MOCNY CIEŃ, który uratuje widoczność w Light Mode
            setShadowLayer(10f, 0f, 0f, android.graphics.Color.BLACK)
        }

        val Y_OFFSET_CORRECTION = -5f

        drawContext.canvas.nativeCanvas.apply {
            save()
            translate(textX, textY)
            // Obrót tekstu tak, aby był prostopadły do promienia (czytelny dla gracza)
            rotate(midAngleDeg + 90f)
            val yCentered = - (paint.descent() + paint.ascent()) / 2f

            drawText(g.effect.label, 0f, yCentered + Y_OFFSET_CORRECTION, paint)

            restore()
        }
    }
}