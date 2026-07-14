plugins {
    id("saju.kotlin-common")
    id("saju.publish") // io.github.jaeyeonling:saju-serialization
    alias(libs.plugins.kotlin.serialization) // @Serializable 컴파일러 플러그인
}

// opt-in 직렬화 모듈 — 이 모듈을 의존할 때만 kotlinx.serialization 런타임이 따라온다.
// core/korea/interpretation 의 '런타임 의존성 0' 강점은 그대로 보존된다.
dependencies {
    api(project(":saju-core"))
    api(project(":saju-korea")) // 한국 보정 trace(CorrectionTrace) DTO 용
    api(project(":saju-interpretation"))
    api(project(":saju-group"))
    implementation(libs.kotlinx.serialization.json)
}
