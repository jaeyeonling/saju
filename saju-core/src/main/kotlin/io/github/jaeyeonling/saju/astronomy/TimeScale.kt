package io.github.jaeyeonling.saju.astronomy

/**
 * 시각 기준 스케일.
 *
 * 천문 엔진은 오직 [TT](역학시)와 [UT](세계시)만 안다 — 타임존(KST/베이징)은 절대 모른다.
 * 시간대·진태양시 보정은 전적으로 saju-korea 레이어의 책임이다.
 *
 * - [TT] (Terrestrial Time): 천문 급수가 산출하는 역학시. 절기/삭망 계산의 내부 기준.
 * - [UT] (Universal Time): TT 에서 ΔT 를 뺀 세계시. 상위 레이어가 +9h 해서 KST 로 바꾼다.
 */
public enum class TimeScale { TT, UT }
