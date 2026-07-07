package io.github.jaeyeonling.saju.domain

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

/**
 * [Gender] — 성별 enum. 대운 방향 판정 브릿지 [Gender.isMale] 와 라벨·[Gender.fromCode] 를 검증한다.
 */
class GenderTest : StringSpec({

    "isMale 은 양남음녀 방향 판정 브릿지 — MALE=true, FEMALE=false" {
        Gender.MALE.isMale shouldBe true
        Gender.FEMALE.isMale shouldBe false
    }

    "한글·한자 라벨" {
        Gender.MALE.koreanName shouldBe "남"
        Gender.MALE.hanja shouldBe "男"
        Gender.FEMALE.koreanName shouldBe "여"
        Gender.FEMALE.hanja shouldBe "女"
    }

    "fromCode — M/F(대소문자 무관)·남/여 를 받는다" {
        Gender.fromCode("F") shouldBe Gender.FEMALE
        Gender.fromCode("f") shouldBe Gender.FEMALE
        Gender.fromCode("여") shouldBe Gender.FEMALE
        Gender.fromCode("M") shouldBe Gender.MALE
        Gender.fromCode("m") shouldBe Gender.MALE
        Gender.fromCode("남") shouldBe Gender.MALE
    }

    "fromCode — 그 외 입력은 대운 방향이 뒤집힐 수 있으므로 명시적으로 실패한다" {
        listOf("", "X", "male", "MALE", " M", "M ").forEach { code ->
            shouldThrow<IllegalArgumentException> { Gender.fromCode(code) }
        }
    }
})
