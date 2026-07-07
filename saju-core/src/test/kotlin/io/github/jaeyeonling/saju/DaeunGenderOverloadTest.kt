package io.github.jaeyeonling.saju

import io.github.jaeyeonling.saju.astronomy.JulianDayConverter
import io.github.jaeyeonling.saju.domain.Gender
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

private const val BEIJING_OFFSET = 8.0

/**
 * [Saju.daeun] 의 [Gender] 오버로드가 기존 isMale:Boolean 버전과 동일 결과를 냄을 보장한다.
 * (성별→대운 방향의 유일한 브릿지 [Gender.isMale] 가 올바로 위임되는지 검증 — 골든 커버리지가 Gender 경로까지 확장된다.)
 */
class DaeunGenderOverloadTest : StringSpec({

    val samples =
        listOf(
            intArrayOf(1990, 3, 15, 7, 0),
            intArrayOf(1984, 5, 20, 10, 0),
            intArrayOf(2000, 2, 4, 0, 0),
        )

    "daeun(Gender) 는 daeun(isMale) 과 동일 — MALE↔true, FEMALE↔false" {
        for (s in samples) {
            val chart = Saju.fromLocalDateTime(s[0], s[1], s[2], s[3], s[4], BEIJING_OFFSET)
            val localJd = JulianDayConverter.fromGregorian(s[0], s[1], s[2], (s[3] * 60 + s[4]) / 1440.0)
            val utJd = localJd - BEIJING_OFFSET / 24.0
            val eumyang = chart.year.gan.eumyang
            val month = chart.month.ganji

            Saju.daeun(utJd, month, eumyang, Gender.MALE) shouldBe Saju.daeun(utJd, month, eumyang, isMale = true)
            Saju.daeun(utJd, month, eumyang, Gender.FEMALE) shouldBe Saju.daeun(utJd, month, eumyang, isMale = false)
        }
    }
})
