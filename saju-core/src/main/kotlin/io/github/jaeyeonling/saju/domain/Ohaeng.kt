package io.github.jaeyeonling.saju.domain

/**
 * 오행(五行) — 목·화·토·금·수. 동양 명리의 연산 규칙(상생·상극)을 담는다.
 *
 * 상생(相生): 木→火→土→金→水→木 (ordinal+1)
 * 상극(相剋): 木→土→水→火→金→木 (ordinal+2)
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
