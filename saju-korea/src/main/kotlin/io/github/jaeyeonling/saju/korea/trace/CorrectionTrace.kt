package io.github.jaeyeonling.saju.korea.trace

import io.github.jaeyeonling.saju.astronomy.CalendarDate
import io.github.jaeyeonling.saju.lunar.CalendarBasis
import io.github.jaeyeonling.saju.trace.ChartComputation

/*
 * 한국 시간 보정 근거(trace) — 법정시(시계 시각) → 진태양시로 가는 각 단계를 분해한다.
 *
 * 지금까지 `KoreanSaju.computeTrueSolar` 내부에서 합산 후 버려지던 단계별 보정량
 * (서머타임·표준 자오선·경도·균시차)을 구조화해, 시각화·검증·LLM 소비자가
 * "왜 07:00 출생이 06:18 사주가 되는가"를 단계별로 보여줄 수 있게 한다.
 */

/** 보정 단계 종류 — 파이프라인 순서(서머타임 → 자오선 → 경도 → 균시차)대로 나열된다. */
public enum class CorrectionStepKind {
    /** 서머타임 제거 — 시행 구간이면 시계가 1시간 앞서 있어 되돌린다(−60분). */
    DST,

    /** 표준 자오선 판정 — 절대 순간(UT)을 확정한다(시각 이동 없음, 델타 0). */
    MERIDIAN,

    /** 경도 보정 — `(출생지 경도 − 표준 자오선) × 4분/도`. */
    LONGITUDE,

    /** 균시차(equation of time) — 평균태양시와 실제 태양시의 차이. */
    EOT,
}

/**
 * 보정 한 단계.
 *
 * @property deltaMinutes 이 단계가 시각에 더한 분(음수 = 되돌림). 자오선 단계는 항상 0.
 * @property basis 단계 설명(한국어) — 시각화·LLM 소비용.
 */
public data class CorrectionStep(
    public val kind: CorrectionStepKind,
    public val deltaMinutes: Double,
    public val basis: String,
)

/**
 * 시간 보정 근거 전체.
 *
 * @property steps 파이프라인 순서(DST → MERIDIAN → LONGITUDE → EOT). 델타 합 = [totalOffsetMinutes].
 * @property totalOffsetMinutes 총 보정량(분) — `trueSolarOffsetMinutes` 와 동치.
 * @property corrected 보정 적용 후 진태양시 벽시계.
 * @property utJd 출생의 절대 순간(UT 율리우스일) — 절기 비교·대운 계산의 기준.
 * @property utOffsetHours 진태양시 벽시계의 UT 오프셋(시간).
 */
public data class CorrectionTrace(
    public val steps: List<CorrectionStep>,
    public val totalOffsetMinutes: Double,
    public val corrected: CalendarDate,
    public val utJd: Double,
    public val utOffsetHours: Double,
)

/**
 * 음력 → 양력 변환 근거 — 음력 입력 경로(`KoreanSaju.fromLunarCivilTimeWithTrace`)에서만 채워진다.
 *
 * @property calendarBasis 음력 기준 역법(KASI vs 중국 농력 — 윤달 배치가 갈리는 해에 결과가 다르다).
 */
public data class LunarConversionBasis(
    public val solarYear: Int,
    public val solarMonth: Int,
    public val solarDay: Int,
    public val calendarBasis: CalendarBasis,
    public val basis: String,
)

/**
 * 한국 보정 사주판 + 전체 도출 근거 — `KoreanSaju.fromCivilTimeWithTrace` 의 반환.
 *
 * @property correction 시간 보정 근거(1단계).
 * @property core 사주판 + 천문·4기둥 근거(2·3단계).
 * @property lunarConversion 음력 입력이면 변환 근거, 양력 입력이면 null.
 */
public data class KoreanChartComputation(
    public val correction: CorrectionTrace,
    public val core: ChartComputation,
    public val lunarConversion: LunarConversionBasis? = null,
)
