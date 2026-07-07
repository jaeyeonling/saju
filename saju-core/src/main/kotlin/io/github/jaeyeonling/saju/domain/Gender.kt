package io.github.jaeyeonling.saju.domain

/**
 * 성별(性別) — **사주 원국(4기둥 8글자)에는 영향이 없다.** 오직 대운(大運)의 진행 방향
 * (순행·역행)을 정하는 데만 쓰인다: 양남음녀(陽男陰女) 순행 — 연간이 양(陽)이면 남성이,
 * 음(陰)이면 여성이 순행한다([io.github.jaeyeonling.saju.derivation.DaeunDirection] 참조).
 *
 * 그래서 원국을 만드는 `fromCivilTime`/`fromLocalDateTime` 은 성별을 받지 않고, `daeun` 만 받는다.
 * [isMale] 은 방향 판정([io.github.jaeyeonling.saju.derivation.DaeunDirection])과의 단일 브릿지다.
 */
public enum class Gender(
    /** 한글 이름(남·여). */
    public val koreanName: String,
    /** 한자(男·女). */
    public val hanja: String,
    /** 대운 방향 판정용 — 양남음녀 규칙의 '남성' 여부. */
    public val isMale: Boolean,
) {
    MALE("남", "男", true),
    FEMALE("여", "女", false),
    ;

    public companion object {
        /**
         * `"M"`/`"F"`(대소문자 무관)·`"남"`/`"여"` → [Gender].
         *
         * 그 외 입력은 [IllegalArgumentException] — 성별은 대운 방향(순행·역행)을 뒤집는 값이라
         * 오타를 조용히 기본값으로 흡수하면 잘못된 대운이 그대로 계산된다.
         */
        @JvmStatic
        public fun fromCode(code: String): Gender =
            when {
                code.equals("M", ignoreCase = true) || code == "남" -> MALE
                code.equals("F", ignoreCase = true) || code == "여" -> FEMALE
                else -> throw IllegalArgumentException("성별 코드는 M/F(대소문자 무관) 또는 남/여 — 입력: \"$code\"")
            }
    }
}
