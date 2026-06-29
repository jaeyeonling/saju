package io.github.jaeyeonling.saju.astronomy

import io.kotest.assertions.withClue
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.doubles.plusOrMinus
import io.kotest.matchers.shouldBe

class DeltaTTest : StringSpec({

    "구간 경계점의 다항식 상수항" {
        // 각 구간 t=0 지점은 다항식 상수항과 정확히 일치해야 한다(계수 전사 검증).
        DeltaT.seconds(1900.0) shouldBe (-2.79 plusOrMinus 1e-9)
        DeltaT.seconds(1920.0) shouldBe (21.20 plusOrMinus 1e-9)
        DeltaT.seconds(1950.0) shouldBe (29.07 plusOrMinus 1e-9)
        DeltaT.seconds(2000.0) shouldBe (63.86 plusOrMinus 1e-9)
    }

    "현대 실측 ΔT 근사" {
        // 실측: 1987≈54.9s, 2000≈63.8s, 2020≈69.4s. 다항식은 예측이라 수 초 차이 허용.
        withClue("1987: ${DeltaT.seconds(1987.0)}") { (DeltaT.seconds(1987.0) in 53.0..57.0).shouldBeTrue() }
        withClue("2020: ${DeltaT.seconds(2020.0)}") { (DeltaT.seconds(2020.0) in 67.0..73.0).shouldBeTrue() }
    }

    "ΔT 는 연속이다 (구간 경계에서 점프 없음)" {
        // 인접 구간 경계에서 불연속이 크면 계수 오류. 경계 ±0.01년 차이가 작아야 한다.
        val boundaries = listOf(1600.0, 1700.0, 1800.0, 1860.0, 1900.0, 1920.0, 1941.0, 1961.0, 1986.0, 2005.0, 2050.0)
        for (b in boundaries) {
            val left = DeltaT.seconds(b - 0.01)
            val right = DeltaT.seconds(b + 0.01)
            withClue("ΔT 불연속 @ $b: $left vs $right") { (abs(left - right) < 1.0).shouldBeTrue() }
        }
    }
})

private fun abs(x: Double) = if (x < 0) -x else x
