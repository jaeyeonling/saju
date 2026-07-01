# Changelog

이 프로젝트의 주요 변경사항을 기록한다. 형식은 [Keep a Changelog](https://keepachangelog.com/ko/1.1.0/) 를,
버전은 [Semantic Versioning](https://semver.org/lang/ko/) 을 따른다.

## [Unreleased]

## [0.1.0] - 2026-07-01

### Added

- **천문 엔진** (`saju-core`) — VSOP87/Meeus 기반 자체 계산: 태양·달 위치, 24절기 절입, 삭(朔),
  율리우스일 변환, ΔT·균시차·장동·황도경사. 런타임 의존성 0.
- **4기둥·대운·세운 도출** — 절기 기반 연/월 경계, 자시 정책, 대운수(3일=1세), 음력 변환(무중치윤).
  지원 입력 1900~2100 (골든 검증 구간 1900~2050).
- **한국 시간 보정** (`saju-korea`) — 진태양시·표준시 연혁·서머타임·자시. 권장 진입점 `KoreanSaju`.
- **해석** (`saju-interpretation`) — 십성·지장간·신살(6종)·합충·공망·오행분포(표면 + 지장간 가중)·
  신강신약·용신·격국·십이운성. 진입점 `Interpretation.of`.
- **그룹 사주** (`saju-group`) — 여러 명 합성: 오행 균형·십성 역할·멤버간 합충·세운/대운 타임라인.
- **JSON 직렬화** (`saju-serialization`, opt-in) — kotlinx.serialization DTO + `toJson()`.
- **CLI** (`saju-cli`) — 데모/수동 검증. `--json`·`--female`·`--seun` 플래그, `group` 서브커맨드.
- Java interop (`@JvmStatic`/`@JvmOverloads`/`@JvmField`), 무상태·스레드세이프 설계.
- java.time-free 아키텍처 강제(Konsist), 커버리지 게이트, 공개 API 검증(BCV),
  CI(JDK 17·21), CodeQL, Dependabot.

[Unreleased]: https://github.com/jaeyeonling/saju/compare/v0.1.0...HEAD
[0.1.0]: https://github.com/jaeyeonling/saju/releases/tag/v0.1.0
