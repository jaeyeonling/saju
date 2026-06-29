package io.github.jaeyeonling.saju

import io.kotest.assertions.throwables.shouldNotThrowAny
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec

private const val KST_OFFSET = 9.0

/**
 * 양력 민간일 입력의 fail-fast 검증 — 존재하지 않는 날짜(2월 30일·4월 31일 등)가
 * '그럴듯하지만 틀린 사주'로 조용히 새지 않고 즉시 거부되는지 확인한다.
 *
 * Meeus JD 공식은 순수 산술이라 범위를 보지 않아 `2월 31일`을 `3월 3일`로 굴려버린다.
 * 음력 경로(LunarRobustnessTest 의 '29일 달 30일 거부')와의 대칭을 보장한다.
 */
class SajuInputGuardTest : StringSpec({

    "존재하지 않는 2월 말일은 거부된다 (30·31일)" {
        shouldThrow<IllegalArgumentException> { Saju.fromLocalDateTime(2023, 2, 30, 9, 0, KST_OFFSET) }
        shouldThrow<IllegalArgumentException> { Saju.fromLocalDateTime(2023, 2, 31, 9, 0, KST_OFFSET) }
    }

    "30일까지인 달의 31일은 거부된다 (4·6·9·11월)" {
        for (month in intArrayOf(4, 6, 9, 11)) {
            shouldThrow<IllegalArgumentException> { Saju.fromLocalDateTime(2023, month, 31, 9, 0, KST_OFFSET) }
        }
    }

    "2월 29일은 윤년 규칙대로만 허용된다 (4·100·400)" {
        shouldNotThrowAny { Saju.requireValidCivilDateTime(2024, 2, 29, 9, 0) } // 4의 배수 → 윤년
        shouldThrow<IllegalArgumentException> { Saju.requireValidCivilDateTime(2023, 2, 29, 9, 0) } // 평년
        shouldThrow<IllegalArgumentException> { Saju.requireValidCivilDateTime(2100, 2, 29, 9, 0) } // 100의 배수 → 평년
        shouldNotThrowAny { Saju.requireValidCivilDateTime(2000, 2, 29, 9, 0) } // 400의 배수 → 윤년
    }

    "지원 연도 경계 — 2100 통과, 2101·1899 거부" {
        shouldNotThrowAny { Saju.requireValidCivilDateTime(2100, 1, 1, 0, 0) }
        shouldThrow<IllegalArgumentException> { Saju.requireValidCivilDateTime(2101, 1, 1, 0, 0) }
        shouldThrow<IllegalArgumentException> { Saju.requireValidCivilDateTime(1899, 12, 31, 0, 0) }
    }

    "정상 날짜는 통과한다 (회귀 방지)" {
        shouldNotThrowAny {
            Saju.fromLocalDateTime(1990, 3, 15, 7, 0, KST_OFFSET)
            Saju.requireValidCivilDateTime(2023, 12, 31, 23, 59)
            Saju.requireValidCivilDateTime(2024, 2, 29, 0, 0)
            Saju.requireValidCivilDateTime(2023, 4, 30, 0, 0)
        }
    }
})
