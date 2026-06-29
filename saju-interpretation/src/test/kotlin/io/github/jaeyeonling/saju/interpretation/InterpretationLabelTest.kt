package io.github.jaeyeonling.saju.interpretation

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldNotBeBlank

/**
 * 해석 enum 표시 라벨 정합성 — 코어와 동일하게 koreanName/hanja 를 일관 제공한다.
 * GyeokgukType·YongsinMethod 만 koreanName 을 갖던 비일관을 해소한다.
 */
class InterpretationLabelTest : StringSpec({

    "십성 라벨이 ordinal 순서와 정합한다" {
        SipSeong.entries.map { it.koreanName } shouldBe
            listOf("비견", "겁재", "식신", "상관", "편재", "정재", "편관", "정관", "편인", "정인")
        SipSeong.BIGYEON.hanja shouldBe "比肩"
    }

    "십이운성 라벨이 ordinal 순서와 정합한다" {
        SibiUnseong.entries.map { it.koreanName } shouldBe
            listOf("장생", "목욕", "관대", "건록", "제왕", "쇠", "병", "사", "묘", "절", "태", "양")
        SibiUnseong.JANGSAENG.hanja shouldBe "長生"
    }

    "신강신약 verdict 라벨이 정합한다" {
        SinStrengthVerdict.entries.map { it.koreanName } shouldBe
            listOf("극신강", "신강", "중화", "신약", "극신약")
        SinStrengthVerdict.JUNGHWA.hanja shouldBe "中和"
    }

    "기존 koreanName 보유 enum 도 그대로 유지된다 (회귀)" {
        GyeokgukType.GEONLOK.koreanName shouldBe "건록격"
        YongsinMethod.BUEOK.koreanName shouldBe "억부"
    }

    "모든 해석 라벨은 비어있지 않다" {
        (SipSeong.entries.map { it.koreanName to it.hanja } +
            SibiUnseong.entries.map { it.koreanName to it.hanja } +
            SinStrengthVerdict.entries.map { it.koreanName to it.hanja }).forEach { (ko, ha) ->
            ko.shouldNotBeBlank()
            ha.shouldNotBeBlank()
        }
    }
})
