package io.github.jaeyeonling.saju.astronomy

/**
 * 율리우스일 역변환 결과 — proleptic Gregorian 달력의 (년, 월, 일, 일내 비율).
 *
 * [dayFraction] 은 자정부터의 하루 비율 `[0.0, 1.0)` 이다. 예: 0.5 = 정오.
 * java.time 에 의존하지 않기 위해 LocalDateTime 대신 이 평면 구조를 쓴다.
 */
public data class CalendarDate(
    @JvmField public val year: Int,
    @JvmField public val month: Int,
    @JvmField public val day: Int,
    @JvmField public val dayFraction: Double,
) {
    /** 일내 비율을 시/분/초로 분해한 시각 부분. */
    public val hour: Int get() = (dayFraction * HOURS_PER_DAY).toInt()
    public val minute: Int get() = ((dayFraction * MINUTES_PER_DAY) % MINUTES_PER_HOUR).toInt()
    public val second: Int get() = ((dayFraction * SECONDS_PER_DAY) % SECONDS_PER_MINUTE).toInt()

    private companion object {
        const val HOURS_PER_DAY = 24.0
        const val MINUTES_PER_HOUR = 60.0
        const val MINUTES_PER_DAY = 1440.0
        const val SECONDS_PER_MINUTE = 60.0
        const val SECONDS_PER_DAY = 86_400.0
    }
}
