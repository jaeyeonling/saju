package io.github.jaeyeonling.saju.astronomy

import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

/**
 * 균시차(均時差, Equation of Time) — 진태양시와 평균태양시의 차이(분).
 *
 * Meeus _Astronomical Algorithms_ Ch.28. 부호 관습은 NOAA 와 동일하게
 * **진태양시 = 평균태양시 + EoT** 가 되도록 [minutes] 가 반환한다
 * (11월 초 ≈ +16분, 2월 중순 ≈ −14분).
 *
 * 한국 진태양시 보정에서 경도보정(평균태양시)에 이 값을 더해 실제 태양시를 얻는다.
 */
internal object EquationOfTime {
    /** [jdeTT] 에서 균시차 (분). 진태양시 = 평균태양시 + 반환값. */
    fun minutes(jdeTT: Double): Double {
        val tau = julianMillennia(jdeTT)
        val meanLongitudeDeg = normalizeDegrees(meanLongitudeDeg(tau))
        val apparentLongitude = SunPosition.apparentLongitudeRad(jdeTT)
        val obliquity = ObliquityOfEcliptic.trueRad(jdeTT)
        val nutationLongitudeDeg = Nutation.longitudeRad(jdeTT) * RAD_TO_DEG

        // 겉보기 적경 α (Meeus 25.6).
        val rightAscensionDeg =
            normalizeDegrees(
                atan2(cos(obliquity) * sin(apparentLongitude), cos(apparentLongitude)) * RAD_TO_DEG,
            )

        // Meeus 28.1: E = L0 − 0.0057183° − α + Δψ·cos ε   (deg). 이미 NOAA 관습(apparent−mean)과 동부호.
        val meeusE = meanLongitudeDeg - FK5_CONSTANT_DEG - rightAscensionDeg + nutationLongitudeDeg * cos(obliquity)
        return wrapDeg180(meeusE) * MINUTES_PER_DEGREE
    }

    /** 태양 평균황경 L0 (도). Meeus 25.2. */
    private fun meanLongitudeDeg(tau: Double): Double =
        horner(
            tau,
            280.4664567,
            360007.6982779,
            0.03032028,
            1.0 / 49931.0,
            -1.0 / 15300.0,
            -1.0 / 2000000.0,
        )

    private fun wrapDeg180(deg: Double): Double {
        val normalized = normalizeDegrees(deg)
        return if (normalized > HALF_CIRCLE) normalized - FULL_CIRCLE else normalized
    }

    private const val FK5_CONSTANT_DEG = 0.0057183
    private const val MINUTES_PER_DEGREE = 4.0
    private const val FULL_CIRCLE = 360.0
    private const val HALF_CIRCLE = 180.0
}
