package io.github.jaeyeonling.saju.astronomy

import io.kotest.assertions.withClue
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.doubles.plusOrMinus
import io.kotest.matchers.shouldBe

class MoonPositionTest : StringSpec({

    "Meeus Ch47 예제 — 1992-04-12 0h TD 달 평균황경" {
        // Meeus Example 47.a: 1992-04-12.0 TD (JDE 2448724.5) → 달 황경 133.162655°.
        val jde = 2448724.5
        val longitudeDeg = MoonPosition.meanLongitudeRad(jde) * RAD_TO_DEG
        longitudeDeg shouldBe (133.162655 plusOrMinus 1e-5)
    }

    "달은 하루 약 13도 동진한다" {
        val jde = 2451545.0
        val lon0 = MoonPosition.apparentLongitudeRad(jde) * RAD_TO_DEG
        val lon1 = MoonPosition.apparentLongitudeRad(jde + 1.0) * RAD_TO_DEG
        val daily = ((lon1 - lon0) % 360.0 + 360.0) % 360.0
        // 달 평균 일주 운동 ≈ 13.18° (12~15° 변동).
        withClue("달 일주 운동 비정상: $daily°/일") { (daily in 11.0..16.0).shouldBeTrue() }
    }
})
