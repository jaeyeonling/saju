package io.github.jaeyeonling.saju.architecture

import com.lemonappdev.konsist.api.Konsist
import com.lemonappdev.konsist.api.verify.assertFalse
import io.kotest.core.spec.style.StringSpec

/**
 * saju-group 의 프로덕션 코드가 java.time 에 의존하지 않음을 빌드 타임에 강제한다.
 *
 * 그룹 합성의 입력 사주는 이미 시간 보정(saju-korea)이 끝난 결과이므로, 그룹 레이어는
 * 새로 시간 계산을 하지 않는다 → java.time-free 유지(KMP 승격·천문 격리 보존).
 */
class JavaTimeFreeTest : StringSpec({
    "saju-group 프로덕션 코드는 java time 을 import 하지 않는다" {
        Konsist
            .scopeFromProduction("saju-group")
            .files
            .assertFalse(additionalMessage = "saju-group 은 java.time-free 여야 한다. 시간 보정은 saju-korea 에서만.") {
                it.hasImport { import -> import.name.startsWith("java.time") }
            }
    }
})
