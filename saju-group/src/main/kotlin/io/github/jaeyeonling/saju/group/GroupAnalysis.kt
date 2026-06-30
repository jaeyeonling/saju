package io.github.jaeyeonling.saju.group

/**
 * 그룹 사주 합성 진입점 — N명의 [GroupMember] 를 4차원(오행 균형·십성 역할·멤버간 합충·세운/대운)으로 합성.
 *
 * 결정론: 같은 입력([members], [seunYear], [context]) → 같은 [GroupReport]. 시계를 읽지 않으므로
 * [seunYear] 는 호출자가 넘긴다(테스트·재현성).
 *
 * ```kotlin
 * val report = GroupAnalysis.of(members, seunYear = 2026)
 * report.ohaeng.deficient        // 그룹에 부족한 오행
 * report.relations.graph.edges   // 멤버간 협력/긴장 관계
 * ```
 */
public object GroupAnalysis {
    /** 그룹 합성. [members] 최소 2명, id 는 유일해야 한다. */
    @JvmStatic
    @JvmOverloads
    public fun of(
        members: List<GroupMember>,
        seunYear: Int,
        context: GroupContext = GroupContext.DEFAULT,
    ): GroupReport {
        require(members.size >= MIN_MEMBERS) { "그룹 합성은 최소 ${MIN_MEMBERS}명이 필요합니다: ${members.size}명" }
        val ids = members.map { it.id }
        require(ids.toSet().size == ids.size) { "멤버 id 는 유일해야 합니다: $ids" }
        return GroupReport(
            memberIds = ids,
            ohaeng = OhaengSynthesis.analyze(members, context),
            sipseong = SipseongSynthesis.analyze(members, context),
            relations = RelationMatrixBuilder.build(members),
            timeline = TimelineAnalysis.analyze(members, seunYear, context),
            context = context,
        )
    }

    private const val MIN_MEMBERS = 2
}
