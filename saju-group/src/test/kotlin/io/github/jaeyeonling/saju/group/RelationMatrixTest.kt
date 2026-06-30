package io.github.jaeyeonling.saju.group

import io.github.jaeyeonling.saju.domain.Cheongan
import io.github.jaeyeonling.saju.domain.Jiji
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.collections.shouldNotContain
import io.kotest.matchers.shouldBe

/** 멤버간 합충 — test_relations.py 핀값 이식. classifyPair 는 글자 목록만 받아 chart 제약이 없다. */
class RelationMatrixTest : StringSpec({
    fun kinds(relations: List<PairRelation>): Set<RelationKind> = relations.map { it.kind }.toSet()

    "자오충 — 충 탐지, 갑·을은 천간합 아님" {
        val relations =
            RelationMatrixBuilder.classifyPair(
                List(4) { Cheongan.GAP },
                List(4) { Jiji.JA },
                List(4) { Cheongan.EUL },
                List(4) { Jiji.O },
            )
        kinds(relations) shouldContain RelationKind.CHUNG
        kinds(relations) shouldNotContain RelationKind.CHEONGAN_HAP
    }

    "천간합 — 갑기합토 (a→b 순서)" {
        val relations =
            RelationMatrixBuilder.classifyPair(
                List(4) { Cheongan.GAP },
                List(4) { Jiji.IN },
                List(4) { Cheongan.GI },
                List(4) { Jiji.SUL },
            )
        relations.first { it.kind == RelationKind.CHEONGAN_HAP }.detail shouldBe "갑기합토"
    }

    "삼합 — 두 멤버에 걸친 인오술 → 화" {
        val relations =
            RelationMatrixBuilder.classifyPair(
                List(4) { Cheongan.GAP },
                List(4) { Jiji.IN },
                List(4) { Cheongan.BYEONG },
                listOf(Jiji.O, Jiji.SUL, Jiji.O, Jiji.SUL),
            )
        relations.first { it.kind == RelationKind.SAMHAP }.detail.contains("화") shouldBe true
    }

    "인해 — 육합과 파 공존" {
        val relations =
            RelationMatrixBuilder.classifyPair(
                List(4) { Cheongan.GAP },
                List(4) { Jiji.IN },
                List(4) { Cheongan.EUL },
                List(4) { Jiji.HAE },
            )
        kinds(relations) shouldContain RelationKind.YUKHAP
        kinds(relations) shouldContain RelationKind.PA
    }

    "net 라벨 — 빈=중립, 협력, 긴장, 합·충 공존=복합" {
        RelationMatrixBuilder.aggregate(emptyList()) shouldBe RelationLabel.NEUTRAL
        RelationMatrixBuilder.aggregate(
            listOf(PairRelation(RelationKind.YUKHAP, RelationLabel.COOPERATION, "x")),
        ) shouldBe RelationLabel.COOPERATION
        RelationMatrixBuilder.aggregate(
            listOf(PairRelation(RelationKind.CHUNG, RelationLabel.TENSION, "x")),
        ) shouldBe RelationLabel.TENSION
        RelationMatrixBuilder.aggregate(
            listOf(
                PairRelation(RelationKind.YUKHAP, RelationLabel.COOPERATION, "x"),
                PairRelation(RelationKind.CHUNG, RelationLabel.TENSION, "y"),
            ),
        ) shouldBe RelationLabel.COMPLEX
    }

    "매트릭스 — nC2 쌍 + 그래프(중립 엣지 제외)" {
        val members =
            listOf(
                memberOf("a", uniformChart(Cheongan.GAP, Jiji.JA)),
                memberOf("b", uniformChart(Cheongan.BYEONG, Jiji.O)),
                memberOf("c", uniformChart(Cheongan.EUL, Jiji.HAE)),
            )
        val matrix = RelationMatrixBuilder.build(members)
        matrix.pairs shouldHaveSize 3
        matrix.pairs.first { setOf(it.a, it.b) == setOf("a", "b") }.netLabel shouldBe RelationLabel.TENSION
        matrix.graph.nodes shouldHaveSize 3
        matrix.graph.edges shouldHaveSize 1 // a-b(자오충)만, 나머지는 중립
    }

    "반합 — 삼합 2글자 + 왕지(신·자, 신자진의 왕지 자)" {
        val relations =
            RelationMatrixBuilder.classifyPair(
                List(4) { Cheongan.GAP },
                List(4) { Jiji.SIN },
                List(4) { Cheongan.BYEONG },
                List(4) { Jiji.JA },
            )
        relations.first { it.kind == RelationKind.BANHAP }.detail.contains("수") shouldBe true
    }

    "반합 아님 — 왕지 없는 삼합 2글자(신·진)" {
        val relations =
            RelationMatrixBuilder.classifyPair(
                List(4) { Cheongan.GAP },
                List(4) { Jiji.SIN },
                List(4) { Cheongan.MU },
                List(4) { Jiji.JIN },
            )
        kinds(relations) shouldNotContain RelationKind.BANHAP
        kinds(relations) shouldNotContain RelationKind.SAMHAP
    }

    "삼형 — 인사신이 두 멤버에 걸침" {
        val relations =
            RelationMatrixBuilder.classifyPair(
                List(4) { Cheongan.GAP },
                List(4) { Jiji.IN },
                List(4) { Cheongan.BYEONG },
                listOf(Jiji.SA, Jiji.SIN, Jiji.SA, Jiji.SIN),
            )
        kinds(relations) shouldContain RelationKind.HYEONG
    }

    "자묘형 — 자·묘" {
        val relations =
            RelationMatrixBuilder.classifyPair(
                List(4) { Cheongan.GAP },
                List(4) { Jiji.JA },
                List(4) { Cheongan.EUL },
                List(4) { Jiji.MYO },
            )
        relations.first { it.kind == RelationKind.HYEONG }.detail shouldBe "자묘형"
    }

    "육해 — 자·미" {
        val relations =
            RelationMatrixBuilder.classifyPair(
                List(4) { Cheongan.GAP },
                List(4) { Jiji.JA },
                List(4) { Cheongan.EUL },
                List(4) { Jiji.MI },
            )
        relations.first { it.kind == RelationKind.HAE }.detail shouldBe "자미해"
    }

    "동일 글자 반복도 관계는 1건만 — 중복 미방출" {
        val relations =
            RelationMatrixBuilder.classifyPair(
                List(4) { Cheongan.GAP },
                List(4) { Jiji.JA },
                List(4) { Cheongan.EUL },
                List(4) { Jiji.O },
            )
        relations.count { it.kind == RelationKind.CHUNG } shouldBe 1
    }
})
