package io.github.jaeyeonling.saju.interpretation

import io.github.jaeyeonling.saju.domain.Jiji
import io.github.jaeyeonling.saju.domain.JijiHiddenStems
import io.github.jaeyeonling.saju.domain.PillarPosition
import io.github.jaeyeonling.saju.domain.SajuChart

/**
 * 한 사주판의 해석 묶음 결과 — 신강신약·용신·격국·공망·합충·오행분포·십이운성·십성·지장간·신살을 한 번에 담는다.
 *
 * 네 기둥 기준 정보([sibiUnseong]·[sipSeong]·[hiddenStems]·[sinSal])는 모두 일간 기준 연·월·일·시 맵이다.
 * 십성·지장간·신살·가중오행은 표시/직렬화가 재계산 없이 공유하도록 여기 한 곳에 담는다(단일 진실 소스).
 */
public data class InterpretationReport(
    public val strength: SinStrength,
    public val yongsin: YongsinResult,
    public val gyeokguk: GyeokgukResult,
    public val gongmang: Pair<Jiji, Jiji>,
    public val hapChung: List<HapChungRelation>,
    public val ohaeng: OhaengDistribution,
    public val sibiUnseong: Map<PillarPosition, SibiUnseong>,
    /** 네 기둥의 십성(천간 + 지장간 본·중·여). */
    public val sipSeong: Map<PillarPosition, PillarSipSeong> = emptyMap(),
    /** 네 기둥 지지의 지장간(본·중·여). */
    public val hiddenStems: Map<PillarPosition, JijiHiddenStems> = emptyMap(),
    /** 네 기둥에 깃든 신살(없으면 빈 리스트). */
    public val sinSal: Map<PillarPosition, List<SinSal>> = emptyMap(),
    /** 지장간까지 가중한 오행 분포(표면 [ohaeng] 과 별개). */
    public val ohaengWeighted: OhaengDistribution = ohaeng,
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
        val pillars = chart.pillars()
        return InterpretationReport(
            strength = strength,
            yongsin = ctx.yongsin.derive(chart, strength),
            gyeokguk = ctx.gyeokguk.classify(chart),
            gongmang = Gongmang.of(chart.day.ganji),
            hapChung = ctx.hapChung.detect(chart.stems(), chart.branches()),
            ohaeng = OhaengDistribution.from(chart),
            sibiUnseong = pillars.associate { it.position to ctx.sibiUnseong.stageOf(chart.dayMaster, it.ji) },
            sipSeong = pillars.associate { it.position to PillarSipSeong.of(chart.dayMaster, it) },
            hiddenStems = pillars.associate { it.position to JijiHiddenStems.of(it.ji) },
            sinSal = SinSalFinder.find(chart),
            ohaengWeighted = OhaengDistribution.weighted(chart),
        )
    }
}
