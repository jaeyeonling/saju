import org.gradle.api.plugins.JavaPluginExtension

// 라이브러리 모듈(saju-core·korea·interpretation) 배포 설정.
// 좌표(GROUP/VERSION_NAME)는 gradle.properties 에서 주입한다. CLI(앱)에는 적용하지 않는다.
plugins {
    `maven-publish`
}

group = providers.gradleProperty("GROUP").get()
version = providers.gradleProperty("VERSION_NAME").get()

// Maven Central 은 sources·javadoc jar 를 요구한다(Kotlin 이라 javadoc 은 우선 빈 jar — 추후 Dokka 로 교체).
configure<JavaPluginExtension> {
    withSourcesJar()
    withJavadocJar()
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
            pom {
                name.set(project.name)
                description.set("한국 사주 만세력 라이브러리 — 자체 천문 엔진 기반 사주팔자·대운·해석(java.time-free 코어).")
                url.set("https://github.com/jaeyeonling/bazi")
                licenses {
                    license {
                        name.set("MIT License")
                        url.set("https://github.com/jaeyeonling/bazi/blob/main/LICENSE")
                        distribution.set("repo")
                    }
                }
                developers {
                    developer {
                        id.set("jaeyeonling")
                        name.set("jaeyeonling")
                        email.set("jaeyeonling@gmail.com")
                    }
                }
                scm {
                    connection.set("scm:git:https://github.com/jaeyeonling/bazi.git")
                    developerConnection.set("scm:git:ssh://git@github.com/jaeyeonling/bazi.git")
                    url.set("https://github.com/jaeyeonling/bazi")
                }
            }
        }
    }
}
