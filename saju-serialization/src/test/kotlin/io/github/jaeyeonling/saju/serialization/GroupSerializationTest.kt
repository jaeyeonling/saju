package io.github.jaeyeonling.saju.serialization

import io.github.jaeyeonling.saju.derivation.Daeun
import io.github.jaeyeonling.saju.derivation.Seun
import io.github.jaeyeonling.saju.domain.Cheongan
import io.github.jaeyeonling.saju.domain.Ganji
import io.github.jaeyeonling.saju.domain.Jiji
import io.github.jaeyeonling.saju.domain.Pillar
import io.github.jaeyeonling.saju.domain.PillarPosition
import io.github.jaeyeonling.saju.domain.SajuChart
import io.github.jaeyeonling.saju.group.GROUP_DISCLAIMER
import io.github.jaeyeonling.saju.group.Gender
import io.github.jaeyeonling.saju.group.GroupAnalysis
import io.github.jaeyeonling.saju.group.GroupMember
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import kotlinx.serialization.decodeFromString

class GroupSerializationTest : StringSpec({
    fun uniformChart(
        gan: Cheongan,
        ji: Jiji,
    ): SajuChart =
        SajuChart(
            year = Pillar(PillarPosition.YEAR, Ganji(gan, ji)),
            month = Pillar(PillarPosition.MONTH, Ganji(gan, ji)),
            day = Pillar(PillarPosition.DAY, Ganji(gan, ji)),
            hour = Pillar(PillarPosition.HOUR, Ganji(gan, ji)),
        )

    "GroupReport → JSON 라운드트립 — 한글 라벨·면책·null 세운 분기" {
        val withSeun =
            GroupMember.of(
                id = "a",
                alias = "에이",
                gender = Gender.MALE,
                birthYear = 1991,
                chart = uniformChart(Cheongan.GAP, Jiji.JA),
                daeun = listOf(Daeun(35, Ganji.fromIndex(1))),
                seun = Seun(2026, Ganji.fromIndex(42)),
            )
        val withoutSeun =
            GroupMember.of(
                id = "b",
                alias = "비",
                gender = Gender.FEMALE,
                birthYear = 0,
                chart = uniformChart(Cheongan.BYEONG, Jiji.O),
                daeun = emptyList(),
                seun = null,
            )

        val json = GroupAnalysis.of(listOf(withSeun, withoutSeun), 2026).toJson()
        val decoded = sajuJson.decodeFromString<GroupReportDto>(json)

        decoded.memberIds shouldBe listOf("a", "b")
        decoded.disclaimer shouldBe GROUP_DISCLAIMER
        decoded.ohaeng.groupVector.keys shouldContain "목" // enum-key 가 한글로 평탄화
        decoded.timeline.year shouldBe 2026
        decoded.timeline.groupSeun?.name shouldBe "병오" // a 의 세운(공통)
        decoded.timeline.memberSeunSipseong.getValue("b").sipSeong shouldBe null // 세운 없음
        decoded.relationMatrix.nodes shouldHaveSize 2
        decoded.context.deficitFactor shouldBe 0.5
    }
})
