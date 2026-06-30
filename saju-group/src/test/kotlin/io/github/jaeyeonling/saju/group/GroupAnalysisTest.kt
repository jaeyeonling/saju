package io.github.jaeyeonling.saju.group

import io.github.jaeyeonling.saju.domain.Cheongan
import io.github.jaeyeonling.saju.domain.Jiji
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe

/** 파사드 — 입력 검증 + 4차원 합성 + 결정론. */
class GroupAnalysisTest : StringSpec({
    val a = memberOf("a", uniformChart(Cheongan.GAP, Jiji.JA))
    val b = memberOf("b", uniformChart(Cheongan.BYEONG, Jiji.O))

    "멤버가 2명 미만이면 예외" {
        shouldThrow<IllegalArgumentException> { GroupAnalysis.of(listOf(a), 2026) }
    }

    "멤버 id 가 중복이면 예외" {
        val dup = memberOf("a", uniformChart(Cheongan.BYEONG, Jiji.O))
        shouldThrow<IllegalArgumentException> { GroupAnalysis.of(listOf(a, dup), 2026) }
    }

    "4차원 합성 결과를 모두 채우고 면책을 포함한다" {
        val report = GroupAnalysis.of(listOf(a, b), 2026)
        report.memberIds shouldBe listOf("a", "b")
        report.relations.pairs shouldHaveSize 1
        report.timeline.year shouldBe 2026
        report.disclaimer shouldBe GROUP_DISCLAIMER
    }

    "결정론 — 같은 입력은 같은 결과" {
        GroupAnalysis.of(listOf(a, b), 2026) shouldBe GroupAnalysis.of(listOf(a, b), 2026)
    }
})
