// 불변식 케이스에 항목별 명리 주석을 인라인으로 단다(의도된 가독성 패턴).
@file:Suppress("ktlint:standard:discouraged-comment-location")

package io.github.jaeyeonling.saju.domain

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec

/** 도메인 불변식이 생성 시점에 강제되는지 — 무효 상태가 타입을 통과하지 못하게 한다(거부경로 검증). */
class DomainInvariantTest : StringSpec({

    "무효 60갑자 조합은 생성 시점에 거부된다" {
        // 천간·지지 음양 불일치 = 60갑자에 없는 조합.
        shouldThrow<IllegalArgumentException> { Ganji(Cheongan.GAP, Jiji.CHUK) } // 갑(양)+축(음)
        shouldThrow<IllegalArgumentException> { Ganji(Cheongan.EUL, Jiji.JA) } // 을(음)+자(양)
    }

    "사주판 기둥 위치 불일치는 생성 시점에 거부된다" {
        shouldThrow<IllegalArgumentException> {
            SajuChart(
                year = Pillar(PillarPosition.HOUR, Ganji.fromIndex(0)), // year 슬롯에 시주를 넣음
                month = Pillar(PillarPosition.MONTH, Ganji.fromIndex(2)),
                day = Pillar(PillarPosition.DAY, Ganji.fromIndex(4)),
                hour = Pillar(PillarPosition.HOUR, Ganji.fromIndex(6)),
            )
        }
    }
})
