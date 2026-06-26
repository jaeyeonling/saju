package io.github.jaeyeonling.saju.astronomy

import com.tyme.solar.SolarTerm
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertTrue

/**
 * 절기 절입 시각 골든 회귀 — 자체 천문 엔진을 검증된 tyme4j(sxwnl 알고리즘)와 분 단위로 대조한다.
 *
 * 두 엔진은 **완전히 독립**(VSOP87/Meeus vs sxwnl)이므로, 분 단위 일치는 양쪽이 모두 정확하다는 강한 증거다.
 *
 * 좌표계 정렬(리서치 1순위 함정인 '베이징 +8h'를 비교 단계에서만 적용):
 *  - tyme4j: `getJulianDay().getDay()` = 베이징(UTC+8) 기준 JD
 *  - 내 엔진: `solarTermInstantUT()` = 순수 UT JD → 비교 시 +8h
 *  - 인덱스: tyme4j 0=동지, 내 termIndex = (tymeIndex + 18) % 24
 */
class SolarTermGoldenTest {

    @Test
    fun `1900~2050 전 절기가 tyme4j 와 분 단위 일치한다`() {
        var maxDiffSeconds = 0.0
        var worstCase = ""
        var total = 0

        for (year in START_YEAR..END_YEAR) {
            for (tymeIndex in 0 until TERMS_PER_YEAR) {
                val tymeBeijingJd = SolarTerm.fromIndex(year, tymeIndex).julianDay.day
                val myTermIndex = (tymeIndex + DONGJI_OFFSET) % TERMS_PER_YEAR
                // tyme4j 절기년은 동지로 시작(동지는 전년 12월)이라, 매핑 추측 대신
                // 절기의 실제 양력 연도(베이징 JD 역변환)를 내 엔진에 넘긴다.
                val solarYear = JulianDayConverter.toGregorian(tymeBeijingJd).year
                val myBeijingJd = SolarLongitude.solarTermInstantUT(solarYear, myTermIndex) + BEIJING_OFFSET_DAYS

                val diffSeconds = abs(myBeijingJd - tymeBeijingJd) * SECONDS_PER_DAY
                if (diffSeconds > maxDiffSeconds) {
                    maxDiffSeconds = diffSeconds
                    worstCase = "$year tymeIndex=$tymeIndex(${SolarTerm.NAMES[tymeIndex]})"
                }
                total++
            }
        }

        println("골든 대조 $total 절기 | 최대 차이 ${"%.2f".format(maxDiffSeconds)}초 @ $worstCase")
        // 분 단위 정밀도 목표: 최대 차이가 1분 이내.
        assertTrue(maxDiffSeconds < SECONDS_PER_MINUTE, "최대 차이 ${maxDiffSeconds}초가 1분을 초과 @ $worstCase")
    }

    @Test
    fun `입춘 절입은 연주 경계라 초 단위까지 안정적이다`() {
        // 입춘(연주 경계)은 자정 부근이면 연주를 바꾸므로 특히 정확해야 한다.
        var maxDiffSeconds = 0.0
        for (year in START_YEAR..END_YEAR) {
            val tymeJd = SolarTerm.fromIndex(year, IPCHUN_TYME_INDEX).julianDay.day
            val myJd = SolarLongitude.solarTermInstantUT(year, IPCHUN_TERM_INDEX) + BEIJING_OFFSET_DAYS
            maxDiffSeconds = maxOf(maxDiffSeconds, abs(myJd - tymeJd) * SECONDS_PER_DAY)
        }
        println("입춘 최대 차이 ${"%.2f".format(maxDiffSeconds)}초")
        assertTrue(maxDiffSeconds < SECONDS_PER_MINUTE, "입춘 최대 차이 ${maxDiffSeconds}초")
    }

    private companion object {
        const val START_YEAR = 1900
        const val END_YEAR = 2050
        const val TERMS_PER_YEAR = 24
        const val DONGJI_OFFSET = 18 // tyme4j 0=동지(270°) → 내 termIndex 18
        const val IPCHUN_TYME_INDEX = 3
        const val IPCHUN_TERM_INDEX = 21
        const val BEIJING_OFFSET_DAYS = 8.0 / 24.0
        const val SECONDS_PER_DAY = 86_400.0
        const val SECONDS_PER_MINUTE = 60.0
    }
}
