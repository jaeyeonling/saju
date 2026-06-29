package io.github.jaeyeonling.saju.cli

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

/** 인자 파싱: `year month day hour minute [longitude]`. 5개 미만이면 데모 기본값. */
internal fun parseArgs(args: Array<String>): CliInput {
    if (args.size < 5) return CliInput.DEFAULT
    return CliInput(
        year = args[0].toInt(),
        month = args[1].toInt(),
        day = args[2].toInt(),
        hour = args[3].toInt(),
        minute = args[4].toInt(),
        longitude = args.getOrNull(5)?.toDouble() ?: Birthplace.SEOUL.longitudeDeg,
    )
}

/** 사주 만세력 데모 CLI — 생년월일시 → 8글자 + 해석 + 대운 출력. */
public fun main(args: Array<String>) {
    print(render(parseArgs(args)))
}

/** 입력 → 전체 출력 문자열(부작용 없는 순수 렌더 — 인수 테스트 대상). */
internal fun render(input: CliInput): String = buildString {
    val chart = KoreanSaju.fromCivilTime(input.year, input.month, input.day, input.hour, input.minute, input.longitude)
    val offset = KoreanSaju.trueSolarOffsetMinutes(input.year, input.month, input.day, input.hour, input.minute, input.longitude)

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

private fun renderLunarExample(): String = buildString {
    // 음력 입력 예시: 음력 2023-1-1(설날) → 양력 변환 후 사주.
    val lunarChart = KoreanSaju.fromLunarCivilTime(2023, 1, 1, isLeapMonth = false, hour = 9, minute = 0)
    appendLine("───── 음력 입력 예시 ─────")
    appendLine("음력 2023-01-01 09:00 → ${ganKorean(lunarChart.year.gan)}${jiKorean(lunarChart.year.ji)}년 일간 ${ganKorean(lunarChart.dayMaster)}")
}

private fun renderPillars(chart: SajuChart): String = buildString {
    appendLine("        시주   일주   월주   연주")
    val pillars = chart.pillars().reversed() // 시→연 (전통 표기)
    appendLine("천간    " + pillars.joinToString("   ") { ganKorean(it.gan) + "(${stemLabel(chart, it)})" })
    appendLine("지지    " + pillars.joinToString("     ") { jiKorean(it.ji) })
    appendLine()
    appendLine("일간(나) : ${ganKorean(chart.dayMaster)} [${ohaengKorean(chart.dayMaster.ohaeng)}/${eumyangKorean(chart.dayMaster.eumyang)}]")
}

private fun stemLabel(chart: SajuChart, pillar: Pillar): String =
    if (pillar.position == PillarPosition.DAY) {
        "일간"
    } else {
        sipSeongKorean(SipSeong.of(chart.dayMaster, pillar.gan))
    }

private fun renderInterpretation(chart: SajuChart): String = buildString {
    val report = Interpretation.of(chart)
    appendLine("───── 해석 ─────")
    appendLine("신강신약 : ${verdictKorean(report.strength.verdict)} (지원율 ${"%.0f".format(report.strength.supportRatio * 100)}%)")
    appendLine("용신     : ${ohaengKorean(report.yongsin.yongsin)} (${report.yongsin.method.koreanName})")
    appendLine("격국     : ${report.gyeokguk.type.koreanName}")
    appendLine("공망     : ${jiKorean(report.gongmang.first)}${jiKorean(report.gongmang.second)}")
    val u = report.sibiUnseong
    appendLine(
        "십이운성 : 연 ${unseongKorean(u.getValue(PillarPosition.YEAR))} · 월 ${unseongKorean(u.getValue(PillarPosition.MONTH))} · " +
            "일 ${unseongKorean(u.getValue(PillarPosition.DAY))} · 시 ${unseongKorean(u.getValue(PillarPosition.HOUR))}",
    )
    if (report.hapChung.isNotEmpty()) {
        appendLine("합충     : ${report.hapChung.size}건")
    }
    appendLine()
    appendLine(
        "· 신강신약=일간이 강한가 약한가 · 용신=균형을 돕는 오행 · 격국=사주의 짜임새 · " +
            "공망=작용력이 빈 지지 · 십이운성=일간이 지지에서 갖는 기운 단계",
    )
}

private fun renderDaeun(input: CliInput): String = buildString {
    val daeun = KoreanSaju.daeun(
        input.year, input.month, input.day, input.hour, input.minute,
        isMale = input.isMale, longitudeDeg = input.longitude, count = 8,
    )
    val label = if (input.isMale) "남성" else "여성"
    appendLine("───── 대운 ($label) ─────")
    appendLine(daeun.joinToString("  ") { "${it.startAge}세 ${ganKorean(it.ganZhi.gan)}${jiKorean(it.ganZhi.ji)}" })
    appendLine("(대운 = 시작 나이부터 10년 단위로 바뀌는 인생의 큰 흐름)")
}

private fun ganKorean(gan: Cheongan): String = GAN_KOREAN[gan.ordinal]
private fun jiKorean(ji: Jiji): String = JI_KOREAN[ji.ordinal]
private fun ohaengKorean(ohaeng: Ohaeng): String = OHAENG_KOREAN[ohaeng.ordinal]
private fun eumyangKorean(eumyang: Eumyang): String = if (eumyang.isYang) "양(陽)" else "음(陰)"
private fun sipSeongKorean(sipSeong: SipSeong): String = SIPSEONG_KOREAN[sipSeong.ordinal]
private fun verdictKorean(verdict: SinStrengthVerdict): String = VERDICT_KOREAN[verdict.ordinal]
private fun unseongKorean(unseong: SibiUnseong): String = SIBIUNSEONG_KOREAN[unseong.ordinal]
private fun pad(value: Int): String = value.toString().padStart(2, '0')

private val GAN_KOREAN = listOf("갑", "을", "병", "정", "무", "기", "경", "신", "임", "계")
private val JI_KOREAN = listOf("자", "축", "인", "묘", "진", "사", "오", "미", "신", "유", "술", "해")
private val OHAENG_KOREAN = listOf("목(木)", "화(火)", "토(土)", "금(金)", "수(水)")
private val SIPSEONG_KOREAN = listOf("비견", "겁재", "식신", "상관", "편재", "정재", "편관", "정관", "편인", "정인")
private val VERDICT_KOREAN = listOf("극신강", "신강", "중화", "신약", "극신약")
private val SIBIUNSEONG_KOREAN =
    listOf("장생", "목욕", "관대", "건록", "제왕", "쇠", "병", "사", "묘", "절", "태", "양")
