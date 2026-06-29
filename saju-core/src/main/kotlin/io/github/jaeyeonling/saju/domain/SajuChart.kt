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
    init {
        // 각 슬롯의 위치가 일치해야 한다 — year 슬롯에 시주를 넣어 dayMaster가 조용히 틀리는 일을 막는다.
        require(year.position == PillarPosition.YEAR) { "year 슬롯에는 연주(YEAR)를 넣어야 합니다: ${year.position}" }
        require(month.position == PillarPosition.MONTH) { "month 슬롯에는 월주(MONTH)를 넣어야 합니다: ${month.position}" }
        require(day.position == PillarPosition.DAY) { "day 슬롯에는 일주(DAY)를 넣어야 합니다: ${day.position}" }
        require(hour.position == PillarPosition.HOUR) { "hour 슬롯에는 시주(HOUR)를 넣어야 합니다: ${hour.position}" }
    }

    /** 일간(日干) = '나'. 십성·신강신약 등 모든 해석의 기준. */
    public val dayMaster: Cheongan get() = day.gan

    /** 네 기둥을 연→월→일→시 순서로. */
    public fun pillars(): List<Pillar> = listOf(year, month, day, hour)

    /** 여덟 글자의 천간 4개. */
    public fun stems(): List<Cheongan> = pillars().map { it.gan }

    /** 여덟 글자의 지지 4개. */
    public fun branches(): List<Jiji> = pillars().map { it.ji }
}
