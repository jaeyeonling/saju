package io.github.jaeyeonling.saju.astronomy

import kotlin.math.PI

/**
 * 태양의 지심 겉보기 황경(apparent geocentric ecliptic longitude).
 *
 * 파이프라인(Jean Meeus _Astronomical Algorithms_ Ch.25, commenthol/astronomia `solar.js` 검증):
 * ```
 * 지구 일심 황경 L  →  +π (지심)  →  FK5 보정(-0.09033″)  →  章動 Δψ  →  光行差(-20.4898″/R)
 * ```
 * 모든 입력은 TT(역학시), 출력은 radian `[0, 2π)`. **타임존을 절대 모른다** — UT/KST 변환은 상위 레이어 책임.
 */
internal object SunPosition {
    /** FK5 좌표계 변환의 황경 상수항(-0.09033″). 태양 황위 β≈0 이라 β-의존항은 생략. */
    private const val FK5_LONGITUDE_CORRECTION = -0.09033 * ARCSEC_TO_RAD

    /** 연주광행차 상수(20.4898″). 실제 보정은 `-상수/R`. */
    private const val ABERRATION_ARCSEC = 20.4898

    /** [jdeTT] 의 태양 겉보기 황경 (radian, `[0, 2π)`). */
    fun apparentLongitudeRad(jdeTT: Double): Double {
        val tau = julianMillennia(jdeTT)
        val heliocentricLongitude = Vsop87Earth.longitudeRad(tau)
        val radiusAu = Vsop87Earth.radiusAu(tau)

        val geocentricLongitude = heliocentricLongitude + PI // 일심 → 지심
        val nutationLongitude = Nutation.longitudeRad(jdeTT)
        val aberration = -ABERRATION_ARCSEC * ARCSEC_TO_RAD / radiusAu

        return normalizeRadians(
            geocentricLongitude + FK5_LONGITUDE_CORRECTION + nutationLongitude + aberration,
        )
    }
}
