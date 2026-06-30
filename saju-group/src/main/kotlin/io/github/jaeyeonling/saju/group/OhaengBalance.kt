package io.github.jaeyeonling.saju.group

import io.github.jaeyeonling.saju.domain.Ohaeng

/**
 * 끊긴 상생(相生) 체인 — [target] 오행은 그룹에 있는데 그를 생(生)하는 [source] 오행이 없다.
 * 공급원이 비어 흐름이 끊긴 지점(예: 목은 있는데 수가 0 → 수→목 공급 결여).
 */
public data class BrokenChain(
    public val source: Ohaeng,
    public val target: Ohaeng,
) {
    /** "수→목 공급원 결여" 형태의 설명. */
    public val description: String
        get() = "${source.koreanName}→${target.koreanName} 공급원 결여"
}

/**
 * 그룹 오행 균형(STAGE3a) — **순수 산술(정답 있음)**.
 *
 * 멤버 표면 오행 벡터(8글자 분포)를 합산·정규화해 그룹의 결핍·과잉·끊긴 상생 체인을 드러낸다.
 * 해석 라벨이 아니라 결정론적 수치이므로 면책 대상이 아니다.
 */
public data class OhaengBalance(
    /** 오행별 그룹 합산 개수. */
    public val groupVector: Map<Ohaeng, Int>,
    /** 합계 대비 비율(소수 3자리). */
    public val normalized: Map<Ohaeng, Double>,
    /** 결핍 오행(0개이거나 기댓값 대비 현저히 적음). */
    public val deficient: List<Ohaeng>,
    /** 과잉 오행(기댓값 대비 현저히 많음). */
    public val excessive: List<Ohaeng>,
    /** 끊긴 상생 체인. */
    public val brokenChains: List<BrokenChain>,
    /** 멤버별 최다 오행(id → 오행). */
    public val dominantByMember: Map<String, Ohaeng>,
)
