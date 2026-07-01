package io.github.jaeyeonling.saju.domain

/**
 * 천간(天干) — 하늘 글자 10개. 각 글자의 [오행][ohaeng]·[음양][eumyang]은 ordinal 에서 **계산**한다(손입력 아님).
 * 갑을=목, 병정=화, 무기=토, 경신=금, 임계=수. 짝수=양, 홀수=음.
 *
 * 일간(日干, 일주의 천간)이 사주 해석의 기준 '나'다.
 *
 * Cheongan (天干) = the ten Heavenly Stems. The Day Stem (일간) is the reference "self".
 */
public enum class Cheongan(
    /** 한글 이름(갑·을·…). 라이브러리 표면의 표시 라벨 — 소비자가 별도 매핑을 다시 짤 필요가 없다. */
    public val koreanName: String,
    /** 한자(甲·乙·…). */
    public val hanja: String,
) {
    GAP("갑", "甲"),
    EUL("을", "乙"),
    BYEONG("병", "丙"),
    JEONG("정", "丁"),
    MU("무", "戊"),
    GI("기", "己"),
    GYEONG("경", "庚"),
    SIN("신", "辛"),
    IM("임", "壬"),
    GYE("계", "癸"),
    ;

    /** 오행 = ordinal / 2 (갑을=목, 병정=화, …). 순서가 곧 규칙이라 별도 테이블이 없다. */
    public val ohaeng: Ohaeng get() = Ohaeng.entries[ordinal / 2]

    /** 음양 = ordinal % 2 (짝수=양, 홀수=음). */
    public val eumyang: Eumyang get() = if (ordinal % 2 == 0) Eumyang.YANG else Eumyang.EUM

    /** 천간합(天干合) 짝. 갑기·을경·병신·정임·무계 — `(ordinal+5)%10`. */
    public fun combinePartner(): Cheongan = fromIndex(ordinal + 5)

    /** 천간합(天干合) 변화 오행 — 갑기합토·을경합금·병신합수·정임합목·무계합화. `ordinal % 5`. */
    public fun combinedOhaeng(): Ohaeng = COMBINED_OHAENG[ordinal % COMBINED_OHAENG.size]

    /**
     * 천간충(天干沖) 짝. 갑경·을신·병임·정계 — ordinal 거리 6(상극 + 같은 음양 = 칠살 관계)의 정면충돌.
     * 무·기(토)는 중앙 오행이라 방위가 없어 충 짝이 없다 → `null`.
     *
     * 극(剋)과 구분: 극은 음양이 교차해도 성립하나, 충은 같은 음양이라 더 격렬하다(갑·경 둘 다 양).
     */
    public fun chungPartner(): Cheongan? =
        when {
            ordinal < 4 -> fromIndex(ordinal + 6) // 갑0→경6·을1→신7·병2→임8·정3→계9
            ordinal >= 6 -> fromIndex(ordinal - 6) // 경6→갑0·신7→을1·임8→병2·계9→정3
            else -> null // 무4·기5(토) — 충 없음
        }

    public companion object {
        /** 천간합 변화 오행 룩업 — 갑기합토·을경합금·병신합수·정임합목·무계합화 (ordinal % 5). */
        private val COMBINED_OHAENG = listOf(Ohaeng.TO, Ohaeng.GEUM, Ohaeng.SU, Ohaeng.MOK, Ohaeng.HWA)

        @JvmStatic
        public fun fromIndex(index: Int): Cheongan = entries[floorMod(index, entries.size)]
    }
}
