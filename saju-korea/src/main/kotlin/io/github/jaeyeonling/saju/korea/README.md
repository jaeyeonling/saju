# korea — 한국 시간 보정

이 라이브러리의 **헤드라인 기능**. 중국식 만세력과 달리, 한반도가 동경 135° 표준시를 쓰면서 생기는
시간 어긋남(서울 ≈ 32분)과 표준시 역사·서머타임을 1급으로 보정한다. `saju-core` 는 타임존을 모르고,
이 모듈만 `java.time` 을 쓴다(레이어 순수성).

## 보정 순서 한눈에 (load-bearing)

법정시(시계 시각)를 넣으면 아래 순서로 **진태양시**가 되어 절기·자시 경계를 가른다:

```
법정시 ──①서머타임──▶ ──②표준 자오선──▶ 절대순간(UT) ──③진태양시──▶ ──④절기·자시 경계──▶ 4기둥
        (구간이면 -1h)   (127.5°↔135° 연혁)            (경도보정+균시차)
```

| 단계 | 무엇 | 코드 |
|------|------|------|
| ① 서머타임 | 시행 구간이면 시계가 1시간 빠르다(12구간) | `KoreanStandardTime` |
| ② 표준 자오선 | 동경 127.5°(UTC+8:30) 시기 vs 135°(UTC+9) 시기 | `KoreanStandardTime` |
| ③ 진태양시 | 경도보정 `(경도−자오선)×4분` + 균시차 | `TrueSolarTimePolicy` |
| ④ 경계 판정 | 보정된 진태양시로 절기(월·연주)·자시(일주) 경계 | `saju-core` 로 위임 |

> ①②는 절대 순간(UT)을 정하고, ③은 그 순간을 출생지 태양시로 표시한다. 이 분리가 "베이징 +8h 오염"을 막는다.

## 진입점 한눈에

| 코드 | 역할 |
|------|------|
| `KoreanSaju.fromCivilTime(y,mo,d,h,mi, 경도=서울)` | 법정시 → 사주판 (권장 진입점) |
| `KoreanSaju.fromLunarCivilTime(…, isLeapMonth, …)` | 음력 생일 → 사주판 |
| `KoreanSaju.daeun(…, isMale)` | 대운 시퀀스 |
| `KoreanSaju.trueSolarOffsetMinutes(…)` | 법정시→진태양시 총 보정량(분) |
| `Birthplace` | 출생지 경도 프리셋(서울·부산·…·평양) |
| `TrueSolarTimePolicy` | 진태양시 보정 강도(NONE/LONGITUDE_ONLY/FULL) |
| `KoreanSajuConfig` | 보정·자시·연주 학파 묶음 설정 |

> 전체 시그니처는 루트의 API 치트시트(`docs/api.md`) 참고.
