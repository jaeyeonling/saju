package io.github.jaeyeonling.saju.derivation

import io.github.jaeyeonling.saju.domain.Cheongan
import io.github.jaeyeonling.saju.domain.Ganji
import io.github.jaeyeonling.saju.domain.Jiji
import io.github.jaeyeonling.saju.domain.floorMod

/**
 * 4기둥(연·월·일·시주) 도출 — 모두 **primitive 입력만 받는 순수 함수**다.
 * 천문 결과(입춘 보정 연도, 월 지지 오프셋, 율리우스일 번호, 시지)는 호출자가 미리 계산해 넘긴다.
 * → core/astronomy 경계를 명확히 하고 java.time 의존을 막는다.
 */
public object PillarDerivation {
    /**
     * 연주(年柱) — 입춘 기준으로 보정된 양력 연도에서. 1984년(갑자년) 기준 `(year−4) mod 60`.
     *
     * @param solarYear 입춘 절입을 반영해 보정된 연도(입춘 전 출생은 전년).
     */
    @JvmStatic
    public fun yearPillar(solarYear: Int): Ganji = Ganji.fromIndex(solarYear - GAPJA_YEAR_BASE)

    /**
     * 월주(月柱) — 오호둔(五虎遁, 年上起月). 월지는 인(寅)月부터 고정, 월간은 연간에서 도출.
     *
     * @param yearGan 연주의 천간.
     * @param monthBranchOffset 인월=0, 묘월=1, … 축월=11 (절기 절입 기준).
     */
    @JvmStatic
    public fun monthPillar(
        yearGan: Cheongan,
        monthBranchOffset: Int,
    ): Ganji {
        val monthJi = Jiji.fromIndex(Jiji.IN.ordinal + monthBranchOffset)
        // 오호둔(五虎遁): 월간은 연간의 인월 천간에서 시작해 월지 오프셋만큼 순행.
        val monthGan = Cheongan.fromIndex(yearGan.monthStartStem().ordinal + monthBranchOffset)
        return Ganji(monthGan, monthJi)
    }

    /**
     * 일주(日柱) — 율리우스일 번호(자정 기준 민간일)에서 60갑자로. [DAY_OFFSET] 은 골든 데이터로 고정.
     *
     * @param julianDayNumber 출생 로컬 날짜의 율리우스일 번호 `floor(JD_자정 + 0.5)`.
     */
    @JvmStatic
    public fun dayPillar(julianDayNumber: Long): Ganji =
        Ganji.fromIndex(floorMod((julianDayNumber + DAY_OFFSET).toInt(), Ganji.CYCLE))

    /**
     * 시주(時柱) — 오자둔(五子遁, 日上起時). 시간은 일간에서 도출.
     *
     * @param dayGan 일주의 천간.
     * @param hourJi 시지(자시 경계 보정 후 확정된 시각의 지지).
     */
    @JvmStatic
    public fun hourPillar(
        dayGan: Cheongan,
        hourJi: Jiji,
    ): Ganji {
        // 오자둔(五子遁): 시간은 일간의 자시 천간에서 시작해 시지 오프셋만큼 순행.
        val hourGan = Cheongan.fromIndex(dayGan.hourStartStem().ordinal + hourJi.ordinal)
        return Ganji(hourGan, hourJi)
    }

    /** 1984년 = 갑자년(60갑자 index 0)이 되도록 하는 보정값. */
    private const val GAPJA_YEAR_BASE = 4

    /**
     * 일주 60갑자 보정 상수. `Ganji.fromIndex(jdn + DAY_OFFSET)` 가 실제 일진과 맞도록 골든으로 고정.
     * (JDN 2451544 = 1999-12-31 자정 → 정묘일 등, 골든 벡터로 검증)
     */
    internal const val DAY_OFFSET = 49
}
