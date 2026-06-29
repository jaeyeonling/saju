package io.github.jaeyeonling.saju.derivation

import io.github.jaeyeonling.saju.domain.ZishiPolicy

/**
 * 역법(曆法) 도출 정책 묶음 — 학파별로 갈리는 4기둥·대운 도출 규칙을 한 곳에 모은다.
 *
 * 기본값은 한국 통설: 정자시 + 입춘세수 + 3일1세 반올림.
 * 호출자가 [copy] 로 일부 정책만 갈아끼워 다른 유파로 도출할 수 있다.
 *
 * 진태양시 보정은 한국 도메인 개념이라 이 묶음에 넣지 않고 `saju-korea` 의 KoreanSajuConfig 가 합성한다
 * (core 단독 사용자는 한국 전용 정책을 보지 않는다).
 */
public data class SajuConfig
    @JvmOverloads
    constructor(
        public val zishi: ZishiPolicy = ZishiPolicy.JEONGJASI,
        public val yearBoundary: YearBoundary = YearBoundary.IPCHUN,
        public val daeunStartAge: DaeunStartAgePolicy = DaeunStartAgePolicy.THREE_DAYS_ONE_YEAR,
    ) {
        public companion object {
            /** 한국 통설 기본 설정. */
            @JvmField
            public val DEFAULT: SajuConfig = SajuConfig()
        }
    }
