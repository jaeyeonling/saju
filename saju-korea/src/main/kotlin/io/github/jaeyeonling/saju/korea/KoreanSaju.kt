package io.github.jaeyeonling.saju.korea

import io.github.jaeyeonling.saju.Saju
import io.github.jaeyeonling.saju.astronomy.Ephemeris
import io.github.jaeyeonling.saju.astronomy.JulianDayConverter
import io.github.jaeyeonling.saju.derivation.Daeun
import io.github.jaeyeonling.saju.domain.Gender
import io.github.jaeyeonling.saju.domain.SajuChart
import io.github.jaeyeonling.saju.korea.trace.CorrectionStep
import io.github.jaeyeonling.saju.korea.trace.CorrectionStepKind
import io.github.jaeyeonling.saju.korea.trace.CorrectionTrace
import io.github.jaeyeonling.saju.korea.trace.KoreanChartComputation
import io.github.jaeyeonling.saju.korea.trace.LunarConversionBasis
import io.github.jaeyeonling.saju.lunar.CalendarBasis
import io.github.jaeyeonling.saju.lunar.LunarConverter
import java.util.Locale
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlin.math.truncate

/** 하루 = 24시간 (율리우스일 ↔ 시각 변환용). */
private const val HOURS_PER_DAY = 24.0

/**
 * 진태양시 순간 — 법정시 보정 결과(단계별 근거 포함).
 *
 * @property jd 진태양시 율리우스일.
 * @property utOffsetHours 진태양시 기준 UT 오프셋(시간).
 * @property steps 단계별 보정 근거(서머타임 → 자오선 → 경도 → 균시차).
 */
