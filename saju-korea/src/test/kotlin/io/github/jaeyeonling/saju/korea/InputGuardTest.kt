package io.github.jaeyeonling.saju.korea

import io.github.jaeyeonling.saju.Saju
import io.github.jaeyeonling.saju.domain.ZishiPolicy
import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlin.test.assertNotEquals

/** 공개 진입점 입력 가드 회귀 — 잘못된 입력이 조용히 오답을 내지 않고 fail-fast 하는지. */
class InputGuardTest {

    @Test
    fun `Saju fromLocalDateTime 이 잘못된 시각을 거부한다`() {
        assertFailsWith<IllegalArgumentException> { Saju.fromLocalDateTime(2000, 13, 1, 7, 0, 9.0) } // 월
        assertFailsWith<IllegalArgumentException> { Saju.fromLocalDateTime(2000, 1, 32, 7, 0, 9.0) } // 일
        assertFailsWith<IllegalArgumentException> { Saju.fromLocalDateTime(2000, 1, 1, 25, 0, 9.0) } // 시
        assertFailsWith<IllegalArgumentException> { Saju.fromLocalDateTime(2000, 1, 1, 7, 60, 9.0) } // 분
        assertFailsWith<IllegalArgumentException> { Saju.fromLocalDateTime(1800, 1, 1, 7, 0, 9.0) } // 범위 밖
        assertFailsWith<IllegalArgumentException> { Saju.fromLocalDateTime(2000, 1, 1, 7, 0, Double.NaN) } // NaN offset
    }

    @Test
    fun `KoreanSaju 진입점들이 잘못된 입력을 거부한다`() {
        assertFailsWith<IllegalArgumentException> { KoreanSaju.fromCivilTime(2000, 13, 1, 7, 0) }
        assertFailsWith<IllegalArgumentException> { KoreanSaju.daeun(2000, 1, 1, 25, 0, isMale = true) }
        assertFailsWith<IllegalArgumentException> { KoreanSaju.trueSolarOffsetMinutes(2000, 1, 1, 7, 0, Double.POSITIVE_INFINITY) }
        // hour=25 가 hour=1 과 같은 시지로 충돌하던 조용한 오답을 차단
        assertFailsWith<IllegalArgumentException> { KoreanSaju.fromCivilTime(2000, 1, 1, 7, 0, 999.0) } // 경도 밖
    }

    @Test
    fun `자시 학파에 따라 23시 일주가 갈린다 (ZishiPolicy 배선)`() {
        // 정자시: 23시→다음날 일주, 야자시: 당일 일주. 학파가 실제로 도달 가능해야 한다.
        val jeongjasi = Saju.fromLocalDateTime(2000, 1, 1, 23, 30, 9.0, ZishiPolicy.JEONGJASI)
        val yajasi = Saju.fromLocalDateTime(2000, 1, 1, 23, 30, 9.0, ZishiPolicy.YAJASI)
        assertNotEquals(jeongjasi.day.ganZhi, yajasi.day.ganZhi, "23시 일주가 자시 학파별로 달라야 한다")
    }
}
