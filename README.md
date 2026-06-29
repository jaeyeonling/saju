# bazi — 한국 사주 만세력 라이브러리

생년월일시를 **사주팔자 8글자**(연·월·일·시 4기둥)로 변환하고, 대운·세운과 해석(십성·합충·신강신약·격국)을 제공하는 Kotlin/Java 라이브러리.

> 중국식 만세력과 달리 **한국 사주 특유의 시간 보정**(진태양시·표준시 역사·서머타임·자시 학파)을 1급으로 다룬다.

## 빠른 시작

생년월일시 한 줄 → **사주 8글자 + 해석 + 대운**. 한국 보정(진태양시·표준시 연혁·서머타임)은 자동.

**① 설치 없이 바로 실행** (데모 CLI):

```bash
./gradlew :saju-cli:run --args="1990 3 15 7 0"   # 연 월 일 시 분
```

```text
════════ 사주 만세력 ════════
입력  : 1990-03-15 07:00
진태양시 보정: -41.2분

        시주   일주   월주   연주
천간    정(편인)   기(일간)   기(비견)   경(상관)
지지    묘     묘     묘     오
일간(나) : 기 [토(土)/음(陰)]      ← 해석의 기준 '나'

───── 해석 ─────
신강신약 : 중화 (지원율 47%)
용신     : 화(火) (억부)
격국     : 편관격
공망     : 신유
십이운성 : 연 건록 · 월 병 · 일 병 · 시 병

───── 대운 (남성) ─────
7세 경진  17세 신사  27세 임오  37세 계미  47세 갑신  …
```

**② 코드로** (핵심 3줄):

```kotlin
import io.github.jaeyeonling.saju.korea.KoreanSaju
import io.github.jaeyeonling.saju.interpretation.Interpretation

val chart  = KoreanSaju.fromCivilTime(1990, 3, 15, 7, 0) // 생년월일시 → 사주판
val report = Interpretation.of(chart)                     // 해석 한 번에

chart.dayMaster                     // 일간(나) → Cheongan.GI         (.koreanName "기", .hanja "己")
report.strength.verdict.koreanName  // 신강신약 → "중화"             (enum SinStrengthVerdict.JUNGHWA)
report.yongsin.yongsin.koreanName   // 용신     → "화"               (enum Ohaeng.HWA)
```

> 모든 도메인/해석 enum 은 `.koreanName`·`.hanja` 표시 라벨을 제공한다 — 한글 매핑을 직접 짤 필요가 없다.

> 입력은 **법정시(시계 시각)** 그대로. 진태양시 변환은 내부에서 처리한다.
> 설치: 좌표 `io.github.jaeyeonling:saju-core:0.1.0`(+`saju-korea`·`saju-interpretation`). `maven-publish` 설정 완료 — Central 릴리스 전까지는 `./gradlew publishToMavenLocal` 후 `mavenLocal()` 의존으로, 또는 소스 빌드(`./gradlew build`)로 사용.

## 사주(四柱)란 무엇인가

> 사주를 모르고 코드를 읽기 시작했다면 이 절부터. 기초 용어(천간·지지·오행)는 `saju-core/.../domain/`, 해석 용어(십성·용신·격국)는 `saju-interpretation/` 의 enum KDoc에 정의가 있다.

사주는 태어난 **연·월·일·시** 네 시점을 각각 한 기둥(柱)으로 삼아 그 사람의 기운을 8글자로 적은 것이다. "사주팔자(四柱八字)"의 팔자가 곧 이 8글자다.

```
        시주    일주    월주    연주      ← 4기둥(四柱)
천간    丁      己      己      庚        ← 천간(天干) 10: 갑을병정무기경신임계  (Cheongan)
지지    卯      卯      卯      午        ← 지지(地支) 12=12띠: 자축인묘진사오미신유술해  (Jiji)
```

- **기둥 1개 = 천간 1글자 + 지지 1글자.** 천간(10)과 지지(12)의 짝이 60갑자(`GanZhi`)로 순환한다.
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

## 설계 원칙

- **자체 천문 엔진** — 24절기·삭망을 VSOP87/Meeus 기반으로 직접 계산(런타임 의존성 0). 회귀 검증은 체크인된 골든 벡터(외부 라이브러리 tyme4j 출력 동결), 절대 정확도는 Meeus 원전 단위 테스트(태양 황경 ~1″)로 확인.
- **java.time-free 코어** — `saju-core`는 순수 계산(불변 도메인 + double 율리우스일). 시간대 보정은 `saju-korea` 가 전담. 이 격리가 "베이징 +8h 하드코딩" 오염을 구조적으로 차단한다.
- **Java·Kotlin 친화** — 불변 `data class` + `@JvmStatic`/`@JvmOverloads`/`@JvmField`.

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
chart.year.ganZhi.koreanName // 연주 = "경오"               (.hanja "庚午")
chart.pillars()              // [연, 월, 일, 시] 네 기둥

// ── 2. 해석 한 번에 ──
val report = Interpretation.of(chart)
report.strength.verdict.koreanName // 신강신약 = "중화"      (enum SinStrengthVerdict.JUNGHWA)
report.yongsin.yongsin.koreanName  // 용신 = "화"            (enum Ohaeng.HWA)
report.gyeokguk.type.koreanName    // 격국 = "편관격"
report.gongmang                    // 공망 = (Jiji.SHIN, Jiji.YU)
report.sibiUnseong                 // 십이운성 Map<PillarPosition, SibiUnseong>

// ── 3. 대운 (10년 단위 인생 흐름) ──
val daeun = KoreanSaju.daeun(1990, 3, 15, 7, 0, isMale = true)
daeun.first().startAge    // 7 (세부터 시작)
daeun.first().ganZhi      // 경진

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

