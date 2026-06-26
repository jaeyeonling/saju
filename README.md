# bazi — 한국 사주 만세력 라이브러리

생년월일시를 **사주팔자 8글자**(연·월·일·시 4기둥)로 변환하고, 대운·세운과 해석(십성·합충·신강신약·격국)을 제공하는 Kotlin/Java 라이브러리.

> 중국식 만세력과 달리 **한국 사주 특유의 시간 보정**(진태양시·표준시 역사·서머타임·자시 학파)을 1급으로 다룬다.

## 설계 원칙

- **자체 천문 엔진** — 24절기·삭망을 VSOP87/Meeus 기반으로 직접 계산(런타임 의존성 0). `tyme4j`는 검증(골든 데이터)용으로만 사용.
- **java.time-free 코어** — `saju-core`는 순수 계산(불변 도메인 + double 율리우스일). 시간대 보정은 `saju-korea` 가 전담. 이 격리가 "베이징 +8h 하드코딩" 오염을 구조적으로 차단한다.
- **Java·Kotlin 친화** — 불변 `data class` + `@JvmStatic`/`@JvmOverloads`/`@JvmField`.

## 모듈

| 모듈 | 역할 | java.time |
|------|------|-----------|
| `saju-core` | 천문 엔진(태양·달·절기·삭) + 도메인(천간·지지·60갑자) + 4기둥/대운 + 음력 변환(무중치윤) | ❌ 금지(Konsist 강제) |
| `saju-korea` | 한국 시간 보정(진태양시·표준시 연혁·서머타임·자시) | ✅ 사용 |
| `saju-interpretation` | 십성·오행·합충·신살·신강신약·용신·격국 | ❌ 금지 |
| `saju-cli` | 데모/수동 검증 CLI | ✅ 사용 |

핵심 데이터 흐름:

```
astronomy(순수 UT JD, 타임존 무지)
   → korea(보정: 서머타임 → 표준시 자오선 → 진태양시 → 자시/절기)
   → derivation(연도 / 절기 idx / JDN / 시지 → 4기둥)
```

## 사용 예시

### Kotlin
```kotlin
import io.github.jaeyeonling.saju.korea.KoreanSaju
import io.github.jaeyeonling.saju.korea.Birthplace
import io.github.jaeyeonling.saju.interpretation.InterpretationContext

// 법정시 + 출생지 → 사주판 (진태양시·표준시 연혁·서머타임 자동 보정)
val chart = KoreanSaju.fromCivilTime(1990, 3, 15, 7, 0, Birthplace.SEOUL.longitudeDeg)
val dayMaster = chart.dayMaster              // 일간(나)

// 해석 (학파 전략 주입 가능, 기본 = 한국 표준)
val ctx = InterpretationContext.DEFAULT
val strength = ctx.sinStrength.evaluate(chart)   // 신강신약
val gyeokguk = ctx.gyeokguk.classify(chart)      // 격국

// 대운
val daeun = KoreanSaju.daeun(1990, 3, 15, 7, 0, isMale = true)

// 음력 생일 입력 (KASI 기준 양력 변환 후 동일 파이프라인)
val lunarChart = KoreanSaju.fromLunarCivilTime(1990, 2, 19, isLeapMonth = false, hour = 7, minute = 0)
val solar = LunarConverter.toSolar(2023, 1, 1)        // 음 → 양 (설날 → 2023-01-22)
val lunar = LunarConverter.toLunar(2023, 1, 22)       // 양 → 음
```

### Java
```java
SajuChart chart = Saju.fromLocalDateTime(1990, 3, 15, 7, 0, 9.0); // @JvmStatic/@JvmOverloads
Cheongan dayMaster = chart.getDayMaster();
List<GanZhi> sixtyCycle = GanZhi.ALL;          // @JvmField
```

## 빌드

```bash
./gradlew build      # 컴파일 + 테스트 + 아키텍처 검증(java.time-free)
./gradlew :saju-cli:run   # 데모: 생년월일시 → 8글자 + 해석 + 대운
```

요구사항: JDK 17+ (toolchain 자동 프로비저닝). 로컬 검증은 JDK 21에서 수행.

## 검증 (tyme4j 골든)

자체 천문 엔진(VSOP87/Meeus)을 **독립 알고리즘(sxwnl 기반 tyme4j)** 과 대조해 정확성을 교차 증명한다:

| 영역 | 골든 결과 |
|------|-----------|
| 절기 절입(1900~2050, 3624건) | 최대 **26초** 차 |
| 4기둥(매년 4시점, 604표본) | 연·월·일·시주 **완전 일치** |
| 천간·지지·60갑자 속성 | 전수 일치 |
| 십성 100조합 / 십이운성 120조합 / 지장간 | 일치 |
| 대운 방향·간지 | 일치 (대운수는 3일=1세 기준 ±1; tyme4j `startAge`의 양력새해 교차식과는 정의가 달라 최대 2세 차) |
| 음↔양력 변환(1900~2050) | 1865/1868 정확, 3건 자정경계 ±1일* |

*삭이 자정 ±5분에 드는 극경계(1914·1916·1920)는 두 독립 천문 엔진의 한계로 중국 농력과 ±1일 갈릴 수 있다. 한국 기준(KASI, KST)은 중국(베이징)과 의도적으로 다를 수 있다(예: 2017 윤달).

신강신약·용신·격국은 정답 데이터셋이 없어(tyme4j 미제공) 결정론성만 보장하며, 가중치·규칙은 KDoc에 근거를 명시한다.

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
- [ ] Maven Central 배포

## 라이선스

MIT (예정). 천문 알고리즘은 Jean Meeus _Astronomical Algorithms_ 및 공개 VSOP87 계수를 참조하며, 검증에 [tyme4j](https://github.com/6tail/tyme4j)(MIT)를 사용한다.
