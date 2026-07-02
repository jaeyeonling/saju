import com.vanniktech.maven.publish.JavadocJar
import com.vanniktech.maven.publish.KotlinJvm
import org.gradle.api.tasks.bundling.Jar

// 라이브러리 모듈(core·korea·interpretation·group·serialization) 의 Maven Central 배포 설정.
// 좌표(GROUP·VERSION_NAME)는 gradle.properties 에서 vanniktech 가 자동으로 읽고, artifactId 는 모듈명이다.
// CLI(앱)에는 적용하지 않는다.
//
// 신규 네임스페이스(io.github.jaeyeonling)는 구 OSSRH 가 아닌 Central Portal 로만 배포된다 —
// publishToMavenCentral() 이 곧 Portal 경로다. 서명·자격증명은 CI 시크릿으로 주입한다(docs/publishing.md).
plugins {
    id("org.jetbrains.dokka") // KDoc → javadoc jar (빈 jar 대체)
    id("com.vanniktech.maven.publish")
}

mavenPublishing {
    publishToMavenCentral()
    // GPG 서명은 키가 주입됐을 때만 켠다 — 로컬 dev(publishToMavenLocal)는 키 없이 동작하고,
    // CI 배포에서만 signingInMemoryKey 시크릿이 들어와 서명된다(Central 은 서명 필수).
    // ORG_GRADLE_PROJECT_signingInMemoryKey 환경변수도 Gradle 이 이 프로퍼티로 노출한다.
    if (providers.gradleProperty("signingInMemoryKey").isPresent) {
        signAllPublications()
    }

    // sources jar + Dokka 로 생성한 javadoc jar.
    // dokka v2(DGP v2) 는 태스크명이 dokkaGeneratePublicationHtml 이다(구 dokkaHtml).
    configure(
        KotlinJvm(
            javadocJar = JavadocJar.Dokka("dokkaGeneratePublicationHtml"),
            sourcesJar = true,
        ),
    )

    pom {
        name.set(project.name)
        description.set(
            "한국 사주 만세력 라이브러리 — 자체 천문 엔진 기반 사주팔자·대운·해석(java.time-free 코어).",
        )
        inceptionYear.set("2026")
        url.set("https://github.com/jaeyeonling/saju")
        licenses {
            license {
                name.set("MIT License")
                url.set("https://github.com/jaeyeonling/saju/blob/main/LICENSE")
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
            url.set("https://github.com/jaeyeonling/saju")
            connection.set("scm:git:https://github.com/jaeyeonling/saju.git")
            developerConnection.set("scm:git:ssh://git@github.com/jaeyeonling/saju.git")
        }
    }
}

// 배포되는 모든 jar(main·sources·javadoc)의 META-INF 에 라이선스·제3자 고지를 동봉한다.
// jar 만 내려받는 다운스트림도 고지를 얻어야 attribution 체인이 끊기지 않는다 —
// 특히 saju-core 는 astronomia(MIT) 계수 데이터를 번들하므로 MIT 고지 동행이 필수다.
tasks.withType<Jar>().configureEach {
    metaInf {
        from(rootProject.file("LICENSE"))
        from(rootProject.file("THIRD-PARTY-NOTICES.md"))
    }
}
