package io.github.jaeyeonling.saju.interpretation

import io.github.jaeyeonling.saju.domain.Cheongan
import io.github.jaeyeonling.saju.domain.Ganji
import io.github.jaeyeonling.saju.domain.Jiji
import io.github.jaeyeonling.saju.domain.JijiHiddenStems
import io.github.jaeyeonling.saju.domain.Ohaeng
import io.github.jaeyeonling.saju.domain.Pillar
import io.github.jaeyeonling.saju.domain.PillarPosition
import io.github.jaeyeonling.saju.domain.SajuChart
import io.kotest.assertions.withClue
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe

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
        Gongmang.of(Ganji.fromIndex(0)) shouldBe (Jiji.SUL to Jiji.HAE) // 갑자
        Gongmang.of(Ganji.fromIndex(5)) shouldBe (Jiji.SUL to Jiji.HAE) // 기사(같은 순)
        Gongmang.of(Ganji.fromIndex(10)) shouldBe (Jiji.SIN to Jiji.YU) // 갑술
    }

    "천간합 — 갑기합토 탐지" {
        val relations = StandardHapChungStrategy.detect(listOf(Cheongan.GAP, Cheongan.GI), emptyList())
        val hap = relations.filterIsInstance<HapChungRelation.CheonganHap>().single()
        hap.transformsTo shouldBe Ohaeng.TO
    }

    "지지 삼합 — 신자진 수국" {
        val relations = StandardHapChungStrategy.detect(emptyList(), listOf(Jiji.SIN, Jiji.JA, Jiji.JIN))
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
                year = Pillar(PillarPosition.YEAR, Ganji.fromIndex(0)),
                month = Pillar(PillarPosition.MONTH, Ganji.fromIndex(15)),
                day = Pillar(PillarPosition.DAY, Ganji.fromIndex(30)),
                hour = Pillar(PillarPosition.HOUR, Ganji.fromIndex(45)),
            )
        val distribution = OhaengDistribution.from(chart)
        distribution.counts.values.sum() shouldBe 8
    }

    "가중 오행 분포는 8글자 + 지장간을 더해 표면보다 크다" {
        // 표면 8글자는 그대로 두고, 4지지 지장간(본·중·여)을 추가 집계 — 숨은 오행을 드러낸다.
        val chart =
            SajuChart(
                year = Pillar(PillarPosition.YEAR, Ganji.fromIndex(0)),
                month = Pillar(PillarPosition.MONTH, Ganji.fromIndex(15)),
                day = Pillar(PillarPosition.DAY, Ganji.fromIndex(30)),
                hour = Pillar(PillarPosition.HOUR, Ganji.fromIndex(45)),
            )
        val surface = OhaengDistribution.from(chart)
        val weighted = OhaengDistribution.weighted(chart)
        val hiddenCount = chart.branches().sumOf { JijiHiddenStems.of(it).all().size }
        withClue("표면은 여전히 8 (회귀 가드)") { surface.counts.values.sum() shouldBe 8 }
        withClue("가중 = 8 + Σ지장간") { weighted.counts.values.sum() shouldBe 8 + hiddenCount }
        // 가중은 각 오행이 표면 이상이어야 한다(지장간은 더하기만 한다).
        Ohaeng.entries.forEach { o ->
            withClue("$o 가중 ≥ 표면") { (weighted.count(o) >= surface.count(o)).shouldBeTrue() }
        }
    }

    "리포트는 네 기둥의 십성·지장간·신살과 가중오행을 모두 채운다" {
        val chart =
            SajuChart(
                year = Pillar(PillarPosition.YEAR, Ganji.fromIndex(0)),
                month = Pillar(PillarPosition.MONTH, Ganji.fromIndex(15)),
                day = Pillar(PillarPosition.DAY, Ganji.fromIndex(30)),
                hour = Pillar(PillarPosition.HOUR, Ganji.fromIndex(45)),
            )
        val report = Interpretation.of(chart)
        withClue("십성 4기둥") { report.sipSeong.keys shouldBe PillarPosition.entries.toSet() }
        withClue("지장간 4기둥") { report.hiddenStems.keys shouldBe PillarPosition.entries.toSet() }
        withClue("신살 4기둥") { report.sinSal.keys shouldBe PillarPosition.entries.toSet() }
        // 일주 천간 십성은 '나'라서 null, 본기 십성은 항상 존재.
        withClue("일간 자리 stem=null") { report.sipSeong.getValue(PillarPosition.DAY).stem shouldBe null }
        report.sipSeong.values.forEach { it.branchMain shouldNotBe null }
        // 지장간은 도메인 표와 일치, 가중오행은 표면과 다른 객체.
        report.hiddenStems.getValue(PillarPosition.DAY) shouldBe JijiHiddenStems.of(chart.day.ji)
        withClue("가중 합 ≥ 표면 합") {
            (report.ohaengWeighted.counts.values.sum() >= report.ohaeng.counts.values.sum()).shouldBeTrue()
        }
    }
})
