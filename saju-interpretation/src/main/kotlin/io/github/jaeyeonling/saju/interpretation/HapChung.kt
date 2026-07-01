// 삼합 그룹 등 도메인 룩업 테이블에 항목별 명리 주석을 인라인으로 단다(의도된 가독성 패턴).
@file:Suppress("ktlint:standard:discouraged-comment-location")

package io.github.jaeyeonling.saju.interpretation

import io.github.jaeyeonling.saju.domain.Cheongan
import io.github.jaeyeonling.saju.domain.Jiji
import io.github.jaeyeonling.saju.domain.Ohaeng
import io.github.jaeyeonling.saju.domain.Samhap

/** 글자끼리의 결합·충돌 관계. 현재 탐지는 원국 8글자(4간4지) 안에서 이뤄진다(운(運) 글자 결합은 호출부 확장 시). */
public sealed interface HapChungRelation {
    /**
     * 천간합(天干合) — 갑기합토 등 두 천간이 묶여 새 오행을 이룬다.
     *
     * [transformsTo] 가 `null` 이면 합은 성립하나 **합화(化)는 보류** — 일간(나)이 낀 합은
     * 화하지 않는다(合而不化). 합 자체와 합화를 구분해 노출한다.
     */
    public data class CheonganHap(
        public val a: Cheongan,
        public val b: Cheongan,
        public val transformsTo: Ohaeng?,
    ) : HapChungRelation

    /** 천간충(天干沖) — 갑경·을신·병임·정계. 거리 6(칠살)의 정면충돌(무기 토는 충 없음). */
    public data class CheonganChung(public val a: Cheongan, public val b: Cheongan) : HapChungRelation

    /** 지지 육합(六合) — 자축·인해 등 두 지지의 결합. */
    public data class JijiYukhap(public val a: Jiji, public val b: Jiji) : HapChungRelation

    /** 지지 육충(六沖) — 자오·축미 등 정면충돌. */
    public data class JijiYukchung(public val a: Jiji, public val b: Jiji) : HapChungRelation

    /** 지지 육해(六害) — 합을 방해하는 어긋남. */
    public data class JijiYukhae(public val a: Jiji, public val b: Jiji) : HapChungRelation

    /** 지지 삼합(三合) — 생·왕·묘 세 글자가 모여 강한 오행을 이룸(신자진→수). */
    public data class JijiSamhap(public val members: List<Jiji>, public val transformsTo: Ohaeng) : HapChungRelation

    /**
     * 지지 방합(方合) — 같은 계절/방위 세 글자가 모임(인묘진→목, 봄·동방).
     * 개별 글자는 삼합과 겹치지만(예: 묘는 방합 인묘진·삼합 해묘미 양쪽), **완전한 3글자 집합**은
     * 어떤 삼합과도 일치하지 않아 containsAll 로는 동일 관계가 중복 탐지되지 않는다.
     */
    public data class JijiBanghap(public val members: List<Jiji>, public val transformsTo: Ohaeng) : HapChungRelation
}

/**
 * 합충 탐지 전략 — 어떤 관계까지 모델링하느냐가 유파마다 달라 추상화한다.
 * (형·파·반합 포함 여부, 합화 성립 조건 등이 분기 지점)
 */
public interface HapChungStrategy {
    /**
     * 주어진 천간·지지 목록에서 합충 관계를 찾는다. (현재 호출부는 원국 4간4지만 넘긴다 — 운(運) 글자 미반영.)
     *
     * [dayMasterIndex] 는 [stems] 에서 일간(일주의 천간)이 놓인 **위치**다. 그 위치가 낀 천간합만
     * 합화(化)를 보류한다(合而不化). `-1`(기본)이면 모든 합이 화하는 것으로 본다 — 위치 무관 순수 탐지.
     *
     * **값이 아니라 위치**로 판정하므로, 일간과 같은 글자가 다른 기둥에 중복돼도 그 비일간 합은 정상 화한다.
     */
    public fun detect(
        stems: List<Cheongan>,
        branches: List<Jiji>,
        dayMasterIndex: Int = -1,
    ): List<HapChungRelation>
}

/**
 * 표준 합충 탐지 — **천간합·천간충** + 지지 **육합·육충·육해·삼합**.
 *
 * (형(刑)·파(破)·반합은 학파별 이견이 커 미모델링 — 골든 레퍼런스도 동일 범위)
 */
public object StandardHapChungStrategy : HapChungStrategy {
    /** 천간합/천간충 + 지지 육합/육충/육해/삼합을 찾는다. */
    override fun detect(
        stems: List<Cheongan>,
        branches: List<Jiji>,
        dayMasterIndex: Int,
    ): List<HapChungRelation> {
        val relations = mutableListOf<HapChungRelation>()

        // 천간합·천간충 (합은 거리 5, 충은 거리 6 — 한 쌍이 둘 다일 수 없으나 각각 독립 판정)
        forEachPair(stems.size) { i, j ->
            val a = stems[i]
            val b = stems[j]
            if (a.combinePartner() == b) {
                // 일간(위치)이 낀 합만 합화 보류(合而不化). 위치 판정이라 같은 글자 중복에 오탐 없음.
                val involvesDayMaster = i == dayMasterIndex || j == dayMasterIndex
                val transformsTo = if (involvesDayMaster) null else a.combinedOhaeng()
                relations += HapChungRelation.CheonganHap(a, b, transformsTo)
            }
            if (a.chungPartner() == b) {
                relations += HapChungRelation.CheonganChung(a, b)
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

        // 지지 삼합·방합 (세 글자가 모두 있으면 성립 — 완전 3집합은 서로 일치 안 해 동일 관계 중복 없이 독립 판정)
        val branchSet = branches.toSet()
        for ((members, ohaeng) in Samhap.GROUPS) {
            if (branchSet.containsAll(members)) {
                relations += HapChungRelation.JijiSamhap(members, ohaeng)
            }
        }
        for ((members, ohaeng) in BANGHAP_GROUPS) {
            if (branchSet.containsAll(members)) {
                relations += HapChungRelation.JijiBanghap(members, ohaeng)
            }
        }

        return relations
    }

    private inline fun forEachPair(
        size: Int,
        action: (Int, Int) -> Unit,
    ) {
        for (i in 0 until size) {
            for (j in i + 1 until size) action(i, j)
        }
    }

    // 방합 = 계절/방위 3글자. 반방합(2글자)은 학파 이견 커 제외 — 3글자 모두 모일 때만 성립.
    private val BANGHAP_GROUPS: List<Pair<List<Jiji>, Ohaeng>> =
        listOf(
            listOf(Jiji.IN, Jiji.MYO, Jiji.JIN) to Ohaeng.MOK, // 인묘진 → 목 (봄·동방)
            listOf(Jiji.SA, Jiji.O, Jiji.MI) to Ohaeng.HWA, // 사오미 → 화 (여름·남방)
            listOf(Jiji.SIN, Jiji.YU, Jiji.SUL) to Ohaeng.GEUM, // 신유술 → 금 (가을·서방)
            listOf(Jiji.HAE, Jiji.JA, Jiji.CHUK) to Ohaeng.SU, // 해자축 → 수 (겨울·북방)
        )
}
