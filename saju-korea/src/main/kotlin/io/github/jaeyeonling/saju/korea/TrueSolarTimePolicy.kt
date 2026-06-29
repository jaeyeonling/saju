package io.github.jaeyeonling.saju.korea

/**
 * 진태양시(眞太陽時) 보정 정책 — 법정시를 실제 태양 남중 기준 시각으로 얼마나 보정하느냐.
 *
 * 통설(명리 실무)은 경도보정 + 균시차 풀보정([FULL]). 일부는 보정을 생략하거나(법정시 그대로) 경도만 본다.
 * 진태양시는 출생지 경도·표준 자오선·서머타임이 얽힌 한국 도메인 개념이라 `saju-korea` 에만 존재한다.
 */
public enum class TrueSolarTimePolicy {
    /** 무보정 — 법정시(표준 자오선)를 그대로 쓴다. */
    NONE,

    /** 경도보정만 — 출생지 경도 차이만 반영하고 균시차는 무시. */
    LONGITUDE_ONLY,

    /** 경도보정 + 균시차 — 실제 진태양시(통설·현 기본). */
    FULL,
}
