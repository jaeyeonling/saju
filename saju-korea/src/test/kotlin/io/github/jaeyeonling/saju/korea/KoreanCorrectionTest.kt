package io.github.jaeyeonling.saju.korea

import io.kotest.assertions.withClue
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.doubles.plusOrMinus
import io.kotest.matchers.shouldBe

class KoreanCorrectionTest : StringSpec({

    "서울 평시 진태양시는 약 -32분 + 균시차" {
        // 2026-06-26(비서머타임, 135° 기준). 경도보정 ≈ −32분, 6월말 균시차 ≈ −2.5분.
        val offset = KoreanSaju.trueSolarOffsetMinutes(2026, 6, 26, 12, 0, Birthplace.SEOUL.longitudeDeg)
        println("서울 2026-06-26 진태양시 보정: ${"%.2f".format(offset)}분")
        withClue("서울 평시 보정 ≈ −34분 이어야: $offset") { (offset in -38.0..-30.0).shouldBeTrue() }
    }

    "부산은 서울보다 진태양시 보정이 작다 (더 동쪽)" {
        // 부산(129.08°)은 서울(126.98°)보다 동쪽이라 135°에 가깝다 → 경도보정 절댓값이 작다.
        val seoul = KoreanSaju.trueSolarOffsetMinutes(2026, 6, 26, 12, 0, Birthplace.SEOUL.longitudeDeg)
        val busan = KoreanSaju.trueSolarOffsetMinutes(2026, 6, 26, 12, 0, Birthplace.BUSAN.longitudeDeg)
        withClue("부산($busan)이 서울($seoul)보다 보정이 작아야(덜 음수)") { (busan > seoul).shouldBeTrue() }
        // 경도 차 (129.08−126.98)=2.1° × 4분 ≈ 8.4분 차이
        (busan - seoul) shouldBe (8.4 plusOrMinus 0.5)
    }

    "1955년 서울 5월 — 서머타임 + 127_5도 시기 이중 보정" {
        // 1955-05-15: 서머타임(−60분) + 동경127.5°(경도보정 ≈ −2분) + 균시차(5월 ≈ +3.7분).
        val offset = KoreanSaju.trueSolarOffsetMinutes(1955, 5, 15, 12, 0, Birthplace.SEOUL.longitudeDeg)
        println("1955-05-15 서울(서머+127.5°): ${"%.2f".format(offset)}분")
        // −60 −2 +3.7 ≈ −58분 근처
        withClue("1955 이중보정 ≈ −58분 이어야: $offset") { (offset in -62.0..-54.0).shouldBeTrue() }
    }

    "표준시 연혁 — 127_5도 시기 판정" {
        // 1954-03-21 ~ 1961-08-09 = 127.5°
        KoreanStandardTime.at(1955, 6, 1, 12, 0).standardMeridianDeg shouldBe 127.5
        // 1961-08-10 이후 = 135°
        KoreanStandardTime.at(1962, 1, 1, 12, 0).standardMeridianDeg shouldBe 135.0
        // 일제강점기 1912~1954 = 135°
        KoreanStandardTime.at(1930, 1, 1, 12, 0).standardMeridianDeg shouldBe 135.0
        // 대한제국 1908~1911 = 127.5°
        KoreanStandardTime.at(1910, 1, 1, 12, 0).standardMeridianDeg shouldBe 127.5
    }

    "서머타임 구간 판정 — 경계 포함·바깥 제외" {
        // 1987 서머타임: 5/10 02:00 ~ 10/11
        withClue("1987 한여름은 서머타임") { KoreanStandardTime.at(1987, 7, 15, 12, 0).isSummerTime.shouldBeTrue() }
        withClue("1987 4월은 서머타임 아님") { (!KoreanStandardTime.at(1987, 4, 1, 12, 0).isSummerTime).shouldBeTrue() }
        withClue("1986은 서머타임 없음") { (!KoreanStandardTime.at(1986, 7, 15, 12, 0).isSummerTime).shouldBeTrue() }
        // 1960은 서머타임 있음
        withClue("1960 6월은 서머타임") { KoreanStandardTime.at(1960, 6, 1, 12, 0).isSummerTime.shouldBeTrue() }
    }

    "보정된 사주판이 정상 도출된다" {
        // 진태양시 보정을 거쳐도 4기둥이 모두 채워진다.
        val chart = KoreanSaju.fromCivilTime(1990, 3, 15, 7, 0, Birthplace.SEOUL.longitudeDeg)
        chart.pillars().size shouldBe 4
    }
})
