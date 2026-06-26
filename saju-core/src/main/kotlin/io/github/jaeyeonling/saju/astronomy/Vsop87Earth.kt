package io.github.jaeyeonling.saju.astronomy

import kotlin.math.cos

/**
 * VSOP87D 지구 일심 황경(L)·반경(R) 급수 평가.
 *
 * 계수는 classpath 리소스 `vsop87d-earth.txt`(commenthol/astronomia, MIT)에서 한 번 로드해 캐싱한다.
 * VSOP87**D** 는 equinox of date 기준이라 세차 보정이 필요 없어 겉보기 황경 계산에 바로 쓰인다.
 *
 * 각 항: `A·cos(B + C·τ)`, τ = J2000 기준 율리우스 천년. 계수는 이미 radian 정규화돼 있어 스케일 분할 불필요.
 */
internal object Vsop87Earth {
    private const val RESOURCE = "/io/github/jaeyeonling/saju/astronomy/vsop87d-earth.txt"
    private const val MAX_ORDER = 6 // L0~L5, R0~R5

    // [order 0..5][term index][A, B, C]
    private val longitudeSeries: Array<Array<DoubleArray>>
    private val radiusSeries: Array<Array<DoubleArray>>

    init {
        val longitude = Array(MAX_ORDER) { mutableListOf<DoubleArray>() }
        val radius = Array(MAX_ORDER) { mutableListOf<DoubleArray>() }
        parseInto(longitude, radius)
        longitudeSeries = longitude.toSeriesArray()
        radiusSeries = radius.toSeriesArray()
    }

    /** 지구 일심 황경 (radian, `[0, 2π)`). */
    fun longitudeRad(tau: Double): Double = normalizeRadians(evaluate(longitudeSeries, tau))

    /** 지구–태양 거리 (AU). 光行差 계산에 쓰인다. */
    fun radiusAu(tau: Double): Double = evaluate(radiusSeries, tau)

    private fun evaluate(series: Array<Array<DoubleArray>>, tau: Double): Double {
        var total = 0.0
        var tauPower = 1.0
        for (order in series.indices) {
            var orderSum = 0.0
            for (term in series[order]) {
                orderSum += term[0] * cos(term[1] + term[2] * tau)
            }
            total += orderSum * tauPower
            tauPower *= tau
        }
        return total
    }

    private fun parseInto(
        longitude: Array<MutableList<DoubleArray>>,
        radius: Array<MutableList<DoubleArray>>,
    ) {
        val stream = Vsop87Earth::class.java.getResourceAsStream(RESOURCE)
            ?: error("VSOP87 리소스를 찾을 수 없습니다: $RESOURCE")
        stream.bufferedReader().useLines { lines ->
            lines.forEach { line ->
                if (line.isBlank() || line.startsWith("#")) return@forEach
                val parts = line.split(' ')
                val order = parts[1].toInt()
                val term = doubleArrayOf(parts[2].toDouble(), parts[3].toDouble(), parts[4].toDouble())
                when (parts[0]) {
                    "L" -> longitude[order].add(term)
                    "R" -> radius[order].add(term)
                }
            }
        }
    }

    private fun Array<MutableList<DoubleArray>>.toSeriesArray(): Array<Array<DoubleArray>> =
        Array(size) { this[it].toTypedArray() }
}
