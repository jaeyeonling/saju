package io.github.jaeyeonling.saju.architecture

import com.lemonappdev.konsist.api.Konsist
import com.lemonappdev.konsist.api.verify.assertFalse
import kotlin.test.Test

/**
 * saju-core 의 프로덕션 코드가 java.time 에 의존하지 않음을 빌드 타임에 강제한다.
 *
 * 이 격리가 두 가지를 보장한다:
 *  1. 천문 엔진이 타임존을 모르게 해 베이징 +8h 하드코딩 오염을 구조적으로 차단한다.
 *  2. 순수 계산 코어를 java.time-free 로 유지해 추후 Kotlin Multiplatform 승격을 저렴하게 한다.
 *
 * 시간대·진태양시·서머타임 보정은 전적으로 saju-korea 레이어의 책임이다.
 */
class JavaTimeFreeTest {
    @Test
    fun `saju-core 프로덕션 코드는 java time 을 import 하지 않는다`() {
        Konsist
            .scopeFromProduction("saju-core")
            .files
            .assertFalse(additionalMessage = "saju-core 는 java.time-free 여야 한다. 시간 보정은 saju-korea 에서만.") {
                it.hasImport { import -> import.name.startsWith("java.time") }
            }
    }
}
