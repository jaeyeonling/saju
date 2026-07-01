// 루트 빌드는 공통 빌드 로직을 담지 않는다.
// 모든 공통 빌드 로직은 build-logic/ 의 convention plugin 4종에 있고,
// 각 모듈은 그 plugin 들을 조합해 적용한다. (DRY)
//
// 예외: 공개 API 표면 검증(binary-compatibility-validator)만 루트에 둔다.
// 이 플러그인은 멀티모듈을 루트에서 일괄 관장하는 구조라 모듈별 convention plugin 으로 나눌 수 없다.
plugins {
    alias(libs.plugins.binary.compatibility.validator)
    // 배포 플러그인을 루트에 apply false 로 먼저 로드한다 — 적용하지는 않되(모듈은 saju.publish 로 적용),
    // 공유 classloader scope 에 올려 vanniktech 의 MavenCentralBuildService 가 서브프로젝트마다
    // 다른 classloader 로 로드돼 충돌하는 문제(멀티모듈 + precompiled convention plugin)를 막는다.
    // vanniktech 가 Kotlin 플러그인 클래스에 같은 scope 에서 접근해야 하므로 kotlin.jvm 도 함께 올린다.
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.vanniktech.maven.publish) apply false
    alias(libs.plugins.dokka) apply false
}

// 각 배포 모듈의 공개 ABI 를 <module>/api/<module>.api 로 동결한다.
// API 가 바뀌면 apiCheck(=check 의존)가 실패하므로, data class 필드 추가 같은
// 의도치 않은 바이너리 호환성 변경을 커밋 단계에서 잡는다. 갱신은 `./gradlew apiDump`.
apiValidation {
    // CLI(:saju-cli)는 배포 대상 앱이라 공개 API 계약이 없다 — 추적 제외.
    ignoredProjects.add("saju-cli")
}
