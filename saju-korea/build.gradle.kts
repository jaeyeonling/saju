plugins {
    id("saju.kotlin-common")   // korea 레이어는 java.time(ZoneRules, 균시차)을 자유롭게 쓴다
}

dependencies {
    api(project(":saju-core"))
}
