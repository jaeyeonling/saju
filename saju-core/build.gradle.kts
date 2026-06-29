plugins {
    id("saju.pure-domain")   // java.time-free 강제 (Konsist)
    id("saju.publish")       // io.github.jaeyeonling:saju-core
}

// saju-core 는 의존성이 없다 (kotlin-stdlib 만). 천문 계산 + 도메인 + 4기둥/대운의 순수 코어.
