package io.github.jaeyeonling.saju.lunar

import io.github.jaeyeonling.saju.astronomy.CalendarDate
import io.github.jaeyeonling.saju.astronomy.JulianDayConverter
import io.github.jaeyeonling.saju.astronomy.LunarPhase
import io.github.jaeyeonling.saju.astronomy.SolarLongitude
import kotlin.math.ceil
import kotlin.math.floor

/**
 * 음력(태음태양력) 변환 엔진 — 정기법(定氣) 기반 삭·중기 + 무중치윤(無中置閏).
 *
 * 규칙:
 *  - 삭(朔)이 드는 날이 음력 월의 1일. 한 달 = 삭에서 다음 삭 전날.
 *  - 동지(황경 270°) 든 삭월 = 11월(자월).
 *  - 두 동지(歲) 사이 13삭월이면 윤년, 중기(中氣, 황경 30°k) 없는 **첫** 삭월이 윤달(무중치윤).
 *
 * 타임존 무지: 천문 계산(삭·중기)은 순수 UT, [utOffsetHours] 로 자정 귀속 프레임만 주입한다.
 * (P1에서 겪은 베이징 하드코딩 함정 방지 — 절대 하드코딩하지 않음)
 */
internal object LunarCalendar {

    /** 양력(현지 날짜) → 음력. */
    fun solarToLunar(year: Int, month: Int, day: Int, utOffsetHours: Double): LunarDate {
        val targetJdn = floor(JulianDayConverter.fromGregorian(year, month, day, 0.0) + 0.5).toLong()
        val newMoonK = newMoonIndexOnOrBefore(targetJdn, utOffsetHours)
        val sui = suiContaining(newMoonK, utOffsetHours)
        val info = sui.first { it.newMoonK == newMoonK }
        val dayOfMonth = (targetJdn - newMoonCivilJdn(newMoonK, utOffsetHours) + 1).toInt()
        return LunarDate(info.lunarYear, info.monthNumber, dayOfMonth, info.isLeap)
    }

    /** 음력 → 양력(현지 날짜). */
    fun lunarToSolar(
        lunarYear: Int,
        lunarMonth: Int,
        day: Int,
        isLeapMonth: Boolean,
        utOffsetHours: Double,
    ): CalendarDate {
        // 음력 1~10월은 그 해 歲에, 11~12월은 다음 歲에 속한다.
        val suiYear = if (lunarMonth >= WINTER_MONTH) lunarYear + 1 else lunarYear
        val sui = buildSui(suiYear, utOffsetHours)
        val info = sui.firstOrNull {
            it.lunarYear == lunarYear && it.monthNumber == lunarMonth && it.isLeap == isLeapMonth
        } ?: error("존재하지 않는 음력 월: ${lunarYear}년 ${if (isLeapMonth) "윤" else ""}${lunarMonth}월")
        val monthStartJdn = newMoonCivilJdn(info.newMoonK, utOffsetHours)
        val resultJdn = monthStartJdn + (day - 1)
        return JulianDayConverter.toGregorian(resultJdn.toDouble() - 0.5)
    }

    /** 한 歲(동지~동지)의 삭월별 음력 월 배정. */
    private fun buildSui(suiYear: Int, utOffsetHours: Double): List<MonthInfo> {
        val firstWinterMonth = winterSolsticeMonthK(suiYear - 1, utOffsetHours) // 음력 (suiYear-1) 11월
        val nextWinterMonth = winterSolsticeMonthK(suiYear, utOffsetHours) // 다음 歲의 11월
        val isLeapYear = (nextWinterMonth - firstWinterMonth) == LEAP_YEAR_MONTHS

        val result = mutableListOf<MonthInfo>()
        var monthNumber = WINTER_MONTH // 11월부터
        var lunarYear = suiYear - 1
        var leapAssigned = false
        var lastNumber = WINTER_MONTH
        var lastYear = suiYear - 1

        var k = firstWinterMonth
        while (k < nextWinterMonth) {
            // 윤달: 윤년이면서, 동지월이 아니고, 아직 윤달 미배정이며, 중기 없는 첫 삭월.
            val isLeapMonth = isLeapYear && !leapAssigned && k != firstWinterMonth && !hasMajorTerm(k, utOffsetHours)
            if (isLeapMonth) {
                result.add(MonthInfo(k, lastYear, lastNumber, isLeap = true))
                leapAssigned = true
            } else {
                result.add(MonthInfo(k, lunarYear, monthNumber, isLeap = false))
                lastNumber = monthNumber
                lastYear = lunarYear
                if (monthNumber == LAST_MONTH) {
                    monthNumber = 1
                    lunarYear++
                } else {
                    monthNumber++
                }
            }
            k++
        }
        return result
    }

