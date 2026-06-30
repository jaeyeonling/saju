package io.github.jaeyeonling.saju.group

/**
 * 그룹 합성 결과 면책 — 역할·트리거·관계 라벨은 통설 기반 도구 해석 규칙이지 점술 정설이 아니다.
 * 직렬화·CLI 출력에 항상 포함되어 소비자/LLM 이 무시할 수 없게 한다.
 */
public const val GROUP_DISCLAIMER: String =
    "역할·트리거·관계 라벨은 통설(자평·합충형해) 기반 도구 해석 규칙이며 점술 정설이 아닙니다. " +
        "오행 균형은 결정론적 산술입니다."

/**
 * 그룹 합성 리포트(STAGE3) — 4차원 결과 + 사용 컨텍스트 + 면책.
 *
 * [ohaeng] 은 결정론적 산술이고, [sipseong]·[relations] 의 라벨은 통설 기반 도구 해석 규칙이다([disclaimer]).
 */
public data class GroupReport(
    public val memberIds: List<String>,
    public val ohaeng: OhaengBalance,
    public val sipseong: SipseongRoles,
    public val relations: RelationMatrix,
    public val timeline: GroupTimeline,
    public val context: GroupContext,
    public val disclaimer: String = GROUP_DISCLAIMER,
)
