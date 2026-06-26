package io.github.jaeyeonling.saju.derivation

import io.github.jaeyeonling.saju.domain.Cheongan
import io.github.jaeyeonling.saju.domain.GanZhi
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
    public fun yearPillar(solarYear: Int): GanZhi = GanZhi.fromIndex(solarYear - GAPJA_YEAR_BASE)

    /**
     * 월주(月柱) — 오호둔(五虎遁, 年上起月). 월지는 인(寅)月부터 고정, 월간은 연간에서 도출.
     *
     * @param yearGan 연주의 천간.
     * @param monthBranchOffset 인월=0, 묘월=1, … 축월=11 (절기 절입 기준).
     */
    @JvmStatic
    public fun monthPillar(yearGan: Cheongan, monthBranchOffset: Int): GanZhi {
        val monthJi = Jiji.fromIndex(Jiji.IN.ordinal + monthBranchOffset)
        // 갑기년→병인월, 을경년→무인월, 병신년→경인월, 정임년→임인월, 무계년→갑인월
        val firstMonthGanIndex = (yearGan.ordinal % HEAVENLY_GROUP) * 2 + WOLDU_BASE
        val monthGan = Cheongan.fromIndex(firstMonthGanIndex + monthBranchOffset)
        return GanZhi(monthGan, monthJi)
    }

    /**
     * 일주(日柱) — 율리우스일 번호(자정 기준 민간일)에서 60갑자로. [DAY_OFFSET] 은 골든 데이터로 고정.
     *
     * @param julianDayNumber 출생 로컬 날짜의 율리우스일 번호 `floor(JD_자정 + 0.5)`.
     */
    @JvmStatic
    public fun dayPillar(julianDayNumber: Long): GanZhi =
        GanZhi.fromIndex(floorMod((julianDayNumber + DAY_OFFSET).toInt(), GanZhi.CYCLE))

    /**
     * 시주(時柱) — 오자둔(五子遁, 日上起時). 시간은 일간에서 도출.
     *
     * @param dayGan 일주의 천간.
     * @param hourJi 시지(자시 경계 보정 후 확정된 시각의 지지).
     */
    @JvmStatic
    public fun hourPillar(dayGan: Cheongan, hourJi: Jiji): GanZhi {
        // 갑기일→갑자시, 을경일→병자시, 병신일→무자시, 정임일→경자시, 무계일→임자시
        val firstHourGanIndex = (dayGan.ordinal % HEAVENLY_GROUP) * 2
        val hourGan = Cheongan.fromIndex(firstHourGanIndex + hourJi.ordinal)
        return GanZhi(hourGan, hourJi)
    }

    /** 1984년 = 갑자년(60갑자 index 0)이 되도록 하는 보정값. */
    private const val GAPJA_YEAR_BASE = 4

    /** 오호둔/오자둔에서 천간을 5그룹(갑기·을경·병신·정임·무계)으로 묶는 주기. */
    private const val HEAVENLY_GROUP = 5

    /** 오호둔 정월(인월) 천간 시작 보정: 갑기년 → 병(丙, index 2)인월. */
    private const val WOLDU_BASE = 2

    /**
     * 일주 60갑자 보정 상수. `GanZhi.fromIndex(jdn + DAY_OFFSET)` 가 실제 일진과 맞도록 골든으로 고정.
     * (JDN 2451544 = 1999-12-31 자정 → 정묘일 등, tyme4j 로 검증)
     */
    internal const val DAY_OFFSET = 49
}
