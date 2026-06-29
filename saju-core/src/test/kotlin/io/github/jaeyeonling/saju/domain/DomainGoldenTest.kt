package io.github.jaeyeonling.saju.domain

import com.tyme.enums.YinYang
import com.tyme.sixtycycle.EarthBranch
import com.tyme.sixtycycle.HeavenStem
import com.tyme.sixtycycle.SixtyCycle
import io.kotest.assertions.withClue
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

/** 천간·지지·60갑자의 오행/음양/순서를 검증된 tyme4j 와 전수 대조한다. */
class DomainGoldenTest : StringSpec({

    "천간 10개의 오행·음양이 tyme4j 와 일치" {
        for (i in Cheongan.entries.indices) {
            val tyme = HeavenStem.fromIndex(i)
            val mine = Cheongan.entries[i]
            withClue("천간 $i(${tyme.name}) 오행") { mine.ohaeng.ordinal shouldBe tyme.element.index }
            withClue("천간 $i(${tyme.name}) 음양") { mine.eumyang.isYang shouldBe (tyme.yinYang == YinYang.YANG) }
        }
    }

    "지지 12개의 오행·음양이 tyme4j 와 일치" {
        for (i in Jiji.entries.indices) {
            val tyme = EarthBranch.fromIndex(i)
            val mine = Jiji.entries[i]
            withClue("지지 $i(${tyme.name}) 오행") { mine.ohaeng.ordinal shouldBe tyme.element.index }
            withClue("지지 $i(${tyme.name}) 음양") { mine.eumyang.isYang shouldBe (tyme.yinYang == YinYang.YANG) }
        }
    }

    "60갑자 순서가 tyme4j 와 일치" {
        for (i in 0 until GanZhi.CYCLE) {
            val tyme = SixtyCycle.fromIndex(i)
            val mine = GanZhi.fromIndex(i)
            withClue("60갑자 $i(${tyme.name}) 천간") { mine.gan.ordinal shouldBe tyme.heavenStem.index }
            withClue("60갑자 $i(${tyme.name}) 지지") { mine.ji.ordinal shouldBe tyme.earthBranch.index }
        }
    }
})
