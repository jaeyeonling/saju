package io.github.jaeyeonling.saju.lunar

import io.github.jaeyeonling.saju.astronomy.CalendarDate

/**
 * 음력 ↔ 양력 변환 공개 진입점. 기본 기준은 한국([CalendarBasis.KOREA], KASI/KST).
 *
 * 입력 범위는 1900~2100 (달 ELP 절단판 정확도 + tyme4j 골든 커버리지 구간).
 */
public object LunarConverter {

    /** 양력(현지 날짜) → 음력. */
    @JvmStatic
    @JvmOverloads
    public fun toLunar(
        year: Int,
        month: Int,
        day: Int,
        basis: CalendarBasis = CalendarBasis.KOREA,
    ): LunarDate {
        require(year in MIN_YEAR..MAX_YEAR) { "지원 범위(1900~2100) 밖: $year" }
        return LunarCalendar.solarToLunar(year, month, day, basis.utOffsetHours)
    }

    /** 음력 → 양력(현지 날짜). */
    @JvmStatic
    @JvmOverloads
    public fun toSolar(
        lunarYear: Int,
        lunarMonth: Int,
        day: Int,
        isLeapMonth: Boolean = false,
        basis: CalendarBasis = CalendarBasis.KOREA,
    ): CalendarDate {
        require(lunarYear in MIN_YEAR..MAX_YEAR) { "지원 범위(1900~2100) 밖: $lunarYear" }
        require(lunarMonth in 1..LAST_MONTH) { "음력 월은 1~12: $lunarMonth" }
        require(day in 1..MAX_DAY) { "음력 일은 1~30: $day" }
        return LunarCalendar.lunarToSolar(lunarYear, lunarMonth, day, isLeapMonth, basis.utOffsetHours)
    }

    private const val MIN_YEAR = 1900
    private const val MAX_YEAR = 2100
    private const val LAST_MONTH = 12
    private const val MAX_DAY = 30
}
