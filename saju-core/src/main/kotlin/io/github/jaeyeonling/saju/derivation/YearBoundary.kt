package io.github.jaeyeonling.saju.derivation

/**
 * 연주(年柱) 경계 학파 — 한 해의 간지(干支)가 바뀌는 시점을 어느 천문 이벤트로 보느냐.
 *
 * 통설은 입춘세수(立春歲首, [IPCHUN]). 일부 유파는 동지세수(冬至歲首, [DONGJI])를 쓴다.
 * 경계 절기의 인덱스와 보정 규칙만 보유하고, 실제 절입 시각 계산은 [Saju] 가 담당한다
 * (SolarLongitude 가 core internal 이라 정책이 직접 시각을 계산할 수 없다 — 정책은 "무엇을 경계로 보는가"만 결정).
 *
 * @property termIndex 절기 인덱스(황경 15°당 1, 춘분 0° = 0). 입춘 315° = 21, 동지 270° = 18.
 * @property yearShiftWhenAfter 경계 절입 이후 출생이면 입력 연도에 더할 보정.
 * @property yearShiftWhenBefore 경계 절입 이전 출생이면 입력 연도에 더할 보정.
 */
public enum class YearBoundary(
    public val termIndex: Int,
    public val yearShiftWhenAfter: Int,
    public val yearShiftWhenBefore: Int,
) {
    /** 입춘세수(立春歲首, 통설) — 입춘(2월, 315°) 절입 이후면 당년, 이전이면 전년. */
    IPCHUN(termIndex = 21, yearShiftWhenAfter = 0, yearShiftWhenBefore = -1),

    /**
     * 동지세수(冬至歲首) — 동지(12월, 270°) 절입 이후면 차년, 이전이면 당년.
     * 동지가 연말이라 입춘과 보정 방향이 반대다. **연주에만 적용**하며 월주는 절기(節) 기준을 유지한다.
     */
    DONGJI(termIndex = 18, yearShiftWhenAfter = 1, yearShiftWhenBefore = 0),
    ;

    /** 경계 절입 시각([boundaryAfter])과 출생 순간 비교로 보정된 간지 연도. */
    public fun resolveYear(year: Int, isAfterBoundary: Boolean): Int =
        year + if (isAfterBoundary) yearShiftWhenAfter else yearShiftWhenBefore
}
