package io.github.jaeyeonling.saju.cli

import io.kotest.assertions.withClue
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.shouldBe

/** CLI 인수 테스트 — 입력 → 출력(stdout) 계약을 렌더 문자열로 고정한다. */
class CliRenderTest : StringSpec({

    // 세운 자동 노출의 기준 연도 — 시계 의존을 없애 테스트를 결정적으로 만든다.
    val fixedYear = 2026

    "인자 5개를 CliInput 으로 파싱한다" {
        parseArgs(arrayOf("1990", "3", "15", "7", "0")) shouldBe CliInput(1990, 3, 15, 7, 0)
    }

    "인자가 없으면 데모를 출력하고 종료코드 0" {
        val (out, code) = runCli(emptyArray(), fixedYear)
        code shouldBe 0
        withClue("데모 출력에 8글자 블록:\n$out") { out.contains("일간(나)").shouldBeTrue() }
    }

    "인자가 부족하면 usage 와 종료코드 2 (조용한 폴백 금지)" {
        val (out, code) = runCli(arrayOf("1990", "3"), fixedYear)
        code shouldBe 2
        withClue("usage 누락:\n$out") { out.contains("사용법").shouldBeTrue() }
    }

    "숫자가 아닌 인자는 usage 로 거부" {
        val (out, code) = runCli(arrayOf("1990", "삼", "15", "7", "0"), fixedYear)
        code shouldBe 2
        out.contains("사용법").shouldBeTrue()
    }

    "잘못된 날짜(2월 30일)는 usage 로 거부" {
        val (_, code) = runCli(arrayOf("1990", "2", "30", "7", "0"), fixedYear)
        code shouldBe 2
    }

    "렌더 출력에 8글자·해석·세운·대운·음력 블록이 모두 포함된다" {
        val out = render(CliInput.DEFAULT, fixedYear)
        withClue("헤더 누락:\n$out") { out.contains("════════ 사주 만세력").shouldBeTrue() }
        withClue("8글자 블록 누락:\n$out") { out.contains("일간(나)").shouldBeTrue() }
        withClue("해석 블록 누락:\n$out") { out.contains("───── 해석 ─────").shouldBeTrue() }
        withClue("세운 블록 누락:\n$out") { out.contains("───── 세운 (2026)").shouldBeTrue() }
        withClue("대운 블록 누락:\n$out") { out.contains("───── 대운").shouldBeTrue() }
        withClue("음력 예시 누락:\n$out") { out.contains("───── 음력 입력 예시").shouldBeTrue() }
    }

    "렌더 출력에 지장간·신살·가중오행이 전면 노출된다" {
        val out = render(CliInput.DEFAULT, fixedYear)
        withClue("지장간 줄 누락:\n$out") { out.contains("지장간").shouldBeTrue() }
        withClue("신살 줄 누락:\n$out") { out.contains("신살").shouldBeTrue() }
        withClue("표면 오행 줄 누락:\n$out") { out.contains("오행(표면)").shouldBeTrue() }
        withClue("가중 오행 줄 누락:\n$out") { out.contains("오행(지장간)").shouldBeTrue() }
    }

    "렌더 출력에 용신·신강신약 근거와 지장간 십성 블록이 노출된다" {
        val out = render(CliInput.DEFAULT, fixedYear)
        withClue("신강신약 근거(가중) 누락:\n$out") { out.contains("가중").shouldBeTrue() }
        withClue("용신 근거(설기/생조) 누락:\n$out") {
            (out.contains("설기") || out.contains("생조")).shouldBeTrue()
        }
        withClue("지장간 십성 블록 누락:\n$out") { out.contains("지장간 십성").shouldBeTrue() }
    }

    "1990-3-15 7시 서울 — 일간 라벨이 한글·한자로 매핑된다" {
        // 알려진 결과: 일간 기(己), 오행 토(土)·음(陰). 라벨 ordinal 매핑 골든.
        val out = render(CliInput.DEFAULT, fixedYear)
        withClue("일간 라벨 매핑 불일치:\n$out") {
            out.contains("일간(나) : 기 [토(土)/음(陰)]").shouldBeTrue()
        }
    }

    "대운 블록은 8개 구간과 설명을 출력한다" {
        val out = render(CliInput.DEFAULT, fixedYear)
        // 대운 라인만 — "N세 간지" 패턴 8개("───── 대운" 이후 구간).
        val daeunBlock = out.substringAfter("───── 대운")
        withClue("대운 8구간이어야:\n$daeunBlock") {
            Regex("""\d+세 [가-힣]{2}""").findAll(daeunBlock).count() shouldBe 8
        }
    }

    "합충 렌더 — 일간 낀 합은 합화 보류(→없음), 화하는 합은 →오행" {
        // 1980-01-21 12시: 일간 계, 계무합(일간 낌 → 보류) + 방합 사오미→화(정상 화) 동시 노출.
        val out = render(CliInput(1980, 1, 21, 12, 0), fixedYear)
        val hapLine = out.lineSequence().first { it.contains("합충") }
        withClue("일간 낀 계무합은 합화 보류라 →오행 없이 렌더:\n$hapLine") {
            hapLine.contains("천간합(계-무)").shouldBeTrue()
            hapLine.contains("천간합(계-무→").shouldBe(false)
        }
        withClue("방합은 정상 화라 →화 렌더:\n$hapLine") {
            hapLine.contains("방합(사-오-미→화)").shouldBeTrue()
        }
    }

    "렌더는 같은 입력·연도에 같은 출력을 낸다 (결정론)" {
        render(CliInput.DEFAULT, fixedYear) shouldBe render(CliInput.DEFAULT, fixedYear)
    }

    "parseFlags 는 --json·--female·--seun 을 추출한다" {
        parseFlags(arrayOf("1990", "3", "15", "7", "0", "--json", "--female", "--seun=2026")) shouldBe
            CliFlags(json = true, isMale = false, seunYear = 2026)
    }

    "parseFlags 기본값 — 플래그 없으면 json=false·남성·세운 없음" {
        parseFlags(arrayOf("1990", "3")) shouldBe CliFlags(json = false, isMale = true, seunYear = null)
    }

    "--json 은 사주판·해석을 JSON 으로 출력하고 종료코드 0" {
        val (out, code) = runCli(arrayOf("1990", "3", "15", "7", "0", "--json"), fixedYear)
        code shouldBe 0
        withClue("JSON 시작 아님:\n$out") { out.trim().startsWith("{").shouldBeTrue() }
        withClue("chart 누락:\n$out") { out.contains("\"chart\"").shouldBeTrue() }
        withClue("interpretation 누락:\n$out") { out.contains("\"interpretation\"").shouldBeTrue() }
    }

    "--json 에 십성·지장간·신살·가중오행 키가 포함된다" {
        val (out, _) = runCli(arrayOf("1990", "3", "15", "7", "0", "--json"), fixedYear)
        withClue("sipSeong 누락:\n$out") { out.contains("\"sipSeong\"").shouldBeTrue() }
        withClue("hiddenStems 누락:\n$out") { out.contains("\"hiddenStems\"").shouldBeTrue() }
        withClue("sinSal 누락:\n$out") { out.contains("\"sinSal\"").shouldBeTrue() }
        withClue("ohaengWeightedCounts 누락:\n$out") { out.contains("\"ohaengWeightedCounts\"").shouldBeTrue() }
    }

    "--seun=2026 은 세운(병오)을 포함한다" {
        val (out, _) = runCli(arrayOf("1990", "3", "15", "7", "0", "--json", "--seun=2026"), fixedYear)
        withClue("세운 키 누락:\n$out") { out.contains("\"seun\"").shouldBeTrue() }
        withClue("2026 세운 병오 누락:\n$out") { out.contains("병오").shouldBeTrue() }
    }

    "세운은 --seun 없어도 올해(currentYear)로 항상 채워진다" {
        val out = renderJson(CliInput.DEFAULT, seunYear = fixedYear)
        withClue("세운 자동 노출 안 됨:\n$out") { out.contains("\"year\": 2026").shouldBeTrue() }
        withClue("세운 간지(병오) 누락:\n$out") { out.contains("병오").shouldBeTrue() }
        withClue("세운 십성 키 누락:\n$out") { out.contains("\"stemSipSeong\"").shouldBeTrue() }
    }

    "--female 은 JSON 입력에 isMale=false 로 반영된다" {
        val (out, _) = runCli(arrayOf("1990", "3", "15", "7", "0", "--json", "--female"), fixedYear)
        withClue("isMale=false 반영 안 됨:\n$out") { out.contains("\"isMale\": false").shouldBeTrue() }
    }

    "인자 없이 --json 이면 데모를 JSON 으로 출력한다" {
        val (out, code) = runCli(arrayOf("--json"), fixedYear)
        code shouldBe 0
        withClue("데모 JSON 아님:\n$out") { out.contains("\"chart\"").shouldBeTrue() }
    }
})
