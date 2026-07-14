package io.github.jaeyeonling.saju.serialization

import kotlinx.serialization.Serializable

/*
 * 도출 근거(trace) DTO 묶음 — `...WithTrace` 결과를 JSON 친화 형태로 평탄화한다.
 *
 * Dtos.kt 와 같은 원칙: 기계 분기용 영문 enum 이름과 사람용 한글 라벨을 함께 담고,
 * 도메인 enum 은 문자열로 푼다. 모든 basis 는 한국어 설명 문자열이다.
 */

/** 절기 절입 순간 — termIndex 는 황경 15°당 1(0=춘분, 21=입춘 315°). */
@Serializable
public data class SolarTermInstantDto(
    public val termIndex: Int,
    public val utJd: Double,
)

/** 연 경계(입춘·동지) 절입과 판정. */
@Serializable
public data class YearBoundaryTraceDto(
    public val termIndex: Int,
    public val utJd: Double,
    public val isAfter: Boolean,
)

/** 천문 근거 — 출생 순간 태양 황경과 절기 좌표. */
@Serializable
public data class AstronomyTraceDto(
    public val solarLongitudeDeg: Double,
    public val prevTerm: SolarTermInstantDto,
    public val nextTerm: SolarTermInstantDto,
    public val yearBoundary: YearBoundaryTraceDto,
)

/** 연주 근거. */
@Serializable
public data class YearPillarTraceDto(
    public val sajuYear: Int,
    public val ganji: GanjiDto,
    public val basis: String,
)

/** 월주 근거 — wolduStartGan 은 오호둔 시작 천간(한글). */
@Serializable
public data class MonthPillarTraceDto(
    public val monthOffset: Int,
    public val wolduStartGan: String,
    public val ganji: GanjiDto,
    public val basis: String,
)

/** 일주 근거. */
@Serializable
public data class DayPillarTraceDto(
    public val julianDayNumber: Long,
    public val zishiAdvanced: Boolean,
    public val ganji: GanjiDto,
    public val basis: String,
)

/** 시주 근거 — hourBranch 는 시지(한글), sijuStartGan 은 오자둔 시작 천간(한글). */
@Serializable
public data class HourPillarTraceDto(
    public val hourBranch: String,
    public val sijuStartGan: String,
    public val ganji: GanjiDto,
    public val basis: String,
)

/** 4기둥 도출 근거 묶음. */
@Serializable
public data class PillarsTraceDto(
    public val year: YearPillarTraceDto,
    public val month: MonthPillarTraceDto,
    public val day: DayPillarTraceDto,
    public val hour: HourPillarTraceDto,
)

/** 사주판 + 도출 근거(천문·4기둥). */
@Serializable
public data class ChartComputationDto(
    public val chart: SajuChartDto,
    public val utJd: Double,
    public val astronomy: AstronomyTraceDto,
    public val pillars: PillarsTraceDto,
)

/** 대운 근거 — direction="FORWARD"|"BACKWARD"(기계 분기용), directionKorean="순행"|"역행". */
@Serializable
public data class DaeunTraceDto(
    public val direction: String,
    public val directionKorean: String,
    public val directionBasis: String,
    public val daysToTerm: Double,
    public val targetTerm: SolarTermInstantDto,
    public val startAge: Int,
    public val startAgeBasis: String,
    public val entries: List<DaeunDto>,
)

/** 보정 한 단계 — kind="DST"|"MERIDIAN"|"LONGITUDE"|"EOT". */
@Serializable
public data class CorrectionStepDto(
    public val kind: String,
    public val deltaMinutes: Double,
    public val basis: String,
)

/** 보정 후 진태양시 벽시계. */
@Serializable
public data class CorrectedTimeDto(
    public val year: Int,
    public val month: Int,
    public val day: Int,
    public val hour: Int,
    public val minute: Int,
    public val second: Int,
)

/** 한국 시간 보정 근거 전체 — steps 델타 합 = totalOffsetMinutes. */
@Serializable
public data class CorrectionTraceDto(
    public val steps: List<CorrectionStepDto>,
    public val totalOffsetMinutes: Double,
    public val corrected: CorrectedTimeDto,
    public val utJd: Double,
    public val utOffsetHours: Double,
)

/** 음력 → 양력 변환 근거 — calendarBasis="KOREA"|"CHINA". */
@Serializable
public data class LunarConversionDto(
    public val solarYear: Int,
    public val solarMonth: Int,
    public val solarDay: Int,
    public val calendarBasis: String,
    public val basis: String,
)

/** 한국 보정 사주판 + 전체 도출 근거 — 양력 입력이면 lunarConversion=null. */
@Serializable
public data class KoreanChartComputationDto(
    public val correction: CorrectionTraceDto,
    public val core: ChartComputationDto,
    public val lunarConversion: LunarConversionDto? = null,
)

/** 세력 기여 1건 — position/slot 은 영문 enum, stem/sipseongGroup 은 한글. */
@Serializable
public data class StrengthContributionDto(
    public val position: String,
    public val slot: String,
    public val slotKorean: String,
    public val stem: String,
    public val sipseongGroup: String,
    public val weight: Double,
    public val basis: String,
)
