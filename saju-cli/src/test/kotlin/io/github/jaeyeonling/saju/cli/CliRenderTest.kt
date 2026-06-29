package io.github.jaeyeonling.saju.cli

import io.kotest.assertions.withClue
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.shouldBe

/** CLI 인수 테스트 — 입력 → 출력(stdout) 계약을 렌더 문자열로 고정한다. */
class CliRenderTest : StringSpec({

    "인자 5개를 CliInput 으로 파싱한다" {
        parseArgs(arrayOf("1990", "3", "15", "7", "0")) shouldBe CliInput(1990, 3, 15, 7, 0)
    }

    "인자가 부족하면 데모 기본값을 쓴다" {
        parseArgs(emptyArray()) shouldBe CliInput.DEFAULT
        parseArgs(arrayOf("1990", "3")) shouldBe CliInput.DEFAULT
    }

    "렌더 출력에 8글자·해석·대운·음력 블록이 모두 포함된다" {
        val out = render(CliInput.DEFAULT)
        withClue("헤더 누락:\n$out") { out.contains("════════ 사주 만세력").shouldBeTrue() }
        withClue("8글자 블록 누락:\n$out") { out.contains("일간(나)").shouldBeTrue() }
        withClue("해석 블록 누락:\n$out") { out.contains("───── 해석 ─────").shouldBeTrue() }
        withClue("대운 블록 누락:\n$out") { out.contains("───── 대운").shouldBeTrue() }
        withClue("음력 예시 누락:\n$out") { out.contains("───── 음력 입력 예시").shouldBeTrue() }
    }

    "1990-3-15 7시 서울 — 일간 라벨이 한글·한자로 매핑된다" {
        // 알려진 결과: 일간 기(己), 오행 토(土)·음(陰). 라벨 ordinal 매핑 골든.
        val out = render(CliInput.DEFAULT)
        withClue("일간 라벨 매핑 불일치:\n$out") {
            out.contains("일간(나) : 기 [토(土)/음(陰)]").shouldBeTrue()
        }
    }

    "대운 블록은 8개 구간과 설명을 출력한다" {
        val out = render(CliInput.DEFAULT)
        // 시작 나이 8개(N세)와 대운 설명 라인.
        withClue("대운 8구간이어야:\n$out") {
            Regex("""\d+세 [가-힣]{2}""").findAll(out).count() shouldBe 8
        }
    }

    "렌더는 같은 입력에 같은 출력을 낸다 (결정론)" {
        render(CliInput.DEFAULT) shouldBe render(CliInput.DEFAULT)
    }
})
