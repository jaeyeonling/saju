package io.github.jaeyeonling.saju.domain

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldNotBeBlank

/**
 * 도메인 enum 표시 라벨(koreanName/hanja) 정합성 — 라이브러리 표면이 한글/한자를 일관 제공한다.
 * 이전에는 CLI 의 private 배열에만 라벨이 있어 소비자가 매핑을 재구현해야 했다(중복·비일관 제거).
 */
class DomainLabelTest : StringSpec({

    "천간 라벨이 ordinal 순서와 정합한다" {
        Cheongan.entries.map { it.koreanName } shouldBe
            listOf("갑", "을", "병", "정", "무", "기", "경", "신", "임", "계")
        Cheongan.GAP.hanja shouldBe "甲"
        Cheongan.GYE.hanja shouldBe "癸"
    }

    "지지 라벨이 ordinal 순서와 정합한다" {
        Jiji.entries.map { it.koreanName } shouldBe
            listOf("자", "축", "인", "묘", "진", "사", "오", "미", "신", "유", "술", "해")
        Jiji.JA.hanja shouldBe "子"
        Jiji.HAE.hanja shouldBe "亥"
    }

    "60갑자 한글/한자 표기가 천간·지지 합성과 일치한다" {
        Ganji.fromIndex(0).koreanName shouldBe "갑자"
        Ganji.fromIndex(0).hanja shouldBe "甲子"
        Ganji.fromIndex(59).koreanName shouldBe "계해"
        Ganji(Cheongan.GYEONG, Jiji.O).koreanName shouldBe "경오"
    }

    "오행·음양 라벨이 정합한다" {
        Ohaeng.entries.map { it.koreanName } shouldBe listOf("목", "화", "토", "금", "수")
        Ohaeng.TO.hanja shouldBe "土"
        Eumyang.YANG.koreanName shouldBe "양"
        Eumyang.EUM.hanja shouldBe "陰"
    }

    "모든 도메인 라벨은 비어있지 않고 천간/지지 한글명이 유일하다" {
        (
            Cheongan.entries.map { it.koreanName to it.hanja } +
                Jiji.entries.map { it.koreanName to it.hanja } +
                Ohaeng.entries.map { it.koreanName to it.hanja }
        ).forEach { (ko, ha) ->
            ko.shouldNotBeBlank()
            ha.shouldNotBeBlank()
        }
        Cheongan.entries.map { it.koreanName }.toSet() shouldHaveSize Cheongan.entries.size
        Jiji.entries.map { it.koreanName }.toSet() shouldHaveSize Jiji.entries.size
    }
})
