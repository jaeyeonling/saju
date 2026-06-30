plugins {
    id("saju.kotlin-common")
    alias(libs.plugins.kotlin.serialization) // --json 출력(@Serializable)
    application
}

dependencies {
    implementation(project(":saju-korea"))
    implementation(project(":saju-interpretation"))
    implementation(project(":saju-serialization")) // toDto/sajuJson 재사용
    implementation(libs.kotlinx.serialization.json)
}

application {
    mainClass.set("io.github.jaeyeonling.saju.cli.MainKt")
}
