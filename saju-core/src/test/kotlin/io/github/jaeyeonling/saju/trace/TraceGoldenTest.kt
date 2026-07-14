package io.github.jaeyeonling.saju.trace

import io.github.jaeyeonling.saju.Golden
import io.github.jaeyeonling.saju.Saju
import io.kotest.assertions.withClue
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.doubles.plusOrMinus
import io.kotest.matchers.shouldBe

private const val BEIJING_OFFSET = 8.0

/** JD 허용 오차 — 골든은 1e-8 일 단위로 동결, 대조는 1e-6 일(≈0.09초)로 여유를 둔다. */
private const val JD_TOLERANCE = 1e-6

/**
 * trace 골든 회귀 — [Saju.fromLocalDateTimeWithTrace]·[Saju.daeunWithTrace] 의 근거 필드를
 * 동결 벡터(trace_chart.csv·trace_daeun.csv, [TraceGoldenWriter] 로 생성)와 대조한다.
 *
 * basis **문자열까지** 대조한다 — TS 포트가 같은 CSV 를 읽으므로 언어 간 문자열 동등성이 보장된다.
 */
class TraceGoldenTest : StringSpec({

    "차트 trace 가 골든 벡터와 일치 (수치 + basis 문자열)" {
        val rows = Golden.rows("trace_chart.csv")
        for (row in rows) {
            val (y, mo, d, h, mi) = row.take(5).map { it.toInt() }
            val tag = "$y-$mo-$d $h:$mi"
            val c = Saju.fromLocalDateTimeWithTrace(y, mo, d, h, mi, row[5].toDouble())

            withClue("utJd @ $tag") { c.utJd shouldBe (row[6].toDouble() plusOrMinus JD_TOLERANCE) }
            withClue("황경 @ $tag") {
                c.astronomy.solarLongitudeDeg shouldBe (row[7].toDouble() plusOrMinus JD_TOLERANCE)
            }
            withClue("직전 절 @ $tag") {
                c.astronomy.prevTerm.termIndex shouldBe row[8].toInt()
                c.astronomy.prevTerm.utJd shouldBe (row[9].toDouble() plusOrMinus JD_TOLERANCE)
            }
            withClue("직후 절 @ $tag") {
                c.astronomy.nextTerm.termIndex shouldBe row[10].toInt()
                c.astronomy.nextTerm.utJd shouldBe (row[11].toDouble() plusOrMinus JD_TOLERANCE)
            }
            withClue("연 경계 @ $tag") {
                c.astronomy.yearBoundary.termIndex shouldBe row[12].toInt()
                c.astronomy.yearBoundary.utJd shouldBe (row[13].toDouble() plusOrMinus JD_TOLERANCE)
                c.astronomy.yearBoundary.isAfter shouldBe row[14].toBoolean()
            }
            withClue("4기둥 중간값 @ $tag") {
                c.pillars.year.sajuYear shouldBe row[15].toInt()
                c.pillars.month.monthOffset shouldBe row[16].toInt()
                c.pillars.month.wolduStartGan.ordinal shouldBe row[17].toInt()
                c.pillars.day.julianDayNumber shouldBe row[18].toLong()
                c.pillars.day.zishiAdvanced shouldBe row[19].toBoolean()
                c.pillars.hour.hourBranch.ordinal shouldBe row[20].toInt()
                c.pillars.hour.sijuStartGan.ordinal shouldBe row[21].toInt()
            }
            withClue("basis @ $tag") {
                c.pillars.year.basis shouldBe row[22]
                c.pillars.month.basis shouldBe row[23]
                c.pillars.day.basis shouldBe row[24]
                c.pillars.hour.basis shouldBe row[25]
            }
        }
        println("차트 trace 골든 대조 ${rows.size} 표본 통과")
    }

    "대운 trace 가 골든 벡터와 일치" {
        val rows = Golden.rows("trace_daeun.csv")
        for (row in rows) {
            val (y, mo, d, h, mi) = row.take(5).map { it.toInt() }
            val isMale = row[6].toBoolean()
            val tag = "$y-$mo-$d $h:$mi ${if (isMale) "남" else "여"}"
            val c = Saju.fromLocalDateTimeWithTrace(y, mo, d, h, mi, row[5].toDouble())
            val t = Saju.daeunWithTrace(c.utJd, c.chart.month.ganji, c.chart.year.gan.eumyang, isMale)

            withClue("방향 @ $tag") { t.direction.name shouldBe row[7] }
            withClue("절기 거리 @ $tag") { t.daysToTerm shouldBe (row[8].toDouble() plusOrMinus JD_TOLERANCE) }
            withClue("목표 절 @ $tag") {
                t.targetTerm.termIndex shouldBe row[9].toInt()
                t.targetTerm.utJd shouldBe (row[10].toDouble() plusOrMinus JD_TOLERANCE)
            }
            withClue("대운수 @ $tag") { t.startAge shouldBe row[11].toInt() }
            withClue("basis @ $tag") {
                t.directionBasis shouldBe row[12]
                t.startAgeBasis shouldBe row[13]
            }
        }
        println("대운 trace 골든 대조 ${rows.size} 표본 통과")
    }

    "무근거 파사드는 traced 결과와 동일 (위임 무결성)" {
        val rows = Golden.rows("trace_chart.csv")
        for (row in rows) {
            val (y, mo, d, h, mi) = row.take(5).map { it.toInt() }
            val plain = Saju.fromLocalDateTime(y, mo, d, h, mi, BEIJING_OFFSET)
            val traced = Saju.fromLocalDateTimeWithTrace(y, mo, d, h, mi, BEIJING_OFFSET)
            plain shouldBe traced.chart
            traced.pillars.year.ganji shouldBe traced.chart.year.ganji
            traced.pillars.month.ganji shouldBe traced.chart.month.ganji
            traced.pillars.day.ganji shouldBe traced.chart.day.ganji
            traced.pillars.hour.ganji shouldBe traced.chart.hour.ganji
        }
    }

    "천문 trace 불변식 — 직전 절 ≤ 출생 < 직후 절, 절은 홀수 termIndex" {
        val rows = Golden.rows("trace_chart.csv")
        for (row in rows) {
            val (y, mo, d, h, mi) = row.take(5).map { it.toInt() }
            val c = Saju.fromLocalDateTimeWithTrace(y, mo, d, h, mi, BEIJING_OFFSET)
            val tag = "$y-$mo-$d $h:$mi"
            withClue("절기 순서 @ $tag") {
                (c.astronomy.prevTerm.utJd <= c.utJd).shouldBeTrue()
                (c.utJd < c.astronomy.nextTerm.utJd).shouldBeTrue()
            }
            withClue("절(節)은 홀수 termIndex @ $tag") {
                c.astronomy.prevTerm.termIndex % 2 shouldBe 1
                c.astronomy.nextTerm.termIndex % 2 shouldBe 1
            }
        }
    }

    "대운 무근거 파사드는 traced entries 와 동일 (위임 무결성)" {
        val rows = Golden.rows("trace_daeun.csv")
        for (row in rows) {
            val (y, mo, d, h, mi) = row.take(5).map { it.toInt() }
            val isMale = row[6].toBoolean()
            val c = Saju.fromLocalDateTimeWithTrace(y, mo, d, h, mi, BEIJING_OFFSET)
            val plain = Saju.daeun(c.utJd, c.chart.month.ganji, c.chart.year.gan.eumyang, isMale)
            val traced = Saju.daeunWithTrace(c.utJd, c.chart.month.ganji, c.chart.year.gan.eumyang, isMale)
            plain shouldBe traced.entries
        }
    }
})
