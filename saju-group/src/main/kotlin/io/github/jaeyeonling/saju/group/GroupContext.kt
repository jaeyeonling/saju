package io.github.jaeyeonling.saju.group

import io.github.jaeyeonling.saju.interpretation.InterpretationContext

/**
 * 그룹 합성 튜닝 — 결핍/과잉 임계와 대운 전환 윈도우.
 *
 * 균등 기댓값(오행은 1/5, 십성은 5묶음 평균)의 배수로 결핍/과잉을 판정한다:
 * - 결핍: 비율 < 기댓값 × [deficitFactor]  (기본 0.20 × 0.5 = 0.10)
 * - 과잉: 비율 > 기댓값 × [excessFactor]   (기본 0.20 × 1.6 = 0.32)
 *
 * 이 임계와 [transitionWindow] 는 점술 정설이 아닌 **도구 자체 규칙**이라 조정 가능하게 노출한다.
 * [interpretation] 은 [GroupMember.of] 가 사주판→해석을 계산할 때 쓰는 학파 전략이다.
 */
public data class GroupContext(
    public val deficitFactor: Double = DEFAULT_DEFICIT_FACTOR,
    public val excessFactor: Double = DEFAULT_EXCESS_FACTOR,
    public val transitionWindow: Int = DEFAULT_TRANSITION_WINDOW,
    public val interpretation: InterpretationContext = InterpretationContext.DEFAULT,
) {
    public companion object {
        /** 결핍 임계 계수 — 균등 기댓값의 이 배수 미만이면 결핍. */
        public const val DEFAULT_DEFICIT_FACTOR: Double = 0.5

        /** 과잉 임계 계수 — 균등 기댓값의 이 배수 초과면 과잉. */
        public const val DEFAULT_EXCESS_FACTOR: Double = 1.6

        /** 대운 경계 ±N년을 '전환기'로 본다(세는/만 나이 불확실성 흡수). */
        public const val DEFAULT_TRANSITION_WINDOW: Int = 1

        /** 한국 표준 기본 컨텍스트. */
        @JvmField
        public val DEFAULT: GroupContext = GroupContext()
    }
}
