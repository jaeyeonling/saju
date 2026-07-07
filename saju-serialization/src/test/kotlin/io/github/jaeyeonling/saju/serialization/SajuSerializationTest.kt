package io.github.jaeyeonling.saju.serialization

import io.github.jaeyeonling.saju.Saju
import io.github.jaeyeonling.saju.astronomy.JulianDayConverter
import io.github.jaeyeonling.saju.domain.Cheongan
import io.github.jaeyeonling.saju.domain.Gender
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
        // 산출 근거(basis)가 신강신약·용신에 노출된다 — LLM 검증용.
        dto.strength.basis shouldContain "가중"
        dto.yongsin.basis.isNotBlank() shouldBe true
        // 십성 5묶음 세력(억부 분기 입력)이 직렬화된다.
        dto.strength.groupScores shouldContainKey "비겁"
        dto.strength.groupScores.size shouldBe 5

        val back = sajuJson.decodeFromString<InterpretationReportDto>(report.toJson())
        back.gongmang shouldBe dto.gongmang
        back.sibiUnseong shouldBe dto.sibiUnseong
    }

    "신규 필드(십성·지장간·신살·가중오행)가 네 기둥으로 직렬화되고 라운드트립한다" {
        // 己卯 일주: 일간 己 → 일주 천간 십성은 '나'라서 null. 일지 卯 본기 = 乙.
        val report = Interpretation.of(chart)
        val dto = report.toDto()
        dto.sipSeong shouldContainKey "DAY"
        dto.sipSeong.getValue("DAY").stem shouldBe null // 일간 자리
        dto.sipSeong.getValue("DAY").branchMain.isNotBlank() shouldBe true
        dto.hiddenStems shouldContainKey "DAY"
        dto.hiddenStems.getValue("DAY").mainQi shouldBe "을" // 卯 본기 乙
        dto.sinSal shouldContainKey "DAY"
        // 가중은 표면 8글자에 지장간을 더하므로 항상 표면 합(8)보다 크다(지지마다 지장간 개수가 달라 정확값은 차트 의존).
        (dto.ohaengWeightedCounts.values.sum() > dto.ohaengCounts.values.sum()) shouldBe true

        val back = sajuJson.decodeFromString<InterpretationReportDto>(report.toJson())
        back.sipSeong shouldBe dto.sipSeong
        back.hiddenStems shouldBe dto.hiddenStems
        back.sinSal shouldBe dto.sinSal
        back.ohaengWeightedCounts shouldBe dto.ohaengWeightedCounts
    }

    "합충 sealed 7종이 각각 올바른 kind 로 평탄화된다 (전 분기 커버)" {
        HapChungRelation.CheonganHap(Cheongan.GAP, Cheongan.GI, Ohaeng.TO).toDto().also {
            it.kind shouldBe "천간합"
            it.transformsTo shouldBe "토"
        }
        // 일간 합화 보류(合而不化) — transformsTo null 도 평탄화된다
        HapChungRelation.CheonganHap(Cheongan.GAP, Cheongan.GI, null).toDto().also {
            it.kind shouldBe "천간합"
            it.transformsTo shouldBe null
        }
        HapChungRelation.CheonganChung(Cheongan.GAP, Cheongan.GYEONG).toDto().also {
            it.kind shouldBe "천간충"
            it.transformsTo shouldBe null
        }
        HapChungRelation.JijiYukhap(Jiji.JA, Jiji.CHUK).toDto().kind shouldBe "육합"
        HapChungRelation.JijiYukchung(Jiji.JA, Jiji.O).toDto().kind shouldBe "육충"
        HapChungRelation.JijiYukhae(Jiji.JA, Jiji.MI).toDto().kind shouldBe "육해"
        HapChungRelation.JijiSamhap(listOf(Jiji.SIN, Jiji.JA, Jiji.JIN), Ohaeng.SU).toDto().also {
            it.kind shouldBe "삼합"
            it.members shouldHaveSize 3
        }
        HapChungRelation.JijiBanghap(listOf(Jiji.IN, Jiji.MYO, Jiji.JIN), Ohaeng.MOK).toDto().also {
            it.kind shouldBe "방합"
            it.transformsTo shouldBe "목"
            it.members shouldHaveSize 3
        }
    }

    "대운 시퀀스가 성별과 함께 JSON 으로 직렬화된다 (성별→대운 방향을 스키마에 노출)" {
        val localJd = JulianDayConverter.fromGregorian(1990, 3, 15, (7 * 60) / 1440.0)
        val utJd = localJd - 9.0 / 24.0
        val maleDaeun = Saju.daeun(utJd, chart.month.ganji, chart.year.gan.eumyang, Gender.MALE, count = 8)

        val dto = maleDaeun.toDaeunSeriesDto(Gender.MALE)
        dto.gender shouldBe "MALE"
        dto.genderKorean shouldBe "남"
        dto.daeun shouldHaveSize 8
        dto.daeun.first().startAge shouldBe maleDaeun.first().startAge
        dto.daeun.first().ganji.name shouldBe maleDaeun.first().ganji.koreanName

        // 성별이 바뀌면 방향이 갈려 대운 시퀀스도 달라진다 — JSON 에 성별이 드러나야 하는 이유.
        val femaleDaeun = Saju.daeun(utJd, chart.month.ganji, chart.year.gan.eumyang, Gender.FEMALE, count = 8)
        val femaleJson = femaleDaeun.toDaeunJson(Gender.FEMALE)
        femaleJson shouldContain "FEMALE"
        femaleJson shouldContain "여"
        val back = sajuJson.decodeFromString<DaeunSeriesDto>(femaleJson)
        back.gender shouldBe "FEMALE"
        back.daeun shouldHaveSize 8
    }
})
