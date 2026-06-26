package io.github.jaeyeonling.saju.interpretation

/**
 * 해석 학파 전략 묶음 — 유파 편차가 큰 영역의 전략을 한 곳에 모아 주입한다.
 *
 * 기본값은 한국 표준(통설): 음포태 십이운성 + 억부 신강신약·용신 + 자평 격국.
 * 호출자가 전략을 갈아끼워 다른 유파로 해석할 수 있다.
 */
public data class InterpretationContext(
    public val sibiUnseong: SibiUnseongStrategy = EumPotaeStrategy,
    public val sinStrength: SinStrengthStrategy = BueokSinStrengthStrategy,
    public val yongsin: YongsinStrategy = BueokYongsinStrategy,
    public val gyeokguk: GyeokgukStrategy = JapyeongGyeokgukStrategy,
) {
    public companion object {
        /** 한국 표준 기본 컨텍스트. */
        @JvmField
        public val DEFAULT: InterpretationContext = InterpretationContext()
    }
}
