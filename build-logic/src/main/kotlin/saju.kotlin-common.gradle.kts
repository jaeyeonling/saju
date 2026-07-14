import org.gradle.accessors.dm.LibrariesForLibs
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    kotlin("jvm")
    jacoco
    id("org.jlleitschuh.gradle.ktlint")
}

val libs = the<LibrariesForLibs>()

repositories {
    mavenCentral()
}

// ktlint_official 포맷을 ktlintFormat 으로 전 파일에 적용하고, ktlintCheck 를 빌드 게이트로 강제한다.
// ktlint_official 의 discouraged-comment-location 은 도메인 룩업 테이블·골든·enum 에 항목별
// 명리/한자 인라인 주석을 다는 이 프로젝트의 핵심 패턴과 충돌하는데, 그런 파일에는
// @file:Suppress("ktlint:standard:discouraged-comment-location") 를 명시해 의도를 드러낸다
// (.editorconfig 전역 disable 은 이 플러그인 버전에서 동작하지 않으므로 파일 단위로 표식).

kotlin {
    // 라이브러리 호환성을 위해 JDK 17 타깃. 로컬은 21이라도 toolchain 이 17을 보장.
    jvmToolchain(17)
    // 공개 API 표면을 명시적으로 강제 — 라이브러리 품질의 기본.
    explicitApi()
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
    }
}

dependencies {
    testImplementation(platform(libs.junit.bom))
    // Java interop 테스트(JavaInteropTest.java)는 JUnit Jupiter 로 작성.
    testImplementation(libs.junit.jupiter)
    // Kotlin 테스트는 Kotest — Kotlin 친화 matcher(shouldBe) + StringSpec 러너. JUnit Platform 위에서 동작.
    testImplementation(libs.kotest.runner.junit5)
    testImplementation(libs.kotest.assertions.core)
    testRuntimeOnly(libs.junit.platform.launcher)
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
    // 골든 CSV 재생성 스위치를 테스트 JVM 으로 전달 — `-Dgolden.write=true` 로 *GoldenWriter 활성화.
    System.getProperty("golden.write")?.let { systemProperty("golden.write", it) }
    finalizedBy(tasks.named("jacocoTestReport"))
}

jacoco {
    toolVersion = "0.8.12"
}

tasks.named<JacocoReport>("jacocoTestReport") {
    dependsOn(tasks.named("test"))
    reports {
        xml.required.set(true)
        html.required.set(true)
    }
}

// 커버리지 게이트 — check 가 의존하므로 build 시 자동 검증.
//  LINE 80%: 전반 실행 보장. BRANCH 60%: 천문 급수·균시차·자정경계 같은 분기 다발 코드에서
//  LINE 만으로는 못 잡는 의미있는 분기 커버리지를 강제(LINE 처럼 80 은 도메인 특성상 비현실적).
tasks.named<JacocoCoverageVerification>("jacocoTestCoverageVerification") {
    dependsOn(tasks.named("test"))
    violationRules {
        rule {
            limit {
                counter = "LINE"
                value = "COVEREDRATIO"
                minimum = "0.80".toBigDecimal()
            }
            limit {
                counter = "BRANCH"
                value = "COVEREDRATIO"
                minimum = "0.60".toBigDecimal()
            }
        }
    }
}

tasks.named("check") {
    dependsOn(tasks.named("jacocoTestCoverageVerification"))
}
