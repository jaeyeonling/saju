package io.github.jaeyeonling.saju.astronomy

import io.kotest.assertions.withClue
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.doubles.plusOrMinus
import io.kotest.matchers.shouldBe

class LunarPhaseTest : StringSpec({

    fun abs(x: Double) = if (x < 0) -x else x

    "삭에서 달·태양 황경이 일치한다" {
        // 삭의 정의: 달황경 = 태양황경.
        for (k in 300..320) {
            val tt = LunarPhase.newMoonInstantTT(k)
            val diff = wrapToPi(
                MoonPosition.apparentLongitudeRad(tt) - SunPosition.apparentLongitudeRad(tt),
            )
            withClue("k=$k 황경차 ${diff}rad") { (abs(diff) < 1e-6).shouldBeTrue() }
        }
    }

    "삭 간격은 약 29_53일이다" {
        val a = LunarPhase.newMoonInstantUT(300)
        val b = LunarPhase.newMoonInstantUT(301)
        (b - a) shouldBe (29.53 plusOrMinus 0.5)
    }

    "k=0 삭은 2000년 1월 6일 경이다" {
        val date = JulianDayConverter.toGregorian(LunarPhase.newMoonInstantUT(0))
        date.year shouldBe 2000
        date.month shouldBe 1
        withClue("신월 날짜: ${date.month}/${date.day}") { (date.day in 6..7).shouldBeTrue() }
    }
})
