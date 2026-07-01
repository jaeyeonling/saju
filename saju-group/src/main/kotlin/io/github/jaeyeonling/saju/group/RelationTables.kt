// 합충형해 룩업 테이블에 항목별 명리 주석을 인라인으로 단다(HapChung.kt 와 동일한 의도된 가독성 패턴).
@file:Suppress("ktlint:standard:discouraged-comment-location")

package io.github.jaeyeonling.saju.group

import io.github.jaeyeonling.saju.domain.Jiji

/**
 * 그룹 관계 매트릭스가 쓰는 보조 판정 테이블 — 형(刑)·파(破)·반합·자묘형만 담는다.
 *
 * 육합·육충·육해는 [Jiji] 의 산술 함수(`sixCombinePartner`/`opposite`/`harmPartner`)로, 천간합 변화오행은
 * [io.github.jaeyeonling.saju.domain.Cheongan.combinedOhaeng] 로, 삼합 4조는
 * [io.github.jaeyeonling.saju.domain.Samhap] 로 판정한다 — 이들 명리 상수의 SSOT 는 `saju-core`(도메인)다.
 * 여기 남은 것은 core 가 **미모델링**(학파 이견)하는 형·파·반합·자묘형 테이블뿐이다.
 */
internal object RelationTables {
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
}
