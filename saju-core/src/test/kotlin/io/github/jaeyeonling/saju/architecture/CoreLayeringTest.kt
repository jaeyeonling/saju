package io.github.jaeyeonling.saju.architecture

import com.lemonappdev.konsist.api.Konsist
import com.lemonappdev.konsist.api.verify.assertFalse
import io.kotest.core.spec.style.StringSpec

/**
 * saju-core 내부 레이어 단방향을 빌드 타임에 강제한다: domain ← astronomy ← derivation/lunar.
 *
 * domain·astronomy 는 최하위라 상위 레이어(derivation/lunar)를 import 하지 않는다.
 * 규율(코드 리뷰)이 아니라 컴파일로 막아, import 한 줄로 레이어가 무너지는 것을 차단한다.
 */
class CoreLayeringTest : StringSpec({

    "domain 은 상위 코어 레이어를 import 하지 않는다" {
        Konsist
            .scopeFromProduction("saju-core")
            .files
            .filter { it.path.contains("/domain/") }
            .assertFalse(additionalMessage = "domain 은 최하위 레이어 — astronomy/derivation/lunar 를 의존하면 안 된다.") {
                it.hasImport { import ->
                    import.name.startsWith("io.github.jaeyeonling.saju.astronomy") ||
                        import.name.startsWith("io.github.jaeyeonling.saju.derivation") ||
                        import.name.startsWith("io.github.jaeyeonling.saju.lunar")
                }
            }
    }

    "astronomy 는 도메인·도출 레이어를 import 하지 않는다" {
        Konsist
            .scopeFromProduction("saju-core")
            .files
            .filter { it.path.contains("/astronomy/") }
            .assertFalse(additionalMessage = "astronomy 는 순수 천문 레이어 — domain/derivation/lunar 를 의존하면 안 된다.") {
                it.hasImport { import ->
                    import.name.startsWith("io.github.jaeyeonling.saju.domain") ||
                        import.name.startsWith("io.github.jaeyeonling.saju.derivation") ||
                        import.name.startsWith("io.github.jaeyeonling.saju.lunar")
                }
            }
    }
})
