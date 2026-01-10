package com.example.sphereescape2125.screens

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import com.example.sphereescape2125.screens.obstacle.RingObstacle
import kotlin.math.*
import kotlin.random.Random

/**
 * Stała określająca bezpieczną szerokość luki (w pikselach) dla celów kolizji ścian.
 * Używana do obliczenia marginesu bezpieczeństwa, aby ściana nie została wygenerowana
 * zbyt blisko krawędzi wyjścia z pierścienia.
 */
const val COLLISION_GAP_SIZE = 120f

/**
 * Struktura danych reprezentująca przeszkodę w formie ściany (promienia).
 *
 * Ściana jest odcinkiem łączącym dwa pierścienie (lub znajdującym się pomiędzy nimi),
 * zdefiniowanym w układzie biegunowym (kąt i promienie graniczne).
 *
 * @property startRing Referencja do pierścienia wewnętrznego.
 * @property endRing Referencja do pierścienia zewnętrznego.
 * @property startRadius Promień początkowy ściany.
 * @property endRadius Promień końcowy ściany.
 * @property angle Kąt umieszczenia ściany (w stopniach, 0-360).
 * @property color Kolor przeszkody.
 */
data class WallObstacle(
    val startRing: RingObstacle,
    val endRing: RingObstacle,
    val startRadius: Float,
    val endRadius: Float,
    val angle: Float,
    val color: Color
)

/**
 * Sprawdza, czy proponowany kąt nowej ściany jest wystarczająco oddalony od istniejących ścian.
 *
 * Funkcja przelicza wymaganą odległość w pikselach na stopnie w oparciu o promień,
 * zgodnie ze wzorem na długość łuku: $L = \theta \cdot r$.
 *
 * @param newAngle Kąt nowej ściany.
 * @param used Lista zajętych już kątów.
 * @param minPx Minimalny wymagany odstęp w pikselach.
 * @param radius Promień, na którym dokonujemy sprawdzenia (zazwyczaj średni promień między pierścieniami).
 * @return `true` jeśli kąt jest bezpieczny, `false` jeśli jest zbyt blisko innej ściany.
 */
fun anglesFarEnoughPx(newAngle: Float, used: List<Float>, minPx: Float, radius: Float): Boolean {
    val minDeg = (minPx / radius) * (180f / PI.toFloat())
    return used.all { existing ->
        val diff = abs(newAngle - existing)
        val wrap = 360f - diff
        val smallest = min(diff, wrap)
        smallest >= minDeg
    }
}

/**
 * Weryfikuje, czy podany kąt nie koliduje z lukami (wyjściami) w pierścieniu.
 *
 * Zapobiega sytuacji, w której ściana zablokowałaby graczowi możliwość prześlizgnięcia się
 * przez otwór w pierścieniu. Uwzględnia margines bezpieczeństwa oraz cykliczność kątów (360 -> 0).
 *
 * @param angle Kąt umieszczenia ściany.
 * @param ring Pierścień, którego luki sprawdzamy.
 * @return `true` jeśli kąt wchodzi w światło luki (jest zablokowany), w przeciwnym razie `false`.
 */
fun isAngleBlockedByGaps(angle: Float, ring: RingObstacle): Boolean {
    val safetyMarginDeg = 8f
    val gapAngleWidth = (COLLISION_GAP_SIZE / ring.innerRadius) * (180f / PI.toFloat())

    for (gapStart in ring.gaps) {
        var gStart = gapStart - safetyMarginDeg
        var gEnd = gapStart + gapAngleWidth + safetyMarginDeg

        val angleNorm = if (angle < 0) angle + 360f else angle % 360f

        val inGap = if (gEnd > 360f) {
            angleNorm >= gStart || angleNorm <= (gEnd - 360f)
        } else if (gStart < 0f) {
            angleNorm >= (gStart + 360f) || angleNorm <= gEnd
        } else {
            angleNorm in gStart..gEnd
        }

        if (inGap) return true
    }
    return false
}

/**
 * Proceduralny generator ścian między zestawem pierścieni.
 *
 * Algorytm dzieli przestrzeń na sektory i próbuje wylosować pozycję ściany w każdym z nich.
 * Generuje różne typy ścian (pełne połączenia lub częściowe wypustki) w sposób losowy.
 *
 * @param rings Lista pierścieni w grze.
 * @param wallsPerGap Docelowa liczba ścian przypadająca na jeden sektor (gęstość).
 * @param color Kolor generowanych ścian.
 * @return Lista nowo utworzonych obiektów [WallObstacle].
 */
