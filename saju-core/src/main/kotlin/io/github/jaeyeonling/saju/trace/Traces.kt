package io.github.jaeyeonling.saju.trace

import io.github.jaeyeonling.saju.derivation.Daeun
import io.github.jaeyeonling.saju.derivation.DaeunDirection
import io.github.jaeyeonling.saju.domain.Cheongan
import io.github.jaeyeonling.saju.domain.Ganji
import io.github.jaeyeonling.saju.domain.Jiji
import io.github.jaeyeonling.saju.domain.SajuChart

/*
 * 도출 근거(trace) 타입 — 결과만이 아니라 **왜 그 결과인가**를 구조화한다.
 *
 * 계산 파이프라인의 중간값(절기 절입 시각, 월지 오프셋, 율리우스일 번호 …)은 지금까지
 * 파사드 내부에서 버려졌다. 시각화·검증·LLM 소비자가 "8글자가 어떻게 나왔는지"를
 * 보여줄 수 있도록, `...WithTrace` 진입점이 이 타입들로 중간값을 보존해 반환한다.
 *
 * 설계 규약([SinStrength.basis][io.github.jaeyeonling.saju.interpretation] 관례의 일반화):
 *  - 수치 필드(기계 소비용)와 한국어 [basis] 문자열(사람/LLM 소비용)을 **둘 다** 담는다.
 *  - 모든 타입은 불변 data class, java.time-free(core 아키텍처 규칙).
 */

/** 절기 절입 순간 — [termIndex] 규약은 황경 15°당 1(0=춘분 0°, 21=입춘 315°). */
public data class SolarTermInstant(
    public val termIndex: Int,
    public val utJd: Double,
)

/**
 * 연 경계(입춘세수·동지세수) 판정 근거.
 *
 * @property termIndex 경계 절기 인덱스(입춘 21, 동지 18).
 * @property utJd 경계 절입 UT 율리우스일.
 * @property isAfter 출생이 경계 절입 이후인가(`utJd_출생 >= utJd_경계`, 경계 포함).
 */
public data class YearBoundaryTrace(
    public val termIndex: Int,
    public val utJd: Double,
    public val isAfter: Boolean,
)

/**
 * 천문 근거 — 출생 순간의 태양 위치와 절기 좌표.
 *
 * @property solarLongitudeDeg 출생 순간 태양 겉보기 황경(도, `[0, 360)`).
 * @property prevTerm 직전 절(節, termIndex 홀수) — 월주 경계.
 * @property nextTerm 직후 절(節).
 * @property yearBoundary 연 경계 절입과 판정.
 */
public data class AstronomyTrace(
    public val solarLongitudeDeg: Double,
    public val prevTerm: SolarTermInstant,
    public val nextTerm: SolarTermInstant,
    public val yearBoundary: YearBoundaryTrace,
)

/** 연주 근거 — 입춘(동지) 보정 후 간지 연도와 60갑자 산식. */
public data class YearPillarTrace(
    public val sajuYear: Int,
    public val ganji: Ganji,
    public val basis: String,
)

/**
 * 월주 근거 — 절기 월 오프셋과 오호둔(五虎遁).
 *
 * @property monthOffset 월지 오프셋(인월=0 … 축월=11, 절기 절입 기준).
 * @property wolduStartGan 오호둔 시작 천간(연간의 인월 천간).
 */
public data class MonthPillarTrace(
    public val monthOffset: Int,
    public val wolduStartGan: Cheongan,
    public val ganji: Ganji,
    public val basis: String,
)

/**
 * 일주 근거 — 율리우스일 번호와 자시 날짜 전환.
 *
 * @property julianDayNumber 자시 정책 반영 후 민간일 JDN.
 * @property zishiAdvanced 정자시(23시 이후)로 하루 전진했는가.
 */
public data class DayPillarTrace(
    public val julianDayNumber: Long,
    public val zishiAdvanced: Boolean,
    public val ganji: Ganji,
    public val basis: String,
)

/**
 * 시주 근거 — 시지와 오자둔(五子遁).
 *
 * @property hourBranch 시지(2시간 단위).
 * @property sijuStartGan 오자둔 시작 천간(일간의 자시 천간).
 */
public data class HourPillarTrace(
    public val hourBranch: Jiji,
    public val sijuStartGan: Cheongan,
    public val ganji: Ganji,
    public val basis: String,
)

/** 4기둥 도출 근거 묶음. */
public data class PillarsTrace(
    public val year: YearPillarTrace,
    public val month: MonthPillarTrace,
    public val day: DayPillarTrace,
    public val hour: HourPillarTrace,
)

/**
 * 사주판 + 도출 근거 — [io.github.jaeyeonling.saju.Saju.fromLocalDateTimeWithTrace] 의 반환.
 *
 * @property utJd 출생의 절대 순간(UT 율리우스일) — 절기 비교·대운 계산의 기준.
 */
public data class ChartComputation(
    public val chart: SajuChart,
    public val utJd: Double,
    public val astronomy: AstronomyTrace,
    public val pillars: PillarsTrace,
)

/**
 * 대운 근거 — 방향 판정과 대운수(시작 나이) 환산 과정.
 *
 * @property daysToTerm 출생 → 목표 절(節)까지 일수(방향 기준, 항상 양수).
 * @property targetTerm 목표 절(순행이면 다음 절, 역행이면 이전 절).
 */
public data class DaeunTrace(
    public val direction: DaeunDirection,
    public val directionBasis: String,
    public val daysToTerm: Double,
    public val targetTerm: SolarTermInstant,
    public val startAge: Int,
    public val startAgeBasis: String,
    public val entries: List<Daeun>,
)
