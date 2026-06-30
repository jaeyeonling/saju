package io.github.jaeyeonling.saju.korea

import io.github.jaeyeonling.saju.Saju
import io.github.jaeyeonling.saju.astronomy.Ephemeris
import io.github.jaeyeonling.saju.astronomy.JulianDayConverter
import io.github.jaeyeonling.saju.derivation.Daeun
import io.github.jaeyeonling.saju.domain.SajuChart
import io.github.jaeyeonling.saju.lunar.CalendarBasis
import io.github.jaeyeonling.saju.lunar.LunarConverter

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
    /**
     * 음력 생일 + 법정시로 사주판 도출. 음력→양력 변환 후 [fromCivilTime] 파이프라인 재사용.
     *
     * @param isLeapMonth 윤달 여부.
     * @param basis 음력 기준 역법(KASI 한국 vs 중국 농력 — 윤달 배치가 갈리는 해에 결과가 다르다).
     */
    @JvmStatic
    @JvmOverloads
    public fun fromLunarCivilTime(
        lunarYear: Int,
        lunarMonth: Int,
        lunarDay: Int,
        isLeapMonth: Boolean,
        hour: Int,
        minute: Int,
        longitudeDeg: Double = Birthplace.SEOUL.longitudeDeg,
        config: KoreanSajuConfig = KoreanSajuConfig.DEFAULT,
        basis: CalendarBasis = CalendarBasis.KOREA,
    ): SajuChart {
        val solar = LunarConverter.toSolar(lunarYear, lunarMonth, lunarDay, isLeapMonth, basis)
        return fromCivilTime(solar.year, solar.month, solar.day, hour, minute, longitudeDeg, config)
    }

    /** 법정시 + 출생지 경도로 사주판 도출. [config] 로 자시·연주·진태양시 학파를 선택한다. */
    @JvmStatic
    @JvmOverloads
    public fun fromCivilTime(
        year: Int,
        month: Int,
        day: Int,
        hour: Int,
        minute: Int,
        longitudeDeg: Double = Birthplace.SEOUL.longitudeDeg,
        config: KoreanSajuConfig = KoreanSajuConfig.DEFAULT,
    ): SajuChart {
        val (trueSolarJd, trueSolarUtOffsetHours) =
            computeTrueSolar(year, month, day, hour, minute, longitudeDeg, config.trueSolarTime)
        val ts = JulianDayConverter.toGregorian(trueSolarJd)
        // 진태양시 시각을 초까지 보존해 절기 경계 오판 방지.
        return Saju.fromLocalDateTime(
            ts.year,
            ts.month,
            ts.day,
            ts.hour,
            ts.minute,
            trueSolarUtOffsetHours,
            config.saju,
            ts.second,
        )
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
        config: KoreanSajuConfig = KoreanSajuConfig.DEFAULT,
    ): List<Daeun> {
        // 진태양시를 한 번만 계산해 사주판과 대운이 동일 순간을 보도록 한다(이중 계산 방지).
        val (trueSolarJd, trueSolarUtOffsetHours) =
            computeTrueSolar(year, month, day, hour, minute, longitudeDeg, config.trueSolarTime)
        val ts = JulianDayConverter.toGregorian(trueSolarJd)
        val chart =
            Saju.fromLocalDateTime(
                ts.year,
                ts.month,
                ts.day,
                ts.hour,
                ts.minute,
                trueSolarUtOffsetHours,
                config.saju,
                ts.second,
            )
        val utJd = trueSolarJd - trueSolarUtOffsetHours / HOURS_PER_DAY
        return Saju.daeun(utJd, chart.month.ganji, chart.year.gan.eumyang, isMale, count, config.saju)
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
        policy: TrueSolarTimePolicy = TrueSolarTimePolicy.FULL,
    ): Double {
        val (trueSolarJd, _) = computeTrueSolar(year, month, day, hour, minute, longitudeDeg, policy)
        val legalJd = legalJulianDay(year, month, day, hour, minute)
        return (trueSolarJd - legalJd) * MINUTES_PER_DAY
    }

    /** (진태양시 율리우스일, 진태양시 기준 UT 오프셋 시간)을 [policy] 에 따라 계산한다. */
    private fun computeTrueSolar(
        year: Int,
        month: Int,
        day: Int,
        hour: Int,
        minute: Int,
        longitudeDeg: Double,
        policy: TrueSolarTimePolicy,
    ): Pair<Double, Double> {
        // 모든 공개 진입점(fromCivilTime/daeun/trueSolarOffsetMinutes)이 이 함수를 거치므로 여기서 fail-fast.
        Saju.requireValidCivilDateTime(year, month, day, hour, minute)
        require(longitudeDeg.isFinite() && longitudeDeg in MIN_LONGITUDE..MAX_LONGITUDE) {
            "출생지 경도는 ${MIN_LONGITUDE}~${MAX_LONGITUDE}: $longitudeDeg"
        }

        val info = KoreanStandardTime.at(year, month, day, hour, minute)
        // LMT(1908 이전)면 표준 자오선이 곧 출생지 경도라 경도보정이 0이 된다.
        val standardMeridian = info.standardMeridianDeg ?: longitudeDeg
        val summerTimeHours = if (info.isSummerTime) SUMMER_TIME_HOURS else 0.0
        val legalUtOffsetHours = standardMeridian / DEGREES_PER_HOUR + summerTimeHours

        val legalJd = legalJulianDay(year, month, day, hour, minute)
        val utJd = legalJd - legalUtOffsetHours / HOURS_PER_DAY

        // 진태양시 = 평균태양시(경도 기준) + 균시차. 정책에 따라 각 항을 켜고 끈다.
        //  NONE: 표준 자오선 평균시(보정 0) / LONGITUDE_ONLY: 경도만 / FULL: 경도+균시차(통설).
        val longitudeHours =
            when (policy) {
                TrueSolarTimePolicy.NONE -> standardMeridian / DEGREES_PER_HOUR
                TrueSolarTimePolicy.LONGITUDE_ONLY, TrueSolarTimePolicy.FULL -> longitudeDeg / DEGREES_PER_HOUR
            }
        val equationOfTimeMinutes =
            if (policy == TrueSolarTimePolicy.FULL) Ephemeris.equationOfTimeMinutes(utJd) else 0.0
        val trueSolarUtOffsetHours = longitudeHours + equationOfTimeMinutes / MINUTES_PER_HOUR
        val trueSolarJd = utJd + trueSolarUtOffsetHours / HOURS_PER_DAY
        return trueSolarJd to trueSolarUtOffsetHours
    }

    private fun legalJulianDay(
        year: Int,
        month: Int,
        day: Int,
        hour: Int,
        minute: Int,
    ): Double = JulianDayConverter.fromGregorian(year, month, day, (hour * MINUTES_PER_HOUR + minute) / MINUTES_PER_DAY)

    private const val DEFAULT_DAEUN_COUNT = 8
    private const val MIN_LONGITUDE = -180.0
    private const val MAX_LONGITUDE = 180.0
    private const val SUMMER_TIME_HOURS = 1.0
    private const val DEGREES_PER_HOUR = 15.0
    private const val HOURS_PER_DAY = 24.0
    private const val MINUTES_PER_HOUR = 60.0
    private const val MINUTES_PER_DAY = 1440.0
}
