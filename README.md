# saju — 한국 사주 만세력 라이브러리

[![License: MIT](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.1-7F52FF?logo=kotlin&logoColor=white)](https://kotlinlang.org)
[![JDK](https://img.shields.io/badge/JDK-17%2B-orange.svg)](https://adoptium.net)
[![CI](https://github.com/jaeyeonling/saju/actions/workflows/ci.yml/badge.svg)](https://github.com/jaeyeonling/saju/actions/workflows/ci.yml)
[![CodeQL](https://github.com/jaeyeonling/saju/actions/workflows/codeql.yml/badge.svg)](https://github.com/jaeyeonling/saju/actions/workflows/codeql.yml)

생년월일시를 **사주팔자 8글자**로 변환하고, 그 위에 운세 흐름과 해석을 얹어 주는 Kotlin/Java 라이브러리.
**사주 용어를 몰라도 입력만 넣으면 구조화된 결과(객체·JSON)를 받는다.**

> 중국식 만세력과 달리 **한국 사주 특유의 시간 보정**(진태양시·표준시 역사·서머타임·자시)을 1급으로 다룬다.

처음 보는 용어가 많다면 [사주란 무엇인가](#사주四柱란-무엇인가)부터. 한 줄로: 사주는 태어난 **연·월·일·시** 네 시점을 8글자로 적은 것이고, 그중 **일간(日干)** 이 해석의 기준 '나'다.

## 빠른 시작

**법정시(시계 시각) 그대로 넣으면 끝.** 진태양시·표준시 연혁·서머타임 같은 한국 보정은 라이브러리가 자동으로 처리한다.

**① 설치 없이 바로 실행** (데모 CLI):

```bash
./gradlew :saju-cli:run --args="1990 3 15 7 0"   # 연 월 일 시 분
```

`1990-03-15 07:00` (서울) → **사주 8글자**:

```text
        시주   일주   월주   연주
천간     정     기     기     경
지지     묘     묘     묘     오
                └ 일간(나) = 기(己)
```

<details>
<summary>해석·신살·세운·대운까지 한 번에 보기 (펼치기)</summary>

```text
════════ 사주 만세력 ════════
입력  : 1990-03-15 07:00
진태양시 보정: -41.2분

        시주       일주       월주       연주
천간    정(편인)   기(일간)   기(비견)   경(상관)
지지    묘(편관)   묘(편관)   묘(편관)   오(편인)   ← 지지도 지장간 본기 십성
지장간  을         을         을         정기       ← 지지가 품은 천간
일간(나) : 기 [토(土)/음(陰)]      ← 해석의 기준 '나'

───── 해석 ─────
신강신약 : 중화 (지원율 47%)
         └ 돕는 세력(비겁·인성) 4.4 / 전체 9.4 = 47% · 월령 2배·지장간 정기1·중기0.4·여기0.2 가중
용신     : 화(火) (억부)
         └ 중화(47%) · 일간 토(土) 생조 → 인성 화(火)   ← 용신 산출 근거(LLM 검증용)
격국     : 편관격
         └ 월지(묘) 본기 을 → 편관
공망     : 신유
십이운성 : 연 건록 · 월 병 · 일 병 · 시 병
신살     : 연 - · 월 - · 일 - · 시 -        ← 도화·역마·화개·천을귀인·양인·문창 6종
오행(표면)   : 목3 화2 토2 금1 수0
오행(지장간) : 목6 화3 토3 금1 수0           ← 지장간까지 가중(숨은 오행)
합충     : (있으면) 천간합(갑-기→토) · 육충(자-오) 식으로 상세

───── 세운 (2026) ─────
2026년 병오(丙午) · 연간 십성 정인          ← 올해 자동 (--seun=YYYY 로 지정)

───── 대운 (남성) ─────
7세 경진  17세 신사  27세 임오  37세 계미  47세 갑신  …
```
</details>

**② 코드로:**

```kotlin
import io.github.jaeyeonling.saju.korea.KoreanSaju
import io.github.jaeyeonling.saju.interpretation.Interpretation

val chart  = KoreanSaju.fromCivilTime(1990, 3, 15, 7, 0) // 생년월일시 → 사주판
val report = Interpretation.of(chart)                     // 해석 한 번에

chart.dayMaster.koreanName          // 일간(나) → "기"
report.strength.verdict.koreanName  // 신강신약 → "중화"
report.yongsin.yongsin.koreanName   // 용신     → "화"
```

> 모든 도메인·해석 enum 은 `.koreanName`·`.hanja` 표시 라벨을 제공한다 — 한글 매핑을 직접 짤 필요가 없다.

## 설치

아직 Maven Central 미배포(`0.1.0`)다. 로컬 Maven 저장소에 게시해 쓴다:

```bash
./gradlew publishToMavenLocal   # 1. 로컬에 0.1.0 게시
```

```kotlin
// 2. build.gradle.kts
repositories {
    mavenLocal()
    mavenCentral()
}

dependencies {
    implementation("io.github.jaeyeonling:saju-core:0.1.0")           // 천문 엔진 + 도메인 + 4기둥
    implementation("io.github.jaeyeonling:saju-korea:0.1.0")          // 한국 시간 보정 (권장 진입점)
    implementation("io.github.jaeyeonling:saju-interpretation:0.1.0") // 십성·신강신약·용신·격국
    implementation("io.github.jaeyeonling:saju-serialization:0.1.0")  // (opt-in) JSON 직렬화
}
```

> 의존성 없이 소스 빌드만으로도 쓸 수 있다: `./gradlew build`.
>
> 저장소명 `bazi`는 명리(사주)의 통칭 八字의 병음 별칭이다. 라이브러리·패키지·아티팩트 정체성은 모두 **`saju`**(한국어)로 통일한다.

## 사주(四柱)란 무엇인가

> 사주를 모르고 코드를 읽기 시작했다면 이 절부터. 기초 용어(천간·지지·오행)는 `saju-core/.../domain/`, 해석 용어(십성·용신·격국)는 `saju-interpretation/` 의 enum KDoc에 정의가 있다.

사주는 태어난 **연·월·일·시** 네 시점을 각각 한 기둥(柱)으로 삼아 그 사람의 기운을 8글자로 적은 것이다. "사주팔자(四柱八字)"의 팔자가 곧 이 8글자다.

```
        시주    일주    월주    연주      ← 4기둥(四柱)
천간    丁      己      己      庚        ← 천간(天干) 10: 갑을병정무기경신임계  (Cheongan)
지지    卯      卯      卯      午        ← 지지(地支) 12=12띠: 자축인묘진사오미신유술해  (Jiji)
```

- **기둥 1개 = 천간 1글자 + 지지 1글자.** 천간(10)과 지지(12)의 짝이 60갑자(`Ganji`)로 순환한다.
- **일간(日干)** = 일주의 천간 = 해석의 기준 '**나**'. 위 예에서 `己`(기, 토).
- 모든 글자는 **오행**(五行: 목·화·토·금·수, `Ohaeng`)과 **음양**(陰陽, `Eumyang`) 라벨을 가지며, 글자끼리 상생·상극·합·충 관계를 맺는다.

개념 흐름 — 코드의 레이어 순서와 같다:

```
생년월일시 ──천문 보정──▶ 4기둥 8글자 ──일간 기준 관계──▶ 해석
 (astronomy·korea)        (derivation)        (interpretation)
                                       십성·신강신약·용신·격국·십이운성
```

- **십성**(十星, `SipSeong`) — 일간 대비 다른 글자의 역할 10종(비견=경쟁자, 정재=재물, 정관=직장…).
- **신강신약**(身強身弱) — 일간이 강한가 약한가. **용신**(用神) — 균형을 돕는 오행. **격국**(格局) — 사주의 짜임새 유형.
- **대운**(大運) — 10년 단위로 바뀌는 인생의 큰 흐름. **세운**(歲運) — 해마다 바뀌는 그 해의 운.

## 모듈

| 모듈 | 역할 | java.time |
|------|------|-----------|
| `saju-core` | 천문 엔진(태양·달·절기·삭) + 도메인(천간·지지·60갑자) + 4기둥/대운 + 음력 변환(무중치윤) | ❌ 금지(Konsist 강제) |
| `saju-korea` | 한국 시간 보정(진태양시·표준시 연혁·서머타임·자시) | ✅ 사용 |
| `saju-interpretation` | 십성·오행·합충·신살·신강신약·용신·격국 | ❌ 금지 |
| `saju-serialization` | (opt-in) JSON 직렬화 — kotlinx.serialization DTO + `toJson()`. 이 모듈을 의존할 때만 직렬화 런타임이 따라온다 | ✅ 사용 |
| `saju-cli` | 데모/수동 검증 CLI | ✅ 사용 |

핵심 데이터 흐름:

```
astronomy(순수 UT JD, 타임존 무지)
   → korea(보정: 서머타임 → 표준시 자오선 → 진태양시 → 자시/절기)
   → derivation(연도 / 절기 idx / JDN / 시지 → 4기둥)
```

## 문서 지도

| 무엇이 궁금한가 | 문서 |
|----------------|------|
| **전체 공개 API 한 장** (진입점·타입·enum·학파 스위치) | [docs/api.md](docs/api.md) |
| **한국 시간 보정** (진태양시·표준시 연혁·서머타임) | [korea/](saju-korea/src/main/kotlin/io/github/jaeyeonling/saju/korea/README.md) |
| **기초 용어** (천간·지지·오행·60갑자·지장간) | [domain/](saju-core/src/main/kotlin/io/github/jaeyeonling/saju/domain/README.md) |
| **해석 용어** (십성·신강신약·용신·격국·십이운성) | [interpretation/](saju-interpretation/src/main/kotlin/io/github/jaeyeonling/saju/interpretation/README.md) |
| **천문 엔진** (절기·삭을 왜·어떻게 계산하나) | [astronomy/](saju-core/src/main/kotlin/io/github/jaeyeonling/saju/astronomy/README.md) |

## 사용 예시

진입점은 단 둘 — **`KoreanSaju`**(생년월일시 → 사주판)와 **`Interpretation.of`**(사주판 → 해석).

### Kotlin
```kotlin
import io.github.jaeyeonling.saju.korea.KoreanSaju
import io.github.jaeyeonling.saju.korea.Birthplace
import io.github.jaeyeonling.saju.interpretation.Interpretation
import io.github.jaeyeonling.saju.lunar.LunarConverter

// ── 1. 사주판 도출 (법정시 입력 → 한국 보정 자동) ──
val chart = KoreanSaju.fromCivilTime(1990, 3, 15, 7, 0)                       // 서울 기본
val busan = KoreanSaju.fromCivilTime(1990, 3, 15, 7, 0, Birthplace.BUSAN.longitudeDeg)
chart.dayMaster              // 일간(나) = Cheongan.GI       (.koreanName "기", .hanja "己")
chart.year.ganji.koreanName  // 연주 = "경오"               (.hanja "庚午")
chart.pillars()              // [연, 월, 일, 시] 네 기둥

// ── 2. 해석 한 번에 ──
val report = Interpretation.of(chart)
report.strength.verdict.koreanName // 신강신약 = "중화"      (enum SinStrengthVerdict.JUNGHWA)
report.yongsin.yongsin.koreanName  // 용신 = "화"            (enum Ohaeng.HWA)
report.gyeokguk.type.koreanName    // 격국 = "편관격"
report.gongmang                    // 공망 = (Jiji.SIN, Jiji.YU)
report.sibiUnseong                 // 십이운성 Map<PillarPosition, SibiUnseong>

// ── 3. 대운 (10년 단위 인생 흐름) ──
val daeun = KoreanSaju.daeun(1990, 3, 15, 7, 0, isMale = true)
daeun.first().startAge    // 7 (세부터 시작)
daeun.first().ganji       // 경진

// ── 4. 음력 생일 입력 / 음↔양 변환 ──
val lunarChart = KoreanSaju.fromLunarCivilTime(1990, 2, 19, isLeapMonth = false, hour = 7, minute = 0)
val solar = LunarConverter.toSolar(2023, 1, 1)   // 음→양: 설날 → 2023-01-22
val lunar = LunarConverter.toLunar(2023, 1, 22)  // 양→음
```

### JSON 직렬화 (`saju-serialization` 모듈, opt-in)

REST 응답으로 바로 내보낼 수 있다. 한글 라벨(`name`)과 영문 enum(`verdict`)을 함께 담고,
`Pair`·`Map`·sealed `합충`을 깔끔한 JSON 구조로 평탄화한다. **이 모듈을 의존할 때만** kotlinx.serialization 런타임이 따라온다 — 코어 3모듈은 여전히 런타임 의존성 0.

```kotlin
import io.github.jaeyeonling.saju.serialization.toJson

val chartJson  = chart.toJson()                    // 사주판 → JSON (일주 name="기묘", hanja="己卯" …)
val reportJson = Interpretation.of(chart).toJson() // 해석 → JSON (strength.verdictKorean="중화" …)
```

### Java
```java
import io.github.jaeyeonling.saju.korea.KoreanSaju;
import io.github.jaeyeonling.saju.domain.SajuChart;
import io.github.jaeyeonling.saju.interpretation.Interpretation;
import io.github.jaeyeonling.saju.interpretation.InterpretationReport;

SajuChart chart = KoreanSaju.fromCivilTime(1990, 3, 15, 7, 0);  // @JvmStatic/@JvmOverloads
InterpretationReport report = Interpretation.of(chart);
report.getStrength().getVerdict().getKoreanName();   // 신강신약 = "중화"
report.getYongsin().getYongsin().getKoreanName();    // 용신 = "화"
```

### `Interpretation.of(chart)` 가 담는 7가지

한 번의 호출로 **신강신약·용신·격국·공망·합충·오행분포·십이운성** 7종이 한꺼번에 나온다.
각 필드의 타입·접근법은 **[API 치트시트](docs/api.md)** 에 한 장으로 정리돼 있다.

> ⚠️ **해석의 정확도 한계** — 신강신약·용신·격국은 정답 데이터셋이 없어 **결정론성만 보장**한다(정확도 보장 아님). 가중치·규칙의 근거는 각 KDoc에 명시돼 있다. 천문·도출(절기·4기둥)이 골든+Meeus로 검증되는 것과는 보증 수준이 다르다.

> **식별자 규칙** — enum 상수·타입명은 한국어 발음을 **국어 개정 로마자**(Revised Romanization)로 옮긴 음차다(예: `Cheongan`=천간, `SipSeong`=십성, `EOKBU`=억부, `GEOLLOK`=건록). 같은 음절·한자어는 항상 같은 철자를 쓴다(ㅓ=eo, 종성 ㅂ=p, ㄴ+ㄹ 유음화=ll). 의미가 필요하면 `.koreanName`/`.hanja` 필드나 KDoc 영문 정의(예: `SipSeong` = Ten Gods)를 보라.

## 빌드

```bash
./gradlew build           # 컴파일 + 테스트 + 아키텍처 검증(java.time-free) + 커버리지 게이트
./gradlew :saju-cli:run   # 데모: 생년월일시 → 8글자 + 해석 + 대운
```

- 요구사항: JDK 17+ (toolchain 자동 프로비저닝). 로컬 검증은 JDK 21에서 수행.
- 포맷은 ktlint — 커밋 전 `./gradlew ktlintFormat`, 검증은 `./gradlew ktlintCheck`(로컬 `build`에 포함).

## 설계 원칙

- **자체 천문 엔진** — 24절기·삭망을 VSOP87/Meeus 기반으로 직접 계산(런타임 의존성 0).
- **java.time-free 코어** — `saju-core`는 순수 계산(불변 도메인 + double 율리우스일), 시간대 보정은 `saju-korea`가 전담. 이 격리가 "베이징 +8h 하드코딩" 오염을 구조적으로 차단한다.
- **Java·Kotlin 친화** — 불변 `data class` + `@JvmStatic`/`@JvmOverloads`/`@JvmField`.
- **무상태·스레드세이프** — 모든 진입점이 불변 데이터만 반환하고 공유 가변 상태가 없어, 웹 동시요청에서 락·인스턴스 풀 없이 호출할 수 있다.

## 검증

자체 천문 엔진(VSOP87/Meeus)을 **이중 검증**한다 — 외부 만세력 [tyme4j](https://github.com/6tail/tyme4j) 출력을 동결한 골든 벡터(`*/src/test/resources/golden/`)로 회귀를 막고, 천문 정확도의 절대 앵커는 Meeus 원전 단위 테스트(태양 황경 ~1″)가 맡는다. 골든은 런타임·테스트 의존성 없이 회귀를 잡는다.

| 영역 | 골든 결과 |
|------|-----------|
| 절기 절입(1900~2050, 3624건) | 최대 **26초** 차 (회귀 게이트 35초) |
| 4기둥(1900~2050, 609표본) | 연·월·일·시주 **완전 일치**(베이징 기준 — 한국 보정 경로는 `KoreanCorrectionTest` 가 별도 검증) |
| 천간·지지·60갑자 속성 | 전수 일치 |
| 십성 100조합 / 십이운성 120조합 / 지장간 / 신살 6종(일간·일지 룩업) | 일치(동결) |
| 대운 방향·간지 | 일치 (대운수는 3일=1세 기준 ±1) |
| 음↔양력 변환(1900~2050) | 대부분 정확, 자정경계 ±1일 극소수\* |

\* 삭이 자정 ±5분 극경계에 들면 자체 엔진·tyme4j(둘 다 VSOP87 계열) 한계로 ±1일 갈릴 수 있다. 한국 기준(KASI·KST)은 중국(베이징)과 의도적으로 다를 수 있다(예: 2017 윤달).

**지원 입력 범위는 1900~2100.** 골든 검증 구간(절기·4기둥 1900~2050, 음력 1900~2099) 밖의 입력도 가드를 통과하며 천문 다항식 외삽으로 분 단위 정밀도를 기대하나, 골든 대조가 없는 구간이다.

> CI는 JDK 17·21 매트릭스에서 빌드+테스트+아키텍처 검증(java.time-free)+커버리지 게이트(LINE 80%/BRANCH 60%)를 돌린다. CodeQL 보안 스캔은 public 전환 시 자동 활성화된다.

## 로드맵

- [ ] Maven Central 릴리스 (예정)

<details>
<summary>완료된 마일스톤 (v0.1)</summary>

코어(천문 엔진·24절기·삭·4기둥·대운/세운) · 한국 시간 보정(진태양시·표준시 연혁·서머타임·자시) · 해석(십성·지장간·신살·합충·공망·오행분포(표면+지장간 가중)·신강신약·용신·격국·십이운성) · 음력 입력(삭·무중치윤·음↔양력 변환, 한국/중국 기준) · Java interop · CLI 데모 · java.time-free 아키텍처 강제 + CI.
</details>

## 라이선스

[MIT](LICENSE). 천문 알고리즘은 Jean Meeus _Astronomical Algorithms_ 및 공개 VSOP87 계수를 참조한다. 골든 테스트 벡터(`*/src/test/resources/golden/*.csv`)는 [tyme4j](https://github.com/6tail/tyme4j)(MIT) 출력을 동결한 테스트 픽스처로, 배포 산출물에는 포함되지 않는다. 제3자 고지 전문은 [THIRD-PARTY-NOTICES.md](THIRD-PARTY-NOTICES.md) 참조.
