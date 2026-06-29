package io.github.jaeyeonling.saju.domain

/** 음수에도 양의 나머지를 주는 floor modulo (KMP 친화 — Math.floorMod 대신 자체 구현). */
internal fun floorMod(
    value: Int,
    modulus: Int,
): Int = ((value % modulus) + modulus) % modulus

/** 그레고리력 윤년 규칙: 4의 배수이되 100의 배수는 제외, 400의 배수는 다시 윤년. */
internal fun isGregorianLeapYear(year: Int): Boolean = (year % 4 == 0 && year % 100 != 0) || year % 400 == 0

/**
 * 그레고리력 월별 일수(윤년 반영). [month] 는 1..12 로 사전 검증된 값을 받는다.
 * Meeus JD 공식은 순수 산술이라 2/31 같은 비존재일을 다음 달로 굴려버리므로,
 * 양력 입력 진입점은 이 값으로 day 상한을 검증해 '조용한 오답'을 차단한다(java.time-free).
 */
internal fun daysInGregorianMonth(
    year: Int,
    month: Int,
): Int =
    when (month) {
        2 -> if (isGregorianLeapYear(year)) 29 else 28
        4, 6, 9, 11 -> 30
        else -> 31
    }
