@file:Suppress("UnstableApiUsage")

rootProject.name = "saju"

pluginManagement {
    includeBuild("build-logic")
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}

plugins {
    // JDK 17 toolchain 자동 프로비저닝 (로컬에 17이 없어도 Gradle이 받아옴)
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.9.0"
}

dependencyResolutionManagement {
    repositories {
        mavenCentral()
    }
}

include(":saju-core")
include(":saju-korea")
include(":saju-interpretation")
include(":saju-serialization")
include(":saju-cli")
