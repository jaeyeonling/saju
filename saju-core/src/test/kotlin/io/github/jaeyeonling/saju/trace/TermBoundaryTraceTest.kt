package io.github.jaeyeonling.saju.trace

import io.github.jaeyeonling.saju.Saju
import io.github.jaeyeonling.saju.astronomy.JulianDayConverter
import io.github.jaeyeonling.saju.astronomy.SolarLongitude
import io.kotest.assertions.withClue
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.shouldBe

private const val SECONDS_PER_DAY = 86_400.0

/**
 * 절입 경계 ±1초에서의 천문 trace 불변식 — 기존 SolarTermBoundaryTest(±5초, 4기둥)보다
 * 경계에 바짝 붙여 trace 필드(prevTerm ≤ 출생 < nextTerm, 절 termIndex 홀수)를 검증한다.
 *
 * 절입 "정확 순간"은 초 단위 민간 시각으로 표현 불가(절입은 소수 초)라 ±1초가 실질 최소 간격이다.
 */
class TermBoundaryTraceTest : StringSpec({

    // 대표 절(節) 경계: 1990 입춘(21)·경칩(23), 2024 입하(3), 1955 한로(13) — 연대·계절 분산.
    val boundaries = listOf(1990 to 21, 1990 to 23, 2024 to 3, 1955 to 13)

    "절입 ±1초에서 prevTerm ≤ 출생 < nextTerm, 절은 홀수 termIndex" {
        for ((year, termIndex) in boundaries) {
            val boundaryUt = SolarLongitude.solarTermInstantUT(year, termIndex)
            for (offsetSeconds in listOf(-1, +1)) {
                val ts = JulianDayConverter.toGregorian(boundaryUt + offsetSeconds / SECONDS_PER_DAY)
                val c =
                    Saju.fromLocalDateTimeWithTrace(
                        year = ts.year,
                        month = ts.month,
                        day = ts.day,
                        hour = ts.hour,
                        minute = ts.minute,
                        utOffsetHours = 0.0,
                        second = ts.second,
                    )
                val tag = "$year 절기$termIndex ${if (offsetSeconds < 0) "직전" else "직후"}"

                withClue("절기 순서 불변식 @ $tag") {
                    (c.astronomy.prevTerm.utJd <= c.utJd).shouldBeTrue()
                    (c.utJd < c.astronomy.nextTerm.utJd).shouldBeTrue()
                }
                withClue("절(節)은 홀수 termIndex @ $tag") {
                    c.astronomy.prevTerm.termIndex % 2 shouldBe 1
                    c.astronomy.nextTerm.termIndex % 2 shouldBe 1
                }
                withClue("prev/next 는 이웃 절(30° 간격) @ $tag") {
                    (c.astronomy.nextTerm.termIndex - c.astronomy.prevTerm.termIndex + 24) % 24 shouldBe 2
                }
            }
        }
    }
})
