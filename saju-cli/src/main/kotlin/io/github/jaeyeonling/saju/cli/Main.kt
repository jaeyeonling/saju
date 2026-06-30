package io.github.jaeyeonling.saju.cli

import io.github.jaeyeonling.saju.Saju
import io.github.jaeyeonling.saju.domain.Cheongan
import io.github.jaeyeonling.saju.domain.Eumyang
import io.github.jaeyeonling.saju.domain.Jiji
import io.github.jaeyeonling.saju.domain.Ohaeng
import io.github.jaeyeonling.saju.domain.Pillar
import io.github.jaeyeonling.saju.domain.PillarPosition
import io.github.jaeyeonling.saju.domain.SajuChart
import io.github.jaeyeonling.saju.interpretation.HapChungRelation
import io.github.jaeyeonling.saju.interpretation.Interpretation
import io.github.jaeyeonling.saju.interpretation.InterpretationReport
import io.github.jaeyeonling.saju.interpretation.OhaengDistribution
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
 *
 * [currentYear] 는 세운 기본값(`--seun` 미지정 시 노출할 해)이다. 시계 읽기는 [main] 으로 격리해
 * 렌더를 결정적으로 유지한다(같은 인자 → 같은 출력).
 */
internal fun runCli(
    args: Array<String>,
    currentYear: Int,
): Pair<String, Int> {
    val flags = parseFlags(args)
    val positional = args.filterNot { it.startsWith("--") }.toTypedArray()
    return when {
        positional.isEmpty() -> renderFor(CliInput.DEFAULT.copy(isMale = flags.isMale), flags, currentYear) to EXIT_OK
        positional.size < REQUIRED_ARGS ->
            usage("생년월일시 인자 ${REQUIRED_ARGS}개가 필요합니다 (받은 ${positional.size}개)") to EXIT_USAGE
        else ->
            try {
                renderFor(parseArgs(positional).copy(isMale = flags.isMale), flags, currentYear) to EXIT_OK
            } catch (e: NumberFormatException) {
                usage("숫자로 바꿀 수 없는 인자: ${e.message}") to EXIT_USAGE
            } catch (e: IllegalArgumentException) {
                usage("잘못된 입력: ${e.message}") to EXIT_USAGE
            }
    }
}

/** 텍스트(기본) 또는 JSON 렌더 선택. 세운은 `--seun` 지정값, 없으면 [currentYear] 로 항상 노출. */
private fun renderFor(
    input: CliInput,
    flags: CliFlags,
    currentYear: Int,
): String {
    val seunYear = flags.seunYear ?: currentYear
    return if (flags.json) renderJson(input, seunYear) else render(input, seunYear)
}

/** 사주 만세력 데모 CLI — 생년월일시 → 8글자 + 해석 + 세운 + 대운 출력. */
public fun main(args: Array<String>) {
    val (output, exitCode) = runCli(args, java.time.Year.now().value)
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

/**
 * 입력 → 전체 출력 문자열(부작용 없는 순수 렌더 — 인수 테스트 대상).
 * [seunYear] 의 세운을 항상 노출한다. 해석은 [Interpretation.of] 를 한 번 계산해 섹션들이 공유한다.
 */
internal fun render(
    input: CliInput,
    seunYear: Int,
): String =
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
        val report = Interpretation.of(chart)

        appendLine("════════ 사주 만세력 ════════")
        appendLine("입력  : ${input.year}-${pad(input.month)}-${pad(input.day)} ${pad(input.hour)}:${pad(input.minute)}")
        appendLine("진태양시 보정: ${"%.1f".format(offset)}분")
        appendLine()
        append(renderPillars(chart, report))
        appendLine()
        append(renderHiddenSipSeong(report))
        appendLine()
        append(renderInterpretation(report))
        appendLine()
        append(renderSeun(seunYear, chart.dayMaster))
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

private fun renderPillars(
    chart: SajuChart,
    report: InterpretationReport,
): String =
    buildString {
        val pillars = chart.pillars().reversed() // 시→연 (전통 표기)
        appendLine(padLabel("") + listOf("시주", "일주", "월주", "연주").joinToString("") { padCell(it) })
        appendLine(
            padLabel("천간") + pillars.joinToString("") { padCell("${ganKorean(it.gan)}(${stemLabel(report, it)})") },
        )
        appendLine(
            padLabel("지지") + pillars.joinToString("") { padCell("${jiKorean(it.ji)}(${branchSipSeong(report, it)})") },
        )
        appendLine(padLabel("지장간") + pillars.joinToString("") { padCell(hiddenLabel(report, it)) })
        appendLine()
        appendLine(
            "일간(나) : ${ganKorean(
                chart.dayMaster,
            )} [${ohaengKorean(chart.dayMaster.ohaeng)}/${eumyangKorean(chart.dayMaster.eumyang)}]",
        )
    }

