package io.github.jaeyeonling.saju.serialization

import io.github.jaeyeonling.saju.group.GroupReport
import io.github.jaeyeonling.saju.group.GroupTimeline
import io.github.jaeyeonling.saju.group.MemberPair
import io.github.jaeyeonling.saju.group.OhaengBalance
import io.github.jaeyeonling.saju.group.RelationMatrix
import io.github.jaeyeonling.saju.group.SipseongRoles
import kotlinx.serialization.encodeToString

/*
 * 그룹 합성(GroupReport) → JSON 직렬화.
 *
 * 도메인 enum 은 한글 라벨로, enum-key Map 은 한글 문자열-key Map 으로 평탄화한다(기존 toDto 관례).
 * Ganji 는 기존 [toDto] 를 재사용한다.
 */

public fun GroupReport.toDto(): GroupReportDto =
    GroupReportDto(
        memberIds = memberIds,
        ohaeng = ohaeng.toDto(),
        sipseong = sipseong.toDto(),
        relationMatrix = relations.toDto(),
        timeline = timeline.toDto(),
        context = GroupContextDto(context.deficitFactor, context.excessFactor, context.transitionWindow),
        disclaimer = disclaimer,
    )

/** 그룹 합성 리포트를 JSON 문자열로. */
public fun GroupReport.toJson(): String = sajuJson.encodeToString(toDto())

private fun OhaengBalance.toDto(): OhaengBalanceDto =
    OhaengBalanceDto(
        groupVector = groupVector.entries.associate { (ohaeng, count) -> ohaeng.koreanName to count },
        normalized = normalized.entries.associate { (ohaeng, ratio) -> ohaeng.koreanName to ratio },
        deficient = deficient.map { it.koreanName },
        excessive = excessive.map { it.koreanName },
        brokenChains = brokenChains.map { it.description },
        dominantByMember = dominantByMember.entries.associate { (id, ohaeng) -> id to ohaeng.koreanName },
    )

private fun SipseongRoles.toDto(): SipseongRolesDto =
    SipseongRolesDto(
        groupDistribution = groupDistribution.entries.associate { (sip, count) -> sip.koreanName to count },
        group5 = group5.entries.associate { (group, count) -> group.koreanName to count },
        roleComposition = roleComposition.entries.associate { (role, ids) -> role.koreanName to ids },
        excess = excess.map { it.koreanName },
        deficit = deficit.map { it.koreanName },
        triggers = triggers.map { it.message },
    )

private fun RelationMatrix.toDto(): RelationMatrixDto =
    RelationMatrixDto(
        pairs = pairs.map { it.toDto() },
        nodes = graph.nodes.map { GraphNodeDto(it.id, it.alias, it.dominant.koreanName) },
        edges = graph.edges.map { GraphEdgeDto(it.from, it.to, it.label.name, it.label.koreanName) },
    )

private fun MemberPair.toDto(): MemberPairDto =
    MemberPairDto(
        a = a,
        b = b,
        relations =
            relations.map {
                PairRelationDto(it.kind.name, it.kind.koreanName, it.label.name, it.label.koreanName, it.detail)
            },
        netLabel = netLabel.name,
        netLabelKorean = netLabel.koreanName,
    )

private fun GroupTimeline.toDto(): GroupTimelineDto =
    GroupTimelineDto(
        year = year,
        groupSeun = groupSeun?.toDto(),
        memberSeunSipseong =
            memberSeunSipseong.entries.associate { (id, seun) ->
                id to MemberSeunDto(seun.alias, seun.sipSeong?.koreanName)
            },
        currentDaeun =
            currentDaeun.entries.associate { (id, daeun) ->
                id to CurrentDaeunDto(daeun.alias, daeun.startAge, daeun.ganji.toDto(), daeun.age)
            },
        daeunTransitions =
            daeunTransitions.map {
                DaeunTransitionDto(it.memberId, it.alias, it.startAge, it.ganji.toDto(), it.age)
            },
    )
