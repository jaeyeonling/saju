# 기여 가이드 (Contributing)

saju 에 관심 가져주셔서 감사합니다. 작은 오타 수정부터 새 기능까지 환영합니다.

## 개발 환경

- **JDK 17+** — Gradle toolchain 이 자동 프로비저닝하므로 로컬에 없어도 됩니다.
- 빌드/테스트:

  ```bash
  ./gradlew build
  ```

  컴파일 + 테스트 + 아키텍처 검증(java.time-free, Konsist) + 커버리지 게이트(LINE 80% / BRANCH 60%)
  + ktlint + 공개 API 검증(apiCheck) 을 한 번에 돕니다.

- 포맷: 커밋 전 `./gradlew ktlintFormat`

## 브랜치 & 커밋

- `main` 에서 브랜치를 딴다: `feat/...`, `fix/...`, `docs/...`, `chore/...`
- 커밋 메시지는 [Conventional Commits](https://www.conventionalcommits.org):
  `feat:`, `fix:`, `docs:`, `refactor:`, `test:`, `chore:`, `perf:`, `ci:`

## PR 체크리스트

- [ ] `./gradlew build` 통과 (테스트·아키텍처·커버리지·ktlint·apiCheck)
- [ ] 공개 API 를 바꿨다면 `./gradlew apiDump` 로 `*/api/*.api` 를 갱신해 함께 커밋
- [ ] 새 동작에는 테스트를 추가 (정확성이 핵심인 프로젝트입니다)
- [ ] 천문/명리 상수·규칙을 추가·수정했다면 **출처(참고 문헌·유파)** 를 주석 또는 PR 설명에 명시

## 설계 제약 (꼭 지켜주세요)

- **`saju-core`·`saju-interpretation`·`saju-group` 은 `java.time` 을 쓰지 않습니다** — 시간대 보정은
  `saju-korea` 전담. Konsist 아키텍처 테스트가 빌드 타임에 강제합니다.
- **explicitApi()** — 공개 타입/함수는 가시성을 명시합니다.
- 모듈 의존은 단방향 DAG 를 유지합니다 (core ← korea / interpretation ← group ← serialization).

## 정확성에 대한 기준

천문·도출(절기·4기둥·음력)은 골든 벡터(tyme4j 동결) + Meeus 원전 단위 테스트로 검증합니다.
계산 로직 변경은 골든 회귀를 통과해야 합니다. 해석 규칙(용신·격국 등)은 유파에 따라 달라지므로,
기본 동작을 바꾸는 변경은 근거와 함께 이슈에서 먼저 논의해 주세요.

## 릴리스

배포 절차와 필요한 시크릿은 [docs/publishing.md](docs/publishing.md) 를 참고하세요(메인테이너용).

## 라이선스

기여한 코드는 프로젝트의 [MIT 라이선스](LICENSE) 로 배포되는 데 동의하는 것으로 간주합니다.
