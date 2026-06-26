package io.github.jaeyeonling.saju.korea

import io.github.jaeyeonling.saju.lunar.CalendarBasis
import io.github.jaeyeonling.saju.lunar.LunarConverter
import kotlin.test.Test
import kotlin.test.assertEquals

class LunarSajuTest {

    @Test
    fun `음력 입력 사주는 변환된 양력 입력과 동일하다`() {
        // 음력 생일 → 양력 변환(KOREA) 후 양력 입력과 같은 SajuChart 여야 한다.
        val solar = LunarConverter.toSolar(1990, 2, 19, isLeapMonth = false, basis = CalendarBasis.KOREA)

        val fromLunar = KoreanSaju.fromLunarCivilTime(1990, 2, 19, isLeapMonth = false, hour = 7, minute = 0)
        val fromSolar = KoreanSaju.fromCivilTime(solar.year, solar.month, solar.day, 7, 0)

        assertEquals(fromSolar, fromLunar)
    }

    @Test
    fun `음력 설날 2023-1-1 은 양력 1월 하순이다`() {
        // 2023 설날(음 1-1) = 양력 2023-01-22.
        val solar = LunarConverter.toSolar(2023, 1, 1, isLeapMonth = false, basis = CalendarBasis.KOREA)
        assertEquals(2023, solar.year)
        assertEquals(1, solar.month)
        assertEquals(22, solar.day)
    }

    @Test
    fun `윤달 입력도 사주판이 도출된다`() {
        // 2023 윤2월(중국기준이지만 한국도 동일) → 사주판 정상 도출.
        val chart = KoreanSaju.fromLunarCivilTime(2023, 2, 1, isLeapMonth = true, hour = 12, minute = 0)
        assertEquals(4, chart.pillars().size)
    }
}
