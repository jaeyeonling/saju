package io.github.jaeyeonling.saju.astronomy

import io.github.jaeyeonling.saju.Saju
import io.github.jaeyeonling.saju.domain.SajuChart
import io.kotest.assertions.withClue
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

private const val KST_OFFSET = 9.0
private const val EPSILON_DAYS = 5.0 / 86_400.0 // 절입 ±5초
private const val MONTH_BRANCHES = 12

// 12절(節, 홀수 termIndex) = 월주 경계. 입춘(21)·경칩(23)·청명(1)·입하(3)·망종(5)·소서(7)·
// 입추(9)·백로(11)·한로(13)·입동(15)·대설(17)·소한(19).
private val JEOL_TERM_INDICES = intArrayOf(21, 23, 1, 3, 5, 7, 9, 11, 13, 15, 17, 19)

private fun sajuAtLocalJd(localJd: Double): SajuChart {
    val d = JulianDayConverter.toGregorian(localJd)
    return Saju.fromLocalDateTime(d.year, d.month, d.day, d.hour, d.minute, KST_OFFSET, second = d.second)
}

/**
 * 절기 절입 경계 불변식 — 외부 오라클 없이 검증 가능한 자기정합성.
 *
 * 절입 시각의 '정확성'(천문 절대값)은 SolarTermGoldenTest 가 외부 골든으로 검증한다.
 * 여기서는 그와 독립적으로, 절입 순간 ±5초에서 **월지가 정확히 한 칸 전진**하는지를 본다 —
 * 절입 시각 계산(solarTermInstantUT)과 월주 도출(황경 floor 양자화)이 같은 경계에서 정합해야 한다.
 * SajuGoldenTest 가 표본에서 의도적으로 피한 '가장 깨지기 쉬운 절입 경계'를 직접 찌르는 회귀다.
 */
class SolarTermBoundaryTest : StringSpec({

    "절(節) 절입 경계 ±5초에서 월지가 정확히 한 칸 전진한다" {
        var checked = 0
        for (year in intArrayOf(1950, 1990, 2024)) {
            for (termIndex in JEOL_TERM_INDICES) {
                val localJd = SolarLongitude.solarTermInstantUT(year, termIndex) + KST_OFFSET / 24.0
                val before = sajuAtLocalJd(localJd - EPSILON_DAYS)
                val after = sajuAtLocalJd(localJd + EPSILON_DAYS)
                val diff = after.month.ji.ordinal - before.month.ji.ordinal
                val step = (diff % MONTH_BRANCHES + MONTH_BRANCHES) % MONTH_BRANCHES
                withClue("$year termIndex=$termIndex 절입: 월지 ${before.month.ji} → ${after.month.ji}") {
                    step shouldBe 1
                }
                checked++
            }
        }
        println("절입 경계 월지 전환 $checked 건 검증(외부 오라클 무관 불변식)")
    }
})
