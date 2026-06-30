package io.github.jaeyeonling.saju.group

import io.github.jaeyeonling.saju.domain.Cheongan
import io.github.jaeyeonling.saju.domain.Jiji
import io.github.jaeyeonling.saju.interpretation.SipSeong
import io.github.jaeyeonling.saju.interpretation.SipSeongGroup
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.shouldBe

/**
 * 십성 역할 합성 — test_synthesis.py 골든 이식.
 * 십성 카운트는 천간(연·월·시)+지지 본기(연·월·일·시)만 센다(R1: report.sipSeong 의 본·중·여 전부와 다름).
 */
class SipseongSynthesisTest : StringSpec({
    // 1990-03-15 07:00 (기 일간): 연 경오·월 기묘·일 기묘·시 정묘.
    val giChart =
        chartOf(
            Cheongan.GYEONG to Jiji.O,
            Cheongan.GI to Jiji.MYO,
            Cheongan.GI to Jiji.MYO,
            Cheongan.JEONG to Jiji.MYO,
        )

    "십성 카운트 골든 — 기 일간: 상관1·비견1·편인2·편관3 (README 예시 일치)" {
        val roles = SipseongSynthesis.analyze(listOf(memberOf("a", giChart)), GroupContext.DEFAULT)
        roles.groupDistribution[SipSeong.SANGGWAN] shouldBe 1
        roles.groupDistribution[SipSeong.BIGYEON] shouldBe 1
        roles.groupDistribution[SipSeong.PYEONIN] shouldBe 2
        roles.groupDistribution[SipSeong.PYEONGWAN] shouldBe 3
        roles.group5[SipSeongGroup.GWANSEONG] shouldBe 3
        roles.group5[SipSeongGroup.INSEONG] shouldBe 2
    }

    "비겁 최다 멤버는 주도·추진형(LEADER)" {
        // 갑인 4기둥: 천간 갑(비견)·지지 본기 갑(비견) → 전부 비겁.
        val roles =
            SipseongSynthesis.analyze(
                listOf(memberOf("a", uniformChart(Cheongan.GAP, Jiji.IN))),
                GroupContext.DEFAULT,
            )
        roles.roleComposition.getValue(GroupRole.LEADER) shouldContain "a"
    }

    "그룹 전체 인성 0이면 결핍 + 회고 트리거" {
        val roles =
            SipseongSynthesis.analyze(
                listOf(memberOf("a", uniformChart(Cheongan.GAP, Jiji.IN))),
                GroupContext.DEFAULT,
            )
        roles.deficit shouldContain SipSeongGroup.INSEONG
        roles.triggers.any { "회고" in it.message } shouldBe true
    }

    "비겁 과잉 — 전부 비겁이면 excess 에 비겁" {
        val roles =
            SipseongSynthesis.analyze(
                listOf(
                    memberOf("a", uniformChart(Cheongan.GAP, Jiji.IN)),
                    memberOf("b", uniformChart(Cheongan.GAP, Jiji.IN)),
                ),
                GroupContext.DEFAULT,
            )
        roles.excess shouldContain SipSeongGroup.BIGEOP
    }
})
