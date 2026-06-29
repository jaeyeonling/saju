package io.github.jaeyeonling.saju.korea

import io.github.jaeyeonling.saju.derivation.SajuConfig

/**
 * 한국 사주 설정 묶음 — 역법 도출 정책([SajuConfig])을 합성하고 한국 전용 진태양시 보정을 더한다.
 *
 * 기본값은 한국 통설: 정자시·입춘세수·3일1세 + 진태양시 풀보정.
 * [saju] 는 그대로 core 도출 파이프라인으로 통과되고, [trueSolarTime] 만 korea 보정 단계에서 소비된다.
 */
public data class KoreanSajuConfig
    @JvmOverloads
    constructor(
        public val saju: SajuConfig = SajuConfig.DEFAULT,
        public val trueSolarTime: TrueSolarTimePolicy = TrueSolarTimePolicy.FULL,
    ) {
        public companion object {
            /** 한국 통설 기본 설정. */
            @JvmField
            public val DEFAULT: KoreanSajuConfig = KoreanSajuConfig()
        }
    }
