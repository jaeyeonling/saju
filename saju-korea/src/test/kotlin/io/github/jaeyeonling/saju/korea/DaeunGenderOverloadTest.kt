package io.github.jaeyeonling.saju.korea

import io.github.jaeyeonling.saju.domain.Gender
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

/**
 * [KoreanSaju.daeun] 의 [Gender] 오버로드가 기존 isMale:Boolean 버전과 동일함을 보장한다
 * (한국 보정 경로에서도 성별→방향 위임이 정확한지 검증).
 */
class DaeunGenderOverloadTest : StringSpec({

    "KoreanSaju.daeun(Gender) 는 daeun(isMale) 과 동일" {
        val cases =
            listOf(
                intArrayOf(1990, 3, 15, 7, 0),
                intArrayOf(1975, 10, 2, 23, 30),
            )
        for (c in cases) {
            KoreanSaju.daeun(c[0], c[1], c[2], c[3], c[4], Gender.MALE) shouldBe
                KoreanSaju.daeun(c[0], c[1], c[2], c[3], c[4], isMale = true)
            KoreanSaju.daeun(c[0], c[1], c[2], c[3], c[4], Gender.FEMALE) shouldBe
                KoreanSaju.daeun(c[0], c[1], c[2], c[3], c[4], isMale = false)
        }
    }
})
