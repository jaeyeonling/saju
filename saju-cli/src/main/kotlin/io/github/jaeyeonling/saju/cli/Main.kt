package io.github.jaeyeonling.saju.cli

import io.github.jaeyeonling.saju.domain.Cheongan
import io.github.jaeyeonling.saju.domain.Jiji
import io.github.jaeyeonling.saju.domain.SajuChart
import io.github.jaeyeonling.saju.interpretation.InterpretationContext
import io.github.jaeyeonling.saju.interpretation.SipSeong
import io.github.jaeyeonling.saju.korea.Birthplace
import io.github.jaeyeonling.saju.korea.KoreanSaju

/** 사주 만세력 데모 CLI — 생년월일시 → 8글자 + 해석 + 대운 출력. */
public fun main() {
    val year = 1990
    val month = 3
    val day = 15
    val hour = 7
    val minute = 0
    val longitude = Birthplace.SEOUL.longitudeDeg

    val chart = KoreanSaju.fromCivilTime(year, month, day, hour, minute, longitude)
    val offset = KoreanSaju.trueSolarOffsetMinutes(year, month, day, hour, minute, longitude)

    println("════════ 사주 만세력 ════════")
    println("입력  : $year-${pad(month)}-${pad(day)} ${pad(hour)}:${pad(minute)} (서울)")
    println("진태양시 보정: ${"%.1f".format(offset)}분")
    println()
    printPillars(chart)
    println()
    printInterpretation(chart)
    println()
    printDaeun(year, month, day, hour, minute, longitude)
}

private fun printPillars(chart: SajuChart) {
    println("        시주   일주   월주   연주")
    val pillars = chart.pillars().reversed() // 시→연 (전통 표기)
    println("천간    " + pillars.joinToString("   ") { ganKorean(it.gan) + "(${stemLabel(chart, it)})" })
    println("지지    " + pillars.joinToString("     ") { jiKorean(it.ji) })
    println()
    println("일간(나) : ${ganKorean(chart.dayMaster)}  [${chart.dayMaster.ohaeng}/${chart.dayMaster.eumyang}]")
}

private fun stemLabel(chart: SajuChart, pillar: io.github.jaeyeonling.saju.domain.Pillar): String =
    if (pillar.position == io.github.jaeyeonling.saju.domain.PillarPosition.DAY) {
        "일간"
    } else {
        SipSeong.of(chart.dayMaster, pillar.gan).name
    }

private fun printInterpretation(chart: SajuChart) {
    val context = InterpretationContext.DEFAULT
    val strength = context.sinStrength.evaluate(chart)
    val yongsin = context.yongsin.derive(chart, strength)
    val gyeokguk = context.gyeokguk.classify(chart)
    println("───── 해석 ─────")
    println("신강신약 : ${strength.verdict} (지원율 ${"%.0f".format(strength.supportRatio * 100)}%)")
    println("용신     : ${yongsin.yongsin} (${yongsin.method})")
    println("격국     : ${gyeokguk.name}")
}

private fun printDaeun(year: Int, month: Int, day: Int, hour: Int, minute: Int, longitude: Double) {
    val daeun = KoreanSaju.daeun(year, month, day, hour, minute, isMale = true, longitudeDeg = longitude, count = 8)
    println("───── 대운 (남성) ─────")
    println(daeun.joinToString("  ") { "${it.startAge}세 ${ganKorean(it.ganZhi.gan)}${jiKorean(it.ganZhi.ji)}" })
}

private fun ganKorean(gan: Cheongan): String = GAN_KOREAN[gan.ordinal]
private fun jiKorean(ji: Jiji): String = JI_KOREAN[ji.ordinal]
private fun pad(value: Int): String = value.toString().padStart(2, '0')

private val GAN_KOREAN = listOf("갑", "을", "병", "정", "무", "기", "경", "신", "임", "계")
private val JI_KOREAN = listOf("자", "축", "인", "묘", "진", "사", "오", "미", "신", "유", "술", "해")
