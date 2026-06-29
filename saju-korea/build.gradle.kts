plugins {
    id("saju.kotlin-common")   // korea 레이어는 java.time(ZoneRules, 균시차)을 자유롭게 쓴다
    id("saju.publish")         // io.github.jaeyeonling:saju-korea
}

dependencies {
    api(project(":saju-core"))
}
