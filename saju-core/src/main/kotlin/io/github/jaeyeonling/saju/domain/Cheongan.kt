package io.github.jaeyeonling.saju.domain

/**
 * 천간(天干) — 하늘 글자 10개. 각 글자는 [오행][Ohaeng] + [음양][Eumyang] 라벨이다.
 * (오행 = ordinal/2, 음양 = ordinal%2 의 규칙성을 enum 정의로 고정한다.)
 *
 * 일간(日干, 일주의 천간)이 사주 해석의 기준 '나'다.
 */
public enum class Cheongan(
    public val ohaeng: Ohaeng,
    public val eumyang: Eumyang,
) {
    GAB(Ohaeng.MOK, Eumyang.YANG), // 갑 甲
    EUL(Ohaeng.MOK, Eumyang.EUM), // 을 乙
    BYEONG(Ohaeng.HWA, Eumyang.YANG), // 병 丙
    JEONG(Ohaeng.HWA, Eumyang.EUM), // 정 丁
    MU(Ohaeng.TO, Eumyang.YANG), // 무 戊
    GI(Ohaeng.TO, Eumyang.EUM), // 기 己
    GYEONG(Ohaeng.GEUM, Eumyang.YANG), // 경 庚
    SIN(Ohaeng.GEUM, Eumyang.EUM), // 신 辛
    IM(Ohaeng.SU, Eumyang.YANG), // 임 壬
    GYE(Ohaeng.SU, Eumyang.EUM), // 계 癸
    ;

    /** 천간합(天干合) 짝. 갑기·을경·병신·정임·무계 — `(ordinal+5)%10`. */
    public fun combinePartner(): Cheongan = fromIndex(ordinal + 5)

    public companion object {
        @JvmStatic
        public fun fromIndex(index: Int): Cheongan = entries[floorMod(index, entries.size)]
    }
}
