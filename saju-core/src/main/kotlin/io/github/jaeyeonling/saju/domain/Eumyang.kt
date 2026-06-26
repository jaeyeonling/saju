package io.github.jaeyeonling.saju.domain

/** 음양(陰陽). 십성의 정(正)/편(偏) 판정에서 동이(同異) 비교축으로 쓰인다. */
public enum class Eumyang {
    YANG, // 양 陽
    EUM, // 음 陰
    ;

    public val isYang: Boolean get() = this == YANG

    public fun opposite(): Eumyang = if (this == YANG) EUM else YANG
}
