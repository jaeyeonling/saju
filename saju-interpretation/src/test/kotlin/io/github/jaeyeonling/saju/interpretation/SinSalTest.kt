// 앵커 차트의 기둥별 명리 주석을 인라인으로 단다(StrategyTest 와 동일한 가독성 패턴).
@file:Suppress("ktlint:standard:discouraged-comment-location")

package io.github.jaeyeonling.saju.interpretation

import io.github.jaeyeonling.saju.domain.Cheongan
import io.github.jaeyeonling.saju.domain.Ganji
import io.github.jaeyeonling.saju.domain.Jiji
import io.github.jaeyeonling.saju.domain.Pillar
import io.github.jaeyeonling.saju.domain.PillarPosition
import io.github.jaeyeonling.saju.domain.SajuChart
import io.kotest.assertions.withClue
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldBeIn
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe

class SinSalTest : StringSpec({

    "신살(일지 삼합 기준) 도화·역마·화개가 골든 벡터와 일치" {
        for (row in Golden.rows("sinsal_branch.csv")) {
            val dayBranch = Jiji.entries[row[0].toInt()]
            withClue("일지 ${dayBranch.koreanName} 도화") { dohwaTarget(dayBranch).ordinal shouldBe row[1].toInt() }
            withClue("일지 ${dayBranch.koreanName} 역마") { yeokmaTarget(dayBranch).ordinal shouldBe row[2].toInt() }
            withClue("일지 ${dayBranch.koreanName} 화개") { hwagaeTarget(dayBranch).ordinal shouldBe row[3].toInt() }
        }
    }

    "신살(일간 기준) 천을귀인·양인·문창이 골든 벡터와 일치" {
        for (row in Golden.rows("sinsal_daystem.csv")) {
            val dayStem = Cheongan.entries[row[0].toInt()]
            val expectedCheoneul = listOf(row[1].toInt(), row[2].toInt()).sorted()
            withClue("일간 ${dayStem.koreanName} 천을") {
                cheoneulTargets(dayStem).map { it.ordinal }.sorted() shouldBe expectedCheoneul
            }
            val expectedYangin = row[3].toInt().takeIf { it >= 0 }
            withClue("일간 ${dayStem.koreanName} 양인") { yanginTarget(dayStem)?.ordinal shouldBe expectedYangin }
            withClue("일간 ${dayStem.koreanName} 문창") { munchangTarget(dayStem).ordinal shouldBe row[4].toInt() }
        }
    }

    "불변식 — 도화∈자오묘유, 역마∈인신사해, 화개∈진술축미 (외부 표본 무관)" {
        val wangji = listOf(0, 3, 6, 9) // 子卯午酉
        val saengjiChung = listOf(2, 5, 8, 11) // 寅巳申亥
        val gozi = listOf(1, 4, 7, 10) // 丑辰未戌
        for (dayBranch in Jiji.entries) {
            withClue("도화 ${dayBranch.koreanName}") { dohwaTarget(dayBranch).ordinal shouldBeIn wangji }
            withClue("역마 ${dayBranch.koreanName}") { yeokmaTarget(dayBranch).ordinal shouldBeIn saengjiChung }
            withClue("화개 ${dayBranch.koreanName}") { hwagaeTarget(dayBranch).ordinal shouldBeIn gozi }
        }
    }

    "불변식 — 양인살은 양간에만, 천을귀인은 일간마다 정확히 2지지" {
        for (stem in Cheongan.entries) {
            if (stem.eumyang.isYang) {
                withClue("양간 ${stem.koreanName} 양인 존재") { yanginTarget(stem) shouldNotBe null }
            } else {
                withClue("음간 ${stem.koreanName} 양인 없음") { yanginTarget(stem) shouldBe null }
            }
            withClue("천을 ${stem.koreanName} 2지지") { cheoneulTargets(stem).size shouldBe 2 }
        }
    }

    "통합 — 갑자(甲子) 일주 신살 손계산 앵커" {
        // 일간 甲: 천을=丑未, 양인=卯, 문창=巳. 일지 子(수국): 도화=酉, 역마=寅, 화개=辰.
        val chart =
            SajuChart(
                year = Pillar(PillarPosition.YEAR, Ganji(Cheongan.JEONG, Jiji.YU)), // 정유 — 酉=도화
                month = Pillar(PillarPosition.MONTH, Ganji(Cheongan.EUL, Jiji.MYO)), // 을묘 — 卯=양인
                day = Pillar(PillarPosition.DAY, Ganji(Cheongan.GAP, Jiji.JA)), // 갑자 — 子엔 신살 없음
                hour = Pillar(PillarPosition.HOUR, Ganji(Cheongan.GAP, Jiji.IN)), // 갑인 — 寅=역마
            )
        val sinsal = SinSalFinder.find(chart)
        sinsal.getValue(PillarPosition.YEAR) shouldBe listOf(SinSal.DOHWA)
        sinsal.getValue(PillarPosition.MONTH) shouldBe listOf(SinSal.YANGIN)
        sinsal.getValue(PillarPosition.DAY) shouldBe emptyList()
        sinsal.getValue(PillarPosition.HOUR) shouldBe listOf(SinSal.YEOKMA)
    }

    "신살 판정은 결정론적이다" {
        val chart =
            SajuChart(
                year = Pillar(PillarPosition.YEAR, Ganji.fromIndex(0)),
                month = Pillar(PillarPosition.MONTH, Ganji.fromIndex(20)),
                day = Pillar(PillarPosition.DAY, Ganji.fromIndex(40)),
                hour = Pillar(PillarPosition.HOUR, Ganji.fromIndex(10)),
            )
        SinSalFinder.find(chart) shouldBe SinSalFinder.find(chart)
    }
})
