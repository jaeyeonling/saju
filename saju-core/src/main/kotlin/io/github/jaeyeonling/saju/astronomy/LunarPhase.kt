package io.github.jaeyeonling.saju.astronomy

import kotlin.math.abs
import kotlin.math.round

/**
 * 삭(朔, 신월) 시각 — 달과 태양의 겉보기 황경이 같아지는 순간. 음력 월의 시작(초하루)이다.
 *
 * Meeus _Astronomical Algorithms_ Ch.49 평삭 초기추정 + (달황경 − 태양황경)=0 뉴턴 정밀화.
 * SolarLongitude 와 동일하게 TT 공간에서 역산하고 마지막에 UT 로 내린다.
 */
internal object LunarPhase {
    /** 삭망월(synodic month) 평균 길이(일). */
    private const val SYNODIC_MONTH = 29.530588861

    /** k=0 평삭의 율리우스일(TT) — 2000-01-06 경. (Meeus 49.1) */
    private const val NEW_MOON_EPOCH = 2451550.09766

    /** 달−태양 상대 평균 각속도 (rad/day). */
    private val RELATIVE_SPEED = TWO_PI / SYNODIC_MONTH

    private const val MAX_ITERATIONS = 8
    private const val CONVERGENCE_RAD = 1e-10
    private const val SECONDS_PER_DAY = 86_400.0
    private const val LUNATIONS_PER_CENTURY = 1236.85

    /** k 번째 삭의 TT 율리우스일. k=0 은 2000-01-06 경. */
    fun newMoonInstantTT(k: Int): Double {
        val t = k / LUNATIONS_PER_CENTURY
        // Meeus 49.1 평삭 초기추정.
        var jde =
            NEW_MOON_EPOCH + SYNODIC_MONTH * k +
                0.00015437 * t * t - 0.000000150 * t * t * t + 0.00000000073 * t * t * t * t

        // 뉴턴: 달황경 − 태양황경 = 0. wrapToPi 로 ±π 경계 안전.
        repeat(MAX_ITERATIONS) {
            val diff =
                wrapToPi(
                    MoonPosition.apparentLongitudeRad(jde) - SunPosition.apparentLongitudeRad(jde),
                )
            jde -= diff / RELATIVE_SPEED
            if (abs(diff) < CONVERGENCE_RAD) return jde
        }
        return jde
    }

    /** k 번째 삭의 UT 율리우스일 (TT − ΔT). */
    fun newMoonInstantUT(k: Int): Double {
        val tt = newMoonInstantTT(k)
        return tt - DeltaT.seconds(decimalYearOf(tt)) / SECONDS_PER_DAY
    }

    /** 주어진 UT 율리우스일에 가장 가까운 삭의 k. */
    fun newMoonIndexNear(utJd: Double): Int = round((utJd - NEW_MOON_EPOCH) / SYNODIC_MONTH).toInt()

    private fun decimalYearOf(jd: Double): Double = 2000.0 + (jd - J2000_EPOCH) / 365.25
}
