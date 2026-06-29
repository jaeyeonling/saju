plugins {
    `kotlin-dsl`
}

repositories {
    gradlePluginPortal()
    mavenCentral()
}

dependencies {
    implementation(libs.kotlin.gradle.plugin)
    implementation(libs.ktlint.gradle) // convention plugin 에서 ktlint 적용 가능하게
    // precompiled script plugin 안에서 version catalog(`libs`) 접근을 가능하게 하는 표준 우회.
    // 생성된 LibrariesForLibs accessor 클래스를 컴파일 클래스패스에 노출한다.
    implementation(files(libs.javaClass.superclass.protectionDomain.codeSource.location))
}
