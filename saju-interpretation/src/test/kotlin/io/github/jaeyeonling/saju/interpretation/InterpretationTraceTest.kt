package io.github.jaeyeonling.saju.interpretation

import io.github.jaeyeonling.saju.domain.Ganji
import io.github.jaeyeonling.saju.domain.Pillar
import io.github.jaeyeonling.saju.domain.PillarPosition
import io.github.jaeyeonling.saju.domain.SajuChart
import io.kotest.assertions.withClue
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.collections.shouldNotBeEmpty
import io.kotest.matchers.doubles.plusOrMinus
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain

private const val SCORE_TOLERANCE = 1e-9

private fun chartOf(
    yearIdx: Int,
    monthIdx: Int,
    dayIdx: Int,
    hourIdx: Int,
): SajuChart =
    SajuChart(
        year = Pillar(PillarPosition.YEAR, Ganji.fromIndex(yearIdx)),
        month = Pillar(PillarPosition.MONTH, Ganji.fromIndex(monthIdx)),
        day = Pillar(PillarPosition.DAY, Ganji.fromIndex(dayIdx)),
        hour = Pillar(PillarPosition.HOUR, Ganji.fromIndex(hourIdx)),
    )

// 대표 표본 — 1990-03-15 07:00 서울(경오·기묘·기묘·정묘) 외 골든 사주판 인덱스.
private val SAMPLE_CHARTS =
    listOf(
        chartOf(6, 15, 15, 3),
        chartOf(15, 12, 54, 54),
        chartOf(42, 30, 7, 31),
        chartOf(0, 2, 5, 5),
    )

/**
 * 해석 trace 검증 — [SinStrength.contributions] 의 합산 무결성과
 * [YongsinResult.decisionPath] 의 구조를 본다. 판정·용신 **값** 자체는 기존 테스트가 보증한다.
 */
class InterpretationTraceTest : StringSpec({

    "contributions 합 = totalScore, 돕는 세력 합 = supportScore" {
        for (chart in SAMPLE_CHARTS) {
            val strength = EokbuSinStrengthStrategy.DEFAULT.evaluate(chart)
            val tag = chart.pillars().joinToString("·") { it.ganji.koreanName }

            withClue("전체 합 @ $tag") {
                strength.contributions.sumOf { it.weight } shouldBe
                    (strength.totalScore plusOrMinus SCORE_TOLERANCE)
            }
            withClue("돕는 세력(비겁·인성) 합 @ $tag") {
                strength.contributions
                    .filter { it.sipseongGroup == SipSeongGroup.BIGEOP || it.sipseongGroup == SipSeongGroup.INSEONG }
                    .sumOf { it.weight } shouldBe (strength.supportScore plusOrMinus SCORE_TOLERANCE)
            }
            withClue("ratio = support/total @ $tag") {
                strength.supportRatio shouldBe
                    (strength.supportScore / strength.totalScore plusOrMinus SCORE_TOLERANCE)
            }
            withClue("groupScores 재구성 @ $tag") {
                for ((group, score) in strength.groupScores) {
                    strength.contributions.filter { it.sipseongGroup == group }.sumOf { it.weight } shouldBe
                        (score plusOrMinus SCORE_TOLERANCE)
                }
            }
        }
    }

    "contributions 슬롯 구성 — 일간 자신은 제외, 지장간은 분야표대로" {
        val chart = SAMPLE_CHARTS.first()
        val strength = EokbuSinStrengthStrategy.DEFAULT.evaluate(chart)

        // 천간 슬롯은 일주 제외 3개.
        strength.contributions.count { it.slot == StrengthSlot.STEM } shouldBe 3
        withClue("일주 천간(나 자신)은 세력에 없어야") {
            strength.contributions.none {
                it.position == PillarPosition.DAY && it.slot == StrengthSlot.STEM
            }.shouldBeTrue()
        }
        // 본기는 네 기둥 모두.
        strength.contributions.count { it.slot == StrengthSlot.BRANCH_MAIN } shouldBe 4
        // 각 기여는 산식 basis 를 갖는다.
        strength.contributions.forEach { it.basis shouldContain "→" }
    }

    "contributions 는 기본 생성자에서 빈 리스트 (스키마 안정)" {
        val bare = SinStrength(supportRatio = 0.5, verdict = SinStrengthVerdict.JUNGHWA)
        bare.contributions shouldBe emptyList()
        bare.supportScore shouldBe 0.0
        bare.totalScore shouldBe 0.0
    }

    "억부 용신 decisionPath — 판정 → 분기 → 용신 3단계" {
        for (chart in SAMPLE_CHARTS) {
            val strength = EokbuSinStrengthStrategy.DEFAULT.evaluate(chart)
            val result = EokbuYongsinStrategy.derive(chart, strength)
            val tag = chart.pillars().joinToString("·") { it.ganji.koreanName }

            withClue("경로 3단계 @ $tag") { result.decisionPath.size shouldBe 3 }
            withClue("1단계 = 강약 판정 @ $tag") {
                result.decisionPath[0] shouldContain strength.verdict.koreanName
            }
            withClue("3단계 = 용신 결론 @ $tag") {
                result.decisionPath[2] shouldContain result.yongsin.koreanName
            }
        }
    }

    "조후·합성 용신 decisionPath — 경로가 채워지고 합성은 선택 단계를 앞에 붙인다" {
        for (chart in SAMPLE_CHARTS) {
            val strength = EokbuSinStrengthStrategy.DEFAULT.evaluate(chart)
            JohuYongsinStrategy.derive(chart, strength).decisionPath.shouldNotBeEmpty()

            val composite = CompositeYongsinStrategy().derive(chart, strength)
            withClue("합성 1단계 = 억부/조후 선택") {
                (composite.decisionPath.first().contains("조후") || composite.decisionPath.first().contains("억부"))
                    .shouldBeTrue()
            }
        }
    }
})
