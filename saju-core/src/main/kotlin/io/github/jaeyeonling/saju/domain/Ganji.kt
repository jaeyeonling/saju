package io.github.jaeyeonling.saju.domain

/**
 * 간지(干支) = 60갑자(六十甲子). 천간 10 × 지지 12 가 맞물려 60번 만에 순환한다.
 *
 * index 0 = 갑자(甲子), 1 = 을축(乙丑), … 59 = 계해(癸亥). index `i` → 천간 `i%10`, 지지 `i%12`.
 * [next] 로 순행/역행 시퀀스를 만든다(대운 계산에 쓰임).
 */
public data class Ganji(
    public val gan: Cheongan,
    public val ji: Jiji,
) {
    init {
        // 60갑자는 천간·지지의 음양(parity)이 일치할 때만 존재한다(양간-양지, 음간-음지).
        // 갑축(甲丑) 같은 무효 조합을 생성 시점에 차단 — index 호출까지 미루지 않는다.
        require(gan.ordinal % 2 == ji.ordinal % 2) {
            "유효하지 않은 60갑자 조합: $gan$ji (천간·지지 음양 불일치)"
        }
    }

    /** 60갑자 인덱스 0..59. */
    public val index: Int get() = sexagenaryIndex(gan, ji)

    /** 한글 표기(예: 경오). 천간·지지 [koreanName][Cheongan.koreanName] 합성. */
    public val koreanName: String get() = "${gan.koreanName}${ji.koreanName}"

    /** 한자 표기(예: 庚午). */
    public val hanja: String get() = "${gan.hanja}${ji.hanja}"

    /** [n] 만큼 순행(양수)/역행(음수)한 간지. */
    public fun next(n: Int): Ganji = fromIndex(index + n)

    public companion object {
        public const val CYCLE: Int = 60

        @JvmStatic
        public fun fromIndex(index: Int): Ganji {
            val normalized = floorMod(index, CYCLE)
            return Ganji(Cheongan.fromIndex(normalized), Jiji.fromIndex(normalized))
        }

        /**
         * 60갑자 전체(0..59). Java 소비자가 수정하지 못하도록 불변 래핑.
         * (KMP 승격 시 kotlinx-collections-immutable 또는 expect/actual 로 교체)
         */
        @JvmField
        public val ALL: List<Ganji> = java.util.Collections.unmodifiableList((0 until CYCLE).map { fromIndex(it) })

        // i%10==gan, i%12==ji 를 만족하는 0..59 (유효 60갑자 조합은 최대 6스텝 내 발견).
        private fun sexagenaryIndex(
            gan: Cheongan,
            ji: Jiji,
        ): Int {
            for (k in 0 until 6) {
                val candidate = gan.ordinal + 10 * k
                if (candidate % 12 == ji.ordinal) return candidate
            }
            error("유효하지 않은 60갑자 조합: $gan$ji")
        }
    }
}
