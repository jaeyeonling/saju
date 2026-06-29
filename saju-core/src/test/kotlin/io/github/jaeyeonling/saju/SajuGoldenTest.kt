package io.github.jaeyeonling.saju

import com.tyme.sixtycycle.SixtyCycleHour
import com.tyme.solar.SolarTime
import io.github.jaeyeonling.saju.domain.SajuChart
import io.kotest.assertions.withClue
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

private const val BEIJING_OFFSET = 8.0

private val SAMPLES = listOf(
    intArrayOf(1990, 3, 15, 7, 0),
    intArrayOf(2000, 1, 1, 12, 0),
    intArrayOf(2026, 6, 26, 14, 30),
    intArrayOf(1984, 2, 5, 10, 0),
    intArrayOf(1955, 5, 15, 9, 0),
)

// (month, day, hour) — 계절별로 흩뿌리고 절기 경계·자시는 피한다.
private val YEARLY_SAMPLES = listOf(
    intArrayOf(3, 10, 8),
    intArrayOf(6, 12, 14),
    intArrayOf(9, 8, 18),
    intArrayOf(11, 25, 4),
)

private fun assertChartMatches(year: Int, month: Int, day: Int, hour: Int, minute: Int) {
    val solarTime = SolarTime.fromYmdHms(year, month, day, hour, minute, 0)
    val tyme = SixtyCycleHour.fromSolarTime(solarTime)
    val mine: SajuChart = Saju.fromLocalDateTime(year, month, day, hour, minute, BEIJING_OFFSET)
    val tag = "$year-$month-$day $hour:$minute"

    withClue("연주 @ $tag") { mine.year.ganZhi.index shouldBe tyme.year.index }
    withClue("월주 @ $tag") { mine.month.ganZhi.index shouldBe tyme.month.index }
    withClue("일주 @ $tag") { mine.day.ganZhi.index shouldBe tyme.day.index }
    withClue("시주 @ $tag") { mine.hour.ganZhi.index shouldBe tyme.sixtyCycle.index }
}

/**
 * 4기둥 도출 골든 회귀 — facade [Saju.fromLocalDateTime] 를 tyme4j 와 대조한다.
 *
 * tyme4j 는 한국 보정 없이 베이징 시각 기준으로 4기둥을 계산하므로, 내 facade 도 `utOffset=8.0`(베이징)으로 맞춘다.
 * 한국 보정(진태양시·자시 학파)은 P4에서 별도 검증한다. 여기선 자시(23·0시) 경계도 일단 제외한다.
 */
class SajuGoldenTest : StringSpec({

    "대표 출생 시각의 4기둥이 tyme4j 와 일치" {
        for (s in SAMPLES) {
            assertChartMatches(s[0], s[1], s[2], s[3], s[4])
        }
    }

    "1900~2050 매년 표본 4기둥이 tyme4j 와 일치" {
        // 각 연도 4개 시점(계절 분산), 자시 회피한 시각.
        var count = 0
        for (year in 1900..2050) {
            for (md in YEARLY_SAMPLES) {
                assertChartMatches(year, md[0], md[1], md[2], 30)
                count++
            }
        }
        println("4기둥 골든 대조 $count 표본 통과")
    }
})
