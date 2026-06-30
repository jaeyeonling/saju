package io.github.jaeyeonling.saju

import io.github.jaeyeonling.saju.domain.SajuChart
import io.kotest.assertions.withClue
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

private const val BEIJING_OFFSET = 8.0

private fun assertChartMatches(row: List<String>) {
    val (year, month, day, hour, minute) = row.take(5).map { it.toInt() }
    val mine: SajuChart = Saju.fromLocalDateTime(year, month, day, hour, minute, BEIJING_OFFSET)
    val tag = "$year-$month-$day $hour:$minute"

    withClue("연주 @ $tag") { mine.year.ganji.index shouldBe row[5].toInt() }
    withClue("월주 @ $tag") { mine.month.ganji.index shouldBe row[6].toInt() }
    withClue("일주 @ $tag") { mine.day.ganji.index shouldBe row[7].toInt() }
    withClue("시주 @ $tag") { mine.hour.ganji.index shouldBe row[8].toInt() }
}

/**
 * 4기둥 도출 골든 회귀 — facade [Saju.fromLocalDateTime] 를 동결된 골든 벡터와 대조한다.
 *
 * 골든 벡터는 한국 보정 없이 베이징 시각 기준으로 계산된 정답이므로, facade 도 `utOffset=8.0`(베이징)으로 맞춘다.
 * 한국 보정(진태양시·자시 학파)은 별도 검증한다. 표본은 자시(23·0시) 경계를 피한다.
 */
class SajuGoldenTest : StringSpec({

    "1900~2050 대표·매년 표본 4기둥이 골든 벡터와 일치" {
        val rows = Golden.rows("saju_pillars.csv")
        for (row in rows) assertChartMatches(row)
        println("4기둥 골든 대조 ${rows.size} 표본 통과")
    }
})
