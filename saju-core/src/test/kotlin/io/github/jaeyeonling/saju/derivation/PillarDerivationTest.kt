package io.github.jaeyeonling.saju.derivation

import io.github.jaeyeonling.saju.domain.Cheongan
import io.github.jaeyeonling.saju.domain.GanZhi
import io.github.jaeyeonling.saju.domain.Jiji
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class PillarDerivationTest : StringSpec({

    "연주 — 1984 갑자년, 2026 병오년" {
        PillarDerivation.yearPillar(1984) shouldBe GanZhi(Cheongan.GAB, Jiji.JA)
        PillarDerivation.yearPillar(2026) shouldBe GanZhi(Cheongan.BYEONG, Jiji.O)
    }

    "월주 오호둔 — 갑기년 병인월 시작" {
        // 갑기년 정월(인월)은 병인. 을경년은 무인, 무계년은 갑인.
        PillarDerivation.monthPillar(Cheongan.GAB, 0) shouldBe GanZhi(Cheongan.BYEONG, Jiji.IN)
        PillarDerivation.monthPillar(Cheongan.GI, 0) shouldBe GanZhi(Cheongan.BYEONG, Jiji.IN)
        PillarDerivation.monthPillar(Cheongan.EUL, 0) shouldBe GanZhi(Cheongan.MU, Jiji.IN)
        PillarDerivation.monthPillar(Cheongan.MU, 0) shouldBe GanZhi(Cheongan.GAB, Jiji.IN)
        // 갑년 축월(offset 11) = 정축
        PillarDerivation.monthPillar(Cheongan.GAB, 11) shouldBe GanZhi(Cheongan.JEONG, Jiji.CHUK)
    }

    "시주 오자둔 — 갑기일 갑자시 시작" {
        // 갑기일 자시는 갑자, 무계일 자시는 임자.
        PillarDerivation.hourPillar(Cheongan.GAB, Jiji.JA) shouldBe GanZhi(Cheongan.GAB, Jiji.JA)
        PillarDerivation.hourPillar(Cheongan.MU, Jiji.JA) shouldBe GanZhi(Cheongan.IM, Jiji.JA)
        // 갑일 오시(offset 6) = 경오
        PillarDerivation.hourPillar(Cheongan.GAB, Jiji.O) shouldBe GanZhi(Cheongan.GYEONG, Jiji.O)
    }
})
