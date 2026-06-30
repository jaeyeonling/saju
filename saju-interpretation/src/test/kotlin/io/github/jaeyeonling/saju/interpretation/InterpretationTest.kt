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
import io.kotest.matchers.booleans.shouldBeFalse
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

    "일간 합화 가드가 파사드(Interpretation.of) 배선까지 적용된다" {
        // 기축·을해·갑인·경오(모두 동일 음양 간지). 일간 갑 + 연간 기 → 일간 낀 합(보류),
        // 월간 을 + 시간 경 → 비일간 합(정상 화). 일간 값(갑) 중복 없음.
        val chart =
            SajuChart(
                year = Pillar(PillarPosition.YEAR, Ganji(Cheongan.GI, Jiji.CHUK)),
                month = Pillar(PillarPosition.MONTH, Ganji(Cheongan.EUL, Jiji.HAE)),
                day = Pillar(PillarPosition.DAY, Ganji(Cheongan.GAP, Jiji.IN)),
                hour = Pillar(PillarPosition.HOUR, Ganji(Cheongan.GYEONG, Jiji.O)),
            )
        val haps = Interpretation.of(chart).hapChung.filterIsInstance<HapChungRelation.CheonganHap>()
        val gapGi = haps.single { setOf(it.a, it.b) == setOf(Cheongan.GAP, Cheongan.GI) }
        val eulGyeong = haps.single { setOf(it.a, it.b) == setOf(Cheongan.EUL, Cheongan.GYEONG) }
        withClue("일간 갑이 낀 갑기합은 합화 보류(null)") { gapGi.transformsTo shouldBe null }
        withClue("비일간 을경합은 정상 화(금)") { eulGyeong.transformsTo shouldBe Ohaeng.GEUM }
    }

    "일간 천간합 합화 가드 — 일간(위치)이 낀 합만 보류" {
        // dayMasterIndex 미지정(-1): 갑기합 → 토(화한다)
        StandardHapChungStrategy.detect(listOf(Cheongan.GAP, Cheongan.GI), emptyList())
            .filterIsInstance<HapChungRelation.CheonganHap>().single()
            .transformsTo shouldBe Ohaeng.TO
        // 일간이 idx0(갑): 갑기합은 일간이 끼어 합화 보류 → null
        StandardHapChungStrategy.detect(listOf(Cheongan.GAP, Cheongan.GI), emptyList(), dayMasterIndex = 0)
            .filterIsInstance<HapChungRelation.CheonganHap>().single()
            .transformsTo shouldBe null
        // 일간이 idx2(병, 합과 무관한 위치): 갑기합은 그대로 화 → 토
        StandardHapChungStrategy
            .detect(listOf(Cheongan.GAP, Cheongan.GI, Cheongan.BYEONG), emptyList(), dayMasterIndex = 2)
            .filterIsInstance<HapChungRelation.CheonganHap>().single()
            .transformsTo shouldBe Ohaeng.TO
    }

    "일간 값이 중복돼도 비일간 합은 정상 화한다 (위치 기반 — 값 기반 오탐 회귀 방지)" {
        // stems=[갑(연), 기(월), 갑(일=일간 idx2)]. 연갑+월기 합은 일주 안 낌 → 화(토). 월기+일갑 합은 일주 낌 → 보류.
        val haps =
            StandardHapChungStrategy
                .detect(listOf(Cheongan.GAP, Cheongan.GI, Cheongan.GAP), emptyList(), dayMasterIndex = 2)
                .filterIsInstance<HapChungRelation.CheonganHap>()
        haps.size shouldBe 2
        withClue("비일간 갑기합(연-월)은 정상 화(토)") { haps.count { it.transformsTo == Ohaeng.TO } shouldBe 1 }
        withClue("일간 갑기합(월-일)은 합화 보류(null)") { haps.count { it.transformsTo == null } shouldBe 1 }
    }

    "천간충 — 갑경충 탐지, 갑기는 충 아님(합)" {
        val chung = StandardHapChungStrategy.detect(listOf(Cheongan.GAP, Cheongan.GYEONG), emptyList())
        withClue("갑경충") {
            chung.filterIsInstance<HapChungRelation.CheonganChung>().single().let { rel ->
                setOf(rel.a, rel.b) shouldBe setOf(Cheongan.GAP, Cheongan.GYEONG)
            }
        }
        withClue("갑기는 합이지 충 아님") {
            StandardHapChungStrategy
                .detect(listOf(Cheongan.GAP, Cheongan.GI), emptyList())
                .any { it is HapChungRelation.CheonganChung }
                .shouldBeFalse()
        }
        withClue("무기(토)는 충 없음") {
            StandardHapChungStrategy
                .detect(listOf(Cheongan.MU, Cheongan.GI), emptyList())
                .any { it is HapChungRelation.CheonganChung }
                .shouldBeFalse()
        }
    }

    "지지 삼합 — 신자진 수국" {
        val relations = StandardHapChungStrategy.detect(emptyList(), listOf(Jiji.SIN, Jiji.JA, Jiji.JIN))
        val samhap = relations.filterIsInstance<HapChungRelation.JijiSamhap>().single()
        samhap.transformsTo shouldBe Ohaeng.SU
    }

    "지지 방합 4국 — 인묘진목·사오미화·신유술금·해자축수" {
        val expected =
            mapOf(
                listOf(Jiji.IN, Jiji.MYO, Jiji.JIN) to Ohaeng.MOK,
                listOf(Jiji.SA, Jiji.O, Jiji.MI) to Ohaeng.HWA,
                listOf(Jiji.SIN, Jiji.YU, Jiji.SUL) to Ohaeng.GEUM,
                listOf(Jiji.HAE, Jiji.JA, Jiji.CHUK) to Ohaeng.SU,
            )
        for ((members, ohaeng) in expected) {
            val banghap =
                StandardHapChungStrategy
                    .detect(emptyList(), members)
                    .filterIsInstance<HapChungRelation.JijiBanghap>()
                    .single()
            withClue("$members 방합") { banghap.transformsTo shouldBe ohaeng }
        }
    }

    "부분집합(2글자)은 방합·삼합을 만들지 않는다 — 반방합/반합 미모델링" {
        // 인묘(방합 부분 2글자)·인오(삼합 부분 2글자)는 완전 3집합이 아니라 성립 안 함.
        StandardHapChungStrategy.detect(emptyList(), listOf(Jiji.IN, Jiji.MYO)).let { rels ->
            withClue("인묘는 방합 아님") { rels.none { it is HapChungRelation.JijiBanghap }.shouldBeTrue() }
        }
        StandardHapChungStrategy.detect(emptyList(), listOf(Jiji.IN, Jiji.O)).let { rels ->
            withClue("인오는 삼합 아님") { rels.none { it is HapChungRelation.JijiSamhap }.shouldBeTrue() }
        }
    }

    "방합과 삼합은 글자가 겹치지 않아 동시 성립하지 않는다" {
        // 인오술(삼합 화) 은 방합 아님, 인묘진(방합 목) 은 삼합 아님
        val samhapBranches = listOf(Jiji.IN, Jiji.O, Jiji.SUL)
        StandardHapChungStrategy.detect(emptyList(), samhapBranches).let { rels ->
            withClue("인오술은 삼합만") { rels.any { it is HapChungRelation.JijiSamhap }.shouldBeTrue() }
            withClue("인오술은 방합 아님") { rels.any { it is HapChungRelation.JijiBanghap }.shouldBeFalse() }
        }
        val banghapBranches = listOf(Jiji.IN, Jiji.MYO, Jiji.JIN)
        StandardHapChungStrategy.detect(emptyList(), banghapBranches).let { rels ->
            withClue("인묘진은 방합만") { rels.any { it is HapChungRelation.JijiBanghap }.shouldBeTrue() }
            withClue("인묘진은 삼합 아님") { rels.any { it is HapChungRelation.JijiSamhap }.shouldBeFalse() }
        }
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
