package io.github.jaeyeonling.saju.group

import io.github.jaeyeonling.saju.domain.Ganji
import io.github.jaeyeonling.saju.interpretation.SipSeong

/** 멤버의 그 해 세운 십성 — 일간 대비 세운 천간. 세운 정보가 없으면 [sipSeong] = null. */
public data class MemberSeun(
    public val alias: String,
    public val sipSeong: SipSeong?,
)

/** 멤버의 현재 대운 — [age](만나이 근사) 시점에 진행 중인 대운. */
public data class CurrentDaeun(
    public val alias: String,
    public val startAge: Int,
    public val ganji: Ganji,
    public val age: Int,
)

/** 대운 전환기 — [age] 가 대운 경계 ±윈도우 안에 든 멤버. */
public data class DaeunTransition(
    public val memberId: String,
    public val alias: String,
    public val startAge: Int,
    public val ganji: Ganji,
    public val age: Int,
)

/**
 * 그룹 시간축(STAGE3d) — 공통 세운 + 멤버별 세운 십성 + 현재 대운/전환기.
 *
 * 세운·대운 간지는 입력([GroupMember.seun]/[GroupMember.daeun])에서 가져오고, 여기서는 모으고
 * 십성·전환 여부만 판정한다(재계산 없음).
 */
public data class GroupTimeline(
    public val year: Int,
    public val groupSeun: Ganji?,
    public val memberSeunSipseong: Map<String, MemberSeun>,
    public val currentDaeun: Map<String, CurrentDaeun>,
    public val daeunTransitions: List<DaeunTransition>,
)
