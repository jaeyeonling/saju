package io.github.jaeyeonling.saju.interpretation

import io.github.jaeyeonling.saju.domain.Cheongan
import io.github.jaeyeonling.saju.domain.GanZhi
import io.github.jaeyeonling.saju.domain.Jiji
import io.github.jaeyeonling.saju.domain.Ohaeng
import io.github.jaeyeonling.saju.domain.Pillar
import io.github.jaeyeonling.saju.domain.PillarPosition
import io.github.jaeyeonling.saju.domain.SajuChart
import io.kotest.assertions.withClue
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.shouldBe

class InterpretationTest : StringSpec({

    "십성 100조합이 골든 벡터와 일치" {
        for (row in Golden.rows("ten_star.csv")) {
            val day = row[0].toInt()
            val target = row[1].toInt()
            val mine = SipSeong.of(Cheongan.entries[day], Cheongan.entries[target])
            withClue("일간 $day 대상 $target 십성") { mine.ordinal shouldBe row[2].toInt() }
        }
    }

    "공망 — 갑자순 술해, 갑술순 신유" {
        Gongmang.of(GanZhi.fromIndex(0)) shouldBe (Jiji.SUL to Jiji.HAE) // 갑자
        Gongmang.of(GanZhi.fromIndex(5)) shouldBe (Jiji.SUL to Jiji.HAE) // 기사(같은 순)
        Gongmang.of(GanZhi.fromIndex(10)) shouldBe (Jiji.SHIN to Jiji.YU) // 갑술
    }

    "천간합 — 갑기합토 탐지" {
        val relations = StandardHapChungStrategy.detect(listOf(Cheongan.GAB, Cheongan.GI), emptyList())
        val hap = relations.filterIsInstance<HapChungRelation.CheonganHap>().single()
        hap.transformsTo shouldBe Ohaeng.TO
    }

    "지지 삼합 — 신자진 수국" {
        val relations = StandardHapChungStrategy.detect(emptyList(), listOf(Jiji.SHIN, Jiji.JA, Jiji.JIN))
        val samhap = relations.filterIsInstance<HapChungRelation.JijiSamhap>().single()
        samhap.transformsTo shouldBe Ohaeng.SU
    }

    "지지 육충·육합 탐지" {
        val chung = StandardHapChungStrategy.detect(emptyList(), listOf(Jiji.JA, Jiji.O))
        withClue("자오충") { chung.any { it is HapChungRelation.JijiYukchung }.shouldBeTrue() }
        val hap = StandardHapChungStrategy.detect(emptyList(), listOf(Jiji.JA, Jiji.CHUK))
        withClue("자축합") { hap.any { it is HapChungRelation.JijiYukhap }.shouldBeTrue() }
    }

    "오행 분포 합은 8글자" {
        val chart =
            SajuChart(
                year = Pillar(PillarPosition.YEAR, GanZhi.fromIndex(0)),
                month = Pillar(PillarPosition.MONTH, GanZhi.fromIndex(15)),
                day = Pillar(PillarPosition.DAY, GanZhi.fromIndex(30)),
                hour = Pillar(PillarPosition.HOUR, GanZhi.fromIndex(45)),
            )
        val distribution = OhaengDistribution.from(chart)
        distribution.counts.values.sum() shouldBe 8
    }
})
