package io.github.jaeyeonling.saju.domain

/**
 * 천간(天干) — 하늘 글자 10개. 각 글자의 [오행][ohaeng]·[음양][eumyang]은 ordinal 에서 **계산**한다(손입력 아님).
 * 갑을=목, 병정=화, 무기=토, 경신=금, 임계=수. 짝수=양, 홀수=음.
 *
 * 일간(日干, 일주의 천간)이 사주 해석의 기준 '나'다.
 */
public enum class Cheongan {
    GAB, // 갑 甲
    EUL, // 을 乙
    BYEONG, // 병 丙
    JEONG, // 정 丁
    MU, // 무 戊
    GI, // 기 己
    GYEONG, // 경 庚
    SIN, // 신 辛
    IM, // 임 壬
    GYE, // 계 癸
    ;

    /** 오행 = ordinal / 2 (갑을=목, 병정=화, …). 순서가 곧 규칙이라 별도 테이블이 없다. */
    public val ohaeng: Ohaeng get() = Ohaeng.entries[ordinal / 2]

    /** 음양 = ordinal % 2 (짝수=양, 홀수=음). */
    public val eumyang: Eumyang get() = if (ordinal % 2 == 0) Eumyang.YANG else Eumyang.EUM

    /** 천간합(天干合) 짝. 갑기·을경·병신·정임·무계 — `(ordinal+5)%10`. */
    public fun combinePartner(): Cheongan = fromIndex(ordinal + 5)

    public companion object {
        @JvmStatic
        public fun fromIndex(index: Int): Cheongan = entries[floorMod(index, entries.size)]
    }
}
