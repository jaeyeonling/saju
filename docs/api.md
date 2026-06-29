# 공개 API 치트시트

한 장으로 보는 bazi 공개 API. 네임스페이스는 모두 `io.github.jaeyeonling.saju`.
입문 개념은 [루트 README](../README.md), 용어는 각 패키지 README([domain](../saju-core/src/main/kotlin/io/github/jaeyeonling/saju/domain/README.md) · [interpretation](../saju-interpretation/src/main/kotlin/io/github/jaeyeonling/saju/interpretation/README.md))를 참고.

---

## 1. 진입점 (이것만 알면 된다)

| 호출 | 입력 → 반환 | 비고 |
|------|------------|------|
| `KoreanSaju.fromCivilTime(y, mo, d, h, mi, longitudeDeg=서울, config=DEFAULT)` | 법정시 → `SajuChart` | **권장 진입점.** 진태양시·표준시 연혁·서머타임 자동 보정 |
| `KoreanSaju.fromLunarCivilTime(ly, lmo, ld, isLeapMonth, h, mi, …, basis=KOREA)` | 음력 생일 → `SajuChart` | 음→양 변환 후 동일 파이프라인 |
| `KoreanSaju.daeun(y, mo, d, h, mi, isMale, longitudeDeg=서울, count=8)` | → `List<Daeun>` | 10년 단위 대운 시퀀스 |
| `KoreanSaju.trueSolarOffsetMinutes(y, mo, d, h, mi, longitudeDeg=서울)` | → `Double` (분) | 법정시→진태양시 총 보정량(검증·표시용) |
| `Interpretation.of(chart, ctx=DEFAULT)` | `SajuChart` → `InterpretationReport` | 해석 7종을 한 번에 |
| `LunarConverter.toSolar(ly, lmo, d, isLeapMonth=false, basis=KOREA)` | 음력 → `CalendarDate` | |
| `LunarConverter.toLunar(y, mo, d, basis=KOREA)` | 양력 → `LunarDate` | |

**코어 진입점** (한국 보정 없이 직접 — `saju-core` 만으로):

| 호출 | 반환 |
|------|------|
| `Saju.fromLocalDateTime(y, mo, d, h, mi, utOffsetHours, config=DEFAULT, second=0)` | `SajuChart` |
| `Saju.daeun(utJd, monthGanZhi, yearStemEumyang, isMale, count=8)` | `List<Daeun>` |
| `Saju.seun(year)` | `Seun` (그 해의 세운) |

> 모든 진입점은 `@JvmStatic`·`@JvmOverloads` — Java 에서 기본값 인자 생략 가능. 잘못된 날짜·경도는 `IllegalArgumentException` 으로 즉시 거부(fail-fast).

---

## 2. 결과 타입

```text
SajuChart ─ year / month / day / hour : Pillar
          ├ dayMaster : Cheongan          (= day.gan, 해석 기준 '나')
          └ pillars() / stems() / branches()

Pillar    ─ position : PillarPosition · ganZhi : GanZhi · gan : Cheongan · ji : Jiji
GanZhi    ─ gan · ji · index(0~59);  fromIndex(i) · next(n) · ALL · CYCLE(=60)
Daeun     ─ startAge : Int · ganZhi : GanZhi
Seun      ─ year : Int · ganZhi : GanZhi
CalendarDate ─ year · month · day · hour · minute · second   (음→양 결과)
LunarDate    ─ year · month · day · isLeapMonth               (양→음 결과)
```

### `InterpretationReport` — 해석 7필드

| 필드 | 타입 | 의미 | 접근 예 |
|------|------|------|---------|
| `strength` | `SinStrength` | 신강신약 | `.verdict` · `.supportRatio` |
| `yongsin` | `YongsinResult` | 용신(균형 오행) | `.yongsin`(Ohaeng) · `.method` |
| `gyeokguk` | `GyeokgukResult` | 격국(짜임새) | `.type` · `.basis` |
| `gongmang` | `Pair<Jiji, Jiji>` | 공망(빈 지지 2개) | `.first` · `.second` |
| `hapChung` | `List<HapChungRelation>` | 합·충 관계 | `.size`, `filterIsInstance<…>()` |
| `ohaeng` | `OhaengDistribution` | 오행 분포 | `.count(목)` · `.dominant()` |
| `sibiUnseong` | `Map<PillarPosition, SibiUnseong>` | 기둥별 십이운성 | `.getValue(YEAR)` |