    /** [newMoonK] 삭월을 포함하는 歲(동지~동지)의 월 배정. */
    private fun suiContaining(newMoonK: Int, utOffsetHours: Double): List<MonthInfo> {
        val approxYear = JulianDayConverter.toGregorian(newMoonCivilJdn(newMoonK, utOffsetHours).toDouble() - 0.5).year
        var suiYear = approxYear
        while (winterSolsticeMonthK(suiYear - 1, utOffsetHours) > newMoonK) suiYear--
        while (winterSolsticeMonthK(suiYear, utOffsetHours) <= newMoonK) suiYear++
        return buildSui(suiYear, utOffsetHours)
    }

    /** [solarYear] 12월 동지(황경 270°)가 드는 삭월의 k. */
    private fun winterSolsticeMonthK(solarYear: Int, utOffsetHours: Double): Int {
        val solsticeUt = SolarLongitude.solarTermInstantUT(solarYear, WINTER_SOLSTICE_TERM_INDEX)
        return newMoonIndexOnOrBefore(civilJdn(solsticeUt, utOffsetHours), utOffsetHours)
    }

    /** 삭월 [newMoonK, newMoonK+1) 에 중기(中氣, 황경 30°k)가 드는가. 없으면 윤달 후보. */
    private fun hasMajorTerm(newMoonK: Int, utOffsetHours: Double): Boolean {
        val startUt = LunarPhase.newMoonInstantUT(newMoonK)
        val endJdn = newMoonCivilJdn(newMoonK + 1, utOffsetHours)
        val startLongitude = SolarLongitude.apparentLongitudeDegAtUT(startUt)
        // 삭 시작 황경 이상 첫 중기(30°의 배수).
        val nextMajorLongitude = ceil(startLongitude / DEGREES_PER_MAJOR_TERM - EPSILON) * DEGREES_PER_MAJOR_TERM
        val near = startUt + (nextMajorLongitude - startLongitude) / MEAN_DAILY_MOTION
        val termUt = SolarLongitude.instantOfLongitudeUT(normalizeDeg(nextMajorLongitude), near)
        return civilJdn(termUt, utOffsetHours) < endJdn
    }

    /** [jdn] 이하 가장 가까운 삭의 k. */
    private fun newMoonIndexOnOrBefore(jdn: Long, utOffsetHours: Double): Int {
        var k = LunarPhase.newMoonIndexNear(jdn.toDouble())
        while (newMoonCivilJdn(k, utOffsetHours) > jdn) k--
        while (newMoonCivilJdn(k + 1, utOffsetHours) <= jdn) k++
        return k
    }

    /** 삭 k의 시작일(현지 자정 기준 민간일 번호). */
    private fun newMoonCivilJdn(newMoonK: Int, utOffsetHours: Double): Long =
        civilJdn(LunarPhase.newMoonInstantUT(newMoonK), utOffsetHours)

    /** UT 율리우스일 → 현지 자정 기준 민간일 번호. 프레임(utOffsetHours)은 여기서만 적용. */
    private fun civilJdn(utJd: Double, utOffsetHours: Double): Long =
        floor(utJd + utOffsetHours / HOURS_PER_DAY + 0.5).toLong()

    private fun normalizeDeg(degrees: Double): Double = ((degrees % FULL_CIRCLE) + FULL_CIRCLE) % FULL_CIRCLE

    /** 歲 안의 한 삭월: 삭 인덱스 + 배정된 음력 연/월/윤달. */
    private data class MonthInfo(
        val newMoonK: Int,
        val lunarYear: Int,
        val monthNumber: Int,
        val isLeap: Boolean,
    )

    private const val WINTER_MONTH = 11 // 동지월 = 음력 11월(자월)
    private const val LAST_MONTH = 12
    private const val LEAP_YEAR_MONTHS = 13
    private const val WINTER_SOLSTICE_TERM_INDEX = 18 // 황경 270° = 동지 (15°×18)
    private const val DEGREES_PER_MAJOR_TERM = 30.0
    private const val FULL_CIRCLE = 360.0
    private const val HOURS_PER_DAY = 24.0
    private const val MEAN_DAILY_MOTION = 0.98565
    private const val EPSILON = 1e-9
}
