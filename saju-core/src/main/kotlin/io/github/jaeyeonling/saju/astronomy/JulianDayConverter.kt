package io.github.jaeyeonling.saju.astronomy

import kotlin.math.floor

/**
 * 그레고리력 ↔ 율리우스일(JD) 변환. 출처: Jean Meeus, _Astronomical Algorithms_ 2판, Ch.7.
 *
 * **proleptic Gregorian** 으로 고정한다 — 라이브러리 지원 범위(1900~2100)에서 안전하다.
 * 산술 자체는 임의 연도에 동작하나, 1582-10-15(그레고리력 개시) 이전 날짜는 율리우스력 분기 없이
 * 외삽하므로 역사적 율리우스력 날짜와 어긋날 수 있다(사주 실사용은 근현대라 무방).
 */
public object JulianDayConverter {

    /**
     * (년, 월, 일, 일내 비율) → JD.
     *
     * @param dayFraction 자정부터의 하루 비율 `[0.0, 1.0)`. 정오면 0.5.
     */
    @JvmStatic
    @JvmOverloads
    public fun fromGregorian(year: Int, month: Int, day: Int, dayFraction: Double = 0.0): Double {
        // 1~2월은 전년도 13~14월로 취급 (Meeus 7.1)
        var y = year
        var m = month
        if (m <= 2) {
            y -= 1
            m += 12
        }
        val a = floor(y / 100.0)
        val b = 2 - a + floor(a / 4.0) // 그레고리력 윤년 보정
        return floor(YEAR_LENGTH * (y + JD_YEAR_OFFSET)) +
            floor(MONTH_LENGTH * (m + 1)) +
            day + b - JD_EPOCH_OFFSET + dayFraction
    }

    /** JD → proleptic Gregorian (년, 월, 일, 일내 비율). Meeus 7.x 역변환. */
    @JvmStatic
    public fun toGregorian(jd: Double): CalendarDate {
        val shifted = jd + 0.5
        val z = floor(shifted).toLong()
        val f = shifted - z
        // proleptic Gregorian: 항상 그레고리력 분기를 적용 (Z >= 2299161 경로)
        val alpha = floor((z - GREGORIAN_ALPHA_BASE) / GREGORIAN_CENTURY).toLong()
        val a = z + 1 + alpha - floor(alpha / 4.0).toLong()
        val b = a + 1524
        val c = floor((b - C_OFFSET) / YEAR_LENGTH).toLong()
        val d = floor(YEAR_LENGTH * c).toLong()
        val e = floor((b - d) / MONTH_LENGTH).toLong()

        val dayWithFraction = b - d - floor(MONTH_LENGTH * e).toLong() + f
        val day = floor(dayWithFraction).toInt()
        val month = if (e < 14) (e - 1).toInt() else (e - 13).toInt()
        val year = if (month > 2) (c - 4716).toInt() else (c - 4715).toInt()
        return CalendarDate(year, month, day, dayWithFraction - day)
    }

    private const val YEAR_LENGTH = 365.25
    private const val MONTH_LENGTH = 30.6001
    private const val JD_YEAR_OFFSET = 4716
    private const val JD_EPOCH_OFFSET = 1524.5
    private const val C_OFFSET = 122.1
    private const val GREGORIAN_ALPHA_BASE = 1_867_216.25
    private const val GREGORIAN_CENTURY = 36_524.25
}
