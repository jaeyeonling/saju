// 달 위치 급수의 계수(Meeus Ch.47)는 한 줄로 둬야 공식 대조가 쉬워 max-line-length 를 면제한다.
@file:Suppress("ktlint:standard:max-line-length")

package io.github.jaeyeonling.saju.astronomy

import kotlin.math.abs
import kotlin.math.sin

/**
 * 달의 지심 황경 — Meeus _Astronomical Algorithms_ Ch.47 (ELP2000 약식).
 *
 * 계수(ta 60항)는 classpath 리소스 `moon-elp-meeus47.txt`(commenthol/astronomia, MIT)에서 로드한다.
 * 삭(朔) 계산엔 황경만 필요하므로 위도(tb)·거리(Σr)는 함수로 구현하지 않는다(리소스엔 보존).
 *
 * [meanLongitudeRad] 는 date 평균분점 기준(章動 미포함), [apparentLongitudeRad] 는 +Δψ 한 겉보기 값이다.
 * 삭은 (달 − 태양) 황경차라, 태양도 +Δψ 겉보기를 쓰므로 章動이 양쪽에 들어가 일관된다.
 */
internal object MoonPosition {
    private const val RESOURCE = "/io/github/jaeyeonling/saju/astronomy/moon-elp-meeus47.txt"

    // [d, m, m', f, Σl]
    private val longitudeTerms: Array<DoubleArray> = load()

    /** date 평균분점 기준 달 황경 (radian, `[0, 2π)`). 章動 미포함. */
    fun meanLongitudeRad(jdeTT: Double): Double = normalizeRadians(meanLongitudeRaw(jdeTT))

    /** 겉보기 달 황경 (radian, `[0, 2π)`). 평균황경 + 章動 Δψ. */
    fun apparentLongitudeRad(jdeTT: Double): Double =
        normalizeRadians(meanLongitudeRaw(jdeTT) + Nutation.longitudeRad(jdeTT))

    private fun meanLongitudeRaw(jdeTT: Double): Double {
        val t = julianCenturies(jdeTT)
        val meanLongitude = horner(t, 218.3164477, 481267.88123421, -0.0015786, 1.0 / 538841.0, -1.0 / 65194000.0) * DEG_TO_RAD
        val elongation = horner(t, 297.8501921, 445267.1114034, -0.0018819, 1.0 / 545868.0, -1.0 / 113065000.0) * DEG_TO_RAD
        val anomalySun = horner(t, 357.5291092, 35999.0502909, -0.0001536, 1.0 / 24490000.0) * DEG_TO_RAD
        val anomalyMoon = horner(t, 134.9633964, 477198.8675055, 0.0087414, 1.0 / 69699.0, -1.0 / 14712000.0) * DEG_TO_RAD
        val argLatitude = horner(t, 93.272095, 483202.0175233, -0.0036539, -1.0 / 3526000.0, 1.0 / 863310000.0) * DEG_TO_RAD
        val a1 = (119.75 + 131.849 * t) * DEG_TO_RAD
        val a2 = (53.09 + 479264.29 * t) * DEG_TO_RAD
        val eccentricity = horner(t, 1.0, -0.002516, -0.0000074)
        val eccentricitySquared = eccentricity * eccentricity

        // 가법항(행성 섭동) — 테이블 밖 별도 항.
        var sigmaL = 3958.0 * sin(a1) + 1962.0 * sin(meanLongitude - argLatitude) + 318.0 * sin(a2)
        for (row in longitudeTerms) {
            val argument = elongation * row[0] + anomalySun * row[1] + anomalyMoon * row[2] + argLatitude * row[3]
            // 태양 이심률 보정: |m|=1 → ×E, |m|=2 → ×E², m=0 → 미적용.
            val eccentricityFactor =
                when (abs(row[1].toInt())) {
                    1 -> eccentricity
                    2 -> eccentricitySquared
                    else -> 1.0
                }
            sigmaL += row[4] * sin(argument) * eccentricityFactor
        }
        return meanLongitude + sigmaL * MICRO_DEGREE * DEG_TO_RAD
    }

    private fun load(): Array<DoubleArray> {
        val stream =
            MoonPosition::class.java.getResourceAsStream(RESOURCE)
                ?: error("달 ELP 리소스를 찾을 수 없습니다: $RESOURCE")
        val terms = mutableListOf<DoubleArray>()
        stream.bufferedReader().useLines { lines ->
            lines.forEach { line ->
                if (line.isBlank() || line.startsWith("#")) return@forEach
                val parts = line.split(' ')
                if (parts[0] != "L") return@forEach // 황경(L)항만 — 위도(B)는 삭에 불필요
                terms.add(
                    doubleArrayOf(
                        parts[1].toDouble(),
                        parts[2].toDouble(),
                        parts[3].toDouble(),
                        parts[4].toDouble(),
                        parts[5].toDouble(),
                    ),
                )
            }
        }
        return terms.toTypedArray()
    }

    private const val MICRO_DEGREE = 1e-6
}
