package io.github.jaeyeonling.saju.astronomy

/**
 * 황도경사(ε) — 황도면과 천구적도의 기울기. 균시차의 적경 변환에 쓰인다.
 * Meeus _Astronomical Algorithms_ Ch.22.
 */
internal object ObliquityOfEcliptic {

    /** 평균 황도경사 ε0 (radian). Meeus 22.2. */
    fun meanRad(jdeTT: Double): Double {
        val t = julianCenturies(jdeTT)
        // 23°26′21.448″ − 46.8150″T − 0.00059″T² + 0.001813″T³
        val seconds = horner(t, 21.448, -46.8150, -0.00059, 0.001813)
        val degrees = 23.0 + 26.0 / 60.0 + seconds / 3600.0
        return degrees * DEG_TO_RAD
    }

    /** 진 황도경사 ε = ε0 + Δε (章動 보정). */
    fun trueRad(jdeTT: Double): Double = meanRad(jdeTT) + Nutation.obliquityRad(jdeTT)
}
