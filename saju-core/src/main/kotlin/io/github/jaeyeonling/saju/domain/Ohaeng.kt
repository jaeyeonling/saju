package io.github.jaeyeonling.saju.domain

/**
 * 오행(五行) — 목·화·토·금·수. 동양 명리의 연산 규칙(상생·상극)을 담는다.
 *
 * 상생(相生): 木→火→土→金→水→木 (ordinal+1) — 나무가 불을 피우고, 불이 재(흙)를 남기고,
 *   흙이 쇠를 품고, 쇠에 물이 맺히고, 물이 나무를 키운다.
 * 상극(相剋): 木→土→水→火→金→木 (ordinal+2) — 나무가 흙을 파고, 흙이 물을 막고,
 *   물이 불을 끄고, 불이 쇠를 녹이고, 쇠가 나무를 벤다.
 */
public enum class Ohaeng {
    MOK, // 목 木
    HWA, // 화 火
    TO, // 토 土
    GEUM, // 금 金
    SU, // 수 水
    ;

    /** 이 오행이 생(生)하는 오행 (내가 낳는 것). */
    public fun generates(): Ohaeng = entries[(ordinal + 1) % SIZE]

    /** 이 오행이 극(剋)하는 오행 (내가 누르는 것). */
    public fun controls(): Ohaeng = entries[(ordinal + 2) % SIZE]

    /** 이 오행을 생하는 오행 (나를 낳는 것). */
    public fun generatedBy(): Ohaeng = entries[(ordinal + SIZE - 1) % SIZE]

    /** 이 오행을 극하는 오행 (나를 누르는 것). */
    public fun controlledBy(): Ohaeng = entries[(ordinal + SIZE - 2) % SIZE]

    private companion object {
        const val SIZE = 5
    }
}