---

## 3. 도메인 enum 사전

| enum | 값 (ordinal 순) | 멤버 |
|------|----------------|------|
| `Cheongan` 천간(10) | 갑 을 병 정 무 기 경 신 임 계 | `.ohaeng` `.eumyang` `.entries` |
| `Jiji` 지지(12) | 자 축 인 묘 진 사 오 미 신 유 술 해 | `.ohaeng` `.eumyang` `.entries` |
| `Ohaeng` 오행(5) | 목 화 토 금 수 | — |
| `Eumyang` 음양 | 양 · 음 | `.isYang` |
| `SipSeong` 십성(10) | 비견 겁재 식신 상관 편재 정재 편관 정관 편인 정인 | `SipSeong.of(dayGan, targetGan)` |
| `SibiUnseong` 십이운성(12) | 장생 목욕 관대 건록 제왕 쇠 병 사 묘 절 태 양 | — |
| `SinStrengthVerdict`(5) | 극신강 신강 중화 신약 극신약 | `.isStrong` |
| `GyeokgukType`(10) | 건록격 양인격 식신격 상관격 편재격 정재격 편관격 정관격 편인격 정인격 | `.koreanName` |
| `YongsinMethod` | 억부 · 조후 | `.koreanName` |

---

## 4. 학파·정밀도 스위치 (`config`)

대부분 기본값(한국 통설)으로 충분. 유파를 바꿀 때만 건드린다.

```kotlin
// 도출 설정 (자시 학파 · 연주 경계 · 대운수)
KoreanSajuConfig(
    saju = SajuConfig(
        zishi        = ZishiPolicy.JEONGJASI,            // 정자시 ↔ YAJASI(야자시)
        yearBoundary = YearBoundary.IPCHUN,              // 입춘세수 ↔ DONGJI(동지세수)
        daeunStartAge = DaeunStartAgePolicy.THREE_DAYS_ONE_YEAR,
    ),
    trueSolarTime = TrueSolarTimePolicy.FULL,            // FULL ↔ LONGITUDE_ONLY ↔ NONE
)
```

```kotlin
// 해석 전략 교체 (InterpretationContext)
val ctx = InterpretationContext.DEFAULT.copy(
    yongsin     = JohuYongsinStrategy,    // 억부(기본) ↔ 조후
    sibiUnseong = YangPotaeStrategy,      // 음포태(기본) ↔ 양포태
    gyeokguk    = TuchulGyeokgukStrategy.DEFAULT, // 자평(기본) ↔ 투출
)
Interpretation.of(chart, ctx)
```

| 스위치 | 값 | 기본 |
|--------|-----|------|
| `TrueSolarTimePolicy` | `NONE` / `LONGITUDE_ONLY` / `FULL` | `FULL`(경도+균시차) |
| `ZishiPolicy` | `JEONGJASI` / `YAJASI` | `JEONGJASI`(정자시) |
| `YearBoundary` | `IPCHUN` / `DONGJI` | `IPCHUN`(입춘세수) |
| 용신 전략 | `BueokYongsinStrategy` / `JohuYongsinStrategy` | 억부 |
| 십이운성 전략 | `EumPotaeStrategy` / `YangPotaeStrategy` | 음포태 |
| 격국 전략 | `JapyeongGyeokgukStrategy` / `TuchulGyeokgukStrategy` | 자평 |

---

## 5. 출생지 프리셋 (`Birthplace`)

`SEOUL` `BUSAN` `DAEGU` `INCHEON` `GWANGJU` `DAEJEON` `ULSAN` `JEJU` `PYONGYANG` — 각 `.longitudeDeg`.
프리셋에 없으면 경도(도)를 직접 넘긴다: `KoreanSaju.fromCivilTime(…, longitudeDeg = 127.5)`.
