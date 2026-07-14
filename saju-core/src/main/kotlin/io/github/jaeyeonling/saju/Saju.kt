package io.github.jaeyeonling.saju

import io.github.jaeyeonling.saju.astronomy.JulianDayConverter
import io.github.jaeyeonling.saju.astronomy.SolarLongitude
import io.github.jaeyeonling.saju.astronomy.daysInGregorianMonth
import io.github.jaeyeonling.saju.astronomy.normalizeDegrees
import io.github.jaeyeonling.saju.derivation.Daeun
import io.github.jaeyeonling.saju.derivation.DaeunCalculator
import io.github.jaeyeonling.saju.derivation.DaeunDirection
import io.github.jaeyeonling.saju.derivation.DaeunStartAgePolicy
import io.github.jaeyeonling.saju.derivation.PillarDerivation
import io.github.jaeyeonling.saju.derivation.SajuConfig
import io.github.jaeyeonling.saju.derivation.Seun
import io.github.jaeyeonling.saju.derivation.YearBoundary
import io.github.jaeyeonling.saju.domain.Cheongan
import io.github.jaeyeonling.saju.domain.Eumyang
import io.github.jaeyeonling.saju.domain.Ganji
import io.github.jaeyeonling.saju.domain.Gender
import io.github.jaeyeonling.saju.domain.Jiji
import io.github.jaeyeonling.saju.domain.Pillar
import io.github.jaeyeonling.saju.domain.PillarPosition
import io.github.jaeyeonling.saju.domain.SajuChart
import io.github.jaeyeonling.saju.domain.floorMod
import io.github.jaeyeonling.saju.trace.AstronomyTrace
import io.github.jaeyeonling.saju.trace.ChartComputation
import io.github.jaeyeonling.saju.trace.DaeunTrace
import io.github.jaeyeonling.saju.trace.DayPillarTrace
import io.github.jaeyeonling.saju.trace.HourPillarTrace
import io.github.jaeyeonling.saju.trace.MonthPillarTrace
import io.github.jaeyeonling.saju.trace.PillarsTrace
import io.github.jaeyeonling.saju.trace.SolarTermInstant
import io.github.jaeyeonling.saju.trace.YearBoundaryTrace
import io.github.jaeyeonling.saju.trace.YearPillarTrace
import java.util.Locale
import kotlin.math.floor
import kotlin.math.roundToInt

