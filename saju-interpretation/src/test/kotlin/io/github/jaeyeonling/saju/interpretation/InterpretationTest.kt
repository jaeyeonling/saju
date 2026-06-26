package io.github.jaeyeonling.saju.interpretation

import com.tyme.sixtycycle.HeavenStem
import io.github.jaeyeonling.saju.domain.Cheongan
import io.github.jaeyeonling.saju.domain.GanZhi
import io.github.jaeyeonling.saju.domain.Jiji
import io.github.jaeyeonling.saju.domain.Ohaeng
import io.github.jaeyeonling.saju.domain.Pillar
import io.github.jaeyeonling.saju.domain.PillarPosition
import io.github.jaeyeonling.saju.domain.SajuChart
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class InterpretationTest {

    @Test
    fun `십성 100조합이 tyme4j 와 일치`() {
        for (day in 0 until 10) {
            for (target in 0 until 10) {
                val tyme = HeavenStem.fromIndex(day).getTenStar(HeavenStem.fromIndex(target))
                val mine = SipSeong.of(Cheongan.entries[day], Cheongan.entries[target])
                assertEquals(tyme.index, mine.ordinal, "일간 $day 대상 $target 십성")
            }
        }
    }

    @Test
    fun `공망 — 갑자순 술해, 갑술순 신유`() {
        assertEquals(Jiji.SUL to Jiji.HAE, Gongmang.of(GanZhi.fromIndex(0))) // 갑자
        assertEquals(Jiji.SUL to Jiji.HAE, Gongmang.of(GanZhi.fromIndex(5))) // 기사(같은 순)
        assertEquals(Jiji.SHIN to Jiji.YU, Gongmang.of(GanZhi.fromIndex(10))) // 갑술
    }

    @Test
    fun `천간합 — 갑기합토 탐지`() {
        val relations = HapChungDetector.detect(listOf(Cheongan.GAB, Cheongan.GI), emptyList())
        val hap = relations.filterIsInstance<HapChungRelation.CheonganHap>().single()
        assertEquals(Ohaeng.TO, hap.transformsTo)
    }

    @Test
    fun `지지 삼합 — 신자진 수국`() {
        val relations = HapChungDetector.detect(emptyList(), listOf(Jiji.SHIN, Jiji.JA, Jiji.JIN))
        val samhap = relations.filterIsInstance<HapChungRelation.JijiSamhap>().single()
        assertEquals(Ohaeng.SU, samhap.transformsTo)
    }

    @Test
    fun `지지 육충·육합 탐지`() {
        val chung = HapChungDetector.detect(emptyList(), listOf(Jiji.JA, Jiji.O))
        assertTrue(chung.any { it is HapChungRelation.JijiYukchung }, "자오충")
        val hap = HapChungDetector.detect(emptyList(), listOf(Jiji.JA, Jiji.CHUK))
        assertTrue(hap.any { it is HapChungRelation.JijiYukhap }, "자축합")
    }

    @Test
    fun `오행 분포 합은 8글자`() {
        val chart = SajuChart(
            year = Pillar(PillarPosition.YEAR, GanZhi.fromIndex(0)),
            month = Pillar(PillarPosition.MONTH, GanZhi.fromIndex(15)),
            day = Pillar(PillarPosition.DAY, GanZhi.fromIndex(30)),
            hour = Pillar(PillarPosition.HOUR, GanZhi.fromIndex(45)),
        )
        val distribution = OhaengDistribution.from(chart)
        assertEquals(8, distribution.counts.values.sum())
    }
}
