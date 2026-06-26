package io.github.jaeyeonling.saju.domain

/**
 * 사주판(원국) — 네 기둥(연·월·일·시)의 불변 집합. 사주팔자 8글자.
 *
 * 일간([dayMaster], 일주의 천간)이 모든 해석의 기준 '나'다.
 */
public data class SajuChart(
    public val year: Pillar,
    public val month: Pillar,
    public val day: Pillar,
    public val hour: Pillar,
) {
    /** 일간(日干) = '나'. 십성·신강신약 등 모든 해석의 기준. */
    public val dayMaster: Cheongan get() = day.gan

    /** 네 기둥을 연→월→일→시 순서로. */
    public fun pillars(): List<Pillar> = listOf(year, month, day, hour)

    /** 여덟 글자의 천간 4개. */
    public fun stems(): List<Cheongan> = pillars().map { it.gan }

    /** 여덟 글자의 지지 4개. */
    public fun branches(): List<Jiji> = pillars().map { it.ji }
}
