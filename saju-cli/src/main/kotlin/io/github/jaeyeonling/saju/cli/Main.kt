package io.github.jaeyeonling.saju.cli

import io.github.jaeyeonling.saju.Saju
import io.github.jaeyeonling.saju.domain.Cheongan
import io.github.jaeyeonling.saju.domain.Eumyang
import io.github.jaeyeonling.saju.domain.Jiji
import io.github.jaeyeonling.saju.domain.Ohaeng
import io.github.jaeyeonling.saju.domain.Pillar
import io.github.jaeyeonling.saju.domain.PillarPosition
import io.github.jaeyeonling.saju.domain.SajuChart
import io.github.jaeyeonling.saju.interpretation.Interpretation
import io.github.jaeyeonling.saju.interpretation.SibiUnseong
import io.github.jaeyeonling.saju.interpretation.SinStrengthVerdict
import io.github.jaeyeonling.saju.interpretation.SipSeong
import io.github.jaeyeonling.saju.korea.Birthplace
import io.github.jaeyeonling.saju.korea.KoreanSaju
import io.github.jaeyeonling.saju.serialization.GanjiDto
import io.github.jaeyeonling.saju.serialization.InterpretationReportDto
import io.github.jaeyeonling.saju.serialization.SajuChartDto
import io.github.jaeyeonling.saju.serialization.sajuJson
import io.github.jaeyeonling.saju.serialization.toDto
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlin.system.exitProcess

/** CLI 입력 — 생년월일시 + 출생지 경도 + 성별. */
internal data class CliInput(
    val year: Int,
    val month: Int,
    val day: Int,
    val hour: Int,
    val minute: Int,
    val longitude: Double = Birthplace.SEOUL.longitudeDeg,
    val isMale: Boolean = true,
) {
    internal companion object {
        /** 인자 없이 실행할 때의 데모 입력. */
        val DEFAULT: CliInput = CliInput(1990, 3, 15, 7, 0)
    }
}

/** 인자 파싱: `year month day hour minute [longitude]` (5개 이상 가정 — [runCli] 가 사전 검증). */
internal fun parseArgs(args: Array<String>): CliInput =
    CliInput(
        year = args[0].toInt(),
        month = args[1].toInt(),
        day = args[2].toInt(),
        hour = args[3].toInt(),
        minute = args[4].toInt(),
        longitude = args.getOrNull(5)?.toDouble() ?: Birthplace.SEOUL.longitudeDeg,
    )

/** `--` 플래그(--json, --female, --seun=YYYY) 파싱 결과. */
internal data class CliFlags(
    val json: Boolean,
    val isMale: Boolean,
    val seunYear: Int?,
)

/** args 에서 `--` 플래그만 추출 — 위치 인자와 무관하게 어디 와도 인식. */
internal fun parseFlags(args: Array<String>): CliFlags {
    val flags = args.filter { it.startsWith("--") }
    return CliFlags(
        json = flags.contains("--json"),
        isMale = !flags.contains("--female"),
        seunYear = flags.firstOrNull { it.startsWith("--seun=") }?.substringAfter('=')?.toIntOrNull(),
    )
}

/**
 * 인자 → (출력 문자열, 종료 코드). 부작용 없는 순수 결정 — 인수 테스트 대상.
 * 인자 없음=데모, 5개 미만/파싱 실패/잘못된 입력=usage + 코드 2(조용한 폴백 금지).
 */
internal fun runCli(args: Array<String>): Pair<String, Int> {
    val flags = parseFlags(args)
    val positional = args.filterNot { it.startsWith("--") }.toTypedArray()
    return when {
        positional.isEmpty() -> renderFor(CliInput.DEFAULT.copy(isMale = flags.isMale), flags) to EXIT_OK
        positional.size < REQUIRED_ARGS ->
            usage("생년월일시 인자 ${REQUIRED_ARGS}개가 필요합니다 (받은 ${positional.size}개)") to EXIT_USAGE
        else ->
            try {
                renderFor(parseArgs(positional).copy(isMale = flags.isMale), flags) to EXIT_OK
            } catch (e: NumberFormatException) {
                usage("숫자로 바꿀 수 없는 인자: ${e.message}") to EXIT_USAGE
            } catch (e: IllegalArgumentException) {
                usage("잘못된 입력: ${e.message}") to EXIT_USAGE
            }
    }
}

/** 텍스트(기본) 또는 JSON 렌더 선택. */
private fun renderFor(
    input: CliInput,
    flags: CliFlags,
): String = if (flags.json) renderJson(input, flags.seunYear) else render(input)