/**
 * 사주 만세력 공개 진입점 — 천문 엔진과 4기둥 도출을 조립한다.
 *
 * **P3 단계**: 한국 시간 보정(진태양시·서머타임·자시 학파) 이전. 입력은 이미 보정된 로컬 시각으로 간주한다.
 * P4 에서 한국 보정 파이프라인을 거치는 진입점이 saju-korea 에 추가된다.
 *
 * `...WithTrace` 계열은 같은 계산에 도출 근거([ChartComputation]·[DaeunTrace])를 붙여 반환하고,
 * 무근거 계열은 traced 결과에서 값만 뽑아 위임한다 — 산식의 단일 진실 소스.
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
        config: SajuConfig = SajuConfig.DEFAULT,
        second: Int = 0,
    ): SajuChart = fromLocalDateTimeWithTrace(year, month, day, hour, minute, utOffsetHours, config, second).chart

    /**
     * [fromLocalDateTime] 과 동일 계산 + 도출 근거([AstronomyTrace]·[PillarsTrace]) 반환.
     * 지금까지 내부에서 버려지던 중간값(절입 시각, 월지 오프셋, JDN, 자시 전진 여부)을 보존한다.
     */
    @JvmStatic
    @JvmOverloads
    public fun fromLocalDateTimeWithTrace(
        year: Int,
        month: Int,
        day: Int,
        hour: Int,
        minute: Int,
        utOffsetHours: Double,
        config: SajuConfig = SajuConfig.DEFAULT,
        second: Int = 0,
    ): ChartComputation {
        requireValidCivilDateTime(year, month, day, hour, minute)
        require(second in 0..MAX_SECOND) { "초는 0~59: $second" }
        require(utOffsetHours.isFinite()) { "utOffsetHours 는 유한값이어야 합니다: $utOffsetHours" }

        // 초까지 반영 — 절기 절입 ±60초 경계에서 연·월주가 흔들리지 않게(진태양시 보정의 초 손실 방지).
        val timeFraction = (hour * SECONDS_PER_HOUR + minute * SECONDS_PER_MINUTE + second) / SECONDS_PER_DAY
        val localJd = JulianDayConverter.fromGregorian(year, month, day, timeFraction)
        val utJd = localJd - utOffsetHours / HOURS_PER_DAY

        // 연주: 경계 절기(입춘세수=입춘, 동지세수=동지) 절입 시각과 비교 — 학파별 보정은 정책에 위임.
        val boundary = config.yearBoundary
        val boundaryUt = SolarLongitude.solarTermInstantUT(year, boundary.termIndex)
        val isAfterBoundary = utJd >= boundaryUt
        val sajuYear = boundary.resolveYear(year, isAfterBoundary)
        val yearGanji = PillarDerivation.yearPillar(sajuYear)

        // 월주: 출생 순간 황경 → 절기 월(입춘 315°부터 30°마다). 학파 무관(절기 기준 고정).
        val solarLongitudeDeg = SolarLongitude.apparentLongitudeDegAtUT(utJd)
        val longitudeFromIpchun = normalizeDegrees(solarLongitudeDeg - IPCHUN_LONGITUDE_DEG)
        val monthOffset = floor(longitudeFromIpchun / DEGREES_PER_MONTH).toInt() % MONTHS_PER_YEAR
        val monthGanji = PillarDerivation.monthPillar(yearGanji.gan, monthOffset)
        val wolduStartGan = yearGanji.gan.monthStartStem()

        // 직전·직후 절(節): 절은 황경 ≡ 15 (mod 30). 시각화·검증용 절기 좌표.
        val degreesIntoMonth = floorModDouble(solarLongitudeDeg - JEOL_PHASE_DEG, DEGREES_PER_MONTH)
        val prevTerm =
            jeolInstant(
                normalizeDegrees(solarLongitudeDeg - degreesIntoMonth),
                utJd - degreesIntoMonth / MEAN_DAILY_MOTION,
            )
        val nextTerm =
            jeolInstant(
                normalizeDegrees(solarLongitudeDeg + (DEGREES_PER_MONTH - degreesIntoMonth)),
                utJd + (DEGREES_PER_MONTH - degreesIntoMonth) / MEAN_DAILY_MOTION,
            )

        // 일주: 로컬 날짜의 율리우스일 번호. 자시 학파(정자시=23시 이후 다음날)는 정책에 위임.
        val zishiDateShift = config.zishi.dayPillarShift(hour)
        val julianDayNumber =
            floor(JulianDayConverter.fromGregorian(year, month, day, 0.0) + 0.5).toLong() + zishiDateShift
        val dayGanji = PillarDerivation.dayPillar(julianDayNumber)

        // 시주: 시지(2시간 단위) + 일간. 시간(時干)은 위에서 정책대로 시프트된 일간을 따라간다.
        val hourJi = Jiji.fromIndex((hour + 1) / HOURS_PER_BRANCH)
        val hourGanji = PillarDerivation.hourPillar(dayGanji.gan, hourJi)
        val sijuStartGan = dayGanji.gan.hourStartStem()

        val chart =
            SajuChart(
                year = Pillar(PillarPosition.YEAR, yearGanji),
                month = Pillar(PillarPosition.MONTH, monthGanji),
                day = Pillar(PillarPosition.DAY, dayGanji),
                hour = Pillar(PillarPosition.HOUR, hourGanji),
            )

        return ChartComputation(
            chart = chart,
            utJd = utJd,
            astronomy =
                AstronomyTrace(
                    solarLongitudeDeg = solarLongitudeDeg,
                    prevTerm = prevTerm,
                    nextTerm = nextTerm,
                    yearBoundary = YearBoundaryTrace(boundary.termIndex, boundaryUt, isAfterBoundary),
                ),
            pillars =
                PillarsTrace(
                    year =
                        YearPillarTrace(
                            sajuYear = sajuYear,
                            ganji = yearGanji,
                            basis = yearBasis(year, boundary, isAfterBoundary, sajuYear, yearGanji),
                        ),
                    month =
                        MonthPillarTrace(
                            monthOffset = monthOffset,
                            wolduStartGan = wolduStartGan,
                            ganji = monthGanji,
                            basis = monthBasis(prevTerm, monthOffset, yearGanji, wolduStartGan, monthGanji),
                        ),
                    day =
                        DayPillarTrace(
                            julianDayNumber = julianDayNumber,
                            zishiAdvanced = zishiDateShift != 0L,
                            ganji = dayGanji,
                            basis = dayBasis(julianDayNumber, zishiDateShift != 0L, dayGanji),
                        ),
                    hour =
                        HourPillarTrace(
                            hourBranch = hourJi,
                            sijuStartGan = sijuStartGan,
                            ganji = hourGanji,
                            basis = hourBasis(hour, hourJi, dayGanji, sijuStartGan, hourGanji),
                        ),
                ),
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
        monthPillar: Ganji,
        yearStemEumyang: Eumyang,
        isMale: Boolean,
        count: Int = DEFAULT_DAEUN_COUNT,
        config: SajuConfig = SajuConfig.DEFAULT,
    ): List<Daeun> = daeunWithTrace(utJd, monthPillar, yearStemEumyang, isMale, count, config).entries

    /**
     * 대운 도출 — 성별([Gender]) 오버로드. 성별은 원국에는 쓰이지 않고 대운 방향(순행·역행)만 정하며,
     * [Gender.isMale] 로 [DaeunDirection.of] 에 위임한다.
     */
    @JvmStatic
    @JvmOverloads
    public fun daeun(
        utJd: Double,
        monthPillar: Ganji,
        yearStemEumyang: Eumyang,
        gender: Gender,
        count: Int = DEFAULT_DAEUN_COUNT,
        config: SajuConfig = SajuConfig.DEFAULT,
    ): List<Daeun> = daeun(utJd, monthPillar, yearStemEumyang, gender.isMale, count, config)

    /**
     * [daeun] 과 동일 계산 + 도출 근거([DaeunTrace]) 반환 —
     * 방향 판정, 목표 절(節)까지의 일수, 대운수 환산 과정을 보존한다.
     */
    @JvmStatic
    @JvmOverloads
    public fun daeunWithTrace(
        utJd: Double,
        monthPillar: Ganji,
        yearStemEumyang: Eumyang,
        isMale: Boolean,
        count: Int = DEFAULT_DAEUN_COUNT,
        config: SajuConfig = SajuConfig.DEFAULT,
    ): DaeunTrace {
        val direction = DaeunDirection.of(yearStemEumyang, isMale)
        val birthLongitude = SolarLongitude.apparentLongitudeDegAtUT(utJd)

        // 절(節)은 황경 ≡ 15 (mod 30). 현재 절 시작부터 경과한 각도.
        val degreesIntoMonth = floorModDouble(birthLongitude - JEOL_PHASE_DEG, DEGREES_PER_MONTH)
        val targetTerm: SolarTermInstant
        val daysToBoundary: Double
        if (direction == DaeunDirection.FORWARD) {
            val nextJeolLon = normalizeDegrees(birthLongitude + (DEGREES_PER_MONTH - degreesIntoMonth))
            val near = utJd + (DEGREES_PER_MONTH - degreesIntoMonth) / MEAN_DAILY_MOTION
            targetTerm = jeolInstant(nextJeolLon, near)
            daysToBoundary = targetTerm.utJd - utJd
        } else {
            val prevJeolLon = normalizeDegrees(birthLongitude - degreesIntoMonth)
            val near = utJd - degreesIntoMonth / MEAN_DAILY_MOTION
            targetTerm = jeolInstant(prevJeolLon, near)
            daysToBoundary = utJd - targetTerm.utJd
        }

        val startAge = config.daeunStartAge.startAge(daysToBoundary)
        val isForward = direction == DaeunDirection.FORWARD
        return DaeunTrace(
            direction = direction,
            directionBasis =
                "연간 ${yearStemEumyang.koreanName} · ${if (isMale) "남성" else "여성"} → " +
                    if (isForward) "순행" else "역행",
            daysToTerm = daysToBoundary,
            targetTerm = targetTerm,
            startAge = startAge,
            startAgeBasis = startAgeBasis(targetTerm, daysToBoundary, startAge, config.daeunStartAge),
            entries = DaeunCalculator.sequence(monthPillar, direction, startAge, count),
        )
    }

    /** 세운(歲運) — 특정 연도의 간지(입춘 기준 연주). */
    @JvmStatic
    public fun seun(year: Int): Seun = Seun(year, PillarDerivation.yearPillar(year))

    /**
     * 양력 시각 입력 공통 검증 — 라이브러리 전역 fail-fast 일관성(음력 경로와 동일 정책).
     * 잘못된 입력이 '그럴듯하지만 틀린 사주'로 조용히 새는 것을 막는다.
     */
    @JvmStatic
    public fun requireValidCivilDateTime(
        year: Int,
        month: Int,
        day: Int,
        hour: Int,
        minute: Int,
    ) {
        require(year in SUPPORTED_MIN_YEAR..SUPPORTED_MAX_YEAR) {
            "지원 연도($SUPPORTED_MIN_YEAR~$SUPPORTED_MAX_YEAR) 밖: $year"
        }
        require(month in 1..MONTHS_PER_YEAR) { "월은 1~12: $month" }
        // 월별 말일·윤년까지 검증 — Meeus JD 공식은 순수 산술이라 2/31 을 3월로 굴려버린다.
        // 공용 daysInGregorianMonth 로 음력 변환 경로(LunarConverter)와 동일 규칙을 공유한다.
        val maxDay = daysInGregorianMonth(year, month)
        require(day in 1..maxDay) { "일은 1~$maxDay ($year-${month}월): $day" }
        require(hour in 0..MAX_HOUR) { "시는 0~23: $hour" }
        require(minute in 0..MAX_MINUTE) { "분은 0~59: $minute" }
    }

    /** 황경 [longitudeDeg] 의 절(節) 절입 순간 — termIndex 는 황경/15°(홀수 = 절). */
    private fun jeolInstant(
        longitudeDeg: Double,
        nearUtJd: Double,
    ): SolarTermInstant =
        SolarTermInstant(
            termIndex = floorMod((longitudeDeg / DEGREES_PER_TERM).roundToInt(), TERMS_PER_YEAR),
            utJd = SolarLongitude.instantOfLongitudeUT(longitudeDeg, nearUtJd),
        )

    private fun yearBasis(
        inputYear: Int,
        boundary: YearBoundary,
        isAfter: Boolean,
        sajuYear: Int,
        ganji: Ganji,
    ): String {
        val boundaryName = if (boundary == YearBoundary.IPCHUN) "입춘" else "동지"
        val sidePhrase = if (isAfter) "이후" else "이전"
        return "${inputYear}년 $boundaryName(황경 ${boundary.termIndex * DEGREES_PER_TERM}°) 절입 $sidePhrase " +
            "→ 사주연도 $sajuYear · ($sajuYear − 4) mod 60 = ${ganji.index} → ${ganji.koreanName}"
    }

    private fun monthBasis(
        prevTerm: SolarTermInstant,
        monthOffset: Int,
        yearGanji: Ganji,
        wolduStartGan: Cheongan,
        monthGanji: Ganji,
    ): String =
        "직전 절(황경 ${prevTerm.termIndex * DEGREES_PER_TERM}°) → 월지 오프셋 $monthOffset(인월 기준) · " +
            "오호둔 ${yearGanji.gan.koreanName}년 시작간 ${wolduStartGan.koreanName} → ${monthGanji.koreanName}"

    private fun dayBasis(
        julianDayNumber: Long,
        zishiAdvanced: Boolean,
        dayGanji: Ganji,
    ): String =
        "JDN $julianDayNumber${if (zishiAdvanced) " (정자시 23시 이후 → +1일)" else ""} · " +
            "($julianDayNumber + ${PillarDerivation.DAY_OFFSET}) mod 60 = ${dayGanji.index} → ${dayGanji.koreanName}"

    private fun hourBasis(
        hour: Int,
        hourJi: Jiji,
        dayGanji: Ganji,
        sijuStartGan: Cheongan,
        hourGanji: Ganji,
    ): String =
        "${hour}시 → 시지 ${hourJi.koreanName}(오프셋 ${hourJi.ordinal}) · " +
            "오자둔 ${dayGanji.gan.koreanName}일 자시 시작간 ${sijuStartGan.koreanName} → ${hourGanji.koreanName}"

    /**
     * 대운수 환산 근거 — 내장 정책만 "÷ 3" 산식을 서술한다.
     * 사용자 정의 정책은 산식을 알 수 없으므로 중립 문구로 거짓 서술을 피한다.
     */
    private fun startAgeBasis(
        targetTerm: SolarTermInstant,
        daysToBoundary: Double,
        startAge: Int,
        policy: DaeunStartAgePolicy,
    ): String {
        val distance =
            "목표 절(황경 ${targetTerm.termIndex * DEGREES_PER_TERM}°)까지 " +
                "${"%.3f".format(Locale.ROOT, daysToBoundary)}일"
        return when {
            policy === DaeunStartAgePolicy.THREE_DAYS_ONE_YEAR -> "$distance ÷ 3 → ${startAge}세 (3일1세 반올림)"
            policy === DaeunStartAgePolicy.FLOOR -> "$distance ÷ 3 → ${startAge}세 (3일1세 버림)"
            else -> "$distance → ${startAge}세 (사용자 정의 정책)"
        }
    }

    private fun floorModDouble(
        value: Double,
        modulus: Double,
    ): Double = ((value % modulus) + modulus) % modulus

    private const val IPCHUN_LONGITUDE_DEG = 315.0
    private const val DEGREES_PER_MONTH = 30.0
    private const val DEGREES_PER_TERM = 15
    private const val TERMS_PER_YEAR = 24
    private const val MONTHS_PER_YEAR = 12
    private const val HOURS_PER_DAY = 24.0
    private const val HOURS_PER_BRANCH = 2
    private const val MAX_HOUR = 23
    private const val MAX_MINUTE = 59
    private const val MAX_SECOND = 59
    private const val SECONDS_PER_HOUR = 3600.0
    private const val SECONDS_PER_MINUTE = 60.0
    private const val SECONDS_PER_DAY = 86_400.0

    /**
     * 지원 입력 연도 범위. 골든 벡터로 **정확도가 검증된 구간은 절기·4기둥 1900~2050, 음력 1900~2099**이며,
     * 그 위(2051~2100)는 천문 다항식 외삽으로 분 단위 정밀도를 유지하나 골든 대조가 없는 구간이다.
     */
    public const val SUPPORTED_MIN_YEAR: Int = 1900
    public const val SUPPORTED_MAX_YEAR: Int = 2100

    private const val JEOL_PHASE_DEG = 15.0

    // 태양의 평균 일일 황경 이동 = 360° / 365.25일 ≈ 0.98565°/일. 절기 경계까지 남은 일수의 1차 추정에 쓴다.
    private const val MEAN_DAILY_MOTION = 0.98565
    private const val DEFAULT_DAEUN_COUNT = 8
}
