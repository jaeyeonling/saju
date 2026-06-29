package io.github.jaeyeonling.saju.domain

/**
 * 자시(子時) 경계 학파 — 자정 부근 출생자의 일주(日柱)를 가르는 논쟁.
 *
 * 23:00~24:00 출생 시 일주가 통째로 달라진다. 미해결 논쟁이라 한쪽을 강제하지 않고 토글한다.
 * 시간(時干)은 일간 기준 오자둔(五子遁)이라, 일주가 시프트되면 시주의 천간도 함께 따라간다.
 */
public enum class ZishiPolicy {
    /** 정자시설(다수설, 약 80%) — 23:00 부터 다음날 일주로 본다(자시 = 하루의 시작). */
    JEONGJASI {
        override fun dayPillarShift(hour: Int): Long = if (hour >= ZISHI_START_HOUR) 1L else 0L
    },

    /** 야자시설 — 23:00~24:00 은 당일 일주를 유지하고 시주만 자시로 본다. */
    YAJASI {
        override fun dayPillarShift(hour: Int): Long = 0L
    },
    ;

    /**
     * 일주 율리우스일 번호에 더할 날짜 시프트(0 또는 1).
     * 자정(00:00) 이후는 이미 민간일이 넘어가 어느 학파든 0이다.
     */
    public abstract fun dayPillarShift(hour: Int): Long

    private companion object {
        private const val ZISHI_START_HOUR = 23
    }
}
