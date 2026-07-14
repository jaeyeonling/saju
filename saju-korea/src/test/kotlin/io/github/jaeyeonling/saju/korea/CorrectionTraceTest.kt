package io.github.jaeyeonling.saju.korea

import io.github.jaeyeonling.saju.korea.trace.CorrectionStepKind
import io.github.jaeyeonling.saju.lunar.LunarConverter
import io.kotest.assertions.withClue
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.doubles.plusOrMinus
import io.kotest.matchers.shouldBe

/** 분 단위 허용 오차 — 골든은 1e-8 로 동결, 대조는 1e-6 분(≈60μs)로 여유를 둔다. */
private const val MINUTE_TOLERANCE = 1e-6

/** `golden/<name>` 을 읽어 헤더 제외 각 행을 컬럼 리스트로 반환한다(saju-core Golden 과 동일 규약). */
private fun goldenRows(name: String): List<List<String>> {
    val stream =
        CorrectionTraceTest::class.java.getResourceAsStream("/golden/$name")
            ?: error("골든 리소스 없음: /golden/$name")
    return stream.bufferedReader().readLines()
        .asSequence()
        .filter { it.isNotBlank() }
        .drop(1)
        .map { line -> line.split(",") }
        .toList()
}

/**
 * 시간 보정 trace 검증 — 골든 회귀([CorrectionTraceGoldenWriter] 로 생성) + 구조 불변식.
 *
 * basis **문자열까지** 대조한다 — TS 포트가 같은 CSV 를 읽으므로 언어 간 문자열 동등성이 보장된다.
 */
class CorrectionTraceTest : StringSpec({

    "보정 trace 가 골든 벡터와 일치 (단계 분해 + basis 문자열)" {
        val rows = goldenRows("trace_correction.csv")
        for (row in rows) {
            val (y, mo, d, h, mi) = row.take(5).map { it.toInt() }
            val policy = TrueSolarTimePolicy.valueOf(row[6])
            val tag = "$y-$mo-$d $h:$mi ${row[6]}"
            val config = KoreanSajuConfig(trueSolarTime = policy)
            val trace = KoreanSaju.fromCivilTimeWithTrace(y, mo, d, h, mi, row[5].toDouble(), config).correction
            val deltas = trace.steps.associate { it.kind to it.deltaMinutes }
            val bases = trace.steps.associate { it.kind to it.basis }

            withClue("단계 델타 @ $tag") {
                deltas.getValue(CorrectionStepKind.DST) shouldBe (row[7].toDouble() plusOrMinus MINUTE_TOLERANCE)
                deltas.getValue(CorrectionStepKind.LONGITUDE) shouldBe
                    (row[8].toDouble() plusOrMinus MINUTE_TOLERANCE)
                deltas.getValue(CorrectionStepKind.EOT) shouldBe (row[9].toDouble() plusOrMinus MINUTE_TOLERANCE)
            }
            withClue("총 보정량 @ $tag") {
                trace.totalOffsetMinutes shouldBe (row[10].toDouble() plusOrMinus MINUTE_TOLERANCE)
            }
            withClue("보정 후 벽시계 @ $tag") {
                listOf(
                    trace.corrected.year, trace.corrected.month, trace.corrected.day,
                    trace.corrected.hour, trace.corrected.minute, trace.corrected.second,
                ) shouldBe row.subList(11, 17).map { it.toInt() }
            }
            withClue("utJd @ $tag") { trace.utJd shouldBe (row[17].toDouble() plusOrMinus MINUTE_TOLERANCE) }
            withClue("basis @ $tag") {
                bases.getValue(CorrectionStepKind.DST) shouldBe row[19]
                bases.getValue(CorrectionStepKind.MERIDIAN) shouldBe row[20]
                bases.getValue(CorrectionStepKind.LONGITUDE) shouldBe row[21]
                bases.getValue(CorrectionStepKind.EOT) shouldBe row[22]
            }
        }
        println("보정 trace 골든 대조 ${rows.size} 표본 통과")
    }

    "단계 델타 합 = 총 보정량 = trueSolarOffsetMinutes" {
        for (case in CASES) {
            val (y, mo, d, h, mi) = case.time
            val config = KoreanSajuConfig(trueSolarTime = case.policy)
            val trace = KoreanSaju.fromCivilTimeWithTrace(y, mo, d, h, mi, case.longitudeDeg, config).correction
            val tag = "${case.time} ${case.policy}"

            withClue("델타 합 = 총량 @ $tag") {
                trace.steps.sumOf { it.deltaMinutes } shouldBe
                    (trace.totalOffsetMinutes plusOrMinus MINUTE_TOLERANCE)
            }
            withClue("총량 = trueSolarOffsetMinutes @ $tag") {
                trace.totalOffsetMinutes shouldBe
                    (
                        KoreanSaju.trueSolarOffsetMinutes(y, mo, d, h, mi, case.longitudeDeg, case.policy)
                            plusOrMinus MINUTE_TOLERANCE
                    )
            }
        }
    }

    "단계는 항상 4개, 파이프라인 순서(DST → 자오선 → 경도 → 균시차)" {
        val trace = KoreanSaju.fromCivilTimeWithTrace(1990, 3, 15, 7, 0).correction
        trace.steps.map { it.kind } shouldBe
            listOf(
                CorrectionStepKind.DST,
                CorrectionStepKind.MERIDIAN,
                CorrectionStepKind.LONGITUDE,
                CorrectionStepKind.EOT,
            )
    }

    "무근거 파사드는 traced 결과와 동일 (위임 무결성)" {
        for (case in CASES) {
            val (y, mo, d, h, mi) = case.time
            val config = KoreanSajuConfig(trueSolarTime = case.policy)
            KoreanSaju.fromCivilTime(y, mo, d, h, mi, case.longitudeDeg, config) shouldBe
                KoreanSaju.fromCivilTimeWithTrace(y, mo, d, h, mi, case.longitudeDeg, config).core.chart
        }
    }

    "음력 진입점은 변환 근거를 채운다" {
        // 변환 자체는 LunarConverter 골든이 보증 — 여기선 trace 배선(변환 근거·양력 경로 동일성)만 본다.
        val solar = LunarConverter.toSolar(1990, 2, 19, false)
        val computation = KoreanSaju.fromLunarCivilTimeWithTrace(1990, 2, 19, false, 7, 0)
        val conversion = computation.lunarConversion ?: error("lunarConversion 이 채워져야 한다")
        withClue("양력 변환 결과") {
            conversion.solarYear shouldBe solar.year
            conversion.solarMonth shouldBe solar.month
            conversion.solarDay shouldBe solar.day
        }
        withClue("양력 경로와 동일 사주판") {
            computation.core.chart shouldBe KoreanSaju.fromCivilTime(solar.year, solar.month, solar.day, 7, 0)
        }
        withClue("양력 진입점은 lunarConversion 이 null") {
            KoreanSaju.fromCivilTimeWithTrace(1990, 3, 15, 7, 0).lunarConversion shouldBe null
        }
    }
})
