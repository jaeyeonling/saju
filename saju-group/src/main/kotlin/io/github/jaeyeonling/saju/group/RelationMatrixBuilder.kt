package io.github.jaeyeonling.saju.group

import io.github.jaeyeonling.saju.domain.Cheongan
import io.github.jaeyeonling.saju.domain.Jiji

/**
 * 멤버간 합충 매트릭스 합성(STAGE3c).
 *
 * 각 멤버 쌍의 8글자를 **A글자 × B글자 교차**로만 본다(같은 멤버 내부 합충 제외).
 * [classifyPair]·[aggregate] 는 사주판 제약 없이 글자 목록만 받아 단위 테스트가 쉽다(internal).
 */
internal object RelationMatrixBuilder {
    fun build(members: List<GroupMember>): RelationMatrix {
        val pairs =
            buildList {
                for (i in members.indices) {
                    for (j in i + 1 until members.size) {
                        val a = members[i]
                        val b = members[j]
                        val relations =
                            classifyPair(
                                a.chart.stems(),
                                a.chart.branches(),
                                b.chart.stems(),
                                b.chart.branches(),
                            )
                        add(MemberPair(a.id, b.id, relations, aggregate(relations)))
                    }
                }
            }
        val nodes = members.map { GraphNode(it.id, it.alias, it.report.ohaeng.dominant()) }
        val edges =
            pairs
                .filter { it.netLabel != RelationLabel.NEUTRAL }
                .map { GraphEdge(it.a, it.b, it.netLabel) }
        return RelationMatrix(pairs, RelationGraph(nodes, edges))
    }

    internal fun classifyPair(
        aStems: List<Cheongan>,
        aBranches: List<Jiji>,
        bStems: List<Cheongan>,
        bBranches: List<Jiji>,
    ): List<PairRelation> =
        buildList {
            addCheonganHap(aStems, bStems)
            addBranchPairs(aBranches, bBranches)
            addTripleGroups(aBranches, bBranches)
        }

    /** 천간합 — 천간 × 천간 교차. 글자는 distinct(같은 글자가 여러 기둥에 반복돼도 관계는 1건). */
    private fun MutableList<PairRelation>.addCheonganHap(
        aStems: List<Cheongan>,
        bStems: List<Cheongan>,
    ) {
        val bUnique = bStems.toSet()
        for (ga in aStems.toSet()) {
            for (gb in bUnique) {
                if (ga != gb && ga.combinePartner() == gb) {
                    val oh = RelationTables.combinedOhaeng(ga.ordinal)
                    val detail = "${ga.koreanName}${gb.koreanName}합${oh.koreanName}"
                    add(PairRelation(RelationKind.CHEONGAN_HAP, RelationLabel.COMPLEMENT, detail))
                }
            }
        }
    }

    /**
     * 지지 2글자 관계 — 육합/충/자묘형/파/해. 한 쌍이 여러 관계일 수 있어 각각 독립 판정.
     * 글자는 distinct(같은 글자가 여러 기둥에 반복돼도 관계는 1건 — 출력 중복 방지).
     */
    private fun MutableList<PairRelation>.addBranchPairs(
        aBranches: List<Jiji>,
        bBranches: List<Jiji>,
    ) {
        val bUnique = bBranches.toSet()
        for (ja in aBranches.toSet()) {
            for (jb in bUnique) {
                if (ja == jb) continue
                val pair = "${ja.koreanName}${jb.koreanName}"
                if (ja.sixCombinePartner() == jb) {
                    add(PairRelation(RelationKind.YUKHAP, RelationLabel.COOPERATION, "${pair}육합"))
                }
                if (ja.opposite() == jb) {
                    add(PairRelation(RelationKind.CHUNG, RelationLabel.TENSION, "${pair}충"))
                }
                if (setOf(ja, jb) == RelationTables.JAMYO) {
                    add(PairRelation(RelationKind.HYEONG, RelationLabel.TENSION, "자묘형"))
                }
                if (setOf(ja, jb) in RelationTables.YUKPA) {
                    add(PairRelation(RelationKind.PA, RelationLabel.FRICTION, "${pair}파"))
                }
                if (ja.harmPartner() == jb) {
                    add(PairRelation(RelationKind.HAE, RelationLabel.FRICTION, "${pair}해"))
                }
            }
        }
    }

    /** 삼합/반합/삼형 — 두 멤버 지지 합집합이 양쪽에 걸쳐야 성립(멤버간 관계). */
    private fun MutableList<PairRelation>.addTripleGroups(
        aBranches: List<Jiji>,
        bBranches: List<Jiji>,
    ) {
        val aSet = aBranches.toSet()
        val bSet = bBranches.toSet()
        val union = aSet + bSet
        for ((combo, oh) in RelationTables.SAMHAP) {
            if (!spansBoth(combo, aSet, bSet)) continue
            val present = combo intersect union
            when {
                present.size == 3 ->
                    add(
                        PairRelation(
                            RelationKind.SAMHAP,
                            RelationLabel.COOPERATION,
                            "${sortedNames(combo)}삼합${oh.koreanName}",
                        ),
                    )
                present.size == 2 && (present intersect RelationTables.WANGJI).isNotEmpty() ->
                    add(
                        PairRelation(
                            RelationKind.BANHAP,
                            RelationLabel.COOPERATION,
                            "${sortedNames(present)}반합${oh.koreanName}",
                        ),
                    )
            }
        }
        for (combo in RelationTables.SAMHYEONG) {
            val present = combo intersect union
            if (present.size >= 2 && spansBoth(combo, aSet, bSet)) {
                add(PairRelation(RelationKind.HYEONG, RelationLabel.TENSION, "${sortedNames(present)}형"))
            }
        }
    }

    /**
     * 관계 묶음 → net 라벨. 합(협력/보완)과 충(긴장/마찰)이 함께면 '복합'(애증), 아니면 가중 합 부호로,
     * 관계 없으면 '중립'.
     */
    internal fun aggregate(relations: List<PairRelation>): RelationLabel {
        if (relations.isEmpty()) return RelationLabel.NEUTRAL
        val score = relations.sumOf { it.label.weight }
        val hasPositive = relations.any { it.label.weight > 0 }
        val hasNegative = relations.any { it.label.weight < 0 }
        return when {
            hasPositive && hasNegative -> RelationLabel.COMPLEX
            score > 0 -> RelationLabel.COOPERATION
            score < 0 -> RelationLabel.TENSION
            else -> RelationLabel.NEUTRAL
        }
    }

    private fun spansBoth(
        combo: Set<Jiji>,
        aSet: Set<Jiji>,
        bSet: Set<Jiji>,
    ): Boolean = (combo intersect aSet).isNotEmpty() && (combo intersect bSet).isNotEmpty()

    private fun sortedNames(branches: Set<Jiji>): String = branches.map { it.koreanName }.sorted().joinToString("")
}
