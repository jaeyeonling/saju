package io.github.jaeyeonling.saju.interpretation

import io.github.jaeyeonling.saju.domain.HiddenStemTable
import io.github.jaeyeonling.saju.domain.PillarPosition
import io.github.jaeyeonling.saju.domain.SajuChart
import io.github.jaeyeonling.saju.domain.StandardHiddenStemTable

/** 신강신약 판정 결과. */
public enum class SinStrengthVerdict {
    GEUKSIN_GANG, // 극신강 極身強 — 일간이 과하게 강함(설기·억제가 급함).
    SIN_GANG, // 신강 身強 — 일간이 강함(돕는 세력 우세).
    JUNGHWA, // 중화 中和 — 이상적 균형.
    SIN_YAK, // 신약 身弱 — 일간이 약함(생조가 필요).
    GEUKSIN_YAK, // 극신약 極身弱 — 일간이 과하게 약함.
    ;

    /** 신강 계열(일간이 강함). */
    public val isStrong: Boolean get() = this == GEUKSIN_GANG || this == SIN_GANG
}

/** 신강신약 평가 — 일간을 돕는 세력 비율과 판정. */
public data class SinStrength(
    public val supportRatio: Double,
    public val verdict: SinStrengthVerdict,
)

/**
 * 신강신약 판정 전략. **정답 데이터셋이 없는 영역**이라 가중치가 곧 사양이다.
 * tyme4j 도 이 값을 제공하지 않으므로 결정론성만 보장한다.
 */
public interface SinStrengthStrategy {
    public fun evaluate(chart: SajuChart): SinStrength
}

/**
 * 억부(抑扶) 가중치/임계값 — 디자인 결정 = 사양. 같은 억부법 안에서 유파별 미세 조정을 [copy] 로 연다.
 *
 * @property month 월지(月令)는 계절의 힘이라 배수 가중.
 * @property mainQi 지장간 본기 가중(천간 1점 기준).
 * @property midQi 지장간 중기 가중.
 * @property residualQi 지장간 여기 가중.
 * @property geuksinGang 극신강 컷오프(지원율 ≥).
 * @property sinGang 신강 컷오프.
 * @property junghwa 중화 컷오프.
 * @property sinYak 신약 컷오프(미만은 극신약).
 */
public data class BueokWeights(
    public val month: Double = 2.0,
    public val mainQi: Double = 1.0,
    public val midQi: Double = 0.4,
    public val residualQi: Double = 0.2,
    public val geuksinGang: Double = 0.70,
    public val sinGang: Double = 0.55,
    public val junghwa: Double = 0.45,
    public val sinYak: Double = 0.30,
) {
    public companion object {
        /** 한국 통설 기본 가중치. */
        @JvmField
        public val DEFAULT: BueokWeights = BueokWeights()
    }
}

/**
 * 억부(抑扶) 기본 구현 — 일간을 돕는 세력(비겁·인성) vs 빼는 세력(식상·재성·관성)의 비율.
 *
 * 가중치는 [weights](기본 [BueokWeights.DEFAULT]), 지장간 분야표는 [hiddenStems](기본 [StandardHiddenStemTable]).
 * 둘 다 생성자로 주입해 '전략 교체' 없이도 '같은 억부의 파라미터 튜닝'이 가능하다.
 */
public class BueokSinStrengthStrategy
    @JvmOverloads
    constructor(
        public val weights: BueokWeights = BueokWeights.DEFAULT,
        public val hiddenStems: HiddenStemTable = StandardHiddenStemTable,
    ) : SinStrengthStrategy {
        override fun evaluate(chart: SajuChart): SinStrength {
            val dayMaster = chart.dayMaster
            var support = 0.0
            var total = 0.0

            for (pillar in chart.pillars()) {
                val pillarWeight = if (pillar.position == PillarPosition.MONTH) weights.month else 1.0
                // 일주의 천간(나 자신)은 세력 계산에서 제외.
                if (pillar.position != PillarPosition.DAY) {
                    add(SipSeong.of(dayMaster, pillar.gan), pillarWeight) { s, t -> support += s; total += t }
                }
                // 지장간 본기·중기·여기를 차등 가중으로 반영(연속 ratio → 5단계 verdict 모두 도달 가능).
                val hidden = hiddenStems.of(pillar.ji)
                add(SipSeong.of(dayMaster, hidden.mainQi), pillarWeight * weights.mainQi) { s, t -> support += s; total += t }
                hidden.midQi?.let {
                    add(SipSeong.of(dayMaster, it), pillarWeight * weights.midQi) { s, t -> support += s; total += t }
                }
                hidden.residualQi?.let {
                    add(SipSeong.of(dayMaster, it), pillarWeight * weights.residualQi) { s, t -> support += s; total += t }
                }
            }

            val ratio = if (total > 0.0) support / total else NEUTRAL
            return SinStrength(ratio, verdictOf(ratio))
        }

        /** 십성을 돕는 세력(비겁·인성)이면 support, 전체는 total 에 가산. */
        private inline fun add(sipSeong: SipSeong, weight: Double, accumulate: (Double, Double) -> Unit) {
            val support = if (isSupport(sipSeong)) weight else 0.0
            accumulate(support, weight)
        }

        // 일간을 돕는 세력 = 비겁(같은 오행) + 인성(나를 생함).
        private fun isSupport(sipSeong: SipSeong): Boolean =
            sipSeong.group == SipSeongGroup.BIGYEOP || sipSeong.group == SipSeongGroup.INSEONG

        private fun verdictOf(ratio: Double): SinStrengthVerdict = when {
            ratio >= weights.geuksinGang -> SinStrengthVerdict.GEUKSIN_GANG
            ratio >= weights.sinGang -> SinStrengthVerdict.SIN_GANG
            ratio >= weights.junghwa -> SinStrengthVerdict.JUNGHWA
            ratio >= weights.sinYak -> SinStrengthVerdict.SIN_YAK
            else -> SinStrengthVerdict.GEUKSIN_YAK
        }

        public companion object {
            private const val NEUTRAL = 0.5

            /** 한국 통설 기본 전략(억부·표준 분야표). */
            @JvmField
            public val DEFAULT: BueokSinStrengthStrategy = BueokSinStrengthStrategy()
        }
    }
