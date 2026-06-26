package io.github.jaeyeonling.saju.domain

import com.tyme.enums.YinYang
import com.tyme.sixtycycle.EarthBranch
import com.tyme.sixtycycle.HeavenStem
import com.tyme.sixtycycle.SixtyCycle
import kotlin.test.Test
import kotlin.test.assertEquals

/** 천간·지지·60갑자의 오행/음양/순서를 검증된 tyme4j 와 전수 대조한다. */
class DomainGoldenTest {

    @Test
    fun `천간 10개의 오행·음양이 tyme4j 와 일치`() {
        for (i in Cheongan.entries.indices) {
            val tyme = HeavenStem.fromIndex(i)
            val mine = Cheongan.entries[i]
            assertEquals(tyme.element.index, mine.ohaeng.ordinal, "천간 $i(${tyme.name}) 오행")
            assertEquals(tyme.yinYang == YinYang.YANG, mine.eumyang.isYang, "천간 $i(${tyme.name}) 음양")
        }
    }

    @Test
    fun `지지 12개의 오행·음양이 tyme4j 와 일치`() {
        for (i in Jiji.entries.indices) {
            val tyme = EarthBranch.fromIndex(i)
            val mine = Jiji.entries[i]
            assertEquals(tyme.element.index, mine.ohaeng.ordinal, "지지 $i(${tyme.name}) 오행")
            assertEquals(tyme.yinYang == YinYang.YANG, mine.eumyang.isYang, "지지 $i(${tyme.name}) 음양")
        }
    }

    @Test
    fun `60갑자 순서가 tyme4j 와 일치`() {
        for (i in 0 until GanZhi.CYCLE) {
            val tyme = SixtyCycle.fromIndex(i)
            val mine = GanZhi.fromIndex(i)
            assertEquals(tyme.heavenStem.index, mine.gan.ordinal, "60갑자 $i(${tyme.name}) 천간")
            assertEquals(tyme.earthBranch.index, mine.ji.ordinal, "60갑자 $i(${tyme.name}) 지지")
        }
    }
}
