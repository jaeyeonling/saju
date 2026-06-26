package io.github.jaeyeonling.saju.astronomy

import kotlin.math.abs

/**
 * 24절기 절입 시각 — 태양 겉보기 황경이 `15°·termIndex` 가 되는 순간을 뉴턴 반복으로 역산한다.
 *
 * **termIndex 규약** (황경 기준, 0°부터 15°씩):
 * - 짝수 = 중기(中氣): 0=춘분, 2=곡우, … 18=동지, …
 * - 홀수 = 절(節): 1=청명, 3=입하, … **21=입춘(315°)**, 23=경칩(345°)
 *
 * 월주 경계는 12절(홀수 termIndex), 연주 경계는 입춘(21)이다 — 그 매핑은 derivation 레이어가 한다.
 * 이 객체는 **순수 천문 시각만** 반환한다(타임존 무지). UT 변환에 [DeltaT] 를 적용한다.
 */
internal object SolarLongitude {
    /** 태양 평균 각속도 (rad/day) — 뉴턴 보정 스텝의 분모. */
    private const val MEAN_ANGULAR_SPEED = TWO_PI / 365.2422
    private const val MAX_ITERATIONS = 8
    private const val CONVERGENCE_RAD = 1e-10
    private const val DEGREES_PER_TERM = 15.0

    /** [year]년 1월 1일 이후 처음으로 황경이 `15°·termIndex` 가 되는 TT 율리우스일. */
    fun solarTermInstantTT(year: Int, termIndex: Int): Double {
        val target = normalizeRadians(termIndex * DEGREES_PER_TERM * DEG_TO_RAD)
        var jde = JulianDayConverter.fromGregorian(year, 1, 1, 0.0)

        // 1월 1일 황경에서 target 까지 평균 속도로 전진 — 그 해 첫 도달점을 근사한다.
        val forward = normalizeRadians(target - SunPosition.apparentLongitudeRad(jde))
        jde += forward / MEAN_ANGULAR_SPEED

        // 뉴턴 정밀화: Δλ 를 평균 각속도로 나눠 보정(±π 경계는 wrapToPi 로 안전).
        repeat(MAX_ITERATIONS) {
            val delta = wrapToPi(target - SunPosition.apparentLongitudeRad(jde))
            jde += delta / MEAN_ANGULAR_SPEED
            if (abs(delta) < CONVERGENCE_RAD) return jde
        }
        return jde
    }

    /**
     * 절입 시각을 **UT 율리우스일**로 반환 (TT − ΔT). 상위 레이어가 여기에 +9h 해서 KST 로 바꾼다.
     * 베이징/KST 하드코딩 없음 — 순수 UT.
     */
    fun solarTermInstantUT(year: Int, termIndex: Int): Double {
        val tt = solarTermInstantTT(year, termIndex)
        val decimalYear = 2000.0 + (tt - J2000_EPOCH) / 365.25
        return tt - DeltaT.seconds(decimalYear) / SECONDS_PER_DAY
    }

    private const val SECONDS_PER_DAY = 86_400.0
}
