package io.github.jaeyeonling.saju.group

import io.github.jaeyeonling.saju.domain.JijiHiddenStems
import io.github.jaeyeonling.saju.domain.PillarPosition
import io.github.jaeyeonling.saju.domain.SajuChart
import io.github.jaeyeonling.saju.interpretation.SipSeong
import io.github.jaeyeonling.saju.interpretation.SipSeongGroup

/**
 * 그룹 십성 역할 합성(STAGE3b).
 *
 * 십성 카운트는 도구 자체 규칙을 따른다: 천간(연·월·시, 일주 제외) + 지지 **본기만**(연·월·일·시).
 * 중기·여기는 세지 않는다 — 표면 십성 분포에 집중한다([InterpretationReport.sipSeong] 의 본·중·여 전체와 다름).
 */
internal object SipseongSynthesis {
    fun analyze(
        members: List<GroupMember>,
        context: GroupContext,
    ): SipseongRoles {
        val groupDistribution = SipSeong.entries.associateWith { 0 }.toMutableMap()
        val group5 = SipSeongGroup.entries.associateWith { 0 }.toMutableMap()
        for (member in members) {
            for (sip in countSipseong(member.chart)) {
                groupDistribution[sip] = groupDistribution.getValue(sip) + 1
                group5[sip.group] = group5.getValue(sip.group) + 1
            }
        }

        val roleComposition = LinkedHashMap<GroupRole, MutableList<String>>()
        for (member in members) {
            roleComposition.getOrPut(roleOf(member)) { mutableListOf() }.add(member.id)
        }

        val mean = (group5.values.sum().toDouble() / SipSeongGroup.entries.size).takeIf { it > 0.0 } ?: 1.0
        val excess = SipSeongGroup.entries.filter { group5.getValue(it) > mean * context.excessFactor }
        val deficit =
            SipSeongGroup.entries.filter {
                group5.getValue(it) == 0 || group5.getValue(it) < mean * context.deficitFactor
            }

        val triggers =
            deficit.map { RoleTrigger(it, TriggerKind.DEFICIT, DEFICIT_TRIGGERS.getValue(it)) } +
                excess.map { RoleTrigger(it, TriggerKind.EXCESS, EXCESS_TRIGGERS.getValue(it)) }

        return SipseongRoles(groupDistribution, group5, roleComposition, excess, deficit, triggers)
    }

    /**
     * 멤버 8글자의 십성 카운트 — 천간(연·월·시, 일주 제외) + 지지 본기(연·월·일·시).
     * 일간(나)은 십성이 없어 천간에서 제외한다.
     */
    internal fun countSipseong(chart: SajuChart): List<SipSeong> {
        val dayMaster = chart.dayMaster
        return buildList {
            chart.pillars().forEach { pillar ->
                if (pillar.position != PillarPosition.DAY) add(SipSeong.of(dayMaster, pillar.gan))
                add(SipSeong.of(dayMaster, JijiHiddenStems.of(pillar.ji).mainQi))
            }
        }
    }

    /** 멤버 역할 — 최다 십성 묶음(동점이면 [SipSeongGroup] 선언 순서가 앞선 것 우선). */
    private fun roleOf(member: GroupMember): GroupRole {
        val counts = SipSeongGroup.entries.associateWith { 0 }.toMutableMap()
        for (sip in countSipseong(member.chart)) counts[sip.group] = counts.getValue(sip.group) + 1
        val top = SipSeongGroup.entries.maxByOrNull { counts.getValue(it) } ?: SipSeongGroup.BIGEOP
        return GroupRole.ofTop(top)
    }

    private val DEFICIT_TRIGGERS: Map<SipSeongGroup, String> =
        mapOf(
            SipSeongGroup.BIGEOP to "주도적으로 끌고 갈 추진 주체가 약함 — 의사결정 오너를 명확히",
            SipSeongGroup.SIKSANG to "표현·아웃풋·창의가 약함 — 결과물 발신/공유 담당 보강",
            SipSeongGroup.JAESEONG to "실리·성과 전환이 약함 — 수익화·마감 챙기는 역할 필요",
            SipSeongGroup.GWANSEONG to "규율·관리가 약함 — 일정·품질 관리자 역할 보강",
            SipSeongGroup.INSEONG to "학습·정리·회고가 약함 — 기록/회고 로테이션 도입",
        )

    private val EXCESS_TRIGGERS: Map<SipSeongGroup, String> =
        mapOf(
            SipSeongGroup.BIGEOP to "주도권이 분산될 수 있음 — 역할 경계 합의 필요",
            SipSeongGroup.SIKSANG to "아이디어는 많고 마감이 약할 수 있음 — 실행 마감자 지정",
            SipSeongGroup.JAESEONG to "성과 압박이 강할 수 있음 — 과정 가치도 점검",
            SipSeongGroup.GWANSEONG to "규율·격식이 강해 경직 위험 — 자율 여지 확보",
            SipSeongGroup.INSEONG to "수용·학습 과다로 실행이 늦을 수 있음 — 빠른 실험 권장",
        )
}
