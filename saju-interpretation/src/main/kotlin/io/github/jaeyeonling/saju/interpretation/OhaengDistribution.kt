package io.github.jaeyeonling.saju.interpretation

import io.github.jaeyeonling.saju.domain.Ohaeng
import io.github.jaeyeonling.saju.domain.SajuChart

/**
 * 오행 분포 — 사주 8글자(천간 4 + 지지 본기 4)의 오행 개수 집계. 해석 보조용 공개 유틸.
 * [counts] 는 불변 맵으로 노출된다.
 */
public data class OhaengDistribution(
    public val counts: Map<Ohaeng, Int>,
) {
    /** 특정 오행의 개수. */
    public fun count(ohaeng: Ohaeng): Int = counts[ohaeng] ?: 0

    /** 가장 많은 오행. */
    public fun dominant(): Ohaeng = counts.maxByOrNull { it.value }?.key ?: Ohaeng.MOK

    public companion object {
        /** 8글자의 천간·지지 오행을 단순 집계. */
        @JvmStatic
        public fun from(chart: SajuChart): OhaengDistribution {
            val counts = Ohaeng.entries.associateWith { 0 }.toMutableMap()
            chart.stems().forEach { counts[it.ohaeng] = counts.getValue(it.ohaeng) + 1 }
            chart.branches().forEach { counts[it.ohaeng] = counts.getValue(it.ohaeng) + 1 }
            return OhaengDistribution(java.util.Collections.unmodifiableMap(LinkedHashMap(counts)))
        }
    }
}
