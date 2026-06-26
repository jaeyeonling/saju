package io.github.jaeyeonling.saju.lunar

/**
 * 음력 날짜 값 객체. 양력 [io.github.jaeyeonling.saju.astronomy.CalendarDate] 와 대칭.
 * 음력은 시각 체계(시/분)를 갖지 않는다 — 사주의 시(時)는 양력 진태양시로 별도 계산한다.
 */
public data class LunarDate(
    @JvmField public val year: Int,
    @JvmField public val month: Int,
    @JvmField public val day: Int,
    @JvmField public val isLeapMonth: Boolean,
)
