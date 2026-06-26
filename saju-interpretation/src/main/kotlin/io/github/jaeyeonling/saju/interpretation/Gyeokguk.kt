package io.github.jaeyeonling.saju.interpretation

import io.github.jaeyeonling.saju.domain.JijiHiddenStems
import io.github.jaeyeonling.saju.domain.SajuChart

/** 격국 분류 결과. */
public data class GyeokgukResult(
    public val name: String,
    public val basis: String,
)

/** 격국 분류 전략. 8격/10격, 투출 우선 vs 절입 기준이 유파마다 달라 전략화한다. */
public interface GyeokgukStrategy {
    public fun classify(chart: SajuChart): GyeokgukResult
}

/**
 * 자평(子平) 기본 구현 — **월지 지장간 본기의 십성**으로 격을 정한다(단순화).
 *
 * 정교한 자평진전은 '월지 지장간이 천간에 투출'을 우선하지만, v1 기본은 월령 본기 십성으로 고정한다.
 * 비겁이면 건록/양인격으로 본다.
 */
public object JapyeongGyeokgukStrategy : GyeokgukStrategy {
    override fun classify(chart: SajuChart): GyeokgukResult {
        val monthMainQi = JijiHiddenStems.of(chart.month.ji).mainQi
        val sipSeong = SipSeong.of(chart.dayMaster, monthMainQi)
        return GyeokgukResult(nameOf(sipSeong), "월지(${chart.month.ji}) 본기 $monthMainQi 의 $sipSeong")
    }

    private fun nameOf(sipSeong: SipSeong): String = when (sipSeong) {
        SipSeong.BIGYEON -> "건록격"
        SipSeong.GEOPJAE -> "양인격"
        SipSeong.SIKSIN -> "식신격"
        SipSeong.SANGGWAN -> "상관격"
        SipSeong.PYEONJAE -> "편재격"
        SipSeong.JEONGJAE -> "정재격"
        SipSeong.PYEONGWAN -> "편관격"
        SipSeong.JEONGGWAN -> "정관격"
        SipSeong.PYEONIN -> "편인격"
        SipSeong.JEONGIN -> "정인격"
    }
}
