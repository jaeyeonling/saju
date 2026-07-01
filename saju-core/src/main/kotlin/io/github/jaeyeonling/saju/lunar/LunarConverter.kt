package io.github.jaeyeonling.saju.lunar

import io.github.jaeyeonling.saju.astronomy.CalendarDate
import io.github.jaeyeonling.saju.astronomy.daysInGregorianMonth

/**
 * 음력 ↔ 양력 변환 공개 진입점. 기본 기준은 한국([CalendarBasis.KOREA], KASI/KST).
 *
 * 입력 범위는 1900~2100 (달 ELP 절단판 정확도 구간). 골든 벡터로 검증된 구간은 1900~2099.
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
        // toSolar 와 대칭되는 입력 가드 — 쓰레기 양력 입력이 그럴듯한 음력으로 새지 않게 fail-fast.
        require(month in 1..LAST_MONTH) { "양력 월은 1~12: $month" }
        // 월별 말일·윤년까지 검증(Saju 양력 경로와 동일 규칙) — 2/31 같은 비존재일 차단.
        val maxDay = daysInGregorianMonth(year, month)
        require(day in 1..maxDay) { "양력 일은 1~$maxDay ($year-${month}월): $day" }
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
        // 하한을 음력 1899까지 허용 — toLunar(1900년 초)가 음력 1899를 반환하므로 왕복이 깨지지 않게 한다.
        require(lunarYear in (MIN_YEAR - 1)..MAX_YEAR) { "지원 범위(1899~2100) 밖: $lunarYear" }
        require(lunarMonth in 1..LAST_MONTH) { "음력 월은 1~12: $lunarMonth" }
        require(day in 1..MAX_DAY) { "음력 일은 1~30: $day" } // 실제 월 길이는 LunarCalendar 에서 재검증
        return LunarCalendar.lunarToSolar(lunarYear, lunarMonth, day, isLeapMonth, basis.utOffsetHours)
    }

    private const val MIN_YEAR = 1900
    private const val MAX_YEAR = 2100
    private const val LAST_MONTH = 12
    private const val MAX_DAY = 30
}
