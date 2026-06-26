package io.github.jaeyeonling.saju.interpretation

import io.github.jaeyeonling.saju.domain.JijiHiddenStems
import io.github.jaeyeonling.saju.domain.PillarPosition
import io.github.jaeyeonling.saju.domain.SajuChart

/** 신강신약 판정 결과. */
public enum class SinStrengthVerdict {
    GEUKSIN_GANG, // 극신강
    SIN_GANG, // 신강
    JUNGHWA, // 중화
    SIN_YAK, // 신약
    GEUKSIN_YAK, // 극신약
    ;

    /** 신강 계열(일간이 강함). */
    public val isStrong: Boolean get() = this == GEUKSIN_GANG || this == SIN_GANG
}

/** 신강신약 평가 — 일간을 돕는 세력 비율과 판정. */
public data class SinStrength(
    public val supportRatio: Double,
    public val verdict: SinStrengthVerdict,
)

/**
 * 신강신약 판정 전략. **정답 데이터셋이 없는 영역**이라 가중치가 곧 사양이다.
 * tyme4j 도 이 값을 제공하지 않으므로 결정론성만 보장한다.
 */
public interface SinStrengthStrategy {
    public fun evaluate(chart: SajuChart): SinStrength
}

/**
 * 억부(抑扶) 기본 구현 — 일간을 돕는 세력(비겁·인성) vs 빼는 세력(식상·재성·관성)의 비율.
 *
 * 가중치(디자인 결정 = 사양):
 *  - 월지(月令)는 계절의 힘이라 [MONTH_WEIGHT] 배 가중.
 *  - 천간 1점, 지지는 본기 1점(중기/여기는 v1 미반영 — 단순화).
 *  - 비율 ≥ 0.7 극신강, ≥ 0.55 신강, ≥ 0.45 중화, ≥ 0.3 신약, 그 외 극신약.
 */
public object BueokSinStrengthStrategy : SinStrengthStrategy {
    override fun evaluate(chart: SajuChart): SinStrength {
        val dayMaster = chart.dayMaster
        var support = 0.0
        var total = 0.0

        for (pillar in chart.pillars()) {
            // 일주의 천간(나 자신)은 세력 계산에서 제외.
            val weight = if (pillar.position == PillarPosition.MONTH) MONTH_WEIGHT else 1.0
            if (pillar.position != PillarPosition.DAY) {
                accumulate(SipSeong.of(dayMaster, pillar.gan), weight).let { (s, t) -> support += s; total += t }
            }
            val mainQi = JijiHiddenStems.of(pillar.ji).mainQi
            accumulate(SipSeong.of(dayMaster, mainQi), weight).let { (s, t) -> support += s; total += t }
        }

        val ratio = if (total > 0.0) support / total else NEUTRAL
        return SinStrength(ratio, verdictOf(ratio))
    }

    /** 십성을 돕는 세력(비겁·인성)이면 support, 전체는 total 에 가산. */
    private fun accumulate(sipSeong: SipSeong, weight: Double): Pair<Double, Double> {
        val isSupport = sipSeong == SipSeong.BIGYEON || sipSeong == SipSeong.GEOPJAE ||
            sipSeong == SipSeong.PYEONIN || sipSeong == SipSeong.JEONGIN
        return (if (isSupport) weight else 0.0) to weight
    }

    private fun verdictOf(ratio: Double): SinStrengthVerdict = when {
        ratio >= 0.70 -> SinStrengthVerdict.GEUKSIN_GANG
        ratio >= 0.55 -> SinStrengthVerdict.SIN_GANG
        ratio >= 0.45 -> SinStrengthVerdict.JUNGHWA
        ratio >= 0.30 -> SinStrengthVerdict.SIN_YAK
        else -> SinStrengthVerdict.GEUKSIN_YAK
    }

    private const val MONTH_WEIGHT = 2.0
    private const val NEUTRAL = 0.5
}
