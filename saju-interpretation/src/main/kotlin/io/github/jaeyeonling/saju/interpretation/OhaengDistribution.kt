package io.github.jaeyeonling.saju.interpretation

import io.github.jaeyeonling.saju.domain.JijiHiddenStems
import io.github.jaeyeonling.saju.domain.Ohaeng
import io.github.jaeyeonling.saju.domain.SajuChart

/**
 * 오행 분포 — 사주 8글자(천간 4 + 지지 본기 4)의 오행 개수 집계. 해석 보조용 공개 유틸.
 * [counts] 는 불변 맵으로 노출된다.
 *
 * 두 가지 집계 방식을 제공한다:
 * - [from]: 표면 8글자만 — 한눈에 보는 분포(합계 항상 8).
 * - [weighted]: 8글자 + 지지 지장간(본·중·여)까지 — 표면에 안 드러난 숨은 오행을 드러낸다(합계 가변).
 */
public data class OhaengDistribution(
    public val counts: Map<Ohaeng, Int>,
) {
    /** 특정 오행의 개수. */
    public fun count(ohaeng: Ohaeng): Int = counts[ohaeng] ?: 0

    /** 가장 많은 오행. */
    public fun dominant(): Ohaeng = counts.maxByOrNull { it.value }?.key ?: Ohaeng.MOK

    public companion object {
        /** 8글자의 천간·지지 오행을 단순 집계(합계 항상 8). */
        @JvmStatic
        public fun from(chart: SajuChart): OhaengDistribution = tally(chart) { /* 지장간 가중 없음 */ }

        /**
         * 8글자 + 4지지 지장간(본·중·여)까지 가중 집계. 표면에 안 드러난 오행(예: 지장간 속 水)을 드러낸다.
         * 본·중·여를 각 1씩 더하므로 합계는 `8 + Σ지장간 개수`로 가변이다 — 표면 분포가 아니라 '잠재 분포'다.
         */
        @JvmStatic
        public fun weighted(chart: SajuChart): OhaengDistribution =
            tally(chart) { counts ->
                chart.branches().forEach { ji ->
                    JijiHiddenStems.of(ji).all().forEach { stem ->
                        counts[stem.ohaeng] = counts.getValue(stem.ohaeng) + 1
                    }
                }
            }

        /** 표면 8글자(천간 4 + 지지 본체 4) 집계 후, [extra] 로 추가 가중을 더해 불변 맵으로 봉인. */
        private inline fun tally(
            chart: SajuChart,
            extra: (MutableMap<Ohaeng, Int>) -> Unit,
        ): OhaengDistribution {
            val counts = Ohaeng.entries.associateWith { 0 }.toMutableMap()
            chart.stems().forEach { counts[it.ohaeng] = counts.getValue(it.ohaeng) + 1 }
            chart.branches().forEach { counts[it.ohaeng] = counts.getValue(it.ohaeng) + 1 }
            extra(counts)
            return OhaengDistribution(java.util.Collections.unmodifiableMap(LinkedHashMap(counts)))
        }
    }
}
