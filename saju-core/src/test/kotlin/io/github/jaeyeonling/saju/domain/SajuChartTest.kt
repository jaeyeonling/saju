package io.github.jaeyeonling.saju.domain

import kotlin.test.Test
import kotlin.test.assertEquals

class SajuChartTest {
    @Test
    fun `4기둥으로 사주판을 만들고 일간을 얻는다`() {
        // Arrange: 갑자·을축·병인·정묘
        val chart = SajuChart(
            year = Pillar(PillarPosition.YEAR, GanZhi.fromIndex(0)),
            month = Pillar(PillarPosition.MONTH, GanZhi.fromIndex(1)),
            day = Pillar(PillarPosition.DAY, GanZhi.fromIndex(2)),
            hour = Pillar(PillarPosition.HOUR, GanZhi.fromIndex(3)),
        )

        // Assert: 일간은 일주(병인)의 천간 = 병(丙)
        assertEquals(Cheongan.BYEONG, chart.dayMaster)
        assertEquals(Jiji.IN, chart.day.ji)
        assertEquals(4, chart.pillars().size)
        assertEquals(listOf(Jiji.JA, Jiji.CHUK, Jiji.IN, Jiji.MYO), chart.branches())
    }
}
