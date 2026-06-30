// 합충형해 룩업 테이블에 항목별 명리 주석을 인라인으로 단다(HapChung.kt 와 동일한 의도된 가독성 패턴).
@file:Suppress("ktlint:standard:discouraged-comment-location")

package io.github.jaeyeonling.saju.group

import io.github.jaeyeonling.saju.domain.Jiji
import io.github.jaeyeonling.saju.domain.Ohaeng

/**
 * 멤버간 합충 판정 테이블 — vault 노트 [[합충형해]](책 교차검증)를 전사한 단일 진실 소스(SSOT).
 *
 * 육합·육충·육해는 [Jiji] 의 산술 함수(`sixCombinePartner`/`opposite`/`harmPartner`)로 판정하므로
 * 테이블이 없다. 천간합 변화오행은 [combinedOhaeng] 가 계산한다. 여기 모은 것은 [io.github.jaeyeonling.saju]
 * interpretation 의 `StandardHapChungStrategy` 가 **미모델링**(학파 이견)하는 형·파·반합·자묘형이다.
 */
internal object RelationTables {
    /** 삼합 4조 + 변화오행. 해묘미→목, 인오술→화, 신자진→수, 사유축→금. */
    val SAMHAP: List<Pair<Set<Jiji>, Ohaeng>> =
        listOf(
            setOf(Jiji.HAE, Jiji.MYO, Jiji.MI) to Ohaeng.MOK, // 해묘미 → 목
            setOf(Jiji.IN, Jiji.O, Jiji.SUL) to Ohaeng.HWA, // 인오술 → 화
            setOf(Jiji.SIN, Jiji.JA, Jiji.JIN) to Ohaeng.SU, // 신자진 → 수
            setOf(Jiji.SA, Jiji.YU, Jiji.CHUK) to Ohaeng.GEUM, // 사유축 → 금
        )

    /** 왕지(旺支) — 반합 성립에 필요(자·오·묘·유). */
    val WANGJI: Set<Jiji> = setOf(Jiji.JA, Jiji.O, Jiji.MYO, Jiji.YU)

    /** 삼형(三刑) — 인사신, 축술미. (자묘형은 2글자라 [JAMYO] 로 별도) */
    val SAMHYEONG: List<Set<Jiji>> =
        listOf(
            setOf(Jiji.IN, Jiji.SA, Jiji.SIN), // 인사신
            setOf(Jiji.CHUK, Jiji.SUL, Jiji.MI), // 축술미
        )

    /** 자묘형(子卯刑) — 2글자 형. */
    val JAMYO: Set<Jiji> = setOf(Jiji.JA, Jiji.MYO)

    /** 육파(六破) — 자유·오묘·신사·인해·진축·술미. */
    val YUKPA: Set<Set<Jiji>> =
        setOf(
            setOf(Jiji.JA, Jiji.YU), // 자유
            setOf(Jiji.O, Jiji.MYO), // 오묘
            setOf(Jiji.SIN, Jiji.SA), // 신사
            setOf(Jiji.IN, Jiji.HAE), // 인해
            setOf(Jiji.JIN, Jiji.CHUK), // 진축
            setOf(Jiji.SUL, Jiji.MI), // 술미
        )

    /** 천간합 변화오행 — 갑기합토·을경합금·병신합수·정임합목·무계합화. `ordinal % 5`. */
    private val COMBINED_OHAENG: List<Ohaeng> =
        listOf(Ohaeng.TO, Ohaeng.GEUM, Ohaeng.SU, Ohaeng.MOK, Ohaeng.HWA)

    /** 천간 [io.github.jaeyeonling.saju.domain.Cheongan] 의 합 변화오행(ordinal 기반). */
    fun combinedOhaeng(stemOrdinal: Int): Ohaeng = COMBINED_OHAENG[stemOrdinal % COMBINED_OHAENG.size]
}
