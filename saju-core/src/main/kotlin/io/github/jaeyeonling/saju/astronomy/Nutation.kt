package io.github.jaeyeonling.saju.astronomy

import kotlin.math.sin

/**
 * IAU1980 章動(nutation) — 황경 章動 Δψ.
 *
 * 계수표(table 22.A, 63항)는 classpath 리소스 `nutation-iau1980.txt`(commenthol/astronomia, MIT)에서 로드한다.
 * 절기 황경 계산엔 황경 章動(Δψ)만 필요하므로 황경경사 章動(Δε)은 평가하지 않는다.
 *
 * 각 행: `[d, m, n, f, ω, s0, s1, c0, c1]`. `Δψ += sin(arg)·(s0 + s1·T)` 를 0.0001″ 단위로 합산.
 */
internal object Nutation {
    private const val RESOURCE = "/io/github/jaeyeonling/saju/astronomy/nutation-iau1980.txt"
    private const val UNIT = 1e-4 * ARCSEC_TO_RAD // 0.0001″ → rad

    private val rows: Array<DoubleArray> = load()

    /** 황경 章動 Δψ (radian). [jdeTT] 는 율리우스일(TT). */
    fun longitudeRad(jdeTT: Double): Double {
        val t = julianCenturies(jdeTT)
        // 기본 인자 (라디안). IAU1980 표준 다항식.
        val elongationMoon = horner(t, 297.85036, 445267.11148, -0.0019142, 1.0 / 189474.0) * DEG_TO_RAD
        val anomalySun = horner(t, 357.52772, 35999.050340, -0.0001603, -1.0 / 300000.0) * DEG_TO_RAD
        val anomalyMoon = horner(t, 134.96298, 477198.867398, 0.0086972, 1.0 / 56250.0) * DEG_TO_RAD
        val argLatMoon = horner(t, 93.27191, 483202.017538, -0.0036825, 1.0 / 327270.0) * DEG_TO_RAD
        val ascNode = horner(t, 125.04452, -1934.136261, 0.0020708, 1.0 / 450000.0) * DEG_TO_RAD

        // 작은 항부터 누적하기 위해 역순(원 데이터는 진폭 내림차순).
        var deltaPsi = 0.0
        for (i in rows.indices.reversed()) {
            val row = rows[i]
            val argument = row[0] * elongationMoon +
                row[1] * anomalySun +
                row[2] * anomalyMoon +
                row[3] * argLatMoon +
                row[4] * ascNode
            deltaPsi += sin(argument) * (row[5] + row[6] * t)
        }
        return deltaPsi * UNIT
    }

    private fun load(): Array<DoubleArray> {
        val stream = Nutation::class.java.getResourceAsStream(RESOURCE)
            ?: error("章動 리소스를 찾을 수 없습니다: $RESOURCE")
        val parsed = mutableListOf<DoubleArray>()
        stream.bufferedReader().useLines { lines ->
            lines.forEach { line ->
                if (line.isBlank() || line.startsWith("#")) return@forEach
                parsed.add(line.split(' ').map { it.toDouble() }.toDoubleArray())
            }
        }
        return parsed.toTypedArray()
    }
}
