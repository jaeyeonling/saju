package io.github.jaeyeonling.saju.korea

import io.github.jaeyeonling.saju.domain.SajuChart
import io.kotest.assertions.withClue
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

/**
 * 한국 보정 ON 4기둥 골든 회귀 — [KoreanSaju.fromCivilTime] 전 파이프라인
 * (서머타임 → 표준 자오선 → 진태양시 → 절기·자시 경계 → 4기둥)을 동결한다.
 *
 * 각 표본의 4기둥은 육십갑자·오호둔·오자둔·절기 규칙으로 **손계산 독립 검증**한 값이다(코드 출력 박제가 아님).
 * [io.github.jaeyeonling.saju.SajuGoldenTest] 는 보정 OFF(베이징) 4기둥을, [KoreanCorrectionTest] 는 보정 분(scalar)만
 * 고정한다. 이 테스트는 그 사이 공백 — "보정이 실제 4기둥을 어떻게 바꾸는가" — 를 메운다.
 */
class KoreanSajuGoldenTest : StringSpec({

    fun assertPillars(
        tag: String,
        year: Int,
        month: Int,
        day: Int,
        hour: Int,
        minute: Int,
        expectYear: String,
        expectMonth: String,
        expectDay: String,
        expectHour: String,
    ) {
        val chart: SajuChart =
            KoreanSaju.fromCivilTime(year, month, day, hour, minute, Birthplace.SEOUL.longitudeDeg)
        withClue("연주 @ $tag") { chart.year.ganji.koreanName shouldBe expectYear }
        withClue("월주 @ $tag") { chart.month.ganji.koreanName shouldBe expectMonth }
        withClue("일주 @ $tag") { chart.day.ganji.koreanName shouldBe expectDay }
        withClue("시주 @ $tag") { chart.hour.ganji.koreanName shouldBe expectHour }
    }

    // 보정(-41분)이 07:00 을 진시→묘시로 당겨 시주가 바뀐다 → 보정이 4기둥에 실제 영향.
    // 연 경오(1990 입춘 후) · 월 기묘(경년 오호둔 무인월 +묘) · 시 정묘(기일 오자둔 갑자시 +묘).
    "1990-03-15 07:00 서울 → 경오·기묘·기묘·정묘" {
        assertPillars("1990-03-15 07:00", 1990, 3, 15, 7, 0, "경오", "기묘", "기묘", "정묘")
    }

    // 서머타임(1988 DST) -60분 포함 -96.5분 보정 → 23:30 이 해시로 밀려 자시 day-flip 회피.
    // 연 무진 · 월 경신(무년 오호둔 갑인월 +신) · 시 신해(임일 오자둔 경자시 +해).
    "1988-08-15 23:30 서울(서머타임) → 무진·경신·임인·신해" {
        assertPillars("1988-08-15 23:30", 1988, 8, 15, 23, 30, "무진", "경신", "임인", "신해")
    }

    // 현대 평일 주간 — 표준 -28.6분 보정. 연 갑진 · 월 기사(갑년 오호둔 병인월 +사) · 시 신미(갑일 오자둔 갑자시 +미).
    "2024-05-20 14:00 서울 → 갑진·기사·갑신·신미" {
        assertPillars("2024-05-20 14:00", 2024, 5, 20, 14, 0, "갑진", "기사", "갑신", "신미")
    }

    // 1908 이전 LMT — 표준 자오선 null 이라 출생지 경도 자체가 기준이 되어 경도보정 0.
    // 연 을사 · 월 임오(을년 오호둔 무인월 +오) · 시 임오(을일 오자둔 병자시 +오).
    "1905-06-15 12:00 서울(LMT) → 을사·임오·을유·임오" {
        assertPillars("1905-06-15 12:00", 1905, 6, 15, 12, 0, "을사", "임오", "을유", "임오")
    }
})
