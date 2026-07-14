package io.github.jaeyeonling.saju.korea

import io.kotest.core.spec.style.StringSpec
import java.nio.file.Files
import java.nio.file.Path
import java.util.Locale

/**
 * 보정 trace 골든 CSV 생성기 —
 * `./gradlew :saju-korea:test -Dgolden.write=true --tests '*CorrectionTraceGoldenWriter*'`
 *
 * 표준시 연혁·서머타임·정책의 대표 조합에 대해 [KoreanSaju.fromCivilTimeWithTrace] 의
 * 단계별 보정([CorrectionTrace])을 동결한다. 보정 **총량**은 [KoreanCorrectionTest] 가
 * 외부 사실로 검증하고, 이 파일은 단계 분해와 basis 문자열을 언어 간 포팅 대조용으로 박제한다.
 */
class CorrectionTraceGoldenWriter : StringSpec({

    "보정 trace 골든 CSV 재생성".config(enabled = System.getProperty("golden.write") != null) {
        val lines = mutableListOf(HEADER)
        for (case in CASES) {
            val (y, mo, d, h, mi) = case.time
            val config = KoreanSajuConfig(trueSolarTime = case.policy)
            val trace = KoreanSaju.fromCivilTimeWithTrace(y, mo, d, h, mi, case.longitudeDeg, config).correction
            val deltas = trace.steps.associate { it.kind.name to it.deltaMinutes }
            val bases = trace.steps.associate { it.kind.name to it.basis }
            lines +=
                listOf(
                    y, mo, d, h, mi, fmt(case.longitudeDeg), case.policy.name,
                    fmt(deltas.getValue("DST")), fmt(deltas.getValue("LONGITUDE")), fmt(deltas.getValue("EOT")),
                    fmt(trace.totalOffsetMinutes),
                    trace.corrected.year, trace.corrected.month, trace.corrected.day,
                    trace.corrected.hour, trace.corrected.minute, trace.corrected.second,
                    fmt(trace.utJd), fmt(trace.utOffsetHours),
                    csvSafe(bases.getValue("DST")), csvSafe(bases.getValue("MERIDIAN")),
                    csvSafe(bases.getValue("LONGITUDE")), csvSafe(bases.getValue("EOT")),
                ).joinToString(",")
        }
        Files.write(Path.of("src/test/resources/golden/trace_correction.csv"), lines)
        println("보정 trace 골든 재생성: ${lines.size - 1}건")
    }
})

internal data class CorrectionCase(
    val time: List<Int>,
    val longitudeDeg: Double,
    val policy: TrueSolarTimePolicy,
)

/**
 * 표준시 연혁 × 서머타임 × 정책의 대표 조합 —
 * 평시 135°, 서머타임+127.5°(1955), 88 올림픽 서머타임, LMT(1905), 127.5° 마지막 날 자시,
 * 부산(경도차), LONGITUDE_ONLY/NONE 정책.
 */
internal val CASES: List<CorrectionCase> =
    listOf(
        CorrectionCase(listOf(1990, 3, 15, 7, 0), Birthplace.SEOUL.longitudeDeg, TrueSolarTimePolicy.FULL),
        CorrectionCase(listOf(1955, 5, 15, 12, 0), Birthplace.SEOUL.longitudeDeg, TrueSolarTimePolicy.FULL),
        CorrectionCase(listOf(1988, 7, 15, 12, 0), Birthplace.SEOUL.longitudeDeg, TrueSolarTimePolicy.FULL),
        CorrectionCase(listOf(1905, 1, 1, 12, 0), Birthplace.SEOUL.longitudeDeg, TrueSolarTimePolicy.FULL),
        CorrectionCase(listOf(1961, 8, 9, 23, 30), Birthplace.SEOUL.longitudeDeg, TrueSolarTimePolicy.FULL),
        CorrectionCase(listOf(2000, 6, 1, 6, 30), Birthplace.BUSAN.longitudeDeg, TrueSolarTimePolicy.FULL),
        CorrectionCase(listOf(2026, 6, 26, 12, 0), Birthplace.SEOUL.longitudeDeg, TrueSolarTimePolicy.LONGITUDE_ONLY),
        CorrectionCase(listOf(2026, 6, 26, 12, 0), Birthplace.SEOUL.longitudeDeg, TrueSolarTimePolicy.NONE),
    )

internal const val HEADER: String =
    "year,month,day,hour,minute,longitudeDeg,policy," +
        "dstDelta,longitudeDelta,eotDelta,totalOffsetMinutes," +
        "correctedYear,correctedMonth,correctedDay,correctedHour,correctedMinute,correctedSecond," +
        "utJd,utOffsetHours,dstBasis,meridianBasis,longitudeBasis,eotBasis"

/** JD·분은 1e-8 정밀도로 동결 — 언어 간 대조 허용 오차(1e-6)보다 두 자리 여유. */
internal fun fmt(v: Double): String = String.format(Locale.ROOT, "%.8f", v)

/** basis 는 CSV 마지막 컬럼들로 박제 — 콤마가 섞이면 파서가 깨지므로 생성 시점에 fail-fast. */
internal fun csvSafe(basis: String): String {
    require(',' !in basis) { "basis 에 콤마 포함(CSV 불가): $basis" }
    return basis
}
