package io.github.jaeyeonling.saju.domain

import io.kotest.assertions.withClue
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

/** 명리 규칙을 인덱스 산술로 구현한 enum 들의 정확성을 하드코딩 기대값으로 검증한다. */
class DomainArithmeticTest : StringSpec({

    "오행 상생 — 木火土金水 순환" {
        Ohaeng.MOK.generates() shouldBe Ohaeng.HWA
        Ohaeng.HWA.generates() shouldBe Ohaeng.TO
        Ohaeng.TO.generates() shouldBe Ohaeng.GEUM
        Ohaeng.GEUM.generates() shouldBe Ohaeng.SU
        Ohaeng.SU.generates() shouldBe Ohaeng.MOK
    }

    "오행 상극 — 木土水火金 순환" {
        Ohaeng.MOK.controls() shouldBe Ohaeng.TO
        Ohaeng.TO.controls() shouldBe Ohaeng.SU
        Ohaeng.SU.controls() shouldBe Ohaeng.HWA
        Ohaeng.HWA.controls() shouldBe Ohaeng.GEUM
        Ohaeng.GEUM.controls() shouldBe Ohaeng.MOK
    }

    "천간합 5쌍 — 갑기 을경 병신 정임 무계" {
        val expected = mapOf(
            Cheongan.GAB to Cheongan.GI,
            Cheongan.EUL to Cheongan.GYEONG,
            Cheongan.BYEONG to Cheongan.SIN,
            Cheongan.JEONG to Cheongan.IM,
            Cheongan.MU to Cheongan.GYE,
        )
        for ((a, b) in expected) {
            withClue("$a 의 천간합") { a.combinePartner() shouldBe b }
            withClue("$b 의 천간합(대칭)") { b.combinePartner() shouldBe a }
        }
    }

    "지지 육충 6쌍 — 자오 축미 인신 묘유 진술 사해" {
        val pairs = listOf(
            Jiji.JA to Jiji.O, Jiji.CHUK to Jiji.MI, Jiji.IN to Jiji.SHIN,
            Jiji.MYO to Jiji.YU, Jiji.JIN to Jiji.SUL, Jiji.SA to Jiji.HAE,
        )
        for ((a, b) in pairs) {
            withClue("$a 충") { a.opposite() shouldBe b }
            withClue("$b 충(대칭)") { b.opposite() shouldBe a }
        }
    }

    "지지 육합 6쌍 — 자축 인해 묘술 진유 사신 오미" {
        val pairs = listOf(
            Jiji.JA to Jiji.CHUK, Jiji.IN to Jiji.HAE, Jiji.MYO to Jiji.SUL,
            Jiji.JIN to Jiji.YU, Jiji.SA to Jiji.SHIN, Jiji.O to Jiji.MI,
        )
        for ((a, b) in pairs) {
            withClue("$a 육합") { a.sixCombinePartner() shouldBe b }
            withClue("$b 육합(대칭)") { b.sixCombinePartner() shouldBe a }
        }
    }

    "지지 육해 6쌍 — 자미 축오 인사 묘진 신해 유술" {
        val pairs = listOf(
            Jiji.JA to Jiji.MI, Jiji.CHUK to Jiji.O, Jiji.IN to Jiji.SA,
            Jiji.MYO to Jiji.JIN, Jiji.SHIN to Jiji.HAE, Jiji.YU to Jiji.SUL,
        )
        for ((a, b) in pairs) {
            withClue("$a 육해") { a.harmPartner() shouldBe b }
            withClue("$b 육해(대칭)") { b.harmPartner() shouldBe a }
        }
    }

    "60갑자 라운드트립 — fromIndex(i)_index == i" {
        for (i in 0 until GanZhi.CYCLE) {
            withClue("60갑자 index $i") { GanZhi.fromIndex(i).index shouldBe i }
        }
        GanZhi.ALL.size shouldBe GanZhi.CYCLE
    }

    "60갑자 경계 — 0=갑자 59=계해, 순환" {
        GanZhi.fromIndex(0) shouldBe GanZhi(Cheongan.GAB, Jiji.JA)
        GanZhi.fromIndex(59) shouldBe GanZhi(Cheongan.GYE, Jiji.HAE)
        // 60 순환, 음수 역행
        GanZhi.fromIndex(60) shouldBe GanZhi.fromIndex(0)
        GanZhi.fromIndex(0).next(-1) shouldBe GanZhi.fromIndex(59)
    }
})
