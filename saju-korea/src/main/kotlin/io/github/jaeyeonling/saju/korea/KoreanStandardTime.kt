package io.github.jaeyeonling.saju.korea

/**
 * 한국 표준시 연혁 — 표준 자오선 변경과 서머타임(일광절약시간) 구간.
 *
 * 이것이 중국식 만세력과 결정적으로 다른 지점이다. 출생 시점의 법정시 정의가 다르면
 * 절기 경계·자시 경계에서 기둥이 통째로 어긋난다.
 *
 * 표준 자오선:
 *  - ~1908-03-31: 표준시 이전(LMT, 지방평균시) → [standardMeridianDeg] = null
 *  - 1908-04-01 ~ 1911-12-31: 동경 127.5° (UTC+8:30)
 *  - 1912-01-01 ~ 1954-03-20: 동경 135° (UTC+9, 일제)
 *  - 1954-03-21 ~ 1961-08-09: 동경 127.5° (UTC+8:30)
 *  - 1961-08-10 ~ : 동경 135° (UTC+9)
 */
public object KoreanStandardTime {

    /** 표준 자오선(도)과 서머타임 여부. [standardMeridianDeg] = null 은 LMT(1908 이전). */
    public data class Info(
        public val standardMeridianDeg: Double?,
        public val isSummerTime: Boolean,
    )

    /** 출생 법정시점의 표준시 정보. */
    @JvmStatic
    public fun at(year: Int, month: Int, day: Int, hour: Int, minute: Int): Info {
        val stamp = stampOf(year, month, day, hour, minute)
        val meridian: Double? = when {
            stamp < STANDARD_TIME_START -> null // LMT
            stamp < MERIDIAN_135_FIRST -> MERIDIAN_127_5
            stamp < MERIDIAN_127_5_SECOND -> MERIDIAN_135
            stamp < MERIDIAN_135_SECOND -> MERIDIAN_127_5
            else -> MERIDIAN_135
        }
        val summerTime = SUMMER_TIME_PERIODS.any { stamp in it.first..it.second }
        return Info(meridian, summerTime)
    }

    /** (년·월·일·시·분)을 비교 가능한 정수 타임스탬프로. */
    private fun stampOf(year: Int, month: Int, day: Int, hour: Int, minute: Int): Long =
        ((((year.toLong() * 100 + month) * 100 + day) * 100 + hour) * 100 + minute)

    public const val MERIDIAN_135: Double = 135.0
    public const val MERIDIAN_127_5: Double = 127.5

    private val STANDARD_TIME_START = stampOf(1908, 4, 1, 0, 0)
    private val MERIDIAN_135_FIRST = stampOf(1912, 1, 1, 0, 0)
    private val MERIDIAN_127_5_SECOND = stampOf(1954, 3, 21, 0, 0)
    private val MERIDIAN_135_SECOND = stampOf(1961, 8, 10, 0, 0)

    // 한국 서머타임 12구간 (시작 포함 ~ 종료 포함 직전). 1948~1960은 자정 전환, 1987~88은 02·03시 전환.
    private val SUMMER_TIME_PERIODS: List<Pair<Long, Long>> = listOf(
        stampOf(1948, 6, 1, 0, 0) to stampOf(1948, 9, 12, 23, 59),
        stampOf(1949, 4, 3, 0, 0) to stampOf(1949, 9, 10, 23, 59),
        stampOf(1950, 4, 1, 0, 0) to stampOf(1950, 9, 9, 23, 59),
        stampOf(1951, 5, 6, 0, 0) to stampOf(1951, 9, 8, 23, 59),
        stampOf(1955, 5, 5, 0, 0) to stampOf(1955, 9, 8, 23, 59),
        stampOf(1956, 5, 20, 0, 0) to stampOf(1956, 9, 29, 23, 59),
        stampOf(1957, 5, 5, 0, 0) to stampOf(1957, 9, 21, 23, 59),
        stampOf(1958, 5, 4, 0, 0) to stampOf(1958, 9, 20, 23, 59),
        stampOf(1959, 5, 3, 0, 0) to stampOf(1959, 9, 19, 23, 59),
        stampOf(1960, 5, 1, 0, 0) to stampOf(1960, 9, 17, 23, 59),
        stampOf(1987, 5, 10, 2, 0) to stampOf(1987, 10, 11, 2, 59),
        stampOf(1988, 5, 8, 2, 0) to stampOf(1988, 10, 9, 2, 59),
    )
}