internal data class TrueSolarInstant(
    val jd: Double,
    val utOffsetHours: Double,
    val steps: List<CorrectionStep>,
) {
    /** 오프셋을 되돌린 UT 기준 율리우스일(절대 순간) — 대운 절기거리 계산에 쓴다. */
    val utJd: Double get() = jd - utOffsetHours / HOURS_PER_DAY
}

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
 *
 * `...WithTrace` 계열은 같은 계산에 단계별 보정 근거([CorrectionTrace])와
 * 천문·4기둥 근거([KoreanChartComputation])를 붙여 반환하고, 무근거 계열은 traced 결과에 위임한다.
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
    ): SajuChart =
        fromLunarCivilTimeWithTrace(
            lunarYear,
            lunarMonth,
            lunarDay,
            isLeapMonth,
            hour,
            minute,
            longitudeDeg,
            config,
            basis,
        ).core.chart

    /**
     * [fromLunarCivilTime] 과 동일 계산 + 전체 도출 근거 반환.
     * [KoreanChartComputation.lunarConversion] 에 음력→양력 변환 근거가 채워진다.
     */
    @JvmStatic
    @JvmOverloads
    public fun fromLunarCivilTimeWithTrace(
        lunarYear: Int,
        lunarMonth: Int,
        lunarDay: Int,
        isLeapMonth: Boolean,
        hour: Int,
        minute: Int,
        longitudeDeg: Double = Birthplace.SEOUL.longitudeDeg,
        config: KoreanSajuConfig = KoreanSajuConfig.DEFAULT,
        basis: CalendarBasis = CalendarBasis.KOREA,
    ): KoreanChartComputation {
        val solar = LunarConverter.toSolar(lunarYear, lunarMonth, lunarDay, isLeapMonth, basis)
        val computation =
            fromCivilTimeWithTrace(solar.year, solar.month, solar.day, hour, minute, longitudeDeg, config)
        return computation.copy(
            lunarConversion =
                LunarConversionBasis(
                    solarYear = solar.year,
                    solarMonth = solar.month,
                    solarDay = solar.day,
                    calendarBasis = basis,
                    basis =
                        "음력 $lunarYear-$lunarMonth-$lunarDay${if (isLeapMonth) " 윤달" else ""} " +
                            "(${if (basis == CalendarBasis.KOREA) "KASI" else "중국 농력"} 기준) " +
                            "→ 양력 ${solar.year}-${solar.month}-${solar.day}",
                ),
        )
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
    ): SajuChart = fromCivilTimeWithTrace(year, month, day, hour, minute, longitudeDeg, config).core.chart

    /**
     * [fromCivilTime] 과 동일 계산 + 전체 도출 근거([KoreanChartComputation]) 반환 —
     * 시간 보정 단계, 천문 좌표, 4기둥 산식을 모두 보존한다.
     */
    @JvmStatic
    @JvmOverloads
    public fun fromCivilTimeWithTrace(
        year: Int,
        month: Int,
        day: Int,
        hour: Int,
        minute: Int,
        longitudeDeg: Double = Birthplace.SEOUL.longitudeDeg,
        config: KoreanSajuConfig = KoreanSajuConfig.DEFAULT,
    ): KoreanChartComputation {
        val instant = computeTrueSolar(year, month, day, hour, minute, longitudeDeg, config.trueSolarTime)
        val ts = JulianDayConverter.toGregorian(instant.jd)
        // 진태양시 시각을 초까지 보존해 절기 경계 오판 방지.
        val core =
            Saju.fromLocalDateTimeWithTrace(
                ts.year,
                ts.month,
                ts.day,
                ts.hour,
                ts.minute,
                instant.utOffsetHours,
                config.saju,
                ts.second,
            )
        val legalJd = legalJulianDay(year, month, day, hour, minute)
        return KoreanChartComputation(
            correction =
                CorrectionTrace(
                    steps = instant.steps,
                    totalOffsetMinutes = (instant.jd - legalJd) * MINUTES_PER_DAY,
                    corrected = ts,
                    utJd = instant.utJd,
                    utOffsetHours = instant.utOffsetHours,
                ),
            core = core,
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
        val computation = fromCivilTimeWithTrace(year, month, day, hour, minute, longitudeDeg, config)
        val chart = computation.core.chart
        return Saju.daeun(
            computation.correction.utJd,
            chart.month.ganji,
            chart.year.gan.eumyang,
            isMale,
            count,
            config.saju,
        )
    }

    /**
     * 법정시 + 출생지 + 성별([Gender])로 대운 시퀀스 — [daeun] 의 성별 오버로드.
     * 성별은 대운 방향(순행·역행)만 정한다(원국 무관).
     */
    @JvmStatic
    @JvmOverloads
    public fun daeun(
        year: Int,
        month: Int,
        day: Int,
        hour: Int,
        minute: Int,
        gender: Gender,
        longitudeDeg: Double = Birthplace.SEOUL.longitudeDeg,
        count: Int = DEFAULT_DAEUN_COUNT,
        config: KoreanSajuConfig = KoreanSajuConfig.DEFAULT,
    ): List<Daeun> = daeun(year, month, day, hour, minute, gender.isMale, longitudeDeg, count, config)

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
        val trueSolarJd = computeTrueSolar(year, month, day, hour, minute, longitudeDeg, policy).jd
        val legalJd = legalJulianDay(year, month, day, hour, minute)
        return (trueSolarJd - legalJd) * MINUTES_PER_DAY
    }

    /** 법정시를 [policy] 에 따라 진태양시 순간([TrueSolarInstant])으로 변환한다. */
    private fun computeTrueSolar(
        year: Int,
        month: Int,
        day: Int,
        hour: Int,
        minute: Int,
        longitudeDeg: Double,
        policy: TrueSolarTimePolicy,
    ): TrueSolarInstant {
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
        return TrueSolarInstant(
            jd = trueSolarJd,
            utOffsetHours = trueSolarUtOffsetHours,
            steps =
                correctionSteps(
                    info = info,
                    year = year,
                    longitudeDeg = longitudeDeg,
                    standardMeridian = standardMeridian,
                    policy = policy,
                    equationOfTimeMinutes = equationOfTimeMinutes,
                ),
        )
    }

    /**
     * 단계별 보정 근거 — 항상 4단계(DST → MERIDIAN → LONGITUDE → EOT)를 방출한다.
     * 적용되지 않은 단계도 델타 0 + 이유를 담아, 소비자가 "왜 이 단계가 비었는지"를 보여줄 수 있게 한다.
     * 델타 합 = 총 보정량(`trueSolarOffsetMinutes`) — MERIDIAN 은 절대 순간 확정 단계라 항상 0.
     */
    private fun correctionSteps(
        info: KoreanStandardTime.Info,
        year: Int,
        longitudeDeg: Double,
        standardMeridian: Double,
        policy: TrueSolarTimePolicy,
        equationOfTimeMinutes: Double,
    ): List<CorrectionStep> {
        val dst =
            CorrectionStep(
                kind = CorrectionStepKind.DST,
                deltaMinutes = if (info.isSummerTime) -SUMMER_TIME_HOURS * MINUTES_PER_HOUR else 0.0,
                basis =
                    if (info.isSummerTime) {
                        "서머타임 시행 구간 — 시계가 1시간 앞서 있어 되돌림"
                    } else {
                        "서머타임 시행 구간 아님"
                    },
            )
        val meridian =
            CorrectionStep(
                kind = CorrectionStepKind.MERIDIAN,
                deltaMinutes = 0.0,
                basis =
                    if (info.standardMeridianDeg == null) {
                        "${year}년은 표준시 도입(1908) 이전 — 지방평균시(LMT) · 출생지 경도가 곧 기준"
                    } else {
                        "표준 자오선 동경 ${formatDegrees(info.standardMeridianDeg)}° " +
                            "(${utcOffsetLabel(info.standardMeridianDeg)}) 기준으로 절대 순간(UT) 확정"
                    },
            )
        val longitudeDelta =
            when {
                policy == TrueSolarTimePolicy.NONE -> 0.0
                else -> (longitudeDeg - standardMeridian) * MINUTES_PER_DEGREE
            }
        val longitude =
            CorrectionStep(
                kind = CorrectionStepKind.LONGITUDE,
                deltaMinutes = longitudeDelta,
                basis =
                    when {
                        policy == TrueSolarTimePolicy.NONE -> "무보정 정책(NONE) — 경도 보정 생략"
                        info.standardMeridianDeg == null -> "LMT 시대 — 표준 자오선 = 출생지 경도라 경도 보정 0"
                        else ->
                            "출생지 경도 ${formatDegrees(longitudeDeg)}° − 표준 자오선 " +
                                "${formatDegrees(standardMeridian)}° = ${signedMinutes(longitudeDelta)}"
                    },
            )
        val eot =
            CorrectionStep(
                kind = CorrectionStepKind.EOT,
                deltaMinutes = equationOfTimeMinutes,
                basis =
                    if (policy == TrueSolarTimePolicy.FULL) {
                        "균시차(궤도 이심률·황도 경사) ${signedMinutes(equationOfTimeMinutes)} — " +
                            "평균태양시와 실제 태양시의 차이"
                    } else {
                        "균시차 미적용 (정책 ${policy.name})"
                    },
            )
        return listOf(dst, meridian, longitude, eot)
    }

    /** 표준 자오선 → UTC 오프셋 라벨. 135°→UTC+9, 127.5°→UTC+8:30 */
    private fun utcOffsetLabel(meridianDeg: Double): String {
        val hours = meridianDeg / DEGREES_PER_HOUR
        val whole = truncate(hours).toInt()
        val minutes = ((hours - whole) * MINUTES_PER_HOUR).roundToInt()
        return if (minutes == 0) "UTC+$whole" else "UTC+$whole:${minutes.toString().padStart(2, '0')}"
    }

    /** 부호 붙인 분 표기(음수 부호는 U+2212 '−'). 예: "+3.20분", "−32.09분" */
    private fun signedMinutes(value: Double): String {
        val sign = if (value >= 0) "+" else "−"
        return "$sign${"%.2f".format(Locale.ROOT, abs(value))}분"
    }

    /** 도(度) 표기 — 정수면 정수로(135.0→"135"), 소수면 그대로(127.5→"127.5"). */
    private fun formatDegrees(deg: Double): String =
        if (deg == deg.toLong().toDouble()) deg.toLong().toString() else deg.toString()

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
    private const val MINUTES_PER_HOUR = 60.0
    private const val MINUTES_PER_DAY = 1440.0

    /** 경도 1° = 4분 (지구 자전 360° / 1440분). */
    private const val MINUTES_PER_DEGREE = 4.0
}
