package io.github.jaeyeonling.saju.korea

import io.github.jaeyeonling.saju.Saju
import io.github.jaeyeonling.saju.derivation.SajuConfig
import io.github.jaeyeonling.saju.domain.ZishiPolicy
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.assertions.withClue
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldNotBe

/** 공개 진입점 입력 가드 회귀 — 잘못된 입력이 조용히 오답을 내지 않고 fail-fast 하는지. */
class InputGuardTest : StringSpec({

    "Saju fromLocalDateTime 이 잘못된 시각을 거부한다" {
        shouldThrow<IllegalArgumentException> { Saju.fromLocalDateTime(2000, 13, 1, 7, 0, 9.0) } // 월
        shouldThrow<IllegalArgumentException> { Saju.fromLocalDateTime(2000, 1, 32, 7, 0, 9.0) } // 일
        shouldThrow<IllegalArgumentException> { Saju.fromLocalDateTime(2000, 1, 1, 25, 0, 9.0) } // 시
        shouldThrow<IllegalArgumentException> { Saju.fromLocalDateTime(2000, 1, 1, 7, 60, 9.0) } // 분
        shouldThrow<IllegalArgumentException> { Saju.fromLocalDateTime(1800, 1, 1, 7, 0, 9.0) } // 범위 밖
        shouldThrow<IllegalArgumentException> { Saju.fromLocalDateTime(2000, 1, 1, 7, 0, Double.NaN) } // NaN offset
    }

    "KoreanSaju 진입점들이 잘못된 입력을 거부한다" {
        shouldThrow<IllegalArgumentException> { KoreanSaju.fromCivilTime(2000, 13, 1, 7, 0) }
        shouldThrow<IllegalArgumentException> { KoreanSaju.daeun(2000, 1, 1, 25, 0, isMale = true) }
        shouldThrow<IllegalArgumentException> { KoreanSaju.trueSolarOffsetMinutes(2000, 1, 1, 7, 0, Double.POSITIVE_INFINITY) }
        // hour=25 가 hour=1 과 같은 시지로 충돌하던 조용한 오답을 차단
        shouldThrow<IllegalArgumentException> { KoreanSaju.fromCivilTime(2000, 1, 1, 7, 0, 999.0) } // 경도 밖
        // 존재하지 않는 양력일(2월 30일)이 진태양시 보정 전에 fail-fast — 양력/음력 검증 대칭.
        shouldThrow<IllegalArgumentException> { KoreanSaju.fromCivilTime(2023, 2, 30, 7, 0) }
    }

    "자시 학파에 따라 23시 일주가 갈린다 (ZishiPolicy 배선)" {
        // 정자시: 23시→다음날 일주, 야자시: 당일 일주. 학파가 실제로 도달 가능해야 한다.
        val jeongjasi = Saju.fromLocalDateTime(2000, 1, 1, 23, 30, 9.0, SajuConfig(zishi = ZishiPolicy.JEONGJASI))
        val yajasi = Saju.fromLocalDateTime(2000, 1, 1, 23, 30, 9.0, SajuConfig(zishi = ZishiPolicy.YAJASI))
        withClue("23시 일주가 자시 학파별로 달라야 한다") { jeongjasi.day.ganZhi shouldNotBe yajasi.day.ganZhi }
    }
})