/** 천간 십성 라벨 — 일주 천간은 '나'라서 "일간". */
private fun stemLabel(
    report: InterpretationReport,
    pillar: Pillar,
): String = report.sipSeong.getValue(pillar.position).stem?.let { sipSeongKorean(it) } ?: "일간"

/** 지지 본기의 십성. */
private fun branchSipSeong(
    report: InterpretationReport,
    pillar: Pillar,
): String = sipSeongKorean(report.sipSeong.getValue(pillar.position).branchMain)

/** 지장간 글자들(본·중·여). */
private fun hiddenLabel(
    report: InterpretationReport,
    pillar: Pillar,
): String = report.hiddenStems.getValue(pillar.position).all().joinToString("") { ganKorean(it) }

private fun renderInterpretation(report: InterpretationReport): String =
    buildString {
        appendLine("───── 해석 ─────")
        appendLine(
            "신강신약 : ${verdictKorean(
                report.strength.verdict,
            )} (지원율 ${"%.0f".format(report.strength.supportRatio * 100)}%)",
        )
        appendLine("         └ ${report.strength.basis}")
        appendLine("용신     : ${ohaengKorean(report.yongsin.yongsin)} (${report.yongsin.method.koreanName})")
        appendLine("         └ ${report.yongsin.basis}")
        appendLine("격국     : ${report.gyeokguk.type.koreanName}")
        appendLine("         └ ${report.gyeokguk.basis}")
        appendLine("공망     : ${jiKorean(report.gongmang.first)}${jiKorean(report.gongmang.second)}")
        val unseong =
            PillarPosition.entries.joinToString(" · ") {
                "${positionKorean(it)} ${unseongKorean(report.sibiUnseong.getValue(it))}"
            }
        appendLine("십이운성 : $unseong")
        appendLine("신살     : ${sinSalLine(report)}")
        appendLine("오행(표면)   : ${ohaengLine(report.ohaeng)}")
        appendLine("오행(지장간) : ${ohaengLine(report.ohaengWeighted)}")
        if (report.hapChung.isNotEmpty()) {
            appendLine("합충     : ${hapChungLine(report.hapChung)}")
        }
        appendLine()
        appendLine(
            "· 신강신약=일간이 강한가 약한가 · 용신=균형을 돕는 오행 · 격국=사주의 짜임새 · " +
                "공망=작용력이 빈 지지 · 십이운성=일간이 지지에서 갖는 기운 단계 · 신살=지지에 깃든 길흉 표식",
        )
    }

/** 합충 상세 — "천간합(신-병→수) · 육합(사-신)". serialization 의 toDto 평탄화를 재사용. */
private fun hapChungLine(relations: List<HapChungRelation>): String =
    relations.joinToString(" · ") { rel ->
        val dto = rel.toDto()
        "${dto.kind}(${dto.members.joinToString("-")}${dto.transformsTo?.let { "→$it" }.orEmpty()})"
    }

