package io.github.jaeyeonling.saju.derivation

import io.github.jaeyeonling.saju.domain.Eumyang
import io.github.jaeyeonling.saju.domain.GanZhi
import kotlin.math.round

/** 대운 진행 방향 — 양남음녀는 순행, 음남양녀는 역행. */
public enum class DaeunDirection {
    FORWARD, // 순행 (월주에서 다음 간지로)
    BACKWARD, // 역행 (이전 간지로)
    ;

    public companion object {
        /** 연간 음양 × 성별로 방향 결정. (양년 ↔ 남성)이 일치하면 순행. */
        @JvmStatic
        public fun of(yearStemEumyang: Eumyang, isMale: Boolean): DaeunDirection =
            if (yearStemEumyang.isYang == isMale) FORWARD else BACKWARD
    }
}

/** 대운(大運) — 10년 단위 큰 흐름. [startAge] 부터 시작하는 [ganZhi] 구간. */
public data class Daeun(
    public val startAge: Int,
    public val ganZhi: GanZhi,
)

/** 세운(歲運) — 그 해의 간지(입춘 기준 연주). */
public data class Seun(
    public val year: Int,
    public val ganZhi: GanZhi,
)

/** 대운 도출 — 절기까지의 거리로 시작 나이를 정하고, 월주에서 방향대로 시퀀스를 만든다. */
public object DaeunCalculator {

    /**
     * 대운 시작 나이(대운수) = 절기 경계까지의 일수 ÷ 3 (3일 = 1세).
     *
     * @param daysToBoundary 순행이면 다음 절(節)까지, 역행이면 이전 절부터의 일수.
     */
    @JvmStatic
    public fun startAge(daysToBoundary: Double): Int = round(daysToBoundary / DAYS_PER_YEAR).toInt()

    /** 월주에서 시작하는 대운 간지 시퀀스. 첫 대운은 월주의 다음/이전 간지. */
    @JvmStatic
    public fun sequence(
        monthPillar: GanZhi,
        direction: DaeunDirection,
        startAge: Int,
        count: Int,
    ): List<Daeun> {
        val step = if (direction == DaeunDirection.FORWARD) 1 else -1
        return (0 until count).map { i ->
            Daeun(startAge + i * YEARS_PER_DECADE, monthPillar.next(step * (i + 1)))
        }
    }

    private const val DAYS_PER_YEAR = 3.0
    private const val YEARS_PER_DECADE = 10
}
