plugins {
    id("saju.pure-domain") // 해석 레이어도 순수(java.time-free)
    id("saju.publish") // io.github.jaeyeonling:saju-interpretation
}

dependencies {
    api(project(":saju-core"))
}
