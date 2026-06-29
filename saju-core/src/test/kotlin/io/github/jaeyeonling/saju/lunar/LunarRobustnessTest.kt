package io.github.jaeyeonling.saju.lunar

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.assertions.withClue
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.shouldBe

/** adversarial 검증에서 확인된 결함들에 대한 회귀 테스트. */
class LunarRobustnessTest : StringSpec({

    "29일 달에 30일 입력은 예외다 (조용한 누수 방지)" {
        // 작은달(29일)이 한 해에 여럿 있으므로 최소 1건은 거부되어야 한다.
        var rejected = 0
        for (month in 1..12) {
            try {
                LunarConverter.toSolar(2023, month, 30, isLeapMonth = false, basis = CalendarBasis.CHINA)
            } catch (e: IllegalArgumentException) {
                rejected++
            }
        }
        withClue("29일 달의 30일이 하나도 거부되지 않았다") { (rejected > 0).shouldBeTrue() }
    }

    "하단 경계 1900년 초 왕복이 복원된다" {
        // toLunar(1900-02)는 음력 1899를 반환 — toSolar 가 거부하지 않아야 한다.
        val lunar = LunarConverter.toLunar(1900, 2, 1, CalendarBasis.CHINA)
        val back = LunarConverter.toSolar(lunar.year, lunar.month, lunar.day, lunar.isLeapMonth, CalendarBasis.CHINA)
        back.year shouldBe 1900
        back.month shouldBe 2
        back.day shouldBe 1
    }

    "toLunar 쓰레기 입력은 거부된다" {
        shouldThrow<IllegalArgumentException> { LunarConverter.toLunar(2024, 13, 1) }
        shouldThrow<IllegalArgumentException> { LunarConverter.toLunar(2024, 2, 32) }
        shouldThrow<IllegalArgumentException> { LunarConverter.toLunar(2024, 0, 1) }
    }

    "toLunar 양력 비존재일은 거부된다 (Saju 경로와 동일 규칙)" {
        shouldThrow<IllegalArgumentException> { LunarConverter.toLunar(2023, 2, 30) } // 2월은 28일까지(평년)
        shouldThrow<IllegalArgumentException> { LunarConverter.toLunar(2024, 2, 30) } // 윤년도 29일까지
        shouldThrow<IllegalArgumentException> { LunarConverter.toLunar(2023, 4, 31) } // 4월은 30일까지
    }

    "존재하지 않는 윤달 입력은 유용한 메시지로 거부된다" {
        // 2023 윤달은 2월뿐 — 윤7월은 없다.
        val ex = shouldThrow<IllegalArgumentException> {
            LunarConverter.toSolar(2023, 7, 1, isLeapMonth = true, basis = CalendarBasis.CHINA)
        }
        withClue("메시지에 윤달 정보: ${ex.message}") { (ex.message?.contains("윤") == true).shouldBeTrue() }
    }

    "2017 윤달 — 한국(KASI) 윤5월 vs 중국 윤6월 분기" {
        // 한·중 기준이 갈리는 대표 해. KASI 공식: 윤5월. 중국 농력: 윤6월.
        val koreaLeap = leapMonthOf(2017, CalendarBasis.KOREA)
        val chinaLeap = leapMonthOf(2017, CalendarBasis.CHINA)
        println("2017 윤달: KOREA=$koreaLeap, CHINA=$chinaLeap")
        withClue("중국 농력 2017 윤6월") { chinaLeap shouldBe 6 }
        withClue("한국 KASI 2017 윤5월") { koreaLeap shouldBe 5 }
    }
})

/** 그 해 윤달 월 번호(없으면 0). 윤달 1일 변환이 성공하는 월을 탐색. */
private fun leapMonthOf(year: Int, basis: CalendarBasis): Int {
    for (month in 1..12) {
        try {
            LunarConverter.toSolar(year, month, 1, isLeapMonth = true, basis = basis)
            return month
        } catch (e: IllegalArgumentException) {
            // 그 월은 윤달 아님
        }
    }
    return 0
}
