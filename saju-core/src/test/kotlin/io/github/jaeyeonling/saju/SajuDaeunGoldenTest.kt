package io.github.jaeyeonling.saju

import io.github.jaeyeonling.saju.astronomy.JulianDayConverter
import io.github.jaeyeonling.saju.derivation.DaeunDirection
import io.kotest.assertions.withClue
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.shouldBe
import kotlin.math.abs

private const val BEIJING_OFFSET = 8.0

/**
 * @return 시작 나이 차이(검증용). 방향·간지는 엄격 비교.
 *
 * 골든 행: year,month,day,hour,minute,isMale,isForward,yearCount,gz0,gz1,gz2
 * (대운 간지 3개는 역행 케이스에서 60갑자를 역방향으로 걸으므로 리터럴로 동결된 값과 대조한다.)
 */
private fun assertDaeunMatches(row: List<String>): Int {
    val year = row[0].toInt()
    val month = row[1].toInt()
    val day = row[2].toInt()
    val hour = row[3].toInt()
    val minute = row[4].toInt()
    val isMale = row[5].toBoolean()
    val expectedForward = row[6].toBoolean()
    val expectedYearCount = row[7].toInt()
    val expectedGz = intArrayOf(row[8].toInt(), row[9].toInt(), row[10].toInt())

    val localJd = JulianDayConverter.fromGregorian(year, month, day, (hour * 60 + minute) / 1440.0)
    val utJd = localJd - BEIJING_OFFSET / 24.0
    val chart = Saju.fromLocalDateTime(year, month, day, hour, minute, BEIJING_OFFSET)
    val mine = Saju.daeun(utJd, chart.month.ganZhi, chart.year.gan.eumyang, isMale, count = 8)
    val tag = "$year-$month-$day ${if (isMale) "남" else "여"}"

    // ① 방향
    val myDirection = DaeunDirection.of(chart.year.gan.eumyang, isMale)
    withClue("대운 방향 @ $tag") { (myDirection == DaeunDirection.FORWARD) shouldBe expectedForward }

    // ② 간지 시퀀스 (첫 3개) — 동결된 정답 인덱스와 엄격 비교
    for (i in 0 until 3) {
        withClue("대운[$i] 간지 @ $tag") { mine[i].ganZhi.index shouldBe expectedGz[i] }
    }

    // ③ 대운수(시작 나이) — 골든(3일=1세, 한국 전통과 동일 정의)과 ±1(round vs floor 차) 비교.
    val diff = abs(expectedYearCount - mine[0].startAge)
    withClue("대운수(3일=1세) @ $tag: golden=$expectedYearCount, mine=${mine[0].startAge}") { (diff <= 1).shouldBeTrue() }
    return diff
}

/**
 * 대운 골든 회귀 — [Saju.daeun] 을 동결된 골든 벡터와 대조한다(베이징 기준, 한국 보정 전).
 *
 * 비교: ① 순행/역행 방향, ② 대운 간지 시퀀스(역행 포함), ③ 대운 시작 나이.
 * 시작 나이는 골든(세밀한 년·월·일 환산)과 round(일수/3) 방식 차이로 ±1 을 허용한다.
 */
class SajuDaeunGoldenTest : StringSpec({

    "대운 방향·간지·시작나이가 골든 벡터와 일치" {
        var startAgeMaxDiff = 0
        var checked = 0
        for (row in Golden.rows("daeun.csv")) {
            startAgeMaxDiff = maxOf(startAgeMaxDiff, assertDaeunMatches(row))
            checked++
        }
        println("대운 골든 $checked 케이스 통과, 시작나이 최대 차이 ${startAgeMaxDiff}세")
    }
})
