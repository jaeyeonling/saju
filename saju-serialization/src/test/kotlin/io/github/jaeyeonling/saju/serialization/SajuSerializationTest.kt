package io.github.jaeyeonling.saju.serialization

import io.github.jaeyeonling.saju.Saju
import io.github.jaeyeonling.saju.domain.Cheongan
import io.github.jaeyeonling.saju.domain.Jiji
import io.github.jaeyeonling.saju.domain.Ohaeng
import io.github.jaeyeonling.saju.interpretation.HapChungRelation
import io.github.jaeyeonling.saju.interpretation.Interpretation
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.maps.shouldContainKey
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import kotlinx.serialization.decodeFromString

class SajuSerializationTest : StringSpec({

    val chart = Saju.fromLocalDateTime(1990, 3, 15, 7, 0, 9.0) // 일주 己卯

    "사주판이 한글·한자 라벨을 포함한 JSON 으로 직렬화·역직렬화된다" {
        val json = chart.toJson()
        json shouldContain "기묘"
        val dto = sajuJson.decodeFromString<SajuChartDto>(json)
        dto.dayMaster.name shouldBe "기"
        dto.dayMaster.hanja shouldBe "己"
        dto.dayMaster.ohaeng shouldBe "토"
        dto.day.ganji.name shouldBe "기묘"
        dto.day.ganji.hanja shouldBe "己卯"
    }

    "해석 리포트가 한글 라벨 JSON 으로 직렬화되고 라운드트립한다" {
        val report = Interpretation.of(chart)
        val dto = report.toDto()
        dto.gongmang shouldHaveSize 2
        dto.sibiUnseong shouldContainKey "YEAR"
        dto.ohaengCounts.values.sum() shouldBe 8 // 천간 4 + 지지 4
        dto.strength.verdictKorean.isNotBlank() shouldBe true
        dto.yongsin.methodKorean.isNotBlank() shouldBe true

        val back = sajuJson.decodeFromString<InterpretationReportDto>(report.toJson())
        back.gongmang shouldBe dto.gongmang
        back.sibiUnseong shouldBe dto.sibiUnseong
    }

    "합충 sealed 5종이 각각 올바른 kind 로 평탄화된다 (전 분기 커버)" {
        HapChungRelation.CheonganHap(Cheongan.GAP, Cheongan.GI, Ohaeng.TO).toDto().also {
            it.kind shouldBe "천간합"
            it.transformsTo shouldBe "토"
        }
        HapChungRelation.JijiYukhap(Jiji.JA, Jiji.CHUK).toDto().kind shouldBe "육합"
        HapChungRelation.JijiYukchung(Jiji.JA, Jiji.O).toDto().kind shouldBe "육충"
        HapChungRelation.JijiYukhae(Jiji.JA, Jiji.MI).toDto().kind shouldBe "육해"
        HapChungRelation.JijiSamhap(listOf(Jiji.SIN, Jiji.JA, Jiji.JIN), Ohaeng.SU).toDto().also {
            it.kind shouldBe "삼합"
            it.members shouldHaveSize 3
        }
    }
})
