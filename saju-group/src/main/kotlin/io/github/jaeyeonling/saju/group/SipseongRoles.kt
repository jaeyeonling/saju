package io.github.jaeyeonling.saju.group

import io.github.jaeyeonling.saju.interpretation.SipSeong
import io.github.jaeyeonling.saju.interpretation.SipSeongGroup

/**
 * 그룹 내 멤버 역할 — 멤버의 최상위 십성 묶음([topGroup]) 기준 라벨.
 *
 * 통설(자평 십성)을 도구가 역할로 환산한 **해석 규칙**이며 점술 정설이 아니다.
 */
public enum class GroupRole(
    /** 한글 역할명(주도·추진형 등). */
    public val koreanName: String,
    /** 이 역할을 부여하는 최상위 십성 묶음. */
    public val topGroup: SipSeongGroup,
) {
    LEADER("주도·추진형", SipSeongGroup.BIGEOP),
    EXECUTOR("실무·표현형", SipSeongGroup.SIKSANG),
    ACHIEVER("성과·실리형", SipSeongGroup.JAESEONG),
    COORDINATOR("관리·조율형", SipSeongGroup.GWANSEONG),
    PLANNER("학습·기획형", SipSeongGroup.INSEONG),
    ;

    public companion object {
        /** 최상위 십성 묶음 → 역할. */
        @JvmStatic
        public fun ofTop(group: SipSeongGroup): GroupRole = entries.first { it.topGroup == group }
    }
}

/** 운영 트리거의 종류 — 결핍/과잉. */
public enum class TriggerKind(
    /** 한글 이름(결핍·과잉). */
    public val koreanName: String,
) {
    DEFICIT("결핍"),
    EXCESS("과잉"),
}

/**
 * 그룹 운영 트리거 — 결핍/과잉 십성 묶음에 대한 제안.
 *
 * 통설 기반 **도구 해석 규칙**이며 점술 정설이 아니다.
 */
public data class RoleTrigger(
    public val group: SipSeongGroup,
    public val kind: TriggerKind,
    public val message: String,
)

/**
 * 그룹 십성 역할 구성(STAGE3b).
 *
 * [groupDistribution]·[group5] 는 결정론적 집계지만, [roleComposition]·[triggers] 는
 * **통설 기반 도구 해석 규칙**(점술 정설 아님)이다.
 */
public data class SipseongRoles(
    /** 십성 10종 그룹 합산. */
    public val groupDistribution: Map<SipSeong, Int>,
    /** 십성 5묶음 그룹 합산. */
    public val group5: Map<SipSeongGroup, Int>,
    /** 역할 → 그 역할에 속한 멤버 id 목록. */
    public val roleComposition: Map<GroupRole, List<String>>,
    /** 과잉 묶음. */
    public val excess: List<SipSeongGroup>,
    /** 결핍 묶음. */
    public val deficit: List<SipSeongGroup>,
    /** 결핍·과잉에서 도출한 운영 트리거(결핍 먼저, 과잉 다음). */
    public val triggers: List<RoleTrigger>,
)
