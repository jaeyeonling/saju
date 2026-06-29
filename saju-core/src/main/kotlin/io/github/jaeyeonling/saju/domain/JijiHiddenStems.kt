package io.github.jaeyeonling.saju.domain

/**
 * 지장간(地藏干) — 지지가 속에 품은 천간(본기/중기/여기).
 *
 * - **본기**(本氣) = 지지를 대표하는 가장 강한 기운, **중기**(中氣)·**여기**(餘氣) = 부차적으로 품은 기운.
 * - **통근**(通根): 천간이 같은 오행을 지지 지장간에서 만나 '뿌리내려' 힘을 얻는 것 — 신강신약의 핵심 입력.
 * - **투출**(透出): 지장간이 천간에 드러나는 것 — 격국 판정의 입력([io.github.jaeyeonling.saju] interpretation 의 Gyeokguk).
 *
 * 데이터는 tyme4j 와 검증된 일반본 기준.
 */
public data class JijiHiddenStems(
    public val mainQi: Cheongan,
    public val midQi: Cheongan?,
    public val residualQi: Cheongan?,
) {
    /** 본기·중기·여기를 순서대로(없는 것 제외). */
    public fun all(): List<Cheongan> = listOfNotNull(mainQi, midQi, residualQi)

    public companion object {
        /** 지지의 지장간. */
        @JvmStatic
        public fun of(jiji: Jiji): JijiHiddenStems = TABLE[jiji.ordinal]

        // Jiji 순서(자축인묘진사오미신유술해). null = 중기/여기 없음.
        private val TABLE: List<JijiHiddenStems> = listOf(
            hidden(Cheongan.GYE), // 子 癸
            hidden(Cheongan.GI, Cheongan.GYE, Cheongan.SIN), // 丑 기계신
            hidden(Cheongan.GAB, Cheongan.BYEONG, Cheongan.MU), // 寅 갑병무
            hidden(Cheongan.EUL), // 卯 乙
            hidden(Cheongan.MU, Cheongan.EUL, Cheongan.GYE), // 辰 무을계
            hidden(Cheongan.BYEONG, Cheongan.GYEONG, Cheongan.MU), // 巳 병경무
            hidden(Cheongan.JEONG, Cheongan.GI, null), // 午 정기
            hidden(Cheongan.GI, Cheongan.JEONG, Cheongan.EUL), // 未 기정을
            hidden(Cheongan.GYEONG, Cheongan.IM, Cheongan.MU), // 申 경임무
            hidden(Cheongan.SIN), // 酉 辛
            hidden(Cheongan.MU, Cheongan.SIN, Cheongan.JEONG), // 戌 무신정
            hidden(Cheongan.IM, Cheongan.GAB, null), // 亥 임갑
        )

        private fun hidden(main: Cheongan, mid: Cheongan? = null, residual: Cheongan? = null) =
            JijiHiddenStems(main, mid, residual)
    }
}
