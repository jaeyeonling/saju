package io.github.jaeyeonling.saju.interpretation

import io.github.jaeyeonling.saju.domain.GanZhi
import io.github.jaeyeonling.saju.domain.Jiji

/**
 * 공망(空亡) — 일주가 속한 순(旬, 10간지 묶음)에서 천간과 짝짓지 못한 2개 지지.
 *
 * 旬首(순의 첫 갑일)의 지지에서 +10, +11 위치가 공망이다.
 * 예: 갑자순(갑자~계유) → 술·해 공망, 갑술순 → 신·유 공망.
 */
public object Gongmang {
    /** 일주 간지의 공망 두 지지. */
    @JvmStatic
    public fun of(dayGanZhi: GanZhi): Pair<Jiji, Jiji> {
        // 旬首 = 일주 index − 일간 ordinal (그 순의 갑X 간지). 그 지지에서 +10, +11.
        val sunHeadBranchIndex = mod(dayGanZhi.index - dayGanZhi.gan.ordinal, BRANCH_COUNT)
        return Jiji.fromIndex(sunHeadBranchIndex + GONGMANG_OFFSET_1) to
            Jiji.fromIndex(sunHeadBranchIndex + GONGMANG_OFFSET_2)
    }

    private fun mod(
        value: Int,
        modulus: Int,
    ): Int = ((value % modulus) + modulus) % modulus

    private const val BRANCH_COUNT = 12
    private const val GONGMANG_OFFSET_1 = 10
    private const val GONGMANG_OFFSET_2 = 11
}
