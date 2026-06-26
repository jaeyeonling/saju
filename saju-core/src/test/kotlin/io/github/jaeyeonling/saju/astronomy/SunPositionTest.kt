package io.github.jaeyeonling.saju.astronomy

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SunPositionTest {

    @Test
    fun `Meeus Example 25_b — 1992-10-13 0h TD 태양 겉보기 황경`() {
        // Meeus, Astronomical Algorithms 2판, Example 25.b.
        // full VSOP87 결과: 겉보기 황경 199°54′21.818″ (= 199.906060°).
        val jde = JulianDayConverter.fromGregorian(1992, 10, 13, dayFraction = 0.0)

        val longitudeDeg = SunPosition.apparentLongitudeRad(jde) * RAD_TO_DEG

        val expectedDeg = 199.0 + 54.0 / 60.0 + 21.818 / 3600.0
        // 章動·光行差·FK5 까지 포함해 1″ 이내 재현되어야 한다.
        assertEquals(expectedDeg, longitudeDeg, ONE_ARCSEC_DEG)
    }

    @Test
    fun `춘분점 부근 — 2000년 춘분 태양 황경은 0도 근처`() {
        // 2000-03-20 07:35 UTC 경 춘분(태양 황경 0°). TT≈UTC+64s 이지만 분 단위로 0° 근처면 충분.
        val jde = JulianDayConverter.fromGregorian(2000, 3, 20, dayFraction = (7.0 * 60 + 36) / 1440.0)
        val longitudeDeg = SunPosition.apparentLongitudeRad(jde) * RAD_TO_DEG
        // 0° 또는 360° 근처 (정규화 경계). 0.05° = 3′ 이내.
        val distanceToZero = minOf(longitudeDeg, 360.0 - longitudeDeg)
        assertTrue(distanceToZero < 0.05, "춘분 황경이 0°에서 너무 멀다: $longitudeDeg°")
    }

    @Test
    fun `황경은 시간에 따라 단조 증가한다 (하루 약 0_985도)`() {
        val jde0 = JulianDayConverter.fromGregorian(2026, 6, 26, 0.0)
        val lon0 = SunPosition.apparentLongitudeRad(jde0) * RAD_TO_DEG
        val lon1 = SunPosition.apparentLongitudeRad(jde0 + 1.0) * RAD_TO_DEG
        val dailyMotion = lon1 - lon0
        // 태양 평균 일주 운동 ≈ 0.985°/일 (근일점 부근 빠름, 원일점 느림).
        assertTrue(dailyMotion in 0.95..1.02, "일주 황경 변화가 비정상: $dailyMotion°/일")
    }

    private companion object {
        const val ONE_ARCSEC_DEG = 1.0 / 3600.0
    }
}
