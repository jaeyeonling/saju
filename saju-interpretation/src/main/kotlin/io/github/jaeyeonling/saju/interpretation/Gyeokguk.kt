package io.github.jaeyeonling.saju.interpretation

import io.github.jaeyeonling.saju.domain.HiddenStemTable
import io.github.jaeyeonling.saju.domain.SajuChart
import io.github.jaeyeonling.saju.domain.StandardHiddenStemTable

/** 격국 유형 10종 — 문자열 대신 타입으로 담아 Java 소비자도 분기할 수 있다. */
public enum class GyeokgukType(public val koreanName: String) {
    GEONLOK("건록격"),
    YANGIN("양인격"),
    SIKSIN("식신격"),
    SANGGWAN("상관격"),
    PYEONJAE("편재격"),
    JEONGJAE("정재격"),
    PYEONGWAN("편관격"),
    JEONGGWAN("정관격"),
    PYEONIN("편인격"),
    JEONGIN("정인격"),
}

/** 격국 분류 결과. [basis] 는 사람이 읽는 근거 설명(디버그용)이고, [type] 이 기계가 분기하는 값이다. */
public data class GyeokgukResult(
    public val type: GyeokgukType,
    public val basis: String,
)

/**
 * 격국 분류 전략.
 *
 * 격국(格局) = 월령(月令, 태어난 달의 지지)을 중심으로 사주의 짜임새를 8~10유형으로 분류하는 틀.
 * 무엇을 격으로 삼느냐(월령 본기 vs 천간 투출), 8격/10격 어느 체계냐가 유파마다 달라 전략화한다.
 */
public interface GyeokgukStrategy {
    public fun classify(chart: SajuChart): GyeokgukResult
}

/** 십성 → 격 유형. 본기가 비겁인 월지는 녹왕지(건록·양인지)라 건록/양인격으로 본다. */
private fun gyeokgukTypeOf(sipSeong: SipSeong): GyeokgukType =
    when (sipSeong) {
        SipSeong.BIGYEON -> GyeokgukType.GEONLOK
        SipSeong.GEOPJAE -> GyeokgukType.YANGIN
        SipSeong.SIKSIN -> GyeokgukType.SIKSIN
        SipSeong.SANGGWAN -> GyeokgukType.SANGGWAN
        SipSeong.PYEONJAE -> GyeokgukType.PYEONJAE
        SipSeong.JEONGJAE -> GyeokgukType.JEONGJAE
        SipSeong.PYEONGWAN -> GyeokgukType.PYEONGWAN
        SipSeong.JEONGGWAN -> GyeokgukType.JEONGGWAN
        SipSeong.PYEONIN -> GyeokgukType.PYEONIN
        SipSeong.JEONGIN -> GyeokgukType.JEONGIN
    }

/**
 * 자평(子平) 기본 구현 — **월지 지장간 본기의 십성**으로 격을 정한다(단순화).
 *
 * 투출을 무시하고 월령 본기 십성으로 고정한다. 비겁이면 건록/양인격으로 본다.
 * 지장간 분야표는 [hiddenStems](기본 [StandardHiddenStemTable])로 주입 — 신강신약과 같은 테이블을 쓰면 해석이 일관된다.
 */
public class JapyeongGyeokgukStrategy
    @JvmOverloads
    constructor(
        private val hiddenStems: HiddenStemTable = StandardHiddenStemTable,
    ) : GyeokgukStrategy {
        override fun classify(chart: SajuChart): GyeokgukResult {
            val monthMainQi = hiddenStems.of(chart.month.ji).mainQi
            val sipSeong = SipSeong.of(chart.dayMaster, monthMainQi)
            return GyeokgukResult(gyeokgukTypeOf(sipSeong), "월지(${chart.month.ji}) 본기 $monthMainQi 의 $sipSeong")
        }

        public companion object {
            /** 표준 분야표 기본 전략. */
            @JvmField
            public val DEFAULT: JapyeongGyeokgukStrategy = JapyeongGyeokgukStrategy()
        }
    }

/**
 * 투출(透出) 우선 구현 — 자평진전식. 월지 지장간 중 **천간(연·월·시간)에 드러난 것**을 우선 격으로 삼는다.
 *
 * 투출 우선순위는 본기 > 중기 > 여기. 단 **비겁(比劫) 투출은 격으로 삼지 않는다**(자평진전) —
 * 건록·양인격은 월령 자체가 일간의 건록지/제왕지일 때만 성립하므로, 비겁 투출은 건너뛴다.
 * 비-비겁 투출이 없으면 월령 본기로 폴백(본기가 비겁인 월지는 녹왕지라 건록/양인격이 옳다).
 */
public class TuchulGyeokgukStrategy
    @JvmOverloads
    constructor(
        private val hiddenStems: HiddenStemTable = StandardHiddenStemTable,
    ) : GyeokgukStrategy {
        override fun classify(chart: SajuChart): GyeokgukResult {
            val monthHidden = hiddenStems.of(chart.month.ji)
            val visibleStems = listOf(chart.year.gan, chart.month.gan, chart.hour.gan)
            // 비겁이 아닌 투출만 격으로 채택(비겁 투출은 격이 아니다).
            val tuchul =
                monthHidden.all().firstOrNull {
                    it in visibleStems && SipSeong.of(chart.dayMaster, it).group != SipSeongGroup.BIGYEOP
                }
            return if (tuchul != null) {
                val sipSeong = SipSeong.of(chart.dayMaster, tuchul)
                GyeokgukResult(gyeokgukTypeOf(sipSeong), "월지(${chart.month.ji}) 투출 $tuchul 의 $sipSeong")
            } else {
                val mainQi = monthHidden.mainQi
                val sipSeong = SipSeong.of(chart.dayMaster, mainQi)
                GyeokgukResult(gyeokgukTypeOf(sipSeong), "월지(${chart.month.ji}) 본기 $mainQi 의 $sipSeong (비겁外 투출 없음)")
            }
        }

        public companion object {
            /** 표준 분야표 기본 전략. */
            @JvmField
            public val DEFAULT: TuchulGyeokgukStrategy = TuchulGyeokgukStrategy()
        }
    }
