package io.github.jaeyeonling.saju.lunar

import io.github.jaeyeonling.saju.Golden
import io.github.jaeyeonling.saju.astronomy.JulianDayConverter
import io.kotest.assertions.withClue
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.shouldBe
import kotlin.math.abs
import kotlin.math.floor

private const val START_YEAR = 1900
private const val ROUNDTRIP_END_YEAR = 2050 // 라운드트립은 자체 일관성이라 표본 구간만
private const val MAX_BOUNDARY_DIFF = 6 // 자정 극근처 삭 허용 상한 (1900~2099 실측 기준 + 여유)

private fun jdnOf(
    year: Int,
    month: Int,
    day: Int,
): Long = floor(JulianDayConverter.fromGregorian(year, month, day, 0.0) + 0.5).toLong()

/**
 * 음력 변환 골든 회귀 — 자체 력법을 동결된 골든 벡터(중국 농력, 외부 라이브러리 tyme4j 출력 동결)와 대조한다.
 *
 * 골든은 베이징(+8) 기준이므로 [CalendarBasis.CHINA] 로 정렬한다.
 * 한국 KASI(+9)와는 의도적으로 다를 수 있어(2017 윤달 등) 골든은 중국 기준으로 한정한다.
 */
class LunarConversionGoldenTest : StringSpec({

    "음력→양력 전수가 골든 벡터와 일치 (중국 기준, 1900~2099)" {
        var total = 0
        var boundaryDiff = 0 // 삭이 자정 극근처라 ±1일 갈리는 경계 케이스
        val boundarySamples = mutableListOf<String>()

        for (row in Golden.rows("lunar_to_solar.csv")) {
            val year = row[0].toInt()
            val month = row[1].toInt()
            val isLeap = row[2].toBoolean()
            val solarYear = row[3].toInt()
            val solarMonth = row[4].toInt()
            val solarDay = row[5].toInt()
            total++

            val mine = LunarConverter.toSolar(year, month, 1, isLeap, CalendarBasis.CHINA)
            val delta = jdnOf(mine.year, mine.month, mine.day) - jdnOf(solarYear, solarMonth, solarDay)
            when {
                delta == 0L -> Unit // 일치
                abs(delta) == 1L -> { // 자정 경계 — 천문 계산 한계로 ±1일 허용
                    boundaryDiff++
                    if (boundarySamples.size < 10) {
                        boundarySamples.add(
                            "$year-${if (isLeap) "윤" else ""}$month (Δ${delta}일)",
                        )
                    }
                }
                else -> error("음 $year-$month 차이 ${delta}일 — 자정 경계 아님(진짜 오류)")
            }
        }
        println("음→양 골든 $total 케이스: 정확 ${total - boundaryDiff}, 자정경계 ±1일 ${boundaryDiff}건 $boundarySamples")
        // 1일 초과 차이는 위 error 로 이미 실패. 자정 경계는 극소수여야 한다.
        withClue("자정 경계 불일치 $boundaryDiff 건이 너무 많다") { (boundaryDiff <= MAX_BOUNDARY_DIFF).shouldBeTrue() }
    }

    "2033 윤11월 예외 케이스가 정확하다" {
        // 2033년은 무중치윤의 유명한 예외(윤11월). thumb-rule이면 틀린다.
        val leapMonth =
            Golden.rows("lunar_leapmonth.csv")
                .single { it[0].toInt() == 2033 }[1].toInt()
        withClue("골든 기준 2033 윤달") { leapMonth shouldBe 11 }

        // 동결된 음→양 정답과 엄격 대조(자정 경계 무관한 안정 케이스).
        val expected =
            Golden.rows("lunar_to_solar.csv")
                .single { it[0].toInt() == 2033 && it[1].toInt() == 11 && it[2].toBoolean() }
        val mine = LunarConverter.toSolar(2033, 11, 1, isLeapMonth = true, basis = CalendarBasis.CHINA)
        withClue("음 2033-윤11-1 year") { mine.year shouldBe expected[3].toInt() }
        withClue("음 2033-윤11-1 month") { mine.month shouldBe expected[4].toInt() }
        withClue("음 2033-윤11-1 day") { mine.day shouldBe expected[5].toInt() }
    }

    "양력→음력→양력 라운드트립 항등 (표본)" {
        // 하단 경계(음력 1899) 허용으로 1900년 초 양력도 왕복 복원된다. 말일 근처(28)까지 표본.
        for (year in START_YEAR..ROUNDTRIP_END_YEAR) {
            for (month in 1..12) {
                for (day in intArrayOf(1, 10, 20, 28)) {
                    val lunar = LunarConverter.toLunar(year, month, day, CalendarBasis.CHINA)
                    val back =
                        LunarConverter.toSolar(
                            lunar.year,
                            lunar.month,
                            lunar.day,
                            lunar.isLeapMonth,
                            CalendarBasis.CHINA,
                        )
                    withClue("라운드트립 year @ $year-$month-$day") { back.year shouldBe year }
                    withClue("라운드트립 month @ $year-$month-$day") { back.month shouldBe month }
                    withClue("라운드트립 day @ $year-$month-$day") { back.day shouldBe day }
                }
            }
        }
    }

    "양력→음력이 골든 벡터와 일치 (표본, 자정경계 허용)" {
        var boundaryDiff = 0
        for (row in Golden.rows("solar_to_lunar.csv")) {
            val year = row[0].toInt()
            val month = row[1].toInt()
            val day = row[2].toInt()
            val expectedMonthWithLeap = row[3].toInt()
            val expectedDay = row[4].toInt()

            val mine = LunarConverter.toLunar(year, month, day, CalendarBasis.CHINA)
            val mineMonthWithLeap = if (mine.isLeapMonth) -mine.month else mine.month
            // 월은 항상 일치해야 한다(틀리면 진짜 오류).
            withClue("양→음 월 @ $year-$month-$day") { mineMonthWithLeap shouldBe expectedMonthWithLeap }
            // 일은 삭이 자정 극근처면 ±1 갈릴 수 있다(천문 한계).
            val dayDelta = abs(expectedDay - mine.day)
            if (dayDelta == 1) {
                boundaryDiff++
            } else {
                withClue(
                    "양→음 일 @ $year-$month-$day",
                ) { mine.day shouldBe expectedDay }
            }
        }
        withClue("양→음 자정 경계 $boundaryDiff 건이 너무 많다") { (boundaryDiff <= MAX_BOUNDARY_DIFF).shouldBeTrue() }
    }
})
