package io.github.jaeyeonling.saju.domain

import kotlin.test.Test
import kotlin.test.assertEquals

/** 명리 규칙을 인덱스 산술로 구현한 enum 들의 정확성을 하드코딩 기대값으로 검증한다. */
class DomainArithmeticTest {

    @Test
    fun `오행 상생 — 木火土金水 순환`() {
        assertEquals(Ohaeng.HWA, Ohaeng.MOK.generates())
        assertEquals(Ohaeng.TO, Ohaeng.HWA.generates())
        assertEquals(Ohaeng.GEUM, Ohaeng.TO.generates())
        assertEquals(Ohaeng.SU, Ohaeng.GEUM.generates())
        assertEquals(Ohaeng.MOK, Ohaeng.SU.generates())
    }

    @Test
    fun `오행 상극 — 木土水火金 순환`() {
        assertEquals(Ohaeng.TO, Ohaeng.MOK.controls())
        assertEquals(Ohaeng.SU, Ohaeng.TO.controls())
        assertEquals(Ohaeng.HWA, Ohaeng.SU.controls())
        assertEquals(Ohaeng.GEUM, Ohaeng.HWA.controls())
        assertEquals(Ohaeng.MOK, Ohaeng.GEUM.controls())
    }

    @Test
    fun `천간합 5쌍 — 갑기 을경 병신 정임 무계`() {
        val expected = mapOf(
            Cheongan.GAB to Cheongan.GI,
            Cheongan.EUL to Cheongan.GYEONG,
            Cheongan.BYEONG to Cheongan.SIN,
            Cheongan.JEONG to Cheongan.IM,
            Cheongan.MU to Cheongan.GYE,
        )
        for ((a, b) in expected) {
            assertEquals(b, a.combinePartner(), "$a 의 천간합")
            assertEquals(a, b.combinePartner(), "$b 의 천간합(대칭)")
        }
    }

    @Test
    fun `지지 육충 6쌍 — 자오 축미 인신 묘유 진술 사해`() {
        val pairs = listOf(
            Jiji.JA to Jiji.O, Jiji.CHUK to Jiji.MI, Jiji.IN to Jiji.SHIN,
            Jiji.MYO to Jiji.YU, Jiji.JIN to Jiji.SUL, Jiji.SA to Jiji.HAE,
        )
        for ((a, b) in pairs) {
            assertEquals(b, a.opposite(), "$a 충")
            assertEquals(a, b.opposite(), "$b 충(대칭)")
        }
    }

    @Test
    fun `지지 육합 6쌍 — 자축 인해 묘술 진유 사신 오미`() {
        val pairs = listOf(
            Jiji.JA to Jiji.CHUK, Jiji.IN to Jiji.HAE, Jiji.MYO to Jiji.SUL,
            Jiji.JIN to Jiji.YU, Jiji.SA to Jiji.SHIN, Jiji.O to Jiji.MI,
        )
        for ((a, b) in pairs) {
            assertEquals(b, a.sixCombinePartner(), "$a 육합")
            assertEquals(a, b.sixCombinePartner(), "$b 육합(대칭)")
        }
    }

    @Test
    fun `지지 육해 6쌍 — 자미 축오 인사 묘진 신해 유술`() {
        val pairs = listOf(
            Jiji.JA to Jiji.MI, Jiji.CHUK to Jiji.O, Jiji.IN to Jiji.SA,
            Jiji.MYO to Jiji.JIN, Jiji.SHIN to Jiji.HAE, Jiji.YU to Jiji.SUL,
        )
        for ((a, b) in pairs) {
            assertEquals(b, a.harmPartner(), "$a 육해")
            assertEquals(a, b.harmPartner(), "$b 육해(대칭)")
        }
    }

    @Test
    fun `60갑자 라운드트립 — fromIndex(i)_index == i`() {
        for (i in 0 until GanZhi.CYCLE) {
            assertEquals(i, GanZhi.fromIndex(i).index, "60갑자 index $i")
        }
        assertEquals(GanZhi.CYCLE, GanZhi.ALL.size)
    }

    @Test
    fun `60갑자 경계 — 0=갑자 59=계해, 순환`() {
        assertEquals(GanZhi(Cheongan.GAB, Jiji.JA), GanZhi.fromIndex(0))
        assertEquals(GanZhi(Cheongan.GYE, Jiji.HAE), GanZhi.fromIndex(59))
        // 60 순환, 음수 역행
        assertEquals(GanZhi.fromIndex(0), GanZhi.fromIndex(60))
        assertEquals(GanZhi.fromIndex(59), GanZhi.fromIndex(0).next(-1))
    }
}
