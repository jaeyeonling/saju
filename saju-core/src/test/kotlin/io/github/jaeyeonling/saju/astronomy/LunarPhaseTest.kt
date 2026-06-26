package io.github.jaeyeonling.saju.astronomy

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LunarPhaseTest {

    @Test
    fun `삭에서 달·태양 황경이 일치한다`() {
        // 삭의 정의: 달황경 = 태양황경.
        for (k in 300..320) {
            val tt = LunarPhase.newMoonInstantTT(k)
            val diff = wrapToPi(
                MoonPosition.apparentLongitudeRad(tt) - SunPosition.apparentLongitudeRad(tt),
            )
            assertTrue(abs(diff) < 1e-6, "k=$k 황경차 ${diff}rad")
        }
    }

    @Test
    fun `삭 간격은 약 29_53일이다`() {
        val a = LunarPhase.newMoonInstantUT(300)
        val b = LunarPhase.newMoonInstantUT(301)
        assertEquals(29.53, b - a, 0.5)
    }

    @Test
    fun `k=0 삭은 2000년 1월 6일 경이다`() {
        val date = JulianDayConverter.toGregorian(LunarPhase.newMoonInstantUT(0))
        assertEquals(2000, date.year)
        assertEquals(1, date.month)
        assertTrue(date.day in 6..7, "신월 날짜: ${date.month}/${date.day}")
    }

    private fun abs(x: Double) = if (x < 0) -x else x
}
