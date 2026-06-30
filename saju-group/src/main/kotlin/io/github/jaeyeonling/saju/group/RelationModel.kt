package io.github.jaeyeonling.saju.group

import io.github.jaeyeonling.saju.domain.Ohaeng

/** 멤버간 관계 종류 — 합충형파해. */
public enum class RelationKind(
    /** 한글 이름(천간합·육합·충·…). */
    public val koreanName: String,
) {
    CHEONGAN_HAP("천간합"),
    YUKHAP("육합"),
    CHUNG("충"),
    HYEONG("형"),
    PA("파"),
    HAE("해"),
    SAMHAP("삼합"),
    BANHAP("반합"),
}

/**
 * 관계 라벨 — '8명 한 팀' 비유의 협력/긴장 색채와 net 집계용 [weight].
 *
 * [COMPLEX]/[NEUTRAL] 은 개별 관계엔 쓰이지 않고 net 집계 결과로만 등장한다(weight=0).
 * 통설(합충형해)을 도구가 팀 관계로 환산한 **해석 규칙**이며 점술 정설이 아니다.
 */
public enum class RelationLabel(
    /** 한글 이름(협력·보완·긴장·마찰·복합·중립). */
    public val koreanName: String,
    /** net 집계 가중(양수=끌림, 음수=부딪힘). */
    public val weight: Int,
) {
    COOPERATION("협력", 2),
    COMPLEMENT("보완", 1),
    TENSION("긴장", -2),
    FRICTION("마찰", -1),
    COMPLEX("복합", 0),
    NEUTRAL("중립", 0),
}

/** 멤버 쌍의 개별 관계 한 건. */
public data class PairRelation(
    public val kind: RelationKind,
    public val label: RelationLabel,
    /** 표시 상세(예: "갑기합토", "자오충", "인오술삼합화"). */
    public val detail: String,
)

/** 멤버 쌍의 관계 묶음 + net 라벨. */
public data class MemberPair(
    public val a: String,
    public val b: String,
    public val relations: List<PairRelation>,
    public val netLabel: RelationLabel,
)

/** 관계 그래프 노드 — 멤버 + 최다 오행. */
public data class GraphNode(
    public val id: String,
    public val alias: String,
    public val dominant: Ohaeng,
)

/** 관계 그래프 엣지 — net 라벨이 중립이 아닌 쌍만. */
public data class GraphEdge(
    public val from: String,
    public val to: String,
    public val label: RelationLabel,
)

/** 관계 그래프 — 노드(멤버) + 엣지(net 관계). */
public data class RelationGraph(
    public val nodes: List<GraphNode>,
    public val edges: List<GraphEdge>,
)

/**
 * 멤버간 합충 매트릭스(STAGE3c).
 *
 * 라벨/그래프는 통설(합충형해)을 도구가 팀 관계로 환산한 **해석 규칙**(점술 정설 아님)이다.
 */
public data class RelationMatrix(
    public val pairs: List<MemberPair>,
    public val graph: RelationGraph,
)
