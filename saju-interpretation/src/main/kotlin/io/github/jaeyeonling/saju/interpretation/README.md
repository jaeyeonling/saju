# interpretation — 사주 해석 용어집

원국 8글자를 **일간(나) 기준**으로 읽어 성향·길흉을 도출한다. 정답 데이터셋이 없는 영역이 많아
**전략(Strategy)으로 학파를 갈아끼운다**(`InterpretationContext`). 기본값은 한국 통설.

## 용어 한눈에

| 용어 | 코드 | 뜻 |
|------|------|-----|
| 십성(十星) | `SipSeong` | 일간 대비 다른 글자의 역할 10종(비견·식신·정재·정관·정인…) |
| 신강신약(身強身弱) | `SinStrength` | 일간이 강한가 약한가 — 억부 용신의 기준 |
| 용신(用神) | `Yongsin` | 사주의 균형을 돕는 핵심 오행 — 억부/조후/합성 |
| 격국(格局) | `Gyeokguk` | 월령(月令) 중심의 짜임새 유형 — 자평(본기)/투출 |
| 십이운성(十二運星) | `SibiUnseong` | 일간이 지지에서 갖는 기운 12단계 — 음포태/양포태 |
| 합충(合沖) | `HapChung` | 글자끼리의 결합·충돌 |
| 공망(空亡) | `Gongmang` | 작용력이 빈 지지 |

> 기초 용어(천간·지지·오행·지장간)는 `saju-core`의 `domain` 패키지에 있다.

## 읽는 순서

1. **`SipSeong`** — 모든 해석의 기본 관계(오행 5묶음 × 음양 동이).
2. **`SinStrength` → `Yongsin`** — 신강신약이 용신의 입력이다(종속 순서).
3. `Gyeokguk` · `SibiUnseong` · `HapChung` — 독립 해석.
4. **`Interpretation.of(chart, ctx)`** — 위를 한 번에 조립하는 파사드.

## 학파 갈아끼우기

```kotlin
// 전략 교체: 조후 용신 + 양포태 십이운성으로
val ctx = InterpretationContext.DEFAULT.copy(
    yongsin = JohuYongsinStrategy,
    sibiUnseong = YangPotaeStrategy,
)
val report = Interpretation.of(chart, ctx)

// 지장간 분야표를 신강신약·격국에 일관 주입 (투출격으로 갈아끼우며 — copy 와 합성된다)
val ctx2 = InterpretationContext.DEFAULT
    .copy(gyeokguk = TuchulGyeokgukStrategy.DEFAULT)
    .withHiddenStems(myTable)
```
