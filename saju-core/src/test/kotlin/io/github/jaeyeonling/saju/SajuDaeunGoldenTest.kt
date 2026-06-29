package io.github.jaeyeonling.saju

import com.tyme.eightchar.ChildLimit
import com.tyme.enums.Gender
import com.tyme.solar.SolarTime
import io.github.jaeyeonling.saju.astronomy.JulianDayConverter
import io.github.jaeyeonling.saju.derivation.DaeunDirection
import io.kotest.assertions.withClue
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.shouldBe
import kotlin.math.abs

private const val BEIJING_OFFSET = 8.0
private val SAMPLES = listOf(
    intArrayOf(1990, 3, 15, 7, 0),
    intArrayOf(2000, 1, 1, 12, 0),
    intArrayOf(2026, 6, 26, 14, 30),
    intArrayOf(1984, 8, 20, 10, 0),
    intArrayOf(1975, 11, 5, 16, 0),
    intArrayOf(2010, 4, 18, 3, 0),
    // 연말 출생 — 순행 대운이 양력 새해를 여러 번 가로지르는 경계
    intArrayOf(1995, 12, 28, 10, 0),
    intArrayOf(2003, 12, 20, 8, 0),
)

/** @return 시작 나이 차이(검증용). 방향·간지는 엄격 비교. */
private fun assertDaeunMatches(year: Int, month: Int, day: Int, hour: Int, minute: Int, isMale: Boolean): Int {
    val solarTime = SolarTime.fromYmdHms(year, month, day, hour, minute, 0)
    val gender = if (isMale) Gender.MAN else Gender.WOMAN
    val childLimit = ChildLimit.fromSolarTime(solarTime, gender)
    val tymeFirst = childLimit.startDecadeFortune

    val localJd = JulianDayConverter.fromGregorian(year, month, day, (hour * 60 + minute) / 1440.0)
    val utJd = localJd - BEIJING_OFFSET / 24.0
    val chart = Saju.fromLocalDateTime(year, month, day, hour, minute, BEIJING_OFFSET)
    val mine = Saju.daeun(utJd, chart.month.ganZhi, chart.year.gan.eumyang, isMale, count = 8)
    val tag = "$year-$month-$day ${if (isMale) "남" else "여"}"

    // ① 방향
    val expectedForward = childLimit.isForward
    val myDirection = DaeunDirection.of(chart.year.gan.eumyang, isMale)
    withClue("대운 방향 @ $tag") { (myDirection == DaeunDirection.FORWARD) shouldBe expectedForward }

    // ② 간지 시퀀스 (첫 3개)
    var tymeFortune = tymeFirst
    for (i in 0 until 3) {
        withClue("대운[$i] 간지 @ $tag") { mine[i].ganZhi.index shouldBe tymeFortune.sixtyCycle.index }
        tymeFortune = tymeFortune.next(1)
    }

    // ③ 대운수(시작 나이) — tyme.yearCount(3일=1세, 한국 전통과 동일 정의)와 ±1(round vs floor 차) 비교.
    // tyme.startDecadeFortune.startAge 는 '양력새해 교차' 정의라 최대 2세까지 벌어져 비교 대상으로 부적합.
    val diff = abs(childLimit.yearCount - mine[0].startAge)
    withClue("대운수(3일=1세) @ $tag: yearCount=${childLimit.yearCount}, mine=${mine[0].startAge}") { (diff <= 1).shouldBeTrue() }
    return diff
}

/**
 * 대운 골든 회귀 — [Saju.daeun] 을 tyme4j [ChildLimit] 와 대조한다(베이징 기준, 한국 보정 전).
 *
 * 비교: ① 순행/역행 방향, ② 대운 간지 시퀀스, ③ 대운 시작 나이.
 * 시작 나이는 tyme4j(세밀한 년·월·일 환산)와 round(일수/3) 방식 차이로 ±1 을 허용한다.
 */
class SajuDaeunGoldenTest : StringSpec({

    "대운 방향·간지·시작나이가 tyme4j 와 일치" {
        var startAgeMaxDiff = 0
        var checked = 0
        for (s in SAMPLES) {
            for (isMale in listOf(true, false)) {
                assertDaeunMatches(s[0], s[1], s[2], s[3], s[4], isMale).let { diff ->
                    startAgeMaxDiff = maxOf(startAgeMaxDiff, diff)
                    checked++
                }
            }
        }
        println("대운 골든 $checked 케이스 통과, 시작나이 최대 차이 ${startAgeMaxDiff}세")
    }
})
