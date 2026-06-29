package io.github.jaeyeonling.saju.domain

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class SajuChartTest : StringSpec({
    "4기둥으로 사주판을 만들고 일간을 얻는다" {
        // Arrange: 갑자·을축·병인·정묘
        val chart =
            SajuChart(
                year = Pillar(PillarPosition.YEAR, GanZhi.fromIndex(0)),
                month = Pillar(PillarPosition.MONTH, GanZhi.fromIndex(1)),
                day = Pillar(PillarPosition.DAY, GanZhi.fromIndex(2)),
                hour = Pillar(PillarPosition.HOUR, GanZhi.fromIndex(3)),
            )

        // Assert: 일간은 일주(병인)의 천간 = 병(丙)
        chart.dayMaster shouldBe Cheongan.BYEONG
        chart.day.ji shouldBe Jiji.IN
        chart.pillars().size shouldBe 4
        chart.branches() shouldBe listOf(Jiji.JA, Jiji.CHUK, Jiji.IN, Jiji.MYO)
    }
})
