package io.github.jaeyeonling.saju.astronomy

/**
 * 율리우스일(Julian Day) 값 객체 — 기준 스케일([TimeScale])을 함께 태깅한다.
 *
 * JD 0.0 = proleptic Julian 4713 BC 1월 1일 12:00. 사주 계산은 double 정밀도(약 0.02ms)면 충분하다.
 *
 * value class 가 아니라 data class 인 이유: value class 는 JVM 에서 unbox 되어 Java 가
 * 생성자/메서드를 호출할 수 없다. 공개 타입은 Java interop 을 위해 data class 로 둔다.
 */
public data class JulianDay(
    @JvmField public val value: Double,
    @JvmField public val scale: TimeScale,
)
