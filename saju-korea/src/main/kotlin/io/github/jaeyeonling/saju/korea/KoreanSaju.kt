package io.github.jaeyeonling.saju.korea

import io.github.jaeyeonling.saju.Saju
import io.github.jaeyeonling.saju.astronomy.Ephemeris
import io.github.jaeyeonling.saju.astronomy.JulianDayConverter
import io.github.jaeyeonling.saju.derivation.Daeun
import io.github.jaeyeonling.saju.domain.SajuChart

/**
 * 한국 사주 진입점 — 법정시(시계 시각)에 한국 보정을 적용해 사주판을 도출한다.
 *
 * 보정 순서(load-bearing):
 *  1. 서머타임 보정 — 시행 구간이면 시계가 1시간 빠르다.
 *  2. 표준 자오선 판정 — 동경 127.5°(UTC+8:30) 시기와 135°(UTC+9) 시기.
 *  3. 진태양시 보정 — 경도보정 `(경도−자오선)×4분` + 균시차.
 *  4. 보정된 진태양시로 절기·자시 경계 판정 → 4기둥.
 *
 * 1·2 는 절대 순간(UT)을 정하고, 3 은 그 순간을 출생지 태양시로 표시한다.
 */
public object KoreanSaju {

    /** 법정시 + 출생지 경도로 사주판 도출. */
    @JvmStatic
    @JvmOverloads
    public fun fromCivilTime(
        year: Int,
        month: Int,
        day: Int,
        hour: Int,
        minute: Int,
        longitudeDeg: Double = Birthplace.SEOUL.longitudeDeg,
    ): SajuChart {
        val (trueSolarJd, trueSolarUtOffsetHours) = computeTrueSolar(year, month, day, hour, minute, longitudeDeg)
        val ts = JulianDayConverter.toGregorian(trueSolarJd)
        val tsHour = (ts.dayFraction * HOURS_PER_DAY).toInt()
        val tsMinute = ((ts.dayFraction * MINUTES_PER_DAY) % MINUTES_PER_HOUR).toInt()
        return Saju.fromLocalDateTime(ts.year, ts.month, ts.day, tsHour, tsMinute, trueSolarUtOffsetHours)
    }

    /** 법정시 + 출생지 + 성별로 대운 시퀀스 도출(한국 보정 반영). */
    @JvmStatic
    @JvmOverloads
    public fun daeun(
        year: Int,
        month: Int,
        day: Int,
        hour: Int,
        minute: Int,
        isMale: Boolean,
        longitudeDeg: Double = Birthplace.SEOUL.longitudeDeg,
        count: Int = DEFAULT_DAEUN_COUNT,
    ): List<Daeun> {
        val chart = fromCivilTime(year, month, day, hour, minute, longitudeDeg)
        val (trueSolarJd, trueSolarUtOffsetHours) = computeTrueSolar(year, month, day, hour, minute, longitudeDeg)
        val utJd = trueSolarJd - trueSolarUtOffsetHours / HOURS_PER_DAY
        return Saju.daeun(utJd, chart.month.ganZhi, chart.year.gan.eumyang, isMale, count)
    }

    /**
     * 법정시 → 진태양시 총 보정(분). 진태양시 = 법정시 + 반환값.
     * 검증·표시용 (서울 평시 ≈ −32분 + 균시차, 서머타임 구간이면 추가 −60분).
     */
    @JvmStatic
    @JvmOverloads
    public fun trueSolarOffsetMinutes(
        year: Int,
        month: Int,
        day: Int,
        hour: Int,
        minute: Int,
        longitudeDeg: Double = Birthplace.SEOUL.longitudeDeg,
    ): Double {
        val (trueSolarJd, _) = computeTrueSolar(year, month, day, hour, minute, longitudeDeg)
        val legalJd = legalJulianDay(year, month, day, hour, minute)
        return (trueSolarJd - legalJd) * MINUTES_PER_DAY
    }

    /** (진태양시 율리우스일, 진태양시 기준 UT 오프셋 시간)을 계산한다. */
    private fun computeTrueSolar(
        year: Int,
        month: Int,
        day: Int,
        hour: Int,
        minute: Int,
        longitudeDeg: Double,
    ): Pair<Double, Double> {
        val info = KoreanStandardTime.at(year, month, day, hour, minute)
        // LMT(1908 이전)면 표준 자오선이 곧 출생지 경도라 경도보정이 0이 된다.
        val standardMeridian = info.standardMeridianDeg ?: longitudeDeg
        val summerTimeHours = if (info.isSummerTime) SUMMER_TIME_HOURS else 0.0
        val legalUtOffsetHours = standardMeridian / DEGREES_PER_HOUR + summerTimeHours

        val legalJd = legalJulianDay(year, month, day, hour, minute)
        val utJd = legalJd - legalUtOffsetHours / HOURS_PER_DAY

        // 진태양시 = 평균태양시(경도 기준) + 균시차.
        val equationOfTimeMinutes = Ephemeris.equationOfTimeMinutes(utJd)
        val trueSolarUtOffsetHours = longitudeDeg / DEGREES_PER_HOUR + equationOfTimeMinutes / MINUTES_PER_HOUR
        val trueSolarJd = utJd + trueSolarUtOffsetHours / HOURS_PER_DAY
        return trueSolarJd to trueSolarUtOffsetHours
    }

    private fun legalJulianDay(year: Int, month: Int, day: Int, hour: Int, minute: Int): Double =
        JulianDayConverter.fromGregorian(year, month, day, (hour * MINUTES_PER_HOUR + minute) / MINUTES_PER_DAY)

    private const val DEFAULT_DAEUN_COUNT = 8
    private const val SUMMER_TIME_HOURS = 1.0
    private const val DEGREES_PER_HOUR = 15.0
    private const val HOURS_PER_DAY = 24.0
    private const val MINUTES_PER_HOUR = 60.0
    private const val MINUTES_PER_DAY = 1440.0
}