/** 사주 만세력 데모 CLI — 생년월일시 → 8글자 + 해석 + 대운 출력. */
public fun main(args: Array<String>) {
    val (output, exitCode) = runCli(args)
    if (exitCode == EXIT_OK) print(output) else System.err.println(output)
    if (exitCode != EXIT_OK) exitProcess(exitCode)
}

private fun usage(reason: String): String =
    """
    [오류] $reason

    사용법: saju <연> <월> <일> <시> <분> [경도] [--json] [--female] [--seun=YYYY]
      예) ./gradlew :saju-cli:run --args="1990 3 15 7 0"          # 서울 기본 경도
          ./gradlew :saju-cli:run --args="1990 3 15 7 0 129.08"   # 부산 경도
          ./gradlew :saju-cli:run --args="1990 3 15 7 0 --json --seun=2026"  # 기계 판독용 JSON
    인자 없이 실행하면 데모(1990-03-15 07:00 서울)를 출력합니다.
    """.trimIndent()

private const val REQUIRED_ARGS = 5
private const val EXIT_OK = 0
private const val EXIT_USAGE = 2

/** 입력 → 전체 출력 문자열(부작용 없는 순수 렌더 — 인수 테스트 대상). */
internal fun render(input: CliInput): String =
    buildString {
        val chart =
            KoreanSaju.fromCivilTime(
                input.year,
                input.month,
                input.day,
                input.hour,
                input.minute,
                input.longitude,
            )
        val offset =
            KoreanSaju.trueSolarOffsetMinutes(
                input.year,
                input.month,
                input.day,
                input.hour,
                input.minute,
                input.longitude,
            )

        appendLine("════════ 사주 만세력 ════════")
        appendLine("입력  : ${input.year}-${pad(input.month)}-${pad(input.day)} ${pad(input.hour)}:${pad(input.minute)}")
        appendLine("진태양시 보정: ${"%.1f".format(offset)}분")
        appendLine()
        append(renderPillars(chart))
        appendLine()
        append(renderInterpretation(chart))
        appendLine()
        append(renderDaeun(input))
        appendLine()
        append(renderLunarExample())
    }

private fun renderLunarExample(): String =
    buildString {
        // 음력 입력 예시: 음력 2023-1-1(설날) → 양력 변환 후 사주.
        val lunarChart = KoreanSaju.fromLunarCivilTime(2023, 1, 1, isLeapMonth = false, hour = 9, minute = 0)
        appendLine("───── 음력 입력 예시 ─────")
        appendLine(
            "음력 2023-01-01 09:00 → ${ganKorean(
                lunarChart.year.gan,
            )}${jiKorean(lunarChart.year.ji)}년 일간 ${ganKorean(lunarChart.dayMaster)}",
        )
    }

private fun renderPillars(chart: SajuChart): String =
    buildString {
        appendLine("        시주   일주   월주   연주")
        val pillars = chart.pillars().reversed() // 시→연 (전통 표기)
        appendLine("천간    " + pillars.joinToString("   ") { ganKorean(it.gan) + "(${stemLabel(chart, it)})" })
        appendLine("지지    " + pillars.joinToString("     ") { jiKorean(it.ji) })
        appendLine()
        appendLine(
            "일간(나) : ${ganKorean(
                chart.dayMaster,
            )} [${ohaengKorean(chart.dayMaster.ohaeng)}/${eumyangKorean(chart.dayMaster.eumyang)}]",
        )
    }

private fun stemLabel(
    chart: SajuChart,
    pillar: Pillar,
): String =
    if (pillar.position == PillarPosition.DAY) {
        "일간"
    } else {
        sipSeongKorean(SipSeong.of(chart.dayMaster, pillar.gan))
    }

private fun renderInterpretation(chart: SajuChart): String =
    buildString {
        val report = Interpretation.of(chart)
        appendLine("───── 해석 ─────")
        appendLine(
            "신강신약 : ${verdictKorean(
                report.strength.verdict,
            )} (지원율 ${"%.0f".format(report.strength.supportRatio * 100)}%)",
        )
        appendLine("용신     : ${ohaengKorean(report.yongsin.yongsin)} (${report.yongsin.method.koreanName})")
        appendLine("격국     : ${report.gyeokguk.type.koreanName}")
        appendLine("공망     : ${jiKorean(report.gongmang.first)}${jiKorean(report.gongmang.second)}")
        val u = report.sibiUnseong
        val unseong =
            PillarPosition.entries.joinToString(" · ") {
                "${positionKorean(it)} ${unseongKorean(u.getValue(it))}"
            }
        appendLine("십이운성 : $unseong")
        if (report.hapChung.isNotEmpty()) {
            appendLine("합충     : ${report.hapChung.size}건")
        }
        appendLine()
        appendLine(
            "· 신강신약=일간이 강한가 약한가 · 용신=균형을 돕는 오행 · 격국=사주의 짜임새 · " +
                "공망=작용력이 빈 지지 · 십이운성=일간이 지지에서 갖는 기운 단계",
        )
    }

