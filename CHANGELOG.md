# Changelog

이 프로젝트의 주요 변경사항을 기록한다. 형식은 [Keep a Changelog](https://keepachangelog.com/ko/1.1.0/) 를,
버전은 [Semantic Versioning](https://semver.org/lang/ko/) 을 따른다.

## [Unreleased]

### Changed

- **성별(`Gender`)을 도메인 개념으로 승격** — `Gender` enum 이 `saju-group` 에서
  `saju-core`(`io.github.jaeyeonling.saju.domain.Gender`)로 이동했다. `saju-group` 의 `Gender` 는
  `typealias` 로 소스 호환을 유지하지만, 바이너리로는 `group/Gender` 클래스가 사라지고 `GroupMember` 의
  성별 시그니처가 `domain.Gender` 로 바뀌므로 **재컴파일이 필요하다(binary breaking)**.
- **`Gender.fromCode` 를 strict 파싱으로 변경** — `"M"`/`"F"`(대소문자 무관)·`"남"`/`"여"` 만 받고,
  그 외 입력은 `IllegalArgumentException` 을 던진다. 기존에는 `"F"` 외 모든 입력(오타 포함)을 조용히
  남성으로 처리해 **대운 방향이 뒤집힌 채 계산될 수 있었다**. CLI 의 `members.json` 에서 잘못된
  `gender` 값은 이제 usage 오류(exit 2)로 즉시 실패한다.

### Added

- **`daeun` 의 `Gender` 오버로드** — `KoreanSaju.daeun(…, gender: Gender, …)` /
  `Saju.daeun(…, gender: Gender, …)`. 기존 `isMale: Boolean` 오버로드는 유지된다.
  성별이 **대운(大運) 방향(순행·역행) 전용 입력**(원국 무관)임을 시그니처로 드러낸다.
- **`DaeunDto` · `DaeunSeriesDto`** (`saju-serialization`) — 대운 시퀀스를 성별과 함께 JSON 으로
  직렬화한다(`List<Daeun>.toDaeunJson(gender)` / `.toDaeunSeriesDto(gender)`).
  JSON 부터 만드는 소비자도 "성별→대운" 의존을 스키마에서 만난다.
- **문서** — README 에 "입력이 쓰이는 곳" 표(성별=대운 방향 전용·원국 무관) 신설,
  진입점 프레이밍 교정(대운을 1급 진입점으로), 대운 예시 남/여 대비.
  `docs/api.md` 에 `Gender` enum·`daeun` 성별 역할을 명시.

## [0.1.0] - 2026-07-02

### Added

- **최초 공개 릴리스** — Maven Central 에 배포: `io.github.jaeyeonling:{saju-core, saju-korea, saju-interpretation, saju-group, saju-serialization}:0.1.0`.
  `mavenCentral()` 저장소에서 바로 의존할 수 있다. `saju-cli` 는 데모 앱이라 미배포.
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