/** 지장간 십성 블록 — 기둥별 본·중·여 글자와 십성(숨은 육친). 시→연 순. */
private fun renderHiddenSipSeong(report: InterpretationReport): String =
    buildString {
        appendLine("───── 지장간 십성 (숨은 육친) ─────")
        for (pos in listOf(PillarPosition.HOUR, PillarPosition.DAY, PillarPosition.MONTH, PillarPosition.YEAR)) {
            val hidden = report.hiddenStems.getValue(pos)
            val ps = report.sipSeong.getValue(pos)
            val midGan = hidden.midQi
            val midSip = ps.branchMid
            val resGan = hidden.residualQi
            val resSip = ps.branchResidual
            val parts =
                buildList {
                    add("${ganKorean(hidden.mainQi)}(${sipSeongKorean(ps.branchMain)})")
                    if (midGan != null && midSip != null) add("${ganKorean(midGan)}(${sipSeongKorean(midSip)})")
                    if (resGan != null && resSip != null) add("${ganKorean(resGan)}(${sipSeongKorean(resSip)})")
                }
            appendLine("${positionKorean(pos)}주 : ${parts.joinToString(" · ")}")
        }
    }

/** 네 기둥 신살 — 위치별로, 없으면 "-". */
private fun sinSalLine(report: InterpretationReport): String =
    PillarPosition.entries.joinToString(" · ") { pos ->
        val names = report.sinSal[pos].orEmpty().joinToString(",") { it.koreanName }
        "${positionKorean(pos)} ${names.ifEmpty { "-" }}"
    }

/** 오행 분포 한 줄 — "목1 화3 토2 금2 수0". */
private fun ohaengLine(distribution: OhaengDistribution): String =
    Ohaeng.entries.joinToString(" ") { "${it.koreanName}${distribution.count(it)}" }

/** 세운 — 해당 연도의 간지 + 연간 십성. `--seun` 없으면 올해가 들어온다(항상 노출). */
private fun renderSeun(
    year: Int,
    dayMaster: Cheongan,
): String =
    buildString {
        val seun = Saju.seun(year)
        val sipSeong = SipSeong.of(dayMaster, seun.ganji.gan)
        appendLine("───── 세운 ($year) ─────")
        appendLine(
            "${year}년 ${seun.ganji.koreanName}(${seun.ganji.hanja}) · 연간 십성 ${sipSeongKorean(sipSeong)}",
        )
        appendLine("(세운 = 그 해의 간지 — 해마다 바뀌는 1년 단위 운)")
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

// 사주판 표 정렬용 — 한글/한자는 2칸, ASCII 는 1칸으로 디스플레이 폭을 계산해 왼쪽 정렬한다.
private const val LABEL_WIDTH = 9
private const val CELL_WIDTH = 14

private fun padLabel(text: String): String = padDisplay(text, LABEL_WIDTH)

private fun padCell(text: String): String = padDisplay(text, CELL_WIDTH)

private fun padDisplay(
    text: String,
    width: Int,
): String {
    val displayWidth = text.fold(0) { acc, ch -> acc + if (ch.code > 0x7F) 2 else 1 }
    return text + " ".repeat((width - displayWidth).coerceAtLeast(1))
}

private const val DAEUN_COUNT = 8

/**
 * --json 모드: 사주판·해석·대운·세운을 기계 판독용 JSON 한 덩어리로 출력(부작용 없는 순수 렌더).
 * 사주판/해석은 saju-serialization DTO 를 재사용하고, 대운/세운만 CLI 가 얇게 매핑한다.
 * [seunYear] 의 세운을 항상 채운다(`--seun` 미지정 시 올해가 들어온다).
 */
internal fun renderJson(
    input: CliInput,
    seunYear: Int,
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
    val seun =
        Saju.seun(seunYear).let {
            CliSeunDto(it.year, it.ganji.toDto(), SipSeong.of(chart.dayMaster, it.ganji.gan).koreanName)
        }
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
    val seun: CliSeunDto,
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
    val stemSipSeong: String,
)
