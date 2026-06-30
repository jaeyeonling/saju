package io.github.jaeyeonling.saju.domain

/** 기둥 위치 — 연주·월주·일주·시주. */
public enum class PillarPosition {
    YEAR, // 연주 年柱
    MONTH, // 월주 月柱
    DAY, // 일주 日柱
    HOUR, // 시주 時柱
}

/** 사주의 한 기둥 — 간지 한 쌍 + 위치. */
public data class Pillar(
    public val position: PillarPosition,
    public val ganji: Ganji,
) {
    public val gan: Cheongan get() = ganji.gan
    public val ji: Jiji get() = ganji.ji
}
