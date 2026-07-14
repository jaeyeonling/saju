package io.github.jaeyeonling.saju.serialization

import io.github.jaeyeonling.saju.Saju
import io.github.jaeyeonling.saju.interpretation.EokbuSinStrengthStrategy
import io.github.jaeyeonling.saju.interpretation.EokbuYongsinStrategy
import io.github.jaeyeonling.saju.korea.KoreanSaju
import io.kotest.assertions.withClue
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.doubles.plusOrMinus
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import kotlinx.serialization.decodeFromString

/**
 * trace DTO 직렬화 검증 — `...WithTrace` 결과의 JSON 라운드트립과 라벨 평탄화 규칙을 본다.
 * trace **값**의 정합성은 core/korea/interpretation 골든 테스트가 보증한다.
 */
class TraceSerializationTest : StringSpec({

    // 1990-03-15 07:00 서울 — 보정이 시주를 바꾸는 대표 표본(경오·기묘·기묘·정묘).
    val computation = KoreanSaju.fromCivilTimeWithTrace(1990, 3, 15, 7, 0)

    "KoreanChartComputation → JSON 라운드트립 — 단계·근거·중첩 구조 보존" {
        val json = computation.toJson()
        val decoded = sajuJson.decodeFromString<KoreanChartComputationDto>(json)

        withClue("보정 단계 4개(DST→자오선→경도→균시차)") {
            decoded.correction.steps.map { it.kind } shouldBe listOf("DST", "MERIDIAN", "LONGITUDE", "EOT")
        }
        withClue("총 보정량 보존") {
            decoded.correction.totalOffsetMinutes shouldBe
                (computation.correction.totalOffsetMinutes plusOrMinus 1e-9)
        }
        withClue("4기둥 간지 라벨") {
            decoded.core.chart.year.ganji.name shouldBe "경오"
            decoded.core.pillars.year.basis shouldContain "입춘"
        }
        withClue("천문 trace 보존") {
            decoded.core.astronomy.prevTerm.termIndex shouldBe
                computation.core.astronomy.prevTerm.termIndex
        }
        withClue("양력 입력은 lunarConversion null") { decoded.lunarConversion shouldBe null }
    }

    "음력 입력은 변환 근거가 직렬화된다" {
        val lunar = KoreanSaju.fromLunarCivilTimeWithTrace(1990, 2, 19, false, 7, 0)
        val decoded = sajuJson.decodeFromString<KoreanChartComputationDto>(lunar.toJson())
        val conversion = decoded.lunarConversion.shouldNotBeNull()
        conversion.calendarBasis shouldBe "KOREA"
        conversion.basis shouldContain "음력"
    }

    "DaeunTrace → JSON — 방향 라벨과 entries 보존" {
        val trace =
            Saju.daeunWithTrace(
                computation.correction.utJd,
                computation.core.chart.month.ganji,
                computation.core.chart.year.gan.eumyang,
                isMale = true,
            )
        val decoded = sajuJson.decodeFromString<DaeunTraceDto>(trace.toJson())
        decoded.direction shouldBe trace.direction.name
        decoded.directionKorean shouldBe (if (trace.direction.name == "FORWARD") "순행" else "역행")
        decoded.entries shouldHaveSize trace.entries.size
        decoded.startAgeBasis shouldContain "÷ 3"
    }

    "SinStrength contributions·YongsinResult decisionPath 가 DTO 에 실린다" {
        val strength = EokbuSinStrengthStrategy.DEFAULT.evaluate(computation.core.chart)
        val strengthDto = strength.toDto()
        withClue("contributions 평탄화(한글 라벨)") {
            strengthDto.contributions shouldHaveSize strength.contributions.size
            strengthDto.contributions.first().slotKorean shouldBe strength.contributions.first().slot.koreanName
        }
        strengthDto.supportScore shouldBe (strength.supportScore plusOrMinus 1e-9)
        strengthDto.totalScore shouldBe (strength.totalScore plusOrMinus 1e-9)

        val yongsin = EokbuYongsinStrategy.derive(computation.core.chart, strength)
        yongsin.toDto().decisionPath shouldBe yongsin.decisionPath
    }
})
