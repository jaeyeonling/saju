// 전략 케이스 테이블에 케이스별 명리 주석을 인라인으로 단다(의도된 가독성 패턴).
@file:Suppress("ktlint:standard:discouraged-comment-location")

package io.github.jaeyeonling.saju.interpretation

import io.github.jaeyeonling.saju.domain.Cheongan
import io.github.jaeyeonling.saju.domain.GanZhi
import io.github.jaeyeonling.saju.domain.Jiji
import io.github.jaeyeonling.saju.domain.JijiHiddenStems
import io.github.jaeyeonling.saju.domain.Pillar
import io.github.jaeyeonling.saju.domain.PillarPosition
import io.github.jaeyeonling.saju.domain.SajuChart
import io.kotest.assertions.withClue
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.shouldBe

class StrategyTest : StringSpec({

    "십이운성 120조합이 골든 벡터와 일치 (음포태)" {
        for (row in Golden.rows("terrain.csv")) {
            val stem = row[0].toInt()
            val branch = row[1].toInt()
            val mine = EumPotaeStrategy.stageOf(Cheongan.entries[stem], Jiji.entries[branch])
            withClue("천간 $stem 지지 $branch 십이운성") { mine.ordinal shouldBe row[2].toInt() }
        }
    }

    "지장간 본기가 골든 벡터와 일치" {
        for (row in Golden.rows("hidden_main.csv")) {
            val branch = row[0].toInt()
            val mine = JijiHiddenStems.of(Jiji.entries[branch]).mainQi
            withClue("지지 $branch 본기") { mine.ordinal shouldBe row[1].toInt() }
        }
    }

    "신강신약·용신·격국은 결정론적이다" {
        // 같은 입력 → 같은 출력 (정답셋 없는 영역은 결정론성만 보장).
        val chart = sampleChart()
        val ctx = InterpretationContext.DEFAULT

        val strength1 = ctx.sinStrength.evaluate(chart)
        val strength2 = ctx.sinStrength.evaluate(chart)
        strength2 shouldBe strength1

        val yongsin = ctx.yongsin.derive(chart, strength1)
        ctx.yongsin.derive(chart, strength1) shouldBe yongsin

        val gyeokguk = ctx.gyeokguk.classify(chart)
        ctx.gyeokguk.classify(chart) shouldBe gyeokguk
        withClue("격국 이름: ${gyeokguk.type.koreanName}") { gyeokguk.type.koreanName.endsWith("격").shouldBeTrue() }
    }

    "신강신약 5단계가 모두 도달 가능하다 (JUNGHWA dead code 회귀)" {
        // 60갑자 조합으로 다양한 사주를 생성해 verdict 분포를 본다 — JUNGHWA 포함 5단계 모두 나와야 한다.
        val verdicts = mutableSetOf<SinStrengthVerdict>()
        for (y in 0 until 60 step 7) {
            for (mo in 0 until 60 step 11) {
                for (d in 0 until 60 step 13) {
                    for (h in 0 until 60 step 17) {
                        val chart =
                            SajuChart(
                                Pillar(PillarPosition.YEAR, GanZhi.fromIndex(y)),
                                Pillar(PillarPosition.MONTH, GanZhi.fromIndex(mo)),
                                Pillar(PillarPosition.DAY, GanZhi.fromIndex(d)),
                                Pillar(PillarPosition.HOUR, GanZhi.fromIndex(h)),
                            )
                        verdicts.add(BueokSinStrengthStrategy.DEFAULT.evaluate(chart).verdict)
                    }
                }
            }
        }
        withClue("5단계 verdict 모두 도달: $verdicts") { verdicts shouldBe SinStrengthVerdict.entries.toSet() }
    }

    "비겁·인성이 많으면 신강으로 판정된다" {
        // 일간 갑목, 주변에 목·수(비겁·인성) 가득 → 신강.
        val strongWood =
            SajuChart(
                year = Pillar(PillarPosition.YEAR, GanZhi(Cheongan.GAB, Jiji.JA)), // 갑자(목/수)
                month = Pillar(PillarPosition.MONTH, GanZhi(Cheongan.GAB, Jiji.IN)), // 갑인(목/목)
                day = Pillar(PillarPosition.DAY, GanZhi(Cheongan.GAB, Jiji.JA)), // 갑자
                hour = Pillar(PillarPosition.HOUR, GanZhi(Cheongan.GYE, Jiji.HAE)), // 계해(수/수)
            )
        val strength = BueokSinStrengthStrategy.DEFAULT.evaluate(strongWood)
        withClue("목 일간 + 목수 다수 → 신강이어야: ${strength.verdict} (${strength.supportRatio})") {
            strength.verdict.isStrong.shouldBeTrue()
        }
    }
})

private fun sampleChart() =
    SajuChart(
        year = Pillar(PillarPosition.YEAR, GanZhi.fromIndex(0)),
        month = Pillar(PillarPosition.MONTH, GanZhi.fromIndex(20)),
        day = Pillar(PillarPosition.DAY, GanZhi.fromIndex(40)),
        hour = Pillar(PillarPosition.HOUR, GanZhi.fromIndex(10)),
    )
