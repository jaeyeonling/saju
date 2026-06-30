package io.github.jaeyeonling.saju.group

import io.github.jaeyeonling.saju.interpretation.SipSeong
import kotlin.math.abs

/**
 * 그룹 시간축 합성(STAGE3d) — 공통 세운 + 멤버별 세운 십성 + 현재 대운/전환기.
 * [birthYear] 가 0(미상)인 멤버는 세운 십성만 채우고 대운 추정은 건너뛴다.
 */
internal object TimelineAnalysis {
    fun analyze(
        members: List<GroupMember>,
        seunYear: Int,
        context: GroupContext,
    ): GroupTimeline {
        val groupSeun = members.firstNotNullOfOrNull { it.seun?.ganji }
        val memberSeun = LinkedHashMap<String, MemberSeun>()
        val currentDaeun = LinkedHashMap<String, CurrentDaeun>()
        val transitions = mutableListOf<DaeunTransition>()

        for (member in members) {
            val seunSipseong = member.seun?.let { SipSeong.of(member.chart.dayMaster, it.ganji.gan) }
            memberSeun[member.id] = MemberSeun(member.alias, seunSipseong)
            if (member.birthYear == 0) continue

            val age = seunYear - member.birthYear
            val ordered = member.daeun.sortedBy { it.startAge }
            ordered.lastOrNull { it.startAge <= age }?.let { current ->
                currentDaeun[member.id] = CurrentDaeun(member.alias, current.startAge, current.ganji, age)
            }
            ordered.firstOrNull { abs(it.startAge - age) <= context.transitionWindow }?.let { transition ->
                transitions.add(
                    DaeunTransition(member.id, member.alias, transition.startAge, transition.ganji, age),
                )
            }
        }

        return GroupTimeline(seunYear, groupSeun, memberSeun, currentDaeun, transitions)
    }
}
