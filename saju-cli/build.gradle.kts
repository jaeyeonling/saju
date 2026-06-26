plugins {
    id("saju.kotlin-common")
    application
}

dependencies {
    implementation(project(":saju-korea"))
    implementation(project(":saju-interpretation"))
}

application {
    mainClass.set("io.github.jaeyeonling.saju.cli.MainKt")
}
