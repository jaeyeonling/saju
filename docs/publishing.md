# 배포 가이드 (Maven Central)

메인테이너용 문서. `saju-*` 라이브러리를 [Maven Central Portal](https://central.sonatype.com) 로 배포하는 절차다.
신규 네임스페이스(`io.github.jaeyeonling`)는 구 OSSRH 가 아닌 **Central Portal** 로만 배포된다.

## 1회 준비

### 1) Central Portal 네임스페이스

1. <https://central.sonatype.com> 에 **GitHub 계정으로 로그인**한다.
2. `io.github.jaeyeonling` 네임스페이스는 GitHub 로그인으로 **자동 검증**된다(별도 TXT 불필요).
3. **Account → Generate User Token** 으로 발급되는 `username`/`password` 를 기록해 둔다.
   이 값은 로그인 비밀번호가 아니라 **토큰**이다.

### 2) GPG 서명 키

```bash
gpg --gen-key                                   # 키 생성 (이름/이메일/패스프레이즈)
gpg --list-secret-keys --keyid-format=long      # KEYID 확인 (sec 줄의 rsa4096/ED25519 뒤 16자리)
gpg --keyserver keyserver.ubuntu.com --send-keys <KEYID>   # 공개키 배포 (Central 검증용)
gpg --export-secret-keys --armor <KEYID>        # 개인키를 ASCII armor 로 출력 → 아래 시크릿에 넣는다
```

### 3) GitHub Secrets 등록

레포 **Settings → Secrets and variables → Actions → New repository secret** 에 4개 등록:

| 시크릿 이름 | 값 |
|-------------|-----|
| `MAVEN_CENTRAL_USERNAME` | Central Portal User Token 의 username |
| `MAVEN_CENTRAL_PASSWORD` | Central Portal User Token 의 password |
| `SIGNING_IN_MEMORY_KEY` | `gpg --export-secret-keys --armor <KEYID>` 출력 전체 (BEGIN~END 포함) |
| `SIGNING_IN_MEMORY_KEY_PASSWORD` | GPG 키의 패스프레이즈 |

> 워크플로우는 이 시크릿을 `ORG_GRADLE_PROJECT_*` 환경변수로 매핑해 Gradle 프로퍼티로 주입한다.
> 로컬 dev(`./gradlew publishToMavenLocal`)는 키가 없어도 동작한다 — 서명은 키가 있을 때만 켜진다.

## 릴리스

1. `gradle.properties` 의 `VERSION_NAME` 을 올린다 (예: `0.1.0`).
2. `CHANGELOG.md` 의 `[Unreleased]` 를 해당 버전으로 옮기고 날짜를 채운다.
3. 커밋 후 태그를 push 한다 — 태그가 `VERSION_NAME` 과 일치해야 한다:

   ```bash
   git tag v0.1.0
   git push origin v0.1.0
   ```

4. `.github/workflows/publish.yml` 이 실행되어 Central Portal 로 업로드한다.
5. <https://central.sonatype.com> → **Deployments** 에서 검증 통과를 확인하고 **Publish** 를 눌러 공개한다.
   (자동 공개를 원하면 `build-logic/src/main/kotlin/saju.publish.gradle.kts` 의
   `publishToMavenCentral()` 을 `publishToMavenCentral(automaticRelease = true)` 로 바꾼다.)

## 로컬에서 산출물 점검

키 없이 아티팩트(POM·sources·Dokka javadoc jar)를 로컬 Maven 저장소에 찍어볼 수 있다:

```bash
./gradlew publishToMavenLocal --no-configuration-cache
# ~/.m2/repository/io/github/jaeyeonling/saju-core/<version>/ 확인
```

## 배포 대상 모듈

`saju.publish` convention plugin 이 적용된 5개 라이브러리 모듈이 배포된다:
`saju-core`, `saju-korea`, `saju-interpretation`, `saju-group`, `saju-serialization`.
`saju-cli` 는 데모 앱이라 배포 대상이 아니다.
