package io.github.jaeyeonling.saju.interpretation

import io.github.jaeyeonling.saju.domain.HiddenStemTable

/**
 * 해석 학파 전략 묶음 — 유파 편차가 큰 영역의 전략을 한 곳에 모아 주입한다.
 *
 * 기본값은 한국 표준(통설): 음포태 십이운성 + 억부 신강신약·용신 + 자평 격국 + 표준 합충.
 * 호출자가 [copy] 로 전략을 갈아끼워 다른 유파로 해석할 수 있다.
 *
 * 지장간 분야표([HiddenStemTable])는 신강신약·격국이 함께 의존한다. 둘에 같은 테이블을 일관 주입하려면
 * [withHiddenStems] 를 쓴다 — copy 기반이라 다른 전략 교체와 자유롭게 합성된다.
 */
public data class InterpretationContext(
    public val sibiUnseong: SibiUnseongStrategy = EumPotaeStrategy,
    public val sinStrength: SinStrengthStrategy = EokbuSinStrengthStrategy.DEFAULT,
    public val yongsin: YongsinStrategy = EokbuYongsinStrategy,
    public val gyeokguk: GyeokgukStrategy = JapyeongGyeokgukStrategy.DEFAULT,
    public val hapChung: HapChungStrategy = StandardHapChungStrategy,
) {
    /**
     * 지장간 분야표를 신강신약·격국에 **일관 주입**한 사본. 나머지 전략(십이운성·용신·합충)은 보존한다.
     *
     * copy 합성이라 `DEFAULT.copy(sibiUnseong = YangPotaeStrategy).withHiddenStems(table)` 처럼
     * 다른 전략 교체와 함께 한 줄로 쓸 수 있다. 격국이 자평/투출이면 같은 테이블을 주입하고, 커스텀 격국은 그대로 둔다.
     */
    public fun withHiddenStems(hiddenStems: HiddenStemTable): InterpretationContext =
        copy(
            // 기존 억부 가중치(EokbuWeights)·통근 십이운성은 보존하고 테이블만 교체한다. 커스텀 신강 전략은 자체 관리하므로 그대로 둔다.
            sinStrength =
                when (val current = sinStrength) {
                    is EokbuSinStrengthStrategy ->
                        EokbuSinStrengthStrategy(current.weights, hiddenStems, current.sibiUnseong)
                    else -> current
                },
            gyeokguk =
                when (gyeokguk) {
                    is TuchulGyeokgukStrategy -> TuchulGyeokgukStrategy(hiddenStems)
                    is JapyeongGyeokgukStrategy -> JapyeongGyeokgukStrategy(hiddenStems)
                    else -> gyeokguk
                },
        )

    public companion object {
        /** 한국 표준 기본 컨텍스트. */
        @JvmField
        public val DEFAULT: InterpretationContext = InterpretationContext()
    }
}
