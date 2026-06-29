package io.github.jaeyeonling.saju.domain

/**
 * 지지(地支) — 땅 글자 12개 = 12띠. 각 글자는 [오행][Ohaeng] + [음양][Eumyang] 라벨이다.
 * 음양은 위치 기준(子=양, 丑=음, … 짝수 ordinal=양).
 *
 * 합·충·해는 인덱스 산술로 표현한다:
 * - 육충(六沖) `(i+6)%12`: 자오·축미·인신·묘유·진술·사해
 * - 육합(六合) `(13-i)%12`: 자축·인해·묘술·진유·사신·오미
 * - 육해(六害) `(19-i)%12`: 자미·축오·인사·묘진·신해·유술
 */
public enum class Jiji(
    public val ohaeng: Ohaeng,
    /** 한글 이름(자·축·…). */
    public val koreanName: String,
    /** 한자(子·丑·…). */
    public val hanja: String,
) {
    JA(Ohaeng.SU, "자", "子"), // 쥐
    CHUK(Ohaeng.TO, "축", "丑"), // 소
    IN(Ohaeng.MOK, "인", "寅"), // 호랑이
    MYO(Ohaeng.MOK, "묘", "卯"), // 토끼
    JIN(Ohaeng.TO, "진", "辰"), // 용
    SA(Ohaeng.HWA, "사", "巳"), // 뱀
    O(Ohaeng.HWA, "오", "午"), // 말
    MI(Ohaeng.TO, "미", "未"), // 양
    SHIN(Ohaeng.GEUM, "신", "申"), // 원숭이
    YU(Ohaeng.GEUM, "유", "酉"), // 닭
    SUL(Ohaeng.TO, "술", "戌"), // 개
    HAE(Ohaeng.SU, "해", "亥"), // 돼지
    ;

    /** 음양 = 위치 기준 ordinal % 2 (짝수=양: 子寅辰午申戌, 홀수=음). */
    public val eumyang: Eumyang get() = if (ordinal % 2 == 0) Eumyang.YANG else Eumyang.EUM

    /** 육충(六沖) 짝 — 정면충돌. */
    public fun opposite(): Jiji = fromIndex(ordinal + 6)

    /** 육합(六合) 짝 — 2글자 결합. */
    public fun sixCombinePartner(): Jiji = fromIndex(13 - ordinal)

    /** 육해(六害) 짝 — 합을 방해. */
    public fun harmPartner(): Jiji = fromIndex(19 - ordinal)

    public companion object {
        @JvmStatic
        public fun fromIndex(index: Int): Jiji = entries[floorMod(index, entries.size)]
    }
}
