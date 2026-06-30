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

    "인자가 없으면 데모를 출력하고 종료코드 0" {
        val (out, code) = runCli(emptyArray())
        code shouldBe 0
        withClue("데모 출력에 8글자 블록:\n$out") { out.contains("일간(나)").shouldBeTrue() }
    }

    "인자가 부족하면 usage 와 종료코드 2 (조용한 폴백 금지)" {
        val (out, code) = runCli(arrayOf("1990", "3"))
        code shouldBe 2
        withClue("usage 누락:\n$out") { out.contains("사용법").shouldBeTrue() }
    }

    "숫자가 아닌 인자는 usage 로 거부" {
        val (out, code) = runCli(arrayOf("1990", "삼", "15", "7", "0"))
        code shouldBe 2
        out.contains("사용법").shouldBeTrue()
    }

    "잘못된 날짜(2월 30일)는 usage 로 거부" {
        val (_, code) = runCli(arrayOf("1990", "2", "30", "7", "0"))
        code shouldBe 2
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

    "parseFlags 는 --json·--female·--seun 을 추출한다" {
        parseFlags(arrayOf("1990", "3", "15", "7", "0", "--json", "--female", "--seun=2026")) shouldBe
            CliFlags(json = true, isMale = false, seunYear = 2026)
    }

    "parseFlags 기본값 — 플래그 없으면 json=false·남성·세운 없음" {
        parseFlags(arrayOf("1990", "3")) shouldBe CliFlags(json = false, isMale = true, seunYear = null)
    }

    "--json 은 사주판·해석을 JSON 으로 출력하고 종료코드 0" {
        val (out, code) = runCli(arrayOf("1990", "3", "15", "7", "0", "--json"))
        code shouldBe 0
        withClue("JSON 시작 아님:\n$out") { out.trim().startsWith("{").shouldBeTrue() }
        withClue("chart 누락:\n$out") { out.contains("\"chart\"").shouldBeTrue() }
        withClue("interpretation 누락:\n$out") { out.contains("\"interpretation\"").shouldBeTrue() }
    }

    "--json --seun=2026 은 세운(병오)을 포함한다" {
        val (out, _) = runCli(arrayOf("1990", "3", "15", "7", "0", "--json", "--seun=2026"))
        withClue("세운 키 누락:\n$out") { out.contains("\"seun\"").shouldBeTrue() }
        withClue("2026 세운 병오 누락:\n$out") { out.contains("병오").shouldBeTrue() }
    }

    "renderJson 은 seunYear=null 이면 세운을 비운다" {
        val out = renderJson(CliInput.DEFAULT, seunYear = null)
        withClue("세운이 null 이어야:\n$out") { out.contains("\"seun\": null").shouldBeTrue() }
    }

    "--female 은 JSON 입력에 isMale=false 로 반영된다" {
        val (out, _) = runCli(arrayOf("1990", "3", "15", "7", "0", "--json", "--female"))
        withClue("isMale=false 반영 안 됨:\n$out") { out.contains("\"isMale\": false").shouldBeTrue() }
    }

    "인자 없이 --json 이면 데모를 JSON 으로 출력한다" {
        val (out, code) = runCli(arrayOf("--json"))
        code shouldBe 0
        withClue("데모 JSON 아님:\n$out") { out.contains("\"chart\"").shouldBeTrue() }
    }
})
