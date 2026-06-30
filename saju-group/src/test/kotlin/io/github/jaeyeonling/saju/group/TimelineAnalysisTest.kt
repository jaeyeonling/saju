package io.github.jaeyeonling.saju.group

import io.github.jaeyeonling.saju.derivation.Daeun
import io.github.jaeyeonling.saju.derivation.Seun
import io.github.jaeyeonling.saju.domain.Cheongan
import io.github.jaeyeonling.saju.domain.Ganji
import io.github.jaeyeonling.saju.domain.Jiji
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.maps.shouldNotContainKey
import io.kotest.matchers.shouldBe

/** 그룹 타임라인 — timeline.py 이식. 만나이 = seunYear - birthYear, 전환기 = 대운 경계 ±윈도우. */
class TimelineAnalysisTest : StringSpec({
    "현재 대운과 전환기 — 만나이 기준, birthYear 0은 대운 스킵" {
        val a =
            memberOf(
                "a",
                uniformChart(Cheongan.GAP, Jiji.JA),
                birthYear = 1991,
                daeun =
                    listOf(
                        Daeun(25, Ganji.fromIndex(0)),
                        Daeun(35, Ganji.fromIndex(1)),
                        Daeun(45, Ganji.fromIndex(2)),
                    ),
                seun = Seun(2026, Ganji.fromIndex(42)),
            )
        val b = memberOf("b", uniformChart(Cheongan.BYEONG, Jiji.O)) // birthYear 0

        val timeline = TimelineAnalysis.analyze(listOf(a, b), 2026, GroupContext.DEFAULT)

        timeline.year shouldBe 2026
        timeline.groupSeun shouldBe Ganji.fromIndex(42)
        timeline.currentDaeun.getValue("a").startAge shouldBe 35 // 35 <= 35 < 45
        timeline.daeunTransitions.first { it.memberId == "a" }.startAge shouldBe 35 // |35-35| <= 1
        timeline.currentDaeun shouldNotContainKey "b"
        timeline.memberSeunSipseong.keys shouldBe setOf("a", "b")
    }

    "세운 정보가 없으면 세운 십성은 null, 공통 세운도 null" {
        val a = memberOf("a", uniformChart(Cheongan.GAP, Jiji.JA), birthYear = 1990)
        val b = memberOf("b", uniformChart(Cheongan.BYEONG, Jiji.O))
        val timeline = TimelineAnalysis.analyze(listOf(a, b), 2026, GroupContext.DEFAULT)

        timeline.memberSeunSipseong.getValue("a").sipSeong shouldBe null
        timeline.groupSeun shouldBe null
    }
})