fun generateWallsBetweenRings(rings: List<RingObstacle>, wallsPerGap: Int, color: Color): List<WallObstacle> {
    val walls = mutableListOf<WallObstacle>()
    if (rings.size < 2) return walls

    for (i in 0 until rings.size - 1) {
        val current = rings[i]
        val next = rings[i + 1]
        val sectorSize = 360f / wallsPerGap
        val usedAngles = mutableListOf<Float>()

        repeat(wallsPerGap) { index ->
            val minAngle = (sectorSize * index)
            val maxAngle = minAngle + sectorSize

            var attempt = 0
            var added = false

            while(attempt < 15 && !added) {
                val baseAngle = Random.nextFloat() * (maxAngle - minAngle) + minAngle
                val avgR = (current.outerRadius + next.innerRadius) / 2f

                if (anglesFarEnoughPx(baseAngle, usedAngles, 150f, avgR) &&
                    !isAngleBlockedByGaps(baseAngle, current) &&
                    !isAngleBlockedByGaps(baseAngle, next)) {

                    val type = Random.nextInt(4)
                    val (startR, endR) = when (type) {
                        0, 1 -> current.outerRadius - 5f to next.innerRadius + 5f
                        2 -> current.outerRadius to (current.outerRadius + (next.innerRadius - current.outerRadius) / 3f) + 10
                        else -> next.innerRadius + 5f to (current.outerRadius + (next.innerRadius - current.outerRadius) / 1.5f) - 10f
                    }

                    walls.add(WallObstacle(current, next, startR, endR, baseAngle % 360f, color))
                    usedAngles.add(baseAngle)
                    added = true
                }
                attempt++
            }
        }
    }
    return walls
}

/**
 * Regeneruje ściany dla konkretnego poziomu (pomiędzy dwoma pierścieniami) w reakcji na wstrząs.
 *
 * Funkcja ta:
 * 1. Usuwa stare ściany między pierścieniem `ringIndex` a `ringIndex + 1`.
 * 2. Generuje nowy układ ścian.
 * 3. Gwarantuje, że nowa ściana nie pojawi się w miejscu, gdzie aktualnie znajduje się gracz ([safeMargin]).
 *
 * @param ringIndex Indeks wewnętrznego pierścienia, dla którego następuje przetasowanie.
 * @param rings Lista wszystkich pierścieni.
 * @param walls Referencja do modyfikowalnej listy ścian (StateList).
 * @param wallsPerGap Gęstość ścian.
 * @param color Kolor nowych ścian.
 * @param playerAngle Aktualny kąt położenia gracza (aby uniknąć spawnu na graczu).
 */
fun regenerateWallsForSpecificRing(
    ringIndex: Int,
    rings: List<RingObstacle>,
    walls: MutableList<WallObstacle>,
    wallsPerGap: Int,
    color: Color,
    playerAngle: Float
) {
    if (ringIndex < 0 || ringIndex >= rings.size - 1) return

    val current = rings[ringIndex]
    val next = rings[ringIndex + 1]

    val iterator = walls.iterator()
    while (iterator.hasNext()) {
        val w = iterator.next()
        if (w.startRing == current || w.endRing == next) {
            iterator.remove()
        }
    }

    val sectorSize = 360f / wallsPerGap
    val usedAngles = mutableListOf<Float>()
    val safeMargin = 40f

    repeat(wallsPerGap) { index ->
        val minA = (sectorSize * index)
        val maxA = minA + sectorSize
        var attempt = 0
        var found = false

        while (attempt < 20 && !found) {
            val angle = Random.nextFloat() * (maxA - minA) + minA
            val diff = abs(angle - playerAngle)
            val smallestDist = min(diff, 360f - diff)

            val blockedByGaps = isAngleBlockedByGaps(angle, current) || isAngleBlockedByGaps(angle, next)

            if (smallestDist > safeMargin && !blockedByGaps &&
                anglesFarEnoughPx(angle, usedAngles, 140f, current.outerRadius)) {

                walls.add(WallObstacle(
                    current, next,
                    current.outerRadius - 5f,
                    next.innerRadius + 5f,
                    angle % 360f,
                    color
                ))
                usedAngles.add(angle)
                found = true
            }
            attempt++
        }
    }
}

/**
 * Rysuje listę ścian na Canvasie.
 *
 * Konwertuje współrzędne biegunowe (kąt i promień) ścian na współrzędne kartezjańskie (X, Y)
 * wymagane przez funkcję [DrawScope.drawLine].
 *
 * @param walls Lista ścian do narysowania.
 */
fun DrawScope.drawWalls(walls: List<WallObstacle>) {
    val visualOffset = -8f
    for (wall in walls) {
        val angleRad = Math.toRadians(wall.angle.toDouble())
        val start = Offset(
            x = wall.startRing.center.x + (wall.startRadius - visualOffset) * cos(angleRad).toFloat(),
            y = wall.startRing.center.y + (wall.startRadius - visualOffset) * sin(angleRad).toFloat()
        )
        val end = Offset(
            x = wall.endRing.center.x + (wall.endRadius - visualOffset) * cos(angleRad).toFloat(),
            y = wall.endRing.center.y + (wall.endRadius - visualOffset) * sin(angleRad).toFloat()
        )
        drawLine(color = wall.color, start = start, end = end, strokeWidth = 50f)
    }
}