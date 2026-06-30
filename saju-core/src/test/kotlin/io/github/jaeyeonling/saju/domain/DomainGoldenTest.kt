package io.github.jaeyeonling.saju.domain

import io.github.jaeyeonling.saju.Golden
import io.kotest.assertions.withClue
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

/** 천간·지지·60갑자의 오행/음양/순서를 동결된 골든 벡터와 전수 대조한다. */
class DomainGoldenTest : StringSpec({

    "천간 10개의 오행·음양이 골든 벡터와 일치" {
        for (row in Golden.rows("domain_stems.csv")) {
            val i = row[0].toInt()
            val mine = Cheongan.entries[i]
            withClue("천간 $i 오행") { mine.ohaeng.ordinal shouldBe row[1].toInt() }
            withClue("천간 $i 음양") { mine.eumyang.isYang shouldBe row[2].toBoolean() }
        }
    }

    "지지 12개의 오행·음양이 골든 벡터와 일치" {
        for (row in Golden.rows("domain_branches.csv")) {
            val i = row[0].toInt()
            val mine = Jiji.entries[i]
            withClue("지지 $i 오행") { mine.ohaeng.ordinal shouldBe row[1].toInt() }
            withClue("지지 $i 음양") { mine.eumyang.isYang shouldBe row[2].toBoolean() }
        }
    }

    "60갑자 순서가 골든 벡터와 일치" {
        for (row in Golden.rows("domain_cycle.csv")) {
            val i = row[0].toInt()
            val mine = Ganji.fromIndex(i)
            withClue("60갑자 $i 천간") { mine.gan.ordinal shouldBe row[1].toInt() }
            withClue("60갑자 $i 지지") { mine.ji.ordinal shouldBe row[2].toInt() }
        }
    }
})
