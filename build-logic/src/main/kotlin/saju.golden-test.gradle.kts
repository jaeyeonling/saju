import org.gradle.accessors.dm.LibrariesForLibs

plugins {
    id("saju.kotlin-common")
}

val libs = the<LibrariesForLibs>()

// tyme4j 는 검증된 사주 엔진을 골든 데이터 생성·대조에만 쓴다(런타임 의존 아님).
// testImplementation 으로만 노출해 프로덕션 아티팩트에 새지 않게 한다.
dependencies {
    "testImplementation"(libs.tyme4j)
}
