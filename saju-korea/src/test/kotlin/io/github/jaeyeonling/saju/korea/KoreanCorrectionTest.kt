package io.github.jaeyeonling.saju.korea

import io.kotest.assertions.withClue
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.doubles.plusOrMinus
import io.kotest.matchers.nulls.shouldBeNull
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

    "표준시 연혁 전환 경계가 외부 사실(법령)과 하루 단위로 정확히 일치한다" {
        // 출처: 한국 표준시 연혁(위키백과/나무위키). 전환 '하루'를 경계로 표준 자오선이 바뀐다.
        // 1908-04-01 칙령 제5호: LMT → 동경 127.5°
        KoreanStandardTime.at(1908, 3, 31, 12, 0).standardMeridianDeg.shouldBeNull()
        KoreanStandardTime.at(1908, 4, 1, 12, 0).standardMeridianDeg shouldBe 127.5
        // 1912-01-01 조선총독부: 127.5° → 135° (일본 표준시 통일)
        KoreanStandardTime.at(1911, 12, 31, 12, 0).standardMeridianDeg shouldBe 127.5
        KoreanStandardTime.at(1912, 1, 1, 12, 0).standardMeridianDeg shouldBe 135.0
        // 1954-03-21 대통령령 제876호(이승만): 135° → 127.5°
        KoreanStandardTime.at(1954, 3, 20, 12, 0).standardMeridianDeg shouldBe 135.0
        KoreanStandardTime.at(1954, 3, 21, 12, 0).standardMeridianDeg shouldBe 127.5
        // 1961-08-10 법률 제676호(박정희): 127.5° → 135° (현재까지)
        KoreanStandardTime.at(1961, 8, 9, 12, 0).standardMeridianDeg shouldBe 127.5
        KoreanStandardTime.at(1961, 8, 10, 12, 0).standardMeridianDeg shouldBe 135.0
    }

    "경도보정은 (출생지경도 − 표준자오선) × 4분과 정확히 일치한다 (LONGITUDE_ONLY 독립 대조)" {
        // 진태양시의 경도항은 순수 산술이라 외부 산술과 직접 대조 가능(135° 시기 = 2026).
        for (place in listOf(Birthplace.SEOUL, Birthplace.BUSAN, Birthplace.INCHEON, Birthplace.ULSAN)) {
            val expected = (place.longitudeDeg - KoreanStandardTime.MERIDIAN_135) * MINUTES_PER_DEGREE
            val actual =
                KoreanSaju.trueSolarOffsetMinutes(
                    2026,
                    6,
                    26,
                    12,
                    0,
                    place.longitudeDeg,
                    TrueSolarTimePolicy.LONGITUDE_ONLY,
                )
            withClue("${place.name} 경도보정: 기대 $expected vs 실제 $actual") {
                actual shouldBe (expected plusOrMinus 0.01)
            }
        }
    }

    "서머타임 구간 판정 — 경계 포함·바깥 제외" {
        // 1987 서머타임: 5/10 02:00 ~ 10/11
        withClue("1987 한여름은 서머타임") { KoreanStandardTime.at(1987, 7, 15, 12, 0).isSummerTime.shouldBeTrue() }
        withClue("1987 4월은 서머타임 아님") { (!KoreanStandardTime.at(1987, 4, 1, 12, 0).isSummerTime).shouldBeTrue() }
        withClue("1986은 서머타임 없음") { (!KoreanStandardTime.at(1986, 7, 15, 12, 0).isSummerTime).shouldBeTrue() }
        // 1960은 서머타임 있음
        withClue("1960 6월은 서머타임") { KoreanStandardTime.at(1960, 6, 1, 12, 0).isSummerTime.shouldBeTrue() }
    }

    "진태양시 정책 NONE→LONGITUDE_ONLY→FULL 이 단계적으로 보정을 누적한다" {
        val seoul = Birthplace.SEOUL.longitudeDeg
        val none = KoreanSaju.trueSolarOffsetMinutes(2026, 6, 26, 12, 0, seoul, TrueSolarTimePolicy.NONE)
        val lon = KoreanSaju.trueSolarOffsetMinutes(2026, 6, 26, 12, 0, seoul, TrueSolarTimePolicy.LONGITUDE_ONLY)
        val full = KoreanSaju.trueSolarOffsetMinutes(2026, 6, 26, 12, 0, seoul, TrueSolarTimePolicy.FULL)
        withClue("NONE 은 무보정(0)") { none shouldBe (0.0 plusOrMinus 0.01) }
        withClue("LONGITUDE_ONLY 는 경도보정만") {
            lon shouldBe ((seoul - KoreanStandardTime.MERIDIAN_135) * MINUTES_PER_DEGREE plusOrMinus 0.01)
        }
        // FULL = LONGITUDE_ONLY + 균시차(6월말 음수, |EoT| < 5분).
        withClue("FULL 은 경도보정에 균시차를 더한다") {
            (full < lon).shouldBeTrue()
            (kotlin.math.abs(full - lon) < MAX_EOT_MINUTES).shouldBeTrue()
        }
        // 보정을 거쳐도 4기둥이 모두 채워진다.
        KoreanSaju.fromCivilTime(1990, 3, 15, 7, 0, seoul).pillars().size shouldBe 4
    }

    "진태양시 보정이 자시 경계 출생의 시주를 바꾼다 (보정의 실질 효과)" {
        // 01:10(법정시)은 축시(01~03)지만, 서울 진태양시 보정(≈−41분)하면 00:2x = 자시(23~01).
        val seoul = Birthplace.SEOUL.longitudeDeg
        val corrected = KoreanSaju.fromCivilTime(1990, 6, 26, 1, 10, seoul) // 기본 FULL 보정
        val noCorrection =
            KoreanSaju.fromCivilTime(
                1990,
                6,
                26,
                1,
                10,
                seoul,
                KoreanSajuConfig(trueSolarTime = TrueSolarTimePolicy.NONE),
            )
        withClue("보정 유무로 시주(시지 경계)가 달라져야: 보정=${corrected.hour.ganji} 무보정=${noCorrection.hour.ganji}") {
            (corrected.hour.ganji != noCorrection.hour.ganji).shouldBeTrue()
        }
    }
})

private const val MINUTES_PER_DEGREE = 4.0
private const val MAX_EOT_MINUTES = 5.0
