package io.github.jaeyeonling.saju.derivation

import io.github.jaeyeonling.saju.domain.Cheongan
import io.github.jaeyeonling.saju.domain.GanZhi
import io.github.jaeyeonling.saju.domain.Jiji
import kotlin.test.Test
import kotlin.test.assertEquals

class PillarDerivationTest {

    @Test
    fun `연주 — 1984 갑자년, 2026 병오년`() {
        assertEquals(GanZhi(Cheongan.GAB, Jiji.JA), PillarDerivation.yearPillar(1984))
        assertEquals(GanZhi(Cheongan.BYEONG, Jiji.O), PillarDerivation.yearPillar(2026))
    }

    @Test
    fun `월주 오호둔 — 갑기년 병인월 시작`() {
        // 갑기년 정월(인월)은 병인. 을경년은 무인, 무계년은 갑인.
        assertEquals(GanZhi(Cheongan.BYEONG, Jiji.IN), PillarDerivation.monthPillar(Cheongan.GAB, 0))
        assertEquals(GanZhi(Cheongan.BYEONG, Jiji.IN), PillarDerivation.monthPillar(Cheongan.GI, 0))
        assertEquals(GanZhi(Cheongan.MU, Jiji.IN), PillarDerivation.monthPillar(Cheongan.EUL, 0))
        assertEquals(GanZhi(Cheongan.GAB, Jiji.IN), PillarDerivation.monthPillar(Cheongan.MU, 0))
        // 갑년 축월(offset 11) = 정축
        assertEquals(GanZhi(Cheongan.JEONG, Jiji.CHUK), PillarDerivation.monthPillar(Cheongan.GAB, 11))
    }

    @Test
    fun `시주 오자둔 — 갑기일 갑자시 시작`() {
        // 갑기일 자시는 갑자, 무계일 자시는 임자.
        assertEquals(GanZhi(Cheongan.GAB, Jiji.JA), PillarDerivation.hourPillar(Cheongan.GAB, Jiji.JA))
        assertEquals(GanZhi(Cheongan.IM, Jiji.JA), PillarDerivation.hourPillar(Cheongan.MU, Jiji.JA))
        // 갑일 오시(offset 6) = 경오
        assertEquals(GanZhi(Cheongan.GYEONG, Jiji.O), PillarDerivation.hourPillar(Cheongan.GAB, Jiji.O))
    }
}
