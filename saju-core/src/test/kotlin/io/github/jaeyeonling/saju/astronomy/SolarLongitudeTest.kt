package io.github.jaeyeonling.saju.astronomy

import io.kotest.assertions.withClue
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.shouldBe

private fun abs(x: Double) = if (x < 0) -x else x

class SolarLongitudeTest : StringSpec({

    "절입 순간의 황경은 정확히 목표값이다" {
        // 역산이 수렴하면, 그 순간의 황경은 15°·termIndex 와 일치해야 한다.
        for (termIndex in 0 until 24) {
            val tt = SolarLongitude.solarTermInstantTT(2026, termIndex)
            val lonDeg = SunPosition.apparentLongitudeRad(tt) * RAD_TO_DEG
            val targetDeg = (termIndex * 15.0) % 360.0
            val diff = minOf(abs(lonDeg - targetDeg), 360.0 - abs(lonDeg - targetDeg))
            withClue("termIndex=$termIndex 황경 오차 ${diff * 3600}″") { (diff < 1.0 / 3600.0).shouldBeTrue() }
        }
    }

    "2000년 춘분(termIndex 0)은 3월 20일 근처다" {
        // 실제 2000년 춘분: 2000-03-20 07:35 UTC.
        val ut = SolarLongitude.solarTermInstantUT(2000, 0)
        val date = JulianDayConverter.toGregorian(ut)
        date.year shouldBe 2000
        date.month shouldBe 3
        withClue("춘분 날짜: ${date.month}/${date.day}") { (date.day in 19..21).shouldBeTrue() }
    }

    "2026년 입춘(termIndex 21)은 2월 4일 근처다" {
        // 입춘은 매년 2월 3~5일.
        val ut = SolarLongitude.solarTermInstantUT(2026, 21)
        val date = JulianDayConverter.toGregorian(ut)
        date.year shouldBe 2026
        date.month shouldBe 2
        withClue("입춘 날짜: ${date.month}/${date.day}") { (date.day in 3..5).shouldBeTrue() }
    }

    "2025년 동지(termIndex 18)는 12월 21~22일 근처다" {
        val ut = SolarLongitude.solarTermInstantUT(2025, 18)
        val date = JulianDayConverter.toGregorian(ut)
        date.year shouldBe 2025
        date.month shouldBe 12
        withClue("동지 날짜: ${date.month}/${date.day}") { (date.day in 21..22).shouldBeTrue() }
    }
})
