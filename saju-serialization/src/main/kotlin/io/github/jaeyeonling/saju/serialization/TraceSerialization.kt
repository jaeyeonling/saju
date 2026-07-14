// Java 소비자에게 파일명 유래 `TraceSerializationKt` 대신 안정적인 `TraceSerialization` 을 노출한다.
@file:JvmName("TraceSerialization")

package io.github.jaeyeonling.saju.serialization

import io.github.jaeyeonling.saju.derivation.DaeunDirection
import io.github.jaeyeonling.saju.interpretation.StrengthContribution
import io.github.jaeyeonling.saju.korea.trace.CorrectionStep
import io.github.jaeyeonling.saju.korea.trace.CorrectionTrace
import io.github.jaeyeonling.saju.korea.trace.KoreanChartComputation
import io.github.jaeyeonling.saju.korea.trace.LunarConversionBasis
import io.github.jaeyeonling.saju.trace.AstronomyTrace
import io.github.jaeyeonling.saju.trace.ChartComputation
import io.github.jaeyeonling.saju.trace.DaeunTrace
import io.github.jaeyeonling.saju.trace.PillarsTrace
import io.github.jaeyeonling.saju.trace.SolarTermInstant
import io.github.jaeyeonling.saju.trace.YearBoundaryTrace
import kotlinx.serialization.encodeToString

/*
 * 도출 근거(trace) → JSON 직렬화 진입점 — SajuSerialization.kt 와 같은 규칙의 toDto()/toJson() 확장.
 */

public fun SolarTermInstant.toDto(): SolarTermInstantDto = SolarTermInstantDto(termIndex, utJd)

public fun YearBoundaryTrace.toDto(): YearBoundaryTraceDto = YearBoundaryTraceDto(termIndex, utJd, isAfter)

public fun AstronomyTrace.toDto(): AstronomyTraceDto =
    AstronomyTraceDto(
        solarLongitudeDeg = solarLongitudeDeg,
        prevTerm = prevTerm.toDto(),
        nextTerm = nextTerm.toDto(),
        yearBoundary = yearBoundary.toDto(),
    )

public fun PillarsTrace.toDto(): PillarsTraceDto =
    PillarsTraceDto(
        year = YearPillarTraceDto(year.sajuYear, year.ganji.toDto(), year.basis),
        month =
            MonthPillarTraceDto(
                monthOffset = month.monthOffset,
                wolduStartGan = month.wolduStartGan.koreanName,
                ganji = month.ganji.toDto(),
                basis = month.basis,
            ),
        day =
            DayPillarTraceDto(
                julianDayNumber = day.julianDayNumber,
                zishiAdvanced = day.zishiAdvanced,
                ganji = day.ganji.toDto(),
                basis = day.basis,
            ),
        hour =
            HourPillarTraceDto(
                hourBranch = hour.hourBranch.koreanName,
                sijuStartGan = hour.sijuStartGan.koreanName,
                ganji = hour.ganji.toDto(),
                basis = hour.basis,
            ),
    )

public fun ChartComputation.toDto(): ChartComputationDto =
    ChartComputationDto(
        chart = chart.toDto(),
        utJd = utJd,
        astronomy = astronomy.toDto(),
        pillars = pillars.toDto(),
    )

/** 사주판 + 도출 근거를 JSON 문자열로. */
public fun ChartComputation.toJson(): String = sajuJson.encodeToString(toDto())

public fun DaeunTrace.toDto(): DaeunTraceDto =
    DaeunTraceDto(
        direction = direction.name,
        directionKorean = if (direction == DaeunDirection.FORWARD) "순행" else "역행",
        directionBasis = directionBasis,
        daysToTerm = daysToTerm,
        targetTerm = targetTerm.toDto(),
        startAge = startAge,
        startAgeBasis = startAgeBasis,
        entries = entries.map { it.toDto() },
    )

/** 대운 근거를 JSON 문자열로. */
public fun DaeunTrace.toJson(): String = sajuJson.encodeToString(toDto())

public fun CorrectionStep.toDto(): CorrectionStepDto = CorrectionStepDto(kind.name, deltaMinutes, basis)

public fun CorrectionTrace.toDto(): CorrectionTraceDto =
    CorrectionTraceDto(
        steps = steps.map { it.toDto() },
        totalOffsetMinutes = totalOffsetMinutes,
        corrected =
            CorrectedTimeDto(
                year = corrected.year,
                month = corrected.month,
                day = corrected.day,
                hour = corrected.hour,
                minute = corrected.minute,
                second = corrected.second,
            ),
        utJd = utJd,
        utOffsetHours = utOffsetHours,
    )

public fun LunarConversionBasis.toDto(): LunarConversionDto =
    LunarConversionDto(
        solarYear = solarYear,
        solarMonth = solarMonth,
        solarDay = solarDay,
        calendarBasis = calendarBasis.name,
        basis = basis,
    )

public fun KoreanChartComputation.toDto(): KoreanChartComputationDto =
    KoreanChartComputationDto(
        correction = correction.toDto(),
        core = core.toDto(),
        lunarConversion = lunarConversion?.toDto(),
    )

/** 한국 보정 사주판 + 전체 도출 근거를 JSON 문자열로. */
public fun KoreanChartComputation.toJson(): String = sajuJson.encodeToString(toDto())

public fun StrengthContribution.toDto(): StrengthContributionDto =
    StrengthContributionDto(
        position = position.name,
        slot = slot.name,
        slotKorean = slot.koreanName,
        stem = stem.koreanName,
        sipseongGroup = sipseongGroup.koreanName,
        weight = weight,
        basis = basis,
    )
