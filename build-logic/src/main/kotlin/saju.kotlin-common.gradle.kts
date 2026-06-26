import org.gradle.accessors.dm.LibrariesForLibs
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    kotlin("jvm")
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
    testImplementation(libs.junit.jupiter)
    testImplementation(libs.kotlin.test.junit5)
    testRuntimeOnly(libs.junit.platform.launcher)
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}
