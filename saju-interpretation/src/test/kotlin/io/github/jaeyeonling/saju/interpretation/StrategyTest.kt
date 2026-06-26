package io.github.jaeyeonling.saju.interpretation

import com.tyme.sixtycycle.EarthBranch
import com.tyme.sixtycycle.HeavenStem
import io.github.jaeyeonling.saju.domain.Cheongan
import io.github.jaeyeonling.saju.domain.GanZhi
import io.github.jaeyeonling.saju.domain.Jiji
import io.github.jaeyeonling.saju.domain.JijiHiddenStems
import io.github.jaeyeonling.saju.domain.Pillar
import io.github.jaeyeonling.saju.domain.PillarPosition
import io.github.jaeyeonling.saju.domain.SajuChart
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class StrategyTest {

    @Test
    fun `십이운성 120조합이 tyme4j 와 일치 (음포태)`() {
        for (stem in 0 until 10) {
            for (branch in 0 until 12) {
                val tyme = HeavenStem.fromIndex(stem).getTerrain(EarthBranch.fromIndex(branch))
                val mine = EumPotaeStrategy.stageOf(Cheongan.entries[stem], Jiji.entries[branch])
                assertEquals(tyme.index, mine.ordinal, "천간 $stem 지지 $branch 십이운성")
            }
        }
    }

    @Test
    fun `지장간 본기가 tyme4j 와 일치`() {
        for (branch in 0 until 12) {
            val tyme = EarthBranch.fromIndex(branch).hideHeavenStemMain
            val mine = JijiHiddenStems.of(Jiji.entries[branch]).mainQi
            assertEquals(tyme.index, mine.ordinal, "지지 $branch 본기")
        }
    }

    @Test
    fun `신강신약·용신·격국은 결정론적이다`() {
        // 같은 입력 → 같은 출력 (정답셋 없는 영역은 결정론성만 보장).
        val chart = sampleChart()
        val ctx = InterpretationContext.DEFAULT

        val strength1 = ctx.sinStrength.evaluate(chart)
        val strength2 = ctx.sinStrength.evaluate(chart)
        assertEquals(strength1, strength2)

        val yongsin = ctx.yongsin.derive(chart, strength1)
        assertEquals(yongsin, ctx.yongsin.derive(chart, strength1))

        val gyeokguk = ctx.gyeokguk.classify(chart)
        assertEquals(gyeokguk, ctx.gyeokguk.classify(chart))
        assertTrue(gyeokguk.name.endsWith("격"), "격국 이름: ${gyeokguk.name}")
    }

    @Test
    fun `신강신약 5단계가 모두 도달 가능하다 (JUNGHWA dead code 회귀)`() {
        // 60갑자 조합으로 다양한 사주를 생성해 verdict 분포를 본다 — JUNGHWA 포함 5단계 모두 나와야 한다.
        val verdicts = mutableSetOf<SinStrengthVerdict>()
        for (y in 0 until 60 step 7) {
            for (mo in 0 until 60 step 11) {
                for (d in 0 until 60 step 13) {
                    for (h in 0 until 60 step 17) {
                        val chart = SajuChart(
                            Pillar(PillarPosition.YEAR, GanZhi.fromIndex(y)),
                            Pillar(PillarPosition.MONTH, GanZhi.fromIndex(mo)),
                            Pillar(PillarPosition.DAY, GanZhi.fromIndex(d)),
                            Pillar(PillarPosition.HOUR, GanZhi.fromIndex(h)),
                        )
                        verdicts.add(BueokSinStrengthStrategy.evaluate(chart).verdict)
                    }
                }
            }
        }
        assertEquals(SinStrengthVerdict.entries.toSet(), verdicts, "5단계 verdict 모두 도달: $verdicts")
    }

    @Test
    fun `비겁·인성이 많으면 신강으로 판정된다`() {
        // 일간 갑목, 주변에 목·수(비겁·인성) 가득 → 신강.
        val strongWood = SajuChart(
            year = Pillar(PillarPosition.YEAR, GanZhi(Cheongan.GAB, Jiji.JA)), // 갑자(목/수)
            month = Pillar(PillarPosition.MONTH, GanZhi(Cheongan.GAB, Jiji.IN)), // 갑인(목/목)
            day = Pillar(PillarPosition.DAY, GanZhi(Cheongan.GAB, Jiji.JA)), // 갑자
            hour = Pillar(PillarPosition.HOUR, GanZhi(Cheongan.GYE, Jiji.HAE)), // 계해(수/수)
        )
        val strength = BueokSinStrengthStrategy.evaluate(strongWood)
        assertTrue(strength.verdict.isStrong, "목 일간 + 목수 다수 → 신강이어야: ${strength.verdict} (${strength.supportRatio})")
    }

    private fun sampleChart() = SajuChart(
        year = Pillar(PillarPosition.YEAR, GanZhi.fromIndex(0)),
        month = Pillar(PillarPosition.MONTH, GanZhi.fromIndex(20)),
        day = Pillar(PillarPosition.DAY, GanZhi.fromIndex(40)),
        hour = Pillar(PillarPosition.HOUR, GanZhi.fromIndex(10)),
    )
}
