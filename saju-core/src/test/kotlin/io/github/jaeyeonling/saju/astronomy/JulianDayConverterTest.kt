package io.github.jaeyeonling.saju.astronomy

import io.kotest.assertions.withClue
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.doubles.plusOrMinus
import io.kotest.matchers.shouldBe

private const val ABS_TOL = 1e-9

class JulianDayConverterTest : StringSpec({

    "J2000_0 기준점 — 2000-01-01 12_00 = JD 2451545_0" {
        val jd = JulianDayConverter.fromGregorian(2000, 1, 1, dayFraction = 0.5)
        jd shouldBe (2451545.0 plusOrMinus ABS_TOL)
    }

    "Meeus Ch7 예제 — Sputnik 1957-10-04_81 = JD 2436116_31" {
        val jd = JulianDayConverter.fromGregorian(1957, 10, 4, dayFraction = 0.81)
        jd shouldBe (2436116.31 plusOrMinus ABS_TOL)
    }

    "Meeus Ch7 예제 — 1987-01-27_0 = JD 2446822_5" {
        val jd = JulianDayConverter.fromGregorian(1987, 1, 27, dayFraction = 0.0)
        jd shouldBe (2446822.5 plusOrMinus ABS_TOL)
    }

    "Meeus Ch7 예제 — 1987-06-19_5 = JD 2446966_0" {
        val jd = JulianDayConverter.fromGregorian(1987, 6, 19, dayFraction = 0.5)
        jd shouldBe (2446966.0 plusOrMinus ABS_TOL)
    }

    "역변환 — JD 2451545_0 = 2000-01-01 12_00" {
        val date = JulianDayConverter.toGregorian(2451545.0)
        date.year shouldBe 2000
        date.month shouldBe 1
        date.day shouldBe 1
        date.dayFraction shouldBe (0.5 plusOrMinus ABS_TOL)
        date.hour shouldBe 12
    }

    "왕복 변환 — 1391~2100 전 구간 라운드트립이 동일하다" {
        // 입력 범위 경계를 포함해 촘촘히 왕복. dayFraction 까지 보존되어야 한다.
        var maxDayError = 0.0
        for (year in 1391..2100) {
            for (month in 1..12) {
                for (day in intArrayOf(1, 15, 28)) {
                    for (frac in doubleArrayOf(0.0, 0.25, 0.5, 0.7341)) {
                        val jd = JulianDayConverter.fromGregorian(year, month, day, frac)
                        val back = JulianDayConverter.toGregorian(jd)
                        withClue("year @ $year-$month-$day f=$frac") { back.year shouldBe year }
                        withClue("month @ $year-$month-$day f=$frac") { back.month shouldBe month }
                        withClue("day @ $year-$month-$day f=$frac") { back.day shouldBe day }
                        maxDayError = maxOf(maxDayError, kotlin.math.abs(back.dayFraction - frac))
                    }
                }
            }
        }
        // 일내 비율 오차는 부동소수점 한계 이내여야 한다.
        withClue("최대 dayFraction 오차 $maxDayError 가 너무 크다") { (maxDayError < 1e-6).shouldBeTrue() }
    }
})
