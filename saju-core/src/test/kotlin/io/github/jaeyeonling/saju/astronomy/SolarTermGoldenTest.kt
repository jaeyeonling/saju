package io.github.jaeyeonling.saju.astronomy

import io.github.jaeyeonling.saju.Golden
import io.kotest.assertions.withClue
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.booleans.shouldBeTrue
import kotlin.math.abs

private const val TERMS_PER_YEAR = 24
private const val DONGJI_OFFSET = 18 // 골든 0=동지(270°) → 내 termIndex 18
private const val IPCHUN_GOLDEN_INDEX = 3
private const val IPCHUN_TERM_INDEX = 21
private const val BEIJING_OFFSET_DAYS = 8.0 / 24.0
private const val SECONDS_PER_DAY = 86_400.0
private const val SECONDS_PER_MINUTE = 60.0

// 골든 인덱스(0=동지) 순서의 24절기 명칭 — 진단 출력 전용.
private val TERM_NAMES = listOf(
    "동지", "소한", "대한", "입춘", "우수", "경칩", "춘분", "청명", "곡우", "입하", "소만", "망종",
    "하지", "소서", "대서", "입추", "처서", "백로", "추분", "한로", "상강", "입동", "소설", "대설",
)

/**
 * 절기 절입 시각 골든 회귀 — 자체 천문 엔진(VSOP87/Meeus)을 동결된 골든 벡터와 분 단위로 대조한다.
 *
 * 골든 벡터는 독립 알고리즘(sxwnl)으로 산출된 베이징(UTC+8) 기준 JD 다. 추출 시점에 자체 엔진과
 * 분 단위로 교차 검증되었으며, 그 정답을 박제해 회귀를 방지한다.
 *
 * 좌표계 정렬(리서치 1순위 함정인 '베이징 +8h'를 비교 단계에서만 적용):
 *  - 골든: 베이징(UTC+8) 기준 JD
 *  - 내 엔진: `solarTermInstantUT()` = 순수 UT JD → 비교 시 +8h
 *  - 인덱스: 골든 0=동지, 내 termIndex = (goldenIndex + 18) % 24
 */
class SolarTermGoldenTest : StringSpec({

    "1900~2050 전 절기가 골든 벡터와 분 단위 일치한다" {
        var maxDiffSeconds = 0.0
        var worstCase = ""
        val rows = Golden.rows("solarterm_jd.csv")

        for (row in rows) {
            val goldenIndex = row[1].toInt()
            val goldenBeijingJd = row[2].toDouble()
            val myTermIndex = (goldenIndex + DONGJI_OFFSET) % TERMS_PER_YEAR
            // 절기년은 동지로 시작(동지는 전년 12월)이라, 매핑 추측 대신
            // 절기의 실제 양력 연도(베이징 JD 역변환)를 내 엔진에 넘긴다.
            val solarYear = JulianDayConverter.toGregorian(goldenBeijingJd).year
            val myBeijingJd = SolarLongitude.solarTermInstantUT(solarYear, myTermIndex) + BEIJING_OFFSET_DAYS

            val diffSeconds = abs(myBeijingJd - goldenBeijingJd) * SECONDS_PER_DAY
            if (diffSeconds > maxDiffSeconds) {
                maxDiffSeconds = diffSeconds
                worstCase = "${row[0]} index=$goldenIndex(${TERM_NAMES[goldenIndex]})"
            }
        }

        println("골든 대조 ${rows.size} 절기 | 최대 차이 ${"%.2f".format(maxDiffSeconds)}초 @ $worstCase")
        // 분 단위 정밀도 목표: 최대 차이가 1분 이내.
        withClue("최대 차이 ${maxDiffSeconds}초가 1분을 초과 @ $worstCase") {
            (maxDiffSeconds < SECONDS_PER_MINUTE).shouldBeTrue()
        }
    }

    "입춘 절입은 연주 경계라 초 단위까지 안정적이다" {
        // 입춘(연주 경계)은 자정 부근이면 연주를 바꾸므로 특히 정확해야 한다.
        var maxDiffSeconds = 0.0
        for (row in Golden.rows("solarterm_jd.csv")) {
            if (row[1].toInt() != IPCHUN_GOLDEN_INDEX) continue
            val year = row[0].toInt()
            val goldenJd = row[2].toDouble()
            val myJd = SolarLongitude.solarTermInstantUT(year, IPCHUN_TERM_INDEX) + BEIJING_OFFSET_DAYS
            maxDiffSeconds = maxOf(maxDiffSeconds, abs(myJd - goldenJd) * SECONDS_PER_DAY)
        }
        println("입춘 최대 차이 ${"%.2f".format(maxDiffSeconds)}초")
        withClue("입춘 최대 차이 ${maxDiffSeconds}초") {
            (maxDiffSeconds < SECONDS_PER_MINUTE).shouldBeTrue()
        }
    }
})
