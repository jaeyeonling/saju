package io.github.jaeyeonling.saju.derivation

import io.github.jaeyeonling.saju.Saju
import io.github.jaeyeonling.saju.domain.Jiji
import io.github.jaeyeonling.saju.domain.ZishiPolicy
import io.kotest.assertions.withClue
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe

/** 역법 도출 정책(SajuConfig)이 실제로 다른 결과를 내는지 — "갈아끼울 것이 있다"의 회귀 가드. */
class DerivationPolicyTest : StringSpec({

    "자시 학파 — 23시 출생의 일주와 시간이 정자시·야자시로 갈리고 시지는 같다" {
        val jeong = Saju.fromLocalDateTime(2000, 6, 1, 23, 30, 9.0, SajuConfig(zishi = ZishiPolicy.JEONGJASI))
        val ya = Saju.fromLocalDateTime(2000, 6, 1, 23, 30, 9.0, SajuConfig(zishi = ZishiPolicy.YAJASI))

        withClue("일주가 학파별로 갈려야") { jeong.day.ganji shouldNotBe ya.day.ganji }
        withClue("시간(時干)이 일간을 따라 갈려야") { jeong.hour.ganji.gan shouldNotBe ya.hour.ganji.gan }
        withClue("시지는 둘 다 자(子)로 동일해야") { jeong.hour.ganji.ji shouldBe ya.hour.ganji.ji }
    }

    "자시 핀값 — 23시 시지는 자(子), 정자시 일주는 야자시 일주의 다음 간지" {
        val jeong = Saju.fromLocalDateTime(2000, 6, 1, 23, 30, 9.0, SajuConfig(zishi = ZishiPolicy.JEONGJASI))
        val ya = Saju.fromLocalDateTime(2000, 6, 1, 23, 30, 9.0, SajuConfig(zishi = ZishiPolicy.YAJASI))

        withClue("23시는 자시(子)") { jeong.hour.ji shouldBe Jiji.JA }
        withClue("23시는 자시(子) — 학파 공통") { ya.hour.ji shouldBe Jiji.JA }
        withClue("정자시 일주 = 야자시 일주의 다음 간지(다음날)") { jeong.day.ganji shouldBe ya.day.ganji.next(1) }
    }

    "자정 직후(0시)는 자시이고 일주는 이미 당일 — 학파 무관" {
        val jeong = Saju.fromLocalDateTime(2000, 6, 2, 0, 30, 9.0, SajuConfig(zishi = ZishiPolicy.JEONGJASI))
        val ya = Saju.fromLocalDateTime(2000, 6, 2, 0, 30, 9.0, SajuConfig(zishi = ZishiPolicy.YAJASI))

        withClue("0시도 자시(子)") { jeong.hour.ji shouldBe Jiji.JA }
        withClue("0시는 어느 학파든 일주 동일(이미 날짜가 넘어감)") { jeong.day.ganji shouldBe ya.day.ganji }
    }

    "연주 경계 — 1월 중순은 입춘설에서 전년, 동지설에서 당년" {
        // 2000-01-15: 입춘(2월) 전이라 입춘설은 1999, 전년 동지(12월) 이후라 동지설은 2000.
        val ipchun = Saju.fromLocalDateTime(2000, 1, 15, 12, 0, 9.0, SajuConfig(yearBoundary = YearBoundary.IPCHUN))
        val dongji = Saju.fromLocalDateTime(2000, 1, 15, 12, 0, 9.0, SajuConfig(yearBoundary = YearBoundary.DONGJI))

        withClue("연주가 세수(歲首) 학파별로 갈려야") { ipchun.year.ganji shouldNotBe dongji.year.ganji }
    }

    "대운수 환산 — 반올림과 버림이 경계에서 1세 갈린다" {
        withClue("round(1.5)=2") { DaeunStartAgePolicy.THREE_DAYS_ONE_YEAR.startAge(4.5) shouldBe 2 }
        withClue("floor(1.5)=1") { DaeunStartAgePolicy.FLOOR.startAge(4.5) shouldBe 1 }
    }

    "SajuConfig DEFAULT 는 정자시·입춘·반올림 (통설 회귀 가드)" {
        SajuConfig.DEFAULT.zishi shouldBe ZishiPolicy.JEONGJASI
        SajuConfig.DEFAULT.yearBoundary shouldBe YearBoundary.IPCHUN
        withClue("기본은 반올림") { SajuConfig.DEFAULT.daeunStartAge.startAge(4.5) shouldBe 2 }
    }
})
