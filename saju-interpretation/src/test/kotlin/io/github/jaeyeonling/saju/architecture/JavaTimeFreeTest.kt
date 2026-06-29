package io.github.jaeyeonling.saju.architecture

import com.lemonappdev.konsist.api.Konsist
import com.lemonappdev.konsist.api.verify.assertFalse
import io.kotest.core.spec.style.StringSpec

/** 해석 레이어도 순수(java.time-free)여야 함을 빌드 타임에 강제한다(pure-domain convention 약속). */
class JavaTimeFreeTest : StringSpec({
    "saju-interpretation 프로덕션 코드는 java time 을 import 하지 않는다" {
        Konsist
            .scopeFromProduction("saju-interpretation")
            .files
            .assertFalse(additionalMessage = "해석 레이어는 java.time-free 여야 한다.") {
                it.hasImport { import -> import.name.startsWith("java.time") }
            }
    }
})
