package io.github.jaeyeonling.saju.serialization

import kotlinx.serialization.Serializable

/*
 * 그룹 합성(saju-group) 직렬화용 DTO 묶음.
 *
 * 도메인의 enum-key Map 은 한글 문자열-key Map 으로, enum 값은 한글 라벨로 평탄화한다(기존 DTO 관례).
 * 키는 camelCase 로, vault Python 도구의 computed.json `group` 블록(snake_case)과 의미상 1:1 대응한다
 * (group_name/persons 래퍼는 CLI 가 조립 — 이 DTO 는 group 블록만 담당).
 */

/** 끊긴 상생 체인은 "수→목 공급원 결여" 같은 설명 문자열로 평탄화. */
@Serializable
public data class OhaengBalanceDto(
    public val groupVector: Map<String, Int>,
    public val normalized: Map<String, Double>,
    public val deficient: List<String>,
    public val excessive: List<String>,
    public val brokenChains: List<String>,
    public val dominantByMember: Map<String, String>,
)

/** 역할/트리거는 한글 라벨·메시지로 평탄화(통설 기반 도구 규칙). */
@Serializable
public data class SipseongRolesDto(
    public val groupDistribution: Map<String, Int>,
    public val group5: Map<String, Int>,
    public val roleComposition: Map<String, List<String>>,
    public val excess: List<String>,
    public val deficit: List<String>,
    public val triggers: List<String>,
)

/**
 * 멤버 쌍의 개별 관계. [kind]/[label] 은 기계 분기용 영문 enum 이름(CHEONGAN_HAP·COOPERATION…),
 * [kindKorean]/[labelKorean] 은 표시용 한글(천간합·협력…) — 기존 Dtos 의 영문+한글 이중 노출 원칙을 따른다.
 */
@Serializable
public data class PairRelationDto(
    public val kind: String,
    public val kindKorean: String,
    public val label: String,
    public val labelKorean: String,
    public val detail: String,
)

/** 멤버 쌍 — [netLabel] 영문(COOPERATION/TENSION/COMPLEX/NEUTRAL) + [netLabelKorean] 한글. */
@Serializable
public data class MemberPairDto(
    public val a: String,
    public val b: String,
    public val relations: List<PairRelationDto>,
    public val netLabel: String,
    public val netLabelKorean: String,
)

/** 그래프 노드 — 멤버 + 최다 오행(한글). */
@Serializable
public data class GraphNodeDto(
    public val id: String,
    public val alias: String,
    public val dominant: String,
)

/** 그래프 엣지 — net 라벨 영문([label]) + 한글([labelKorean]). */
@Serializable
public data class GraphEdgeDto(
    public val from: String,
    public val to: String,
    public val label: String,
    public val labelKorean: String,
)

/** 멤버간 합충 매트릭스. */
@Serializable
public data class RelationMatrixDto(
    public val pairs: List<MemberPairDto>,
    public val nodes: List<GraphNodeDto>,
    public val edges: List<GraphEdgeDto>,
)

/** 멤버 세운 십성(없으면 null). */
@Serializable
public data class MemberSeunDto(
    public val alias: String,
    public val sipSeong: String? = null,
)

/** 멤버 현재 대운. */
@Serializable
public data class CurrentDaeunDto(
    public val alias: String,
    public val startAge: Int,
    public val ganji: GanjiDto,
    public val age: Int,
)

/** 멤버 대운 전환기. */
@Serializable
public data class DaeunTransitionDto(
    public val memberId: String,
    public val alias: String,
    public val startAge: Int,
    public val ganji: GanjiDto,
    public val age: Int,
)

/** 그룹 시간축 — 공통 세운 + 멤버별 세운 십성 + 현재 대운/전환기. */
@Serializable
public data class GroupTimelineDto(
    public val year: Int,
    public val groupSeun: GanjiDto? = null,
    public val memberSeunSipseong: Map<String, MemberSeunDto>,
    public val currentDaeun: Map<String, CurrentDaeunDto>,
    public val daeunTransitions: List<DaeunTransitionDto>,
)

/** 그룹 합성 튜닝 스냅샷(재현성). */
@Serializable
public data class GroupContextDto(
    public val deficitFactor: Double,
    public val excessFactor: Double,
    public val transitionWindow: Int,
)

/**
 * 그룹 합성 리포트 전체.
 * [disclaimer] 는 항상 채워져 역할/관계 라벨이 점술 정설이 아님을 소비자에게 명시한다.
 */
@Serializable
public data class GroupReportDto(
    public val memberIds: List<String>,
    public val ohaeng: OhaengBalanceDto,
    public val sipseong: SipseongRolesDto,
    public val relationMatrix: RelationMatrixDto,
    public val timeline: GroupTimelineDto,
    public val context: GroupContextDto,
    public val disclaimer: String,
)
