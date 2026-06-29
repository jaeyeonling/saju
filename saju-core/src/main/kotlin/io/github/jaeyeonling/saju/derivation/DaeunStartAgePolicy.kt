package io.github.jaeyeonling.saju.derivation

import kotlin.math.floor
import kotlin.math.round

/** 대운수 통설 — 3일 = 1세(1일 = 4개월). */
private const val DAYS_PER_YEAR = 3.0

/**
 * 대운수(大運數) 환산 정책 — 절기 경계까지의 일수를 대운 시작 나이로 바꾸는 규칙.
 *
 * 통설은 3일 = 1세(1일 = 4개월). 경계의 우수리를 반올림하느냐 버리느냐가 유파마다 달라
 * 경계 표본에서 ±1세가 갈린다. 순수 함수형 인터페이스라 core(java.time-free)에 둔다.
 */
public fun interface DaeunStartAgePolicy {
    /**
     * @param daysToBoundary 절기 경계까지의 일수(순행이면 다음 절까지, 역행이면 이전 절부터).
     * @return 대운 시작 나이.
     */
    public fun startAge(daysToBoundary: Double): Int

    public companion object {
        /** 3일 = 1세, 표준 반올림(통설·현 기본). */
        @JvmField
        public val THREE_DAYS_ONE_YEAR: DaeunStartAgePolicy =
            DaeunStartAgePolicy { round(it / DAYS_PER_YEAR).toInt() }

        /** 3일 = 1세, 버림 — 만 나이를 보수적으로 산정하는 유파. */
        @JvmField
        public val FLOOR: DaeunStartAgePolicy =
            DaeunStartAgePolicy { floor(it / DAYS_PER_YEAR).toInt() }
    }
}
