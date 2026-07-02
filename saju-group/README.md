# saju-group — 그룹 사주 합성

여러 명의 개인 사주를 **합쳐서** 그룹 차원의 특성을 산출한다. 개인 사주의 단순 나열이 아니라,
오행을 벡터로 합산했을 때만 드러나는 *그룹의 결핍·과잉·끊긴 체인*, 멤버 간 *합충 관계망*, *역할 구성*,
*세운/대운 타임라인*을 본다.

> ⚠️ **면책** — 오행 균형은 결정론적 산술(정답 있음)이지만, **역할·트리거·관계 라벨은 통설(자평·합충형해)
> 기반 도구 해석 규칙이며 점술 정설이 아니다**(학파 의존). 가볍게 — 진지한 점술이 아니라 대화 촉매.

개인 단위 계산(오행·십성·합충 판정·세운·대운)은 전부 `saju-interpretation`/`saju-core`에 위임한다.
이 모듈은 "여럿을 합쳤을 때만 드러나는" 합성만 담당하며 `java.time`-free다(입력 사주는 이미 시간 보정 완료).

## 4차원 합성 (결정론 — 같은 입력 = 같은 출력)

| 차원 | 산출 | 성격 |
|------|------|------|
| **오행 균형** (`OhaengBalance`) | 그룹 오행 벡터·정규화·결핍·과잉·끊긴 상생 체인·멤버별 최다 오행 | 순수 산술 |
| **십성 역할** (`SipseongRoles`) | 십성 분포·5묶음·멤버 역할 구성·결핍/과잉·운영 트리거 | 통설 해석 규칙 |
| **멤버간 합충** (`RelationMatrix`) | 쌍별 합충형파해 + 협력/긴장/복합/중립 net 라벨 + 관계 그래프 | 통설 해석 규칙 |
| **세운/대운 타임라인** (`GroupTimeline`) | 공통 세운·멤버별 세운 십성·현재 대운·전환기(±1년) | 결정론 |

## 라이브러리 사용법

[Maven Central](https://central.sonatype.com/artifact/io.github.jaeyeonling/saju-group) 에서 의존성으로 추가한다(멤버 사주 조립은 `saju-korea`/`saju-core` 에 위임):

```kotlin
// build.gradle.kts
dependencies {
    implementation("io.github.jaeyeonling:saju-group:0.1.0")
    implementation("io.github.jaeyeonling:saju-korea:0.1.0") // 멤버 사주 조립용
}
```

```kotlin
import io.github.jaeyeonling.saju.group.GroupAnalysis
import io.github.jaeyeonling.saju.group.GroupMember
import io.github.jaeyeonling.saju.group.Gender
import io.github.jaeyeonling.saju.korea.KoreanSaju
import io.github.jaeyeonling.saju.Saju

// 1) 멤버별 사주판·대운·세운을 KoreanSaju/Saju 로 만든다(개인 계산은 라이브러리 위임)
val seunYear = 2026
val member = GroupMember.of(
    id = "neo",
    alias = "네오",
    gender = Gender.MALE,
    birthYear = 1990,
    chart = KoreanSaju.fromCivilTime(1990, 3, 15, 7, 0),
    daeun = KoreanSaju.daeun(1990, 3, 15, 7, 0, isMale = true, longitudeDeg = 126.978, count = 8),
    seun = Saju.seun(seunYear),
)
// ... 멤버를 2명 이상 모은 뒤

// 2) 그룹 합성 (최소 2명, id 는 유일)
val report = GroupAnalysis.of(members, seunYear)

report.ohaeng.deficient        // 그룹에 부족한 오행
report.ohaeng.excessive        // 과잉 오행
report.sipseong.roleComposition // 역할 → 멤버 id 들
report.relations.graph.edges   // 멤버간 협력/긴장 관계
report.timeline.currentDaeun   // 멤버별 현재 대운
```

> `saju-group`은 `saju-korea`를 의존하지 않는다(천문 엔진 격리·KMP 격리 보존). "생년월일 → GroupMember"
> 조립은 호출자(또는 CLI)가 `KoreanSaju`로 한다.

튜닝(점술 정설 아닌 도구 규칙)은 `GroupContext`로:

```kotlin
GroupAnalysis.of(members, seunYear, GroupContext(deficitFactor = 0.5, excessFactor = 1.6, transitionWindow = 1))
```

## CLI

```bash
./gradlew :saju-cli:installDist
saju-cli/build/install/saju-cli/bin/saju-cli group members.json            # 텍스트 요약
saju-cli/build/install/saju-cli/bin/saju-cli group members.json --json --seun=2026
```

`members.json` 스키마(멤버 최소 2명):

```json
{
  "group_name": "초록 밋업",
  "seun_year": 2026,
  "members": [
    {"id": "neo", "name": "김재연", "alias": "네오", "gender": "M",
     "year": 1990, "month": 3, "day": 15, "hour": 7, "minute": 0, "longitude": null}
  ]
}
```

`longitude` 생략/`null`이면 서울 경도. `seun_year` 생략 시 올해.

## JSON 직렬화 (`saju-serialization`)

```kotlin
import io.github.jaeyeonling.saju.serialization.toJson
report.toJson()  // GroupReportDto → JSON
```

enum-key Map 은 한글 문자열 키로 평탄화된다. 관계 종류·라벨(`kind`/`label`/`netLabel`)은 기계 분기용
**영문 enum 이름 + 한글(`kindKorean` 등)을 병기**한다(기존 DTO 관례). CLI `--json`은 멤버 개인(차트+해석)을
`persons`로, 합성 결과를 `group`으로 함께 내보낸다.

> vault computed.json 의 한글 `kind`/`net_label` 은 Kotlin DTO 의 `kindKorean`/`netLabelKorean` 에 대응한다.

### computed.json 호환 (vault Python 도구 레퍼런스)

이 모듈은 vault Python 도구의 `computed.json` **`group` 블록**을 Kotlin으로 재구현한 것이다(알고리즘 SSOT는
`relations.py`/`synthesis.py`/`timeline.py`, 단위 테스트로 동치 고정). 키만 snake_case → camelCase로 바뀐다:

| computed.json (Python) | GroupReportDto (Kotlin) |
|------------------------|--------------------------|
| `group.ohaeng` | `ohaeng` |
| `group.sipseong` | `sipseong` |
| `group.relation_matrix` | `relationMatrix` |
| `group.timeline` | `timeline` |
| `group.config_snapshot` | `context` |
