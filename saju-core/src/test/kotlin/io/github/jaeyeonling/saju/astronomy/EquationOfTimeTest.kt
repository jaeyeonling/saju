package io.github.jaeyeonling.saju.astronomy

import kotlin.test.Test
import kotlin.test.assertTrue

class EquationOfTimeTest {

    @Test
    fun `균시차 극값의 부호·크기가 천문 관측과 일치`() {
        // NOAA 관습(진태양시 = 평균 + EoT): 11월 초 ≈ +16.4분, 2월 중순 ≈ −14.2분.
        val nov = EquationOfTime.minutes(JulianDayConverter.fromGregorian(2026, 11, 3, 0.5))
        val feb = EquationOfTime.minutes(JulianDayConverter.fromGregorian(2026, 2, 11, 0.5))
        val may = EquationOfTime.minutes(JulianDayConverter.fromGregorian(2026, 5, 14, 0.5))

        println("EoT 11/3=$nov, 2/11=$feb, 5/14=$may")
        assertTrue(nov in 14.0..18.0, "11월 초 ≈ +16분 이어야: $nov")
        assertTrue(feb in -16.5..-12.0, "2월 중순 ≈ −14분 이어야: $feb")
        assertTrue(may in 2.0..5.5, "5월 중순 ≈ +3.7분 이어야: $may")
    }

    @Test
    fun `4개 영점 부근에서 균시차는 작다`() {
        // 균시차는 연 4회(4/15, 6/13, 9/1, 12/25 경) 0 을 지난다.
        val apr = EquationOfTime.minutes(JulianDayConverter.fromGregorian(2026, 4, 15, 0.5))
        assertTrue(kotlin.math.abs(apr) < 2.0, "4월 중순 균시차는 작아야: $apr")
    }
}
