plugins {
    id("saju.pure-domain")   // 해석 레이어도 순수(java.time-free)
    id("saju.golden-test")   // tyme4j 로 십성 등 골든 검증
}

dependencies {
    api(project(":saju-core"))
}
