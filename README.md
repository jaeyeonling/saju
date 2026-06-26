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
| `saju-core` | 천문 엔진 + 도메인(천간·지지·60갑자) + 4기둥/대운 도출 | ❌ 금지(Konsist 강제) |
| `saju-korea` | 한국 시간 보정(진태양시·표준시 연혁·서머타임·자시) | ✅ 사용 |
| `saju-interpretation` | 십성·오행·합충·신살·신강신약·용신·격국 | ❌ 금지 |
| `saju-cli` | 데모/수동 검증 CLI | ✅ 사용 |

핵심 데이터 흐름:

```
astronomy(순수 UT JD, 타임존 무지)
   → korea(보정: 서머타임 → 표준시 자오선 → 진태양시 → 자시/절기)
   → derivation(연도 / 절기 idx / JDN / 시지 → 4기둥)
```

## 빌드

```bash
./gradlew build      # 컴파일 + 테스트 + 아키텍처 검증(java.time-free)
./gradlew :saju-cli:run
```

요구사항: JDK 17+ (toolchain 자동 프로비저닝). 로컬 검증은 JDK 21에서 수행.

## 로드맵

- [x] **P0** 멀티모듈 스캐폴딩 + java.time-free 강제 + CI
- [ ] **P1** 천문 엔진(절기·삭망, ~1″ 정밀도)
- [ ] **P2** 기본 도메인(천간·지지·60갑자, 인덱스 산술)
- [ ] **P3** 4기둥 도출(연·월·일·시주)
- [ ] **P4** 한국 시간 보정 파이프라인
- [ ] **P5** 대운·세운
- [ ] **P6** 해석: 십성·합충·공망
- [ ] **P7** 학파 전략: 십이운성·신강신약·용신·격국
- [ ] **P8** Java interop 마감 + Maven Central 배포

## 라이선스

MIT (예정). 천문 알고리즘은 Jean Meeus _Astronomical Algorithms_ 및 공개 VSOP87 계수를 참조하며, 검증에 [tyme4j](https://github.com/6tail/tyme4j)(MIT)를 사용한다.
