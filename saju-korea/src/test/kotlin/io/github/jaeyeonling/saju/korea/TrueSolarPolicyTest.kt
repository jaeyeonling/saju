package io.github.jaeyeonling.saju.korea

import io.kotest.assertions.withClue
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import kotlin.math.abs

/** 진태양시 보정 정책이 실제로 다른 보정을 내는지 — korea 전용 정책 회귀 가드. */
class TrueSolarPolicyTest : StringSpec({

    "세 정책이 서로 다른 보정을 낸다" {
        // 균시차가 큰 2월(약 -14분) + 서울(경도 127, 표준 135 → 경도보정 약 -32분).
        val lon = Birthplace.SEOUL.longitudeDeg
        val none = KoreanSaju.trueSolarOffsetMinutes(2000, 2, 12, 12, 0, lon, TrueSolarTimePolicy.NONE)
        val lonOnly = KoreanSaju.trueSolarOffsetMinutes(2000, 2, 12, 12, 0, lon, TrueSolarTimePolicy.LONGITUDE_ONLY)
        val full = KoreanSaju.trueSolarOffsetMinutes(2000, 2, 12, 12, 0, lon, TrueSolarTimePolicy.FULL)

        withClue("무보정(none=$none)이 경도보정(lonOnly=$lonOnly)보다 작아야") {
            (abs(none) < abs(lonOnly)).shouldBeTrue()
        }
        withClue("균시차 유무로 경도보정과 풀보정이 갈려야 (lonOnly=$lonOnly, full=$full)") {
            full shouldNotBe lonOnly
        }
    }

    "무보정은 표준시 그대로라 보정이 0에 가깝다" {
        val lon = Birthplace.SEOUL.longitudeDeg
        val none = KoreanSaju.trueSolarOffsetMinutes(2000, 6, 1, 12, 0, lon, TrueSolarTimePolicy.NONE)
        withClue("무보정은 ±1분 미만이어야: $none") {
            (abs(none) < 1.0).shouldBeTrue()
        }
    }

    "KoreanSajuConfig DEFAULT 진태양시는 FULL (통설 회귀 가드)" {
        KoreanSajuConfig.DEFAULT.trueSolarTime shouldBe TrueSolarTimePolicy.FULL
    }
})
