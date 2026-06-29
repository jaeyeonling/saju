package io.github.jaeyeonling.saju.interpretation

import io.github.jaeyeonling.saju.domain.Jiji
import io.github.jaeyeonling.saju.domain.PillarPosition
import io.github.jaeyeonling.saju.domain.SajuChart

/**
 * 한 사주판의 해석 묶음 결과 — 신강신약·용신·격국·공망·합충·오행분포·십이운성을 한 번에 담는다.
 *
 * [sibiUnseong] 은 일간 기준 네 기둥 지지의 십이운성(연·월·일·시).
 */
public data class InterpretationReport(
    public val strength: SinStrength,
    public val yongsin: YongsinResult,
    public val gyeokguk: GyeokgukResult,
    public val gongmang: Pair<Jiji, Jiji>,
    public val hapChung: List<HapChungRelation>,
    public val ohaeng: OhaengDistribution,
    public val sibiUnseong: Map<PillarPosition, SibiUnseong>,
)

/**
 * 해석 파사드 — 사주판 + [InterpretationContext] → [InterpretationReport].
 *
 * 각 전략을 따로 조립하던 호출 부담을 한 줄로 줄인다(전략 의존 순서: 용신은 신강신약 결과에 종속).
 * 판 도출(Saju/KoreanSaju)과 해석을 분리한 채로 둔 얇은 조립층이다.
 */
public object Interpretation {
    @JvmStatic
    @JvmOverloads
    public fun of(
        chart: SajuChart,
        ctx: InterpretationContext = InterpretationContext.DEFAULT,
    ): InterpretationReport {
        val strength = ctx.sinStrength.evaluate(chart)
        return InterpretationReport(
            strength = strength,
            yongsin = ctx.yongsin.derive(chart, strength),
            gyeokguk = ctx.gyeokguk.classify(chart),
            gongmang = Gongmang.of(chart.day.ganZhi),
            hapChung = ctx.hapChung.detect(chart.stems(), chart.branches()),
            ohaeng = OhaengDistribution.from(chart),
            sibiUnseong = chart.pillars().associate { it.position to ctx.sibiUnseong.stageOf(chart.dayMaster, it.ji) },
        )
    }
}