private fun renderDaeun(input: CliInput): String =
    buildString {
        val daeun =
            KoreanSaju.daeun(
                input.year,
                input.month,
                input.day,
                input.hour,
                input.minute,
                isMale = input.isMale,
                longitudeDeg = input.longitude,
                count = 8,
            )
        val label = if (input.isMale) "남성" else "여성"
        appendLine("───── 대운 ($label) ─────")
        appendLine(daeun.joinToString("  ") { "${it.startAge}세 ${ganKorean(it.ganji.gan)}${jiKorean(it.ganji.ji)}" })
        appendLine("(대운 = 시작 나이부터 10년 단위로 바뀌는 인생의 큰 흐름)")
    }

// 표시 라벨은 라이브러리 enum 의 koreanName/hanja 를 단일 진실 소스로 쓴다(CLI 가 매핑을 재정의하지 않는다).
// 한자 괄호 표기(目→목(木)) 여부만 CLI 의 표현 정책으로 남긴다.
private fun ganKorean(gan: Cheongan): String = gan.koreanName

private fun jiKorean(ji: Jiji): String = ji.koreanName

private fun ohaengKorean(ohaeng: Ohaeng): String = "${ohaeng.koreanName}(${ohaeng.hanja})"

private fun eumyangKorean(eumyang: Eumyang): String = "${eumyang.koreanName}(${eumyang.hanja})"

private fun sipSeongKorean(sipSeong: SipSeong): String = sipSeong.koreanName

private fun verdictKorean(verdict: SinStrengthVerdict): String = verdict.koreanName

private fun unseongKorean(unseong: SibiUnseong): String = unseong.koreanName

private fun positionKorean(position: PillarPosition): String =
    when (position) {
        PillarPosition.YEAR -> "연"
        PillarPosition.MONTH -> "월"
        PillarPosition.DAY -> "일"
        PillarPosition.HOUR -> "시"
    }

private fun pad(value: Int): String = value.toString().padStart(2, '0')

private const val DAEUN_COUNT = 8

/**
 * --json 모드: 사주판·해석·대운·(선택)세운을 기계 판독용 JSON 한 덩어리로 출력(부작용 없는 순수 렌더).
 * 사주판/해석은 saju-serialization DTO 를 재사용하고, 대운/세운만 CLI 가 얇게 매핑한다.
 */
internal fun renderJson(
    input: CliInput,
    seunYear: Int?,
): String {
    val chart =
        KoreanSaju.fromCivilTime(input.year, input.month, input.day, input.hour, input.minute, input.longitude)
    val offset =
        KoreanSaju.trueSolarOffsetMinutes(input.year, input.month, input.day, input.hour, input.minute, input.longitude)
    val daeun =
        KoreanSaju.daeun(
            input.year,
            input.month,
            input.day,
            input.hour,
            input.minute,
            isMale = input.isMale,
            longitudeDeg = input.longitude,
            count = DAEUN_COUNT,
        ).map { CliDaeunDto(it.startAge, it.ganji.toDto()) }
    val seun = seunYear?.let { Saju.seun(it) }?.let { CliSeunDto(it.year, it.ganji.toDto()) }
    val output =
        CliJsonOutput(
            input =
                CliJsonInput(
                    input.year,
                    input.month,
                    input.day,
                    input.hour,
                    input.minute,
                    input.longitude,
                    input.isMale,
                ),
            trueSolarOffsetMinutes = offset,
            chart = chart.toDto(),
            interpretation = Interpretation.of(chart).toDto(),
            daeun = daeun,
            seun = seun,
        )
    return sajuJson.encodeToString(output)
}

/** --json 출력 묶음. 사주판/해석은 라이브러리 DTO 재사용, 대운/세운은 CLI 전용 얇은 DTO. */
@Serializable
internal data class CliJsonOutput(
    val input: CliJsonInput,
    val trueSolarOffsetMinutes: Double,
    val chart: SajuChartDto,
    val interpretation: InterpretationReportDto,
    val daeun: List<CliDaeunDto>,
    val seun: CliSeunDto? = null,
)

@Serializable
internal data class CliJsonInput(
    val year: Int,
    val month: Int,
    val day: Int,
    val hour: Int,
    val minute: Int,
    val longitude: Double,
    val isMale: Boolean,
)

@Serializable
internal data class CliDaeunDto(
    val startAge: Int,
    val ganji: GanjiDto,
)

@Serializable
internal data class CliSeunDto(
    val year: Int,
    val ganji: GanjiDto,
)
