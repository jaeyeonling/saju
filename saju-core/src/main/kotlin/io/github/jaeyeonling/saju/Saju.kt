package io.github.jaeyeonling.saju

import io.github.jaeyeonling.saju.astronomy.JulianDayConverter
import io.github.jaeyeonling.saju.astronomy.SolarLongitude
import io.github.jaeyeonling.saju.derivation.Daeun
import io.github.jaeyeonling.saju.derivation.DaeunCalculator
import io.github.jaeyeonling.saju.derivation.DaeunDirection
import io.github.jaeyeonling.saju.derivation.PillarDerivation
import io.github.jaeyeonling.saju.domain.Eumyang
import io.github.jaeyeonling.saju.domain.GanZhi
import io.github.jaeyeonling.saju.domain.Jiji
import io.github.jaeyeonling.saju.domain.Pillar
import io.github.jaeyeonling.saju.domain.PillarPosition
import io.github.jaeyeonling.saju.domain.SajuChart
import io.github.jaeyeonling.saju.domain.ZishiPolicy
import kotlin.math.floor

/**
 * 사주 만세력 공개 진입점 — 천문 엔진과 4기둥 도출을 조립한다.
 *
 * **P3 단계**: 한국 시간 보정(진태양시·서머타임·자시 학파) 이전. 입력은 이미 보정된 로컬 시각으로 간주한다.
 * P4 에서 한국 보정 파이프라인을 거치는 진입점이 saju-korea 에 추가된다.
 */
public object Saju {

    /**
     * 로컬 시각 + UT 오프셋으로 사주판을 도출한다(보정 전 단계).
     *
     * @param utOffsetHours 로컬 시각을 UT 로 바꾸는 오프셋. 예: 베이징 8.0, 한국 표준시 9.0.
     */
    @JvmStatic
    @JvmOverloads
    public fun fromLocalDateTime(
        year: Int,
        month: Int,
        day: Int,
        hour: Int,
        minute: Int,
        utOffsetHours: Double,
        zishiPolicy: ZishiPolicy = ZishiPolicy.JEONGJASI,
    ): SajuChart {
        val timeFraction = (hour * MINUTES_PER_HOUR + minute) / MINUTES_PER_DAY
        val localJd = JulianDayConverter.fromGregorian(year, month, day, timeFraction)
        val utJd = localJd - utOffsetHours / HOURS_PER_DAY

        val yearGanZhi = run {
            // 연주: 그 해 입춘 절입 시각과 비교 — 입춘 전이면 전년.
            val ipchunUt = SolarLongitude.solarTermInstantUT(year, IPCHUN_TERM_INDEX)
            val solarYear = if (utJd >= ipchunUt) year else year - 1
            PillarDerivation.yearPillar(solarYear)
        }

        val monthGanZhi = run {
            // 월주: 출생 순간 황경 → 절기 월(입춘 315°부터 30°마다).
            val longitudeFromIpchun = normalizeDeg(SolarLongitude.apparentLongitudeDegAtUT(utJd) - IPCHUN_LONGITUDE_DEG)
            val monthOffset = floor(longitudeFromIpchun / DEGREES_PER_MONTH).toInt() % MONTHS_PER_YEAR
            PillarDerivation.monthPillar(yearGanZhi.gan, monthOffset)
        }

        // 일주: 로컬 날짜의 율리우스일 번호. 정자시설은 23시 이후를 다음날 일주로 본다.
        val zishiDateShift = if (zishiPolicy == ZishiPolicy.JEONGJASI && hour >= ZISHI_START_HOUR) 1L else 0L
        val julianDayNumber =
            floor(JulianDayConverter.fromGregorian(year, month, day, 0.0) + 0.5).toLong() + zishiDateShift
        val dayGanZhi = PillarDerivation.dayPillar(julianDayNumber)

        // 시주: 시지(2시간 단위) + 일간. (자시 학파는 P4)
        val hourJi = Jiji.fromIndex((hour + 1) / HOURS_PER_BRANCH)
        val hourGanZhi = PillarDerivation.hourPillar(dayGanZhi.gan, hourJi)

        return SajuChart(
            year = Pillar(PillarPosition.YEAR, yearGanZhi),
            month = Pillar(PillarPosition.MONTH, monthGanZhi),
            day = Pillar(PillarPosition.DAY, dayGanZhi),
            hour = Pillar(PillarPosition.HOUR, hourGanZhi),
        )
    }

    /**
     * 대운 도출 — 절기 경계까지의 거리로 시작 나이를 정하고 월주에서 방향대로 시퀀스를 만든다.
     *
     * @param utJd 출생의 절대 순간(UT 율리우스일).
     * @param monthPillar 월주 간지.
     * @param yearStemEumyang 연간 음양(방향 판정용).
     */
    @JvmStatic
    @JvmOverloads
    public fun daeun(
        utJd: Double,
        monthPillar: GanZhi,
        yearStemEumyang: Eumyang,
        isMale: Boolean,
        count: Int = DEFAULT_DAEUN_COUNT,
    ): List<Daeun> {
        val direction = DaeunDirection.of(yearStemEumyang, isMale)
        val birthLongitude = SolarLongitude.apparentLongitudeDegAtUT(utJd)

        // 절(節)은 황경 ≡ 15 (mod 30). 현재 절 시작부터 경과한 각도.
        val degreesIntoMonth = floorModDouble(birthLongitude - JEOL_PHASE_DEG, DEGREES_PER_MONTH)
        val daysToBoundary = if (direction == DaeunDirection.FORWARD) {
            val nextJeolLon = normalizeDeg(birthLongitude + (DEGREES_PER_MONTH - degreesIntoMonth))
            val near = utJd + (DEGREES_PER_MONTH - degreesIntoMonth) / MEAN_DAILY_MOTION
            SolarLongitude.instantOfLongitudeUT(nextJeolLon, near) - utJd
        } else {
            val prevJeolLon = normalizeDeg(birthLongitude - degreesIntoMonth)
            val near = utJd - degreesIntoMonth / MEAN_DAILY_MOTION
            utJd - SolarLongitude.instantOfLongitudeUT(prevJeolLon, near)
        }

        return DaeunCalculator.sequence(monthPillar, direction, DaeunCalculator.startAge(daysToBoundary), count)
    }

    /** 세운(歲運) — 특정 연도의 간지(입춘 기준 연주). */
    @JvmStatic
    public fun seun(year: Int): GanZhi = PillarDerivation.yearPillar(year)

    private fun floorModDouble(value: Double, modulus: Double): Double = ((value % modulus) + modulus) % modulus

    private fun normalizeDeg(deg: Double): Double = ((deg % FULL_CIRCLE) + FULL_CIRCLE) % FULL_CIRCLE

    private const val IPCHUN_TERM_INDEX = 21
    private const val IPCHUN_LONGITUDE_DEG = 315.0
    private const val DEGREES_PER_MONTH = 30.0
    private const val MONTHS_PER_YEAR = 12
    private const val FULL_CIRCLE = 360.0
    private const val MINUTES_PER_HOUR = 60.0
    private const val MINUTES_PER_DAY = 1440.0
    private const val HOURS_PER_DAY = 24.0
    private const val HOURS_PER_BRANCH = 2
    private const val ZISHI_START_HOUR = 23
    private const val JEOL_PHASE_DEG = 15.0
    private const val MEAN_DAILY_MOTION = 0.98565
    private const val DEFAULT_DAEUN_COUNT = 8
}
