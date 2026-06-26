package io.github.jaeyeonling.saju.domain

import kotlin.test.Test
import kotlin.test.assertEquals

class SajuChartTest {
    @Test
    fun `스켈레톤 SajuChart 가 생성된다`() {
        // Arrange & Act
        val chart = SajuChart()

        // Assert
        assertEquals("P0", chart.skeletonMarker)
    }
}
