import org.gradle.accessors.dm.LibrariesForLibs

plugins {
    id("saju.kotlin-common")
}

val libs = the<LibrariesForLibs>()

// 순수 도메인 모듈(saju-core, saju-interpretation)은 java.time 에 의존하지 않는다.
// 이 격리가 추후 KMP 승격을 저렴하게 하고, 천문 엔진이 타임존을 모르게(베이징 +8h 오염 차단) 만든다.
// 강제는 Konsist 아키텍처 테스트(JavaTimeFreeTest)로 빌드 타임에 검증한다.
dependencies {
    "testImplementation"(libs.konsist)
}
