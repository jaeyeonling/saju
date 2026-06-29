package io.github.jaeyeonling.saju.interpretation

import io.github.jaeyeonling.saju.domain.Cheongan
import io.github.jaeyeonling.saju.domain.Ohaeng

/**
 * 십성 오행 관계 5묶음 — 일간(나) 기준 다른 오행과의 관계. 정/편을 가르기 전의 큰 분류축이다.
 */
public enum class SipSeongGroup {
    BIGYEOP, // 비겁 比劫 — 나와 같은 오행(동료·경쟁·나를 돕는 힘)
    SIKSANG, // 식상 食傷 — 내가 생하는 것(표현·산출)
    JAESEONG, // 재성 財星 — 내가 극하는 것(재물)
    GWANSEONG, // 관성 官星 — 나를 극하는 것(직업·권위·나를 누르는 힘)
    INSEONG, // 인성 印星 — 나를 생하는 것(후원·학문·나를 돕는 힘)
}

/**
 * 십성(十星/十神) — 일간(나) 대비 다른 글자의 관계 역할 10종.
 *
 * 도출: **오행 관계 5묶음([group]) × 음양 동이([isSameEumyang])**.
 * - 오행: 같음=비겁, 내가 생=식상, 내가 극=재성, 나를 극=관성, 나를 생=인성
 * - 음양: 같으면 편(偏)계열, 다르면 정(正)계열.
 *   같은 음양은 음양의 끌림 없이 한쪽으로 **치우쳐(偏)** 강하고 거칠며, 다른 음양은 음양이 **조화·견제해(正)** 안정적이다.
 *   (예: 같은 음양 재성=편재=유동·투기, 다른 음양 재성=정재=고정·정당한 소득)
 *
 * [of] 는 enum 선언순서가 아니라 [group]·[isSameEumyang] 속성으로 찾으므로 상수 재배치에 영향받지 않는다.
 */
public enum class SipSeong(
    public val group: SipSeongGroup,
    public val isSameEumyang: Boolean,
    /** 한글 이름(비견·겁재·…). */
    public val koreanName: String,
    /** 한자(比肩·劫財·…). */
    public val hanja: String,
) {
    BIGYEON(SipSeongGroup.BIGYEOP, true, "비견", "比肩"), // 나와 같은 오행·음양. 동료·경쟁자·자존심.
    GEOPJAE(SipSeongGroup.BIGYEOP, false, "겁재", "劫財"), // 같은 오행, 다른 음양. 형제·동업·재물 다툼.
    SIKSIN(SipSeongGroup.SIKSANG, true, "식신", "食神"), // 내가 생하는 것. 표현·먹을복·여유.
    SANGGWAN(SipSeongGroup.SIKSANG, false, "상관", "傷官"), // 내가 생하는 것(다른 음양). 재능·끼·반항.
    PYEONJAE(SipSeongGroup.JAESEONG, true, "편재", "偏財"), // 내가 극하는 것. 유동 재물·사업·큰돈.
    JEONGJAE(SipSeongGroup.JAESEONG, false, "정재", "正財"), // 내가 극하는 것(다른 음양). 고정 수입·정당한 재물·처(妻).
    PYEONGWAN(SipSeongGroup.GWANSEONG, true, "편관", "偏官"), // (칠살 七殺) 나를 극하는 것. 권력·압박·도전.
    JEONGGWAN(SipSeongGroup.GWANSEONG, false, "정관", "正官"), // 나를 극하는 것(다른 음양). 직장·명예·규범.
    PYEONIN(SipSeongGroup.INSEONG, true, "편인", "偏印"), // 나를 생하는 것. 비주류 학문·종교·눈치.
    JEONGIN(SipSeongGroup.INSEONG, false, "정인", "正印"), // 나를 생하는 것(다른 음양). 학문·후원·모친.
    ;

    public companion object {
        /** 일간 [dayMaster] 기준 [target] 천간의 십성. */
        @JvmStatic
        public fun of(dayMaster: Cheongan, target: Cheongan): SipSeong {
            val group = relationGroup(dayMaster.ohaeng, target.ohaeng)
            val sameEumyang = dayMaster.eumyang == target.eumyang
            return entries.first { it.group == group && it.isSameEumyang == sameEumyang }
        }

        private fun relationGroup(day: Ohaeng, target: Ohaeng): SipSeongGroup = when {
            target == day -> SipSeongGroup.BIGYEOP
            day.generates() == target -> SipSeongGroup.SIKSANG
            day.controls() == target -> SipSeongGroup.JAESEONG
            target.controls() == day -> SipSeongGroup.GWANSEONG
            else -> SipSeongGroup.INSEONG // target.generates() == day
        }
    }
}
