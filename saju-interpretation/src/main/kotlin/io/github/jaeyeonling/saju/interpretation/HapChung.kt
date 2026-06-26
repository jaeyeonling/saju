package io.github.jaeyeonling.saju.interpretation

import io.github.jaeyeonling.saju.domain.Cheongan
import io.github.jaeyeonling.saju.domain.Jiji
import io.github.jaeyeonling.saju.domain.Ohaeng

/** 글자끼리의 결합·충돌 관계. 원국 8글자 + 운(運)의 글자가 만나며 사건을 만든다. */
public sealed interface HapChungRelation {
    /** 천간합(天干合) — 갑기합토 등 두 천간이 묶여 새 오행을 이룬다. */
    public data class CheonganHap(
        public val a: Cheongan,
        public val b: Cheongan,
        public val transformsTo: Ohaeng,
    ) : HapChungRelation

    /** 지지 육합(六合) — 자축·인해 등 두 지지의 결합. */
    public data class JijiYukhap(public val a: Jiji, public val b: Jiji) : HapChungRelation

    /** 지지 육충(六沖) — 자오·축미 등 정면충돌. */
    public data class JijiYukchung(public val a: Jiji, public val b: Jiji) : HapChungRelation

    /** 지지 육해(六害) — 합을 방해하는 어긋남. */
    public data class JijiYukhae(public val a: Jiji, public val b: Jiji) : HapChungRelation

    /** 지지 삼합(三合) — 세 글자가 모여 강한 오행을 이룸(띠 궁합). */
    public data class JijiSamhap(public val members: List<Jiji>, public val transformsTo: Ohaeng) : HapChungRelation
}

/**
 * 천간·지지 집합에서 합충 관계를 테이블 기반으로 탐지한다.
 *
 * v1 탐지 범위: **천간합** + 지지 **육합·육충·육해·삼합**.
 * (형(刑)·파(破)·방합·반합은 학파별 이견이 커 미모델링 — 골든 레퍼런스 tyme4j도 동일 범위)
 */
public object HapChungDetector {

    /** 천간합 + 지지 육합/육충/육해/삼합을 찾는다. */
    @JvmStatic
    public fun detect(stems: List<Cheongan>, branches: List<Jiji>): List<HapChungRelation> {
        val relations = mutableListOf<HapChungRelation>()

        // 천간합
        forEachPair(stems.size) { i, j ->
            if (stems[i].combinePartner() == stems[j]) {
                relations += HapChungRelation.CheonganHap(stems[i], stems[j], combinedOhaeng(stems[i]))
            }
        }

        // 지지 육합/육충/육해 (한 쌍이 여러 관계일 수 없으나 각각 독립 판정)
        forEachPair(branches.size) { i, j ->
            val a = branches[i]
            val b = branches[j]
            when {
                a.sixCombinePartner() == b -> relations += HapChungRelation.JijiYukhap(a, b)
                a.opposite() == b -> relations += HapChungRelation.JijiYukchung(a, b)
                a.harmPartner() == b -> relations += HapChungRelation.JijiYukhae(a, b)
            }
        }

        // 지지 삼합 (세 글자가 모두 있으면 성립)
        val branchSet = branches.toSet()
        for ((members, ohaeng) in SAMHAP_GROUPS) {
            if (branchSet.containsAll(members)) {
                relations += HapChungRelation.JijiSamhap(members, ohaeng)
            }
        }

        return relations
    }

    /** 천간합 변화 오행: 갑기합토·을경합금·병신합수·정임합목·무계합화. */
    private fun combinedOhaeng(stem: Cheongan): Ohaeng = COMBINED_OHAENG[stem.ordinal % COMBINED_OHAENG.size]

    private inline fun forEachPair(size: Int, action: (Int, Int) -> Unit) {
        for (i in 0 until size) {
            for (j in i + 1 until size) action(i, j)
        }
    }

    private val COMBINED_OHAENG = listOf(Ohaeng.TO, Ohaeng.GEUM, Ohaeng.SU, Ohaeng.MOK, Ohaeng.HWA)

    private val SAMHAP_GROUPS: List<Pair<List<Jiji>, Ohaeng>> = listOf(
        listOf(Jiji.SHIN, Jiji.JA, Jiji.JIN) to Ohaeng.SU, // 신자진 → 수
        listOf(Jiji.HAE, Jiji.MYO, Jiji.MI) to Ohaeng.MOK, // 해묘미 → 목
        listOf(Jiji.IN, Jiji.O, Jiji.SUL) to Ohaeng.HWA, // 인오술 → 화
        listOf(Jiji.SA, Jiji.YU, Jiji.CHUK) to Ohaeng.GEUM, // 사유축 → 금
    )
}
