package io.github.jaeyeonling.saju.group

import io.github.jaeyeonling.saju.domain.Cheongan
import io.github.jaeyeonling.saju.domain.Jiji
import io.github.jaeyeonling.saju.domain.Ohaeng
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldNotContain
import io.kotest.matchers.shouldBe

/**
 * 오행 균형 합성 — test_synthesis.py 의 핀값 의도를 chart 기반으로 이식.
 * 표면 8글자 분포(목2화3토2금1수0 등)를 정확히 만드는 유효 간지로 검증한다.
 */
class OhaengSynthesisTest : StringSpec({
    // 갑인·병오·무진·병신 → 천간 갑병무병, 지지 인오진신 → 목2 화3 토2 금1 수0.
    val mok2hwa3 =
        chartOf(
            Cheongan.GAP to Jiji.IN,
            Cheongan.BYEONG to Jiji.O,
            Cheongan.MU to Jiji.JIN,
            Cheongan.BYEONG to Jiji.SIN,
        )

    "그룹 오행 벡터는 멤버 표면 분포의 합" {
        val balance = OhaengSynthesis.analyze(listOf(memberOf("a", mok2hwa3)), GroupContext.DEFAULT)
        balance.groupVector shouldBe
            mapOf(
                Ohaeng.MOK to 2,
                Ohaeng.HWA to 3,
                Ohaeng.TO to 2,
                Ohaeng.GEUM to 1,
                Ohaeng.SU to 0,
            )
    }

    "수가 0이고 목이 있으면 끊긴 상생 체인 — 수→목" {
        val balance = OhaengSynthesis.analyze(listOf(memberOf("a", mok2hwa3)), GroupContext.DEFAULT)
        balance.brokenChains shouldContain BrokenChain(Ohaeng.SU, Ohaeng.MOK)
        balance.brokenChains.map { it.description } shouldContain "수→목 공급원 결여"
    }

    "0개 오행은 결핍, 기댓값의 1.6배 초과는 과잉" {
        val balance = OhaengSynthesis.analyze(listOf(memberOf("a", mok2hwa3)), GroupContext.DEFAULT)
        balance.deficient shouldContain Ohaeng.SU // 0개
        balance.excessive shouldContain Ohaeng.HWA // 3/8=0.375 > 0.32
    }

    "다섯 오행이 모두 있으면 끊긴 체인 없음" {
        // 갑자·병인·무진·경신 → 목2 화1 토2 금2 수1 (모든 오행 ≥ 1).
        val balanced =
            chartOf(
                Cheongan.GAP to Jiji.JA,
                Cheongan.BYEONG to Jiji.IN,
                Cheongan.MU to Jiji.JIN,
                Cheongan.GYEONG to Jiji.SIN,
            )
        val balance = OhaengSynthesis.analyze(listOf(memberOf("a", balanced)), GroupContext.DEFAULT)
        balance.brokenChains.shouldBeEmpty()
        balance.deficient shouldNotContain Ohaeng.SU
    }

    "여러 멤버의 오행은 합산된다" {
        val balanced =
            chartOf(
                Cheongan.GAP to Jiji.JA,
                Cheongan.BYEONG to Jiji.IN,
                Cheongan.MU to Jiji.JIN,
                Cheongan.GYEONG to Jiji.SIN,
            )
        val balance =
            OhaengSynthesis.analyze(
                listOf(memberOf("a", balanced), memberOf("b", balanced)),
                GroupContext.DEFAULT,
            )
        balance.groupVector[Ohaeng.MOK] shouldBe 4
        balance.groupVector[Ohaeng.SU] shouldBe 2
        balance.dominantByMember.keys shouldBe setOf("a", "b")
    }

    "0은 아니지만 기댓값 미만이면 결핍 (== 0 이 아닌 임계 분기)" {
        // 목이 24글자 중 1개뿐 → norm 1/24 ≈ 0.042 < 0.10. count 는 0 이 아니다.
        val a =
            memberOf(
                "a",
                chartOf(
                    Cheongan.GAP to Jiji.JA,
                    Cheongan.BYEONG to Jiji.O,
                    Cheongan.MU to Jiji.JIN,
                    Cheongan.GYEONG to Jiji.SIN,
                ),
            )
        val heavy =
            chartOf(
                Cheongan.BYEONG to Jiji.O,
                Cheongan.BYEONG to Jiji.O,
                Cheongan.MU to Jiji.JIN,
                Cheongan.GYEONG to Jiji.SIN,
            )
        val balance =
            OhaengSynthesis.analyze(
                listOf(a, memberOf("b", heavy), memberOf("c", heavy)),
                GroupContext.DEFAULT,
            )
        balance.groupVector[Ohaeng.MOK] shouldBe 1 // 0 이 아님
        balance.deficient shouldContain Ohaeng.MOK // norm < 0.10 임계 분기
    }
})
