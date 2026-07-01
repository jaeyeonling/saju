package io.github.jaeyeonling.saju.astronomy

import kotlin.math.PI

// 천문 계산 공용 상수·헬퍼 (java.time-free, 순수 산술).

/** J2000.0 의 율리우스일 (2000-01-01 12:00 TT). */
internal const val J2000_EPOCH = 2451545.0

/** 율리우스 세기(36525일)·천년(365250일) 길이. */
internal const val JULIAN_CENTURY_DAYS = 36525.0
internal const val JULIAN_MILLENNIUM_DAYS = 365250.0

internal const val TWO_PI = 2.0 * PI
internal const val DEG_TO_RAD = PI / 180.0
internal const val RAD_TO_DEG = 180.0 / PI

/** 1초각(arcsecond) → 라디안. */
internal const val ARCSEC_TO_RAD = PI / 180.0 / 3600.0

/** 각도를 `[0, 2π)` 로 정규화. */
internal fun normalizeRadians(angle: Double): Double {
    var a = angle % TWO_PI
    if (a < 0.0) a += TWO_PI
    return a
}

/** 각도를 `[0, 360)` 도(degree)로 정규화. */
internal fun normalizeDegrees(deg: Double): Double {
    val d = deg % 360.0
    return if (d < 0.0) d + 360.0 else d
}

/** `[-π, π]` 로 감싼다 — 뉴턴 역산에서 목표 황경과의 최단 차이를 구할 때 쓴다. */
internal fun wrapToPi(angle: Double): Double {
    var a = (angle + PI) % TWO_PI
    if (a < 0.0) a += TWO_PI
    return a - PI
}

/** Horner 다항식 평가: coeffs = [c0, c1, c2, ...] → c0 + c1·x + c2·x² + … */
internal fun horner(
    x: Double,
    vararg coeffs: Double,
): Double {
    var result = 0.0
    for (i in coeffs.indices.reversed()) {
        result = result * x + coeffs[i]
    }
    return result
}

/** 율리우스일(TT) → J2000 기준 율리우스 세기. */
internal fun julianCenturies(jde: Double): Double = (jde - J2000_EPOCH) / JULIAN_CENTURY_DAYS

/** 율리우스일(TT) → J2000 기준 율리우스 천년 (VSOP87 의 τ). */
internal fun julianMillennia(jde: Double): Double = (jde - J2000_EPOCH) / JULIAN_MILLENNIUM_DAYS

// ── ΔT(지구 자전 지연) 시간척도 변환 — TT(역학시)↔UT(세계시). 단일 진실 소스. ──

/** 하루의 초 = 86400. ΔT(초)를 일(day) 단위로 환산할 때 쓴다. */
internal const val SECONDS_PER_DAY = 86_400.0

/** 율리우스일 → 십진 연도(ΔT 다항식 입력용). 2000 + (jd − J2000)/365.25. */
internal fun decimalYearOf(jd: Double): Double = 2000.0 + (jd - J2000_EPOCH) / 365.25

/** UT 율리우스일 → TT 율리우스일 (ΔT 만큼 더함). */
internal fun toTT(utJd: Double): Double = utJd + DeltaT.seconds(decimalYearOf(utJd)) / SECONDS_PER_DAY

/** TT 율리우스일 → UT 율리우스일 (ΔT 만큼 뺌). */
internal fun toUT(ttJd: Double): Double = ttJd - DeltaT.seconds(decimalYearOf(ttJd)) / SECONDS_PER_DAY
