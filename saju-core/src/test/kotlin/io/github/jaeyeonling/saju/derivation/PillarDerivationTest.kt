package io.github.jaeyeonling.saju.derivation

import io.github.jaeyeonling.saju.domain.Cheongan
import io.github.jaeyeonling.saju.domain.Ganji
import io.github.jaeyeonling.saju.domain.Jiji
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class PillarDerivationTest : StringSpec({

    "연주 — 1984 갑자년, 2026 병오년" {
        PillarDerivation.yearPillar(1984) shouldBe Ganji(Cheongan.GAP, Jiji.JA)
        PillarDerivation.yearPillar(2026) shouldBe Ganji(Cheongan.BYEONG, Jiji.O)
    }

    "월주 오호둔 — 갑기년 병인월 시작" {
        // 갑기년 정월(인월)은 병인. 을경년은 무인, 무계년은 갑인.
        PillarDerivation.monthPillar(Cheongan.GAP, 0) shouldBe Ganji(Cheongan.BYEONG, Jiji.IN)
        PillarDerivation.monthPillar(Cheongan.GI, 0) shouldBe Ganji(Cheongan.BYEONG, Jiji.IN)
        PillarDerivation.monthPillar(Cheongan.EUL, 0) shouldBe Ganji(Cheongan.MU, Jiji.IN)
        PillarDerivation.monthPillar(Cheongan.MU, 0) shouldBe Ganji(Cheongan.GAP, Jiji.IN)
        // 갑년 축월(offset 11) = 정축
        PillarDerivation.monthPillar(Cheongan.GAP, 11) shouldBe Ganji(Cheongan.JEONG, Jiji.CHUK)
    }

    "시주 오자둔 — 갑기일 갑자시 시작" {
        // 갑기일 자시는 갑자, 무계일 자시는 임자.
        PillarDerivation.hourPillar(Cheongan.GAP, Jiji.JA) shouldBe Ganji(Cheongan.GAP, Jiji.JA)
        PillarDerivation.hourPillar(Cheongan.MU, Jiji.JA) shouldBe Ganji(Cheongan.IM, Jiji.JA)
        // 갑일 오시(offset 6) = 경오
        PillarDerivation.hourPillar(Cheongan.GAP, Jiji.O) shouldBe Ganji(Cheongan.GYEONG, Jiji.O)
    }
})
