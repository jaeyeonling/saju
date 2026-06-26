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
 *  - 천간 1점, 지지는 지장간 본기·중기·여기를 차등 가중([MAIN_QI_WEIGHT]/[MID_QI_WEIGHT]/[RESIDUAL_QI_WEIGHT]).
 *  - 비율 ≥ 0.7 극신강, ≥ 0.55 신강, ≥ 0.45 중화, ≥ 0.3 신약, 그 외 극신약 (5단계 모두 도달 가능).
 */
public object BueokSinStrengthStrategy : SinStrengthStrategy {
    override fun evaluate(chart: SajuChart): SinStrength {
        val dayMaster = chart.dayMaster
        var support = 0.0
        var total = 0.0

        for (pillar in chart.pillars()) {
            val pillarWeight = if (pillar.position == PillarPosition.MONTH) MONTH_WEIGHT else 1.0
            // 일주의 천간(나 자신)은 세력 계산에서 제외.
            if (pillar.position != PillarPosition.DAY) {
                add(SipSeong.of(dayMaster, pillar.gan), pillarWeight) { s, t -> support += s; total += t }
            }
            // 지장간 본기·중기·여기를 차등 가중으로 반영(연속 ratio → 5단계 verdict 모두 도달 가능).
            val hidden = JijiHiddenStems.of(pillar.ji)
            add(SipSeong.of(dayMaster, hidden.mainQi), pillarWeight * MAIN_QI_WEIGHT) { s, t -> support += s; total += t }
            hidden.midQi?.let { add(SipSeong.of(dayMaster, it), pillarWeight * MID_QI_WEIGHT) { s, t -> support += s; total += t } }
            hidden.residualQi?.let { add(SipSeong.of(dayMaster, it), pillarWeight * RESIDUAL_QI_WEIGHT) { s, t -> support += s; total += t } }
        }

        val ratio = if (total > 0.0) support / total else NEUTRAL
        return SinStrength(ratio, verdictOf(ratio))
    }

    /** 십성을 돕는 세력(비겁·인성)이면 support, 전체는 total 에 가산. */
    private inline fun add(sipSeong: SipSeong, weight: Double, accumulate: (Double, Double) -> Unit) {
        val support = if (isSupport(sipSeong)) weight else 0.0
        accumulate(support, weight)
    }

    private fun isSupport(sipSeong: SipSeong): Boolean =
        sipSeong == SipSeong.BIGYEON || sipSeong == SipSeong.GEOPJAE ||
            sipSeong == SipSeong.PYEONIN || sipSeong == SipSeong.JEONGIN

    private fun verdictOf(ratio: Double): SinStrengthVerdict = when {
        ratio >= 0.70 -> SinStrengthVerdict.GEUKSIN_GANG
        ratio >= 0.55 -> SinStrengthVerdict.SIN_GANG
        ratio >= 0.45 -> SinStrengthVerdict.JUNGHWA
        ratio >= 0.30 -> SinStrengthVerdict.SIN_YAK
        else -> SinStrengthVerdict.GEUKSIN_YAK
    }

    private const val MONTH_WEIGHT = 2.0
    private const val NEUTRAL = 0.5

    // 지장간 본기/중기/여기 차등 가중 — 본기가 가장 세고 여기가 가장 약하다.
    private const val MAIN_QI_WEIGHT = 1.0
    private const val MID_QI_WEIGHT = 0.4
    private const val RESIDUAL_QI_WEIGHT = 0.2
}
