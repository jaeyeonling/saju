// 삼합 룩업 테이블에 항목별 명리 주석을 인라인으로 단다(의도된 가독성 패턴).
@file:Suppress("ktlint:standard:discouraged-comment-location")

package io.github.jaeyeonling.saju.domain

/**
 * 지지 삼합(三合) 4조 — 세 글자가 모여 강한 오행을 이룬다. 명리 상수의 단일 진실 소스(SSOT).
 *
 * 12지지를 `ordinal % 4` 로 나눈 네 삼각 그룹이며, 각 조는 생지·왕지·묘지의 세 글자다.
 * `saju-interpretation`(단일 사주 합충)과 `saju-group`(그룹 관계 매트릭스)이 이 하나를 공유한다.
 *
 * 각 조의 **내부 글자 순서**는 삼합 관계의 표시·직렬화 페이로드([HapChungRelation.JijiSamhap] 멤버)로
 * 그대로 노출되므로 의미 있는 값이다 — 생지→왕지→묘지 관습 순서를 보존한다.
 */
public object Samhap {
    /**
     * 삼합 4조 + 변화오행. 해묘미→목·인오술→화·신자진→수·사유축→금.
     *
     * `@JvmField` 로 노출 — Java 에서 getGROUPS() 게터 없이 다른 도메인 상수(Ganji.ALL)처럼 필드로 접근한다.
     */
    @JvmField
    public val GROUPS: List<Pair<List<Jiji>, Ohaeng>> =
        listOf(
            listOf(Jiji.HAE, Jiji.MYO, Jiji.MI) to Ohaeng.MOK, // 해묘미 → 목
            listOf(Jiji.IN, Jiji.O, Jiji.SUL) to Ohaeng.HWA, // 인오술 → 화
            listOf(Jiji.SIN, Jiji.JA, Jiji.JIN) to Ohaeng.SU, // 신자진 → 수
            listOf(Jiji.SA, Jiji.YU, Jiji.CHUK) to Ohaeng.GEUM, // 사유축 → 금
        )
}
