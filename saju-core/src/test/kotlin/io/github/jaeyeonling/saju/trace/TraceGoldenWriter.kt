package io.github.jaeyeonling.saju.trace

import io.github.jaeyeonling.saju.Golden
import io.github.jaeyeonling.saju.Saju
import io.kotest.core.spec.style.StringSpec
import java.nio.file.Files
import java.nio.file.Path
import java.util.Locale

/**
 * trace 골든 CSV 생성기 — `./gradlew :saju-core:test -Dgolden.write=true --tests '*TraceGoldenWriter*'`
 *
 * 기존 골든(saju_pillars.csv·daeun.csv)의 입력 표본에 대해 `...WithTrace` 출력을 동결한다.
 * 4기둥·대운 **값**은 손계산 검증된 기존 골든이 보증하고, 이 파일은 그 위의 **trace 필드**
 * (절기 좌표·중간값·basis 문자열)를 언어 간 포팅 대조용으로 박제한다(TS 포트가 같은 CSV 를 읽는다).
 */
class TraceGoldenWriter : StringSpec({

    "trace 골든 CSV 재생성".config(enabled = System.getProperty("golden.write") != null) {
        val dir = Path.of("src/test/resources/golden")

        // ── trace_chart.csv — saju_pillars 표본의 1/12 서브셋(≈50건) ──
        val chartLines = mutableListOf(CHART_HEADER)
        Golden.rows("saju_pillars.csv")
            .filterIndexed { i, _ -> i % CHART_SAMPLE_STRIDE == 0 }
            .forEach { row ->
                val (y, mo, d, h, mi) = row.take(5).map { it.toInt() }
                val c = Saju.fromLocalDateTimeWithTrace(y, mo, d, h, mi, BEIJING_OFFSET)
                chartLines +=
                    listOf(
                        y, mo, d, h, mi, fmt(BEIJING_OFFSET),
                        fmt(c.utJd), fmt(c.astronomy.solarLongitudeDeg),
                        c.astronomy.prevTerm.termIndex, fmt(c.astronomy.prevTerm.utJd),
                        c.astronomy.nextTerm.termIndex, fmt(c.astronomy.nextTerm.utJd),
                        c.astronomy.yearBoundary.termIndex, fmt(c.astronomy.yearBoundary.utJd),
                        c.astronomy.yearBoundary.isAfter,
                        c.pillars.year.sajuYear, c.pillars.month.monthOffset,
                        c.pillars.month.wolduStartGan.ordinal,
                        c.pillars.day.julianDayNumber, c.pillars.day.zishiAdvanced,
                        c.pillars.hour.hourBranch.ordinal, c.pillars.hour.sijuStartGan.ordinal,
                        csvSafe(c.pillars.year.basis), csvSafe(c.pillars.month.basis),
                        csvSafe(c.pillars.day.basis), csvSafe(c.pillars.hour.basis),
                    ).joinToString(",")
            }
        Files.write(dir.resolve("trace_chart.csv"), chartLines)

        // ── trace_daeun.csv — daeun.csv 전체 표본 ──
        val daeunLines = mutableListOf(DAEUN_HEADER)
        Golden.rows("daeun.csv").forEach { row ->
            val (y, mo, d, h, mi) = row.take(5).map { it.toInt() }
            val isMale = row[5].toBoolean()
            val c = Saju.fromLocalDateTimeWithTrace(y, mo, d, h, mi, BEIJING_OFFSET)
            val t = Saju.daeunWithTrace(c.utJd, c.chart.month.ganji, c.chart.year.gan.eumyang, isMale)
            daeunLines +=
                listOf(
                    y, mo, d, h, mi, fmt(BEIJING_OFFSET), isMale,
                    t.direction.name, fmt(t.daysToTerm),
                    t.targetTerm.termIndex, fmt(t.targetTerm.utJd), t.startAge,
                    csvSafe(t.directionBasis), csvSafe(t.startAgeBasis),
                ).joinToString(",")
        }
        Files.write(dir.resolve("trace_daeun.csv"), daeunLines)

        println("trace 골든 재생성: chart ${chartLines.size - 1}건, daeun ${daeunLines.size - 1}건")
    }
})

private const val BEIJING_OFFSET = 8.0
private const val CHART_SAMPLE_STRIDE = 12

private const val CHART_HEADER =
    "year,month,day,hour,minute,utOffsetHours," +
        "utJd,solarLongitudeDeg,prevTermIndex,prevTermUtJd,nextTermIndex,nextTermUtJd," +
        "boundaryTermIndex,boundaryUtJd,isAfter,sajuYear,monthOffset,wolduStartGan," +
        "jdn,zishiAdvanced,hourBranch,sijuStartGan,yearBasis,monthBasis,dayBasis,hourBasis"

private const val DAEUN_HEADER =
    "year,month,day,hour,minute,utOffsetHours,isMale," +
        "direction,daysToTerm,targetTermIndex,targetTermUtJd,startAge,directionBasis,startAgeBasis"

/** JD·황경은 1e-8 일(≈1ms) 정밀도로 동결 — 언어 간 부동소수 오차 허용(1e-6)보다 두 자리 여유. */
private fun fmt(v: Double): String = String.format(Locale.ROOT, "%.8f", v)

/** basis 는 CSV 마지막 컬럼들로 박제 — 콤마가 섞이면 파서가 깨지므로 생성 시점에 fail-fast. */
private fun csvSafe(basis: String): String {
    require(',' !in basis) { "basis 에 콤마 포함(CSV 불가): $basis" }
    return basis
}