한 번의 호출로 아래 7개 해석이 한꺼번에 나온다:

| 필드 | 타입 | 의미 | 접근 예 |
|------|------|------|---------|
| `strength` | `SinStrength` | **신강신약** — 일간이 강한가 약한가 | `.verdict`(극신강~극신약) · `.supportRatio`(0~1, 0.45~0.55=중화·≥0.55 신강·<0.45 신약) |
| `yongsin` | `YongsinResult` | **용신** — 균형을 돕는 오행 | `.yongsin`(오행) · `.method`(억부/조후) |
| `gyeokguk` | `GyeokgukResult` | **격국** — 사주의 짜임새 유형 | `.type`(편관격 등) · `.basis` |
| `gongmang` | `Pair<Jiji, Jiji>` | **공망** — 작용력이 빈 지지 2개 | `.first` · `.second` |
| `hapChung` | `List<HapChungRelation>` | **합충** — 글자끼리의 결합·충돌 | `.size`, `filterIsInstance` |
| `ohaeng` | `OhaengDistribution` | **오행 분포** — 8글자의 오행 개수 | `.count(목)` · `.dominant()` |
| `sibiUnseong` | `Map<PillarPosition, SibiUnseong>` | **십이운성** — 기둥별 기운 단계 | `.getValue(YEAR)` |

> 전체 공개 API(진입점·타입·enum·학파 스위치)는 **[API 치트시트](docs/api.md)** 한 장으로.

## 빌드

```bash
./gradlew build      # 컴파일 + 테스트 + 아키텍처 검증(java.time-free)
./gradlew :saju-cli:run   # 데모: 생년월일시 → 8글자 + 해석 + 대운
```

요구사항: JDK 17+ (toolchain 자동 프로비저닝). 로컬 검증은 JDK 21에서 수행.

## 검증 (골든 회귀)

자체 천문 엔진(VSOP87/Meeus)을 **외부 만세력 라이브러리 [tyme4j](https://github.com/6tail/tyme4j) 출력에서 추출해 체크인한 골든 벡터**(`*/src/test/resources/golden/`)와 대조해 회귀 검증한다. tyme4j 도 VSOP87 계열이라 자체 엔진과 수학 계보를 일부 공유하므로, 이 골든은 *두 구현의 분 단위 합치*를 박제한 것이다 — 정설 천문학에 대한 **절대 앵커는 Meeus 원전 단위 테스트**(태양 황경 ~1″)가 맡는다. 골든은 런타임·테스트 의존성 없이 회귀를 막는다:

| 영역 | 골든 결과 |
|------|-----------|
| 절기 절입(1900~2050, 3624건) | 최대 **26초** 차 (회귀 게이트 35초) |
| 4기둥(1900~2050, 609표본) | 연·월·일·시주 **완전 일치**(베이징 기준 — 한국 보정 경로는 `KoreanCorrectionTest` 가 별도 검증) |
| 천간·지지·60갑자 속성 | 전수 일치 |
| 십성 100조합 / 십이운성 120조합 / 지장간 | 일치 |
| 대운 방향·간지 | 일치 (대운수는 3일=1세 기준 ±1; 양력새해 교차식 시작나이 정의와는 달라 최대 2세 차) |
| 음↔양력 변환(1900~2050) | 대부분 정확, 자정경계 ±1일 극소수* |

*삭이 자정 ±5분에 드는 극경계는 자체 엔진과 tyme4j(둘 다 VSOP87 계열)의 한계로 중국 농력과 ±1일 갈릴 수 있다. 한국 기준(KASI, KST)은 중국(베이징)과 의도적으로 다를 수 있다(예: 2017 윤달).

**지원 입력 범위는 1900~2100**이다. 위 골든 검증 구간(절기·4기둥 1900~2050, 음력 1900~2099) 밖의 입력도 가드를 통과하며 천문 다항식 외삽으로 분 단위 정밀도를 기대하나, 골든 대조가 없는 구간임을 밝혀 둔다.

신강신약·용신·격국은 정답 데이터셋이 없어 결정론성만 보장하며(정확도 보장 아님), 가중치·규칙은 KDoc에 근거를 명시한다.

## 로드맵

- [x] **P0** 멀티모듈 스캐폴딩 + java.time-free 강제 + CI
- [x] **P1** 천문 엔진(절기·황경·ΔT·균시차, ~1″ 정밀도)
- [x] **P2** 기본 도메인(천간·지지·60갑자, 인덱스 산술)
- [x] **P3** 4기둥 도출(연·월·일·시주)
- [x] **P4** 한국 시간 보정(진태양시·표준시 연혁·서머타임)
- [x] **P5** 대운·세운
- [x] **P6** 해석: 십성·합충·공망·오행분포
- [x] **P7** 학파 전략: 십이운성·신강신약·용신·격국 + 자시
- [x] **P8** Java interop + CLI 데모 + 문서
- [x] 음력 입력 지원(삭·무중치윤·음양력 변환, 한국/중국 기준)
- [ ] Maven Central 릴리스 (`maven-publish`·POM·sources/javadoc jar 설정 완료 — GPG 서명·태그·Sonatype 자격증명 연결 대기)

## 라이선스

[MIT](LICENSE). 천문 알고리즘은 Jean Meeus _Astronomical Algorithms_ 및 공개 VSOP87 계수를 참조한다. 골든 테스트 벡터(`*/src/test/resources/golden/*.csv`)는 [tyme4j](https://github.com/6tail/tyme4j)(MIT) 출력을 동결한 테스트 픽스처로, 배포 산출물에는 포함되지 않는다. 자세한 제3자 고지는 [LICENSE](LICENSE) 참조.
