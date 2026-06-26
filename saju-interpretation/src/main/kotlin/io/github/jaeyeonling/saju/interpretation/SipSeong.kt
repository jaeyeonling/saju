package io.github.jaeyeonling.saju.interpretation

import io.github.jaeyeonling.saju.domain.Cheongan
import io.github.jaeyeonling.saju.domain.Ohaeng

/**
 * 십성(十星/十神) — 일간(나) 대비 다른 글자의 관계 역할 10종.
 *
 * 도출: **오행 관계 5묶음 × 음양 동이**.
 * - 오행: 같음=비겁, 내가 생=식상, 내가 극=재성, 나를 극=관성, 나를 생=인성
 * - 음양: 같으면 편(偏)계열, 다르면 정(正)계열 (비견/겁재 등)
 *
 * enum 순서는 tyme4j TenStar 와 동일하다(비견·겁재·식신·상관·편재·정재·편관(칠살)·정관·편인·정인).
 */
public enum class SipSeong {
    BIGYEON, // 비견 比肩
    GEOPJAE, // 겁재 劫財
    SIKSIN, // 식신 食神
    SANGGWAN, // 상관 傷官
    PYEONJAE, // 편재 偏財
    JEONGJAE, // 정재 正財
    PYEONGWAN, // 편관 偏官(칠살)
    JEONGGWAN, // 정관 正官
    PYEONIN, // 편인 偏印
    JEONGIN, // 정인 正印
    ;

    public companion object {
        /** 일간 [dayMaster] 기준 [target] 천간의 십성. */
        @JvmStatic
        public fun of(dayMaster: Cheongan, target: Cheongan): SipSeong {
            val group = relationGroup(dayMaster.ohaeng, target.ohaeng)
            val sameEumyang = dayMaster.eumyang == target.eumyang
            return entries[group * 2 + if (sameEumyang) 0 else 1]
        }

        // 오행 관계 묶음: 0=비겁, 1=식상, 2=재성, 3=관성, 4=인성
        private fun relationGroup(day: Ohaeng, target: Ohaeng): Int = when {
            target == day -> 0
            day.generates() == target -> 1
            day.controls() == target -> 2
            target.controls() == day -> 3
            else -> 4 // target.generates() == day
        }
    }
}
