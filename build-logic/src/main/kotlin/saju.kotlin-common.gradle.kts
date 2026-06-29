import org.gradle.accessors.dm.LibrariesForLibs
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    kotlin("jvm")
    jacoco
}

val libs = the<LibrariesForLibs>()

repositories {
    mavenCentral()
}

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

// 커버리지 게이트 — 라인 80% 미만이면 빌드 실패(회귀 방지). check 가 의존하므로 build 시 자동 검증.
tasks.named<JacocoCoverageVerification>("jacocoTestCoverageVerification") {
    dependsOn(tasks.named("test"))
    violationRules {
        rule {
            limit {
                counter = "LINE"
                value = "COVEREDRATIO"
                minimum = "0.80".toBigDecimal()
            }
        }
    }
}

tasks.named("check") {
    dependsOn(tasks.named("jacocoTestCoverageVerification"))
}
