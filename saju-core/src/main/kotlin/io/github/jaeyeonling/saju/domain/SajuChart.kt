package io.github.jaeyeonling.saju.domain

/**
 * 사주판(원국) — 네 기둥(연·월·일·시)의 불변 집합.
 *
 * P0 스켈레톤: P2에서 [Pillar] 4개로 확장된다. 지금은 멀티모듈 골격과
 * java.time-free 강제 장치를 검증하기 위한 최소 자리표시자다.
 */
public data class SajuChart(
    public val skeletonMarker: String = "P0",
)
