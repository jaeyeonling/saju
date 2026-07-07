package io.github.jaeyeonling.saju.cli

import io.github.jaeyeonling.saju.serialization.sajuJson
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import kotlinx.serialization.decodeFromString

/** 그룹 서브커맨드 렌더 — 부작용 없는 renderGroup + 파일 읽기 주입 runGroupCli. */
class GroupCliRenderTest : StringSpec({
    val sample =
        """
        {
          "group_name": "테스트 그룹",
          "seun_year": 2026,
          "members": [
            {"id":"m1","name":"갑","alias":"멤버1","gender":"M","year":1990,"month":3,"day":15,"hour":7,"minute":0},
            {"id":"m2","name":"을","alias":"멤버2","gender":"F","year":1992,"month":8,"day":22,"hour":14,"minute":30},
            {"id":"m3","name":"병","alias":"멤버3","gender":"M","year":1988,"month":11,"day":5,"hour":20,"minute":0}
          ]
        }
        """.trimIndent()

    "텍스트 렌더 — 그룹명·4섹션·면책" {
        val output = renderGroup(sample, flagSeunYear = null, currentYear = 2026, asJson = false)
        output shouldContain "그룹 사주"
        output shouldContain "테스트 그룹"
        output shouldContain "오행 균형"
        output shouldContain "십성 역할"
        output shouldContain "멤버 관계"
        output shouldContain "시간축"
        output shouldContain "점술 정설이 아닙니다"
    }

    "JSON 렌더 — 멤버(persons)+그룹+면책" {
        val output = renderGroup(sample, flagSeunYear = null, currentYear = 2026, asJson = true)
        val decoded = sajuJson.decodeFromString<GroupCliOutput>(output)
        decoded.groupName shouldBe "테스트 그룹"
        decoded.seunYear shouldBe 2026
        decoded.members shouldHaveSize 3
        decoded.group.memberIds shouldBe listOf("m1", "m2", "m3")
        decoded.group.disclaimer.contains("점술") shouldBe true
    }

    "config 에 seun_year 없으면 currentYear 사용" {
        val noSeun =
            """
            {"group_name":"g","members":[
              {"id":"a","year":1990,"month":3,"day":15,"hour":7,"minute":0},
              {"id":"b","year":1991,"month":4,"day":16,"hour":8,"minute":0}
            ]}
            """.trimIndent()
        val decoded =
            sajuJson.decodeFromString<GroupCliOutput>(
                renderGroup(noSeun, flagSeunYear = null, currentYear = 2030, asJson = true),
            )
        decoded.seunYear shouldBe 2030
    }

    "--seun 플래그가 config.seun_year 보다 우선" {
        // sample 의 seun_year=2026 을 명령행 플래그(2099)가 덮어쓴다.
        val decoded =
            sajuJson.decodeFromString<GroupCliOutput>(
                renderGroup(sample, flagSeunYear = 2099, currentYear = 2026, asJson = true),
            )
        decoded.seunYear shouldBe 2099
    }

    "runGroupCli — 파일 읽기 주입으로 성공" {
        val (output, code) = runGroupCli(arrayOf("group", "members.json", "--json"), 2026) { sample }
        code shouldBe 0
        output shouldContain "테스트 그룹"
    }

    "runGroupCli — 경로 누락은 usage + code 2" {
        val (output, code) = runGroupCli(arrayOf("group"), 2026) { "" }
        code shouldBe 2
        output shouldContain "members.json 경로"
    }

    "runGroupCli — 깨진 JSON 은 usage + code 2" {
        val (_, code) = runGroupCli(arrayOf("group", "x.json"), 2026) { "{ broken" }
        code shouldBe 2
    }

    "runGroupCli — 멤버 1명이면 usage(최소 2명)" {
        val one = """{"group_name":"g","members":[{"id":"m1","year":1990,"month":3,"day":15,"hour":7,"minute":0}]}"""
        val (_, code) = runGroupCli(arrayOf("group", "x.json"), 2026) { one }
        code shouldBe 2
    }

    "runGroupCli — 잘못된 성별 코드는 usage + code 2 (조용히 남성으로 흡수하지 않는다)" {
        val badGender =
            """
            {"group_name":"g","members":[
              {"id":"a","gender":"male","year":1990,"month":3,"day":15,"hour":7,"minute":0},
              {"id":"b","gender":"F","year":1991,"month":4,"day":16,"hour":8,"minute":0}
            ]}
            """.trimIndent()
        val (output, code) = runGroupCli(arrayOf("group", "x.json"), 2026) { badGender }
        code shouldBe 2
        output shouldContain "성별 코드"
    }

    "runGroupCli — 파일 읽기 예외는 usage + code 2" {
        val (_, code) = runGroupCli(arrayOf("group", "x.json"), 2026) { throw java.io.IOException("없음") }
        code shouldBe 2
    }
})
