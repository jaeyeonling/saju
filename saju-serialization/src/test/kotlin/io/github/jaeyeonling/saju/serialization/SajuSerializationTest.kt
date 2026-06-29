package io.github.jaeyeonling.saju.serialization

import io.github.jaeyeonling.saju.Saju
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
        dto.day.ganZhi.name shouldBe "기묘"
        dto.day.ganZhi.hanja shouldBe "己卯"
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

    "합충은 유효한 kind 한글 분류명으로 평탄화된다" {
        val dto = Interpretation.of(chart).toDto()
        val validKinds = setOf("천간합", "육합", "육충", "육해", "삼합")
        dto.hapChung.forEach { (it.kind in validKinds) shouldBe true }
    }
})
