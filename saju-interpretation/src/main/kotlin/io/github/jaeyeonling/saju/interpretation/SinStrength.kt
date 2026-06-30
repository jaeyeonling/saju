package io.github.jaeyeonling.saju.interpretation

import io.github.jaeyeonling.saju.domain.HiddenStemTable
import io.github.jaeyeonling.saju.domain.PillarPosition
import io.github.jaeyeonling.saju.domain.SajuChart
import io.github.jaeyeonling.saju.domain.StandardHiddenStemTable

/** 신강신약 판정 결과 — Day Master strength verdict (극신강→극신약 5단계). */
public enum class SinStrengthVerdict(
    /** 한글 이름(극신강·신강·…). */
    public val koreanName: String,
    /** 한자(極身強·身強·…). */
    public val hanja: String,
) {
    GEUKSIN_GANG("극신강", "極身強"), // 일간이 과하게 강함(설기·억제가 급함).
    SIN_GANG("신강", "身強"), // 일간이 강함(돕는 세력 우세).
    JUNGHWA("중화", "中和"), // 이상적 균형.
    SIN_YAK("신약", "身弱"), // 일간이 약함(생조가 필요).
    GEUKSIN_YAK("극신약", "極身弱"), // 일간이 과하게 약함.
    ;

    /** 신강 계열(일간이 강함). */
    public val isStrong: Boolean get() = this == GEUKSIN_GANG || this == SIN_GANG
}

/**
 * 신강신약 평가 — 일간을 돕는 세력 비율과 판정.
 *
 * [basis] 는 산출 근거(돕는 세력 점수·전체 점수·가중 정책). 용신이 이 판정에 종속되므로,
 * 근거를 노출해 "단순 합산"이 아니라 월령·지장간 차등 가중임을 드러낸다(소비자/LLM 검증용).
 *
 * [groupScores] 는 십성 5묶음(비겁·식상·재성·관성·인성)별 가중 세력 점수다. 억부 용신이
 * "무엇이 과한가"로 분기할 때의 입력이며(예: 비겁과다→관성용), 합은 [전체 세력][basis]과 같다.
 */
public data class SinStrength(
    public val supportRatio: Double,
    public val verdict: SinStrengthVerdict,
    public val basis: String = "",
    public val groupScores: Map<SipSeongGroup, Double> = emptyMap(),
)

/**
 * 신강신약 판정 전략. **정답 데이터셋이 없는 영역**이라 가중치가 곧 사양이다.
 * 골든 정답셋도 이 값을 제공하지 않으므로 결정론성만 보장한다.
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
 * @property rootStrong 통근(通根) 강근 배수 — 일간이 그 지지에서 장생·건록·제왕일 때 지장간 세력을 곱한다(重).
 * @property rootWeak 통근 약근 배수 — 일간이 묘(墓)지일 때(輕).
 * @property rootMid 통근 중간 배수 — 그 외 십이운성 단계.
 * @property geuksinGang 극신강 컷오프(지원율 ≥).
 * @property sinGang 신강 컷오프.
 * @property junghwa 중화 컷오프.
 * @property sinYak 신약 컷오프(미만은 극신약).
 */
public data class EokbuWeights(
    public val month: Double = 2.0,
    public val mainQi: Double = 1.0,
    public val midQi: Double = 0.4,
    public val residualQi: Double = 0.2,
    // 통근 가중은 학파 의존이라 기본 중립(1.0) — 셋 다 1.0이면 기존 산식과 동일(verdict·용신 보존). 튜닝 시 차등.
    public val rootStrong: Double = 1.0,
    public val rootWeak: Double = 1.0,
    public val rootMid: Double = 1.0,
    public val geuksinGang: Double = 0.70,
    public val sinGang: Double = 0.55,
    public val junghwa: Double = 0.45,
    public val sinYak: Double = 0.30,
) {
    public companion object {
        /** 한국 통설 기본 가중치. */
        @JvmField
        public val DEFAULT: EokbuWeights = EokbuWeights()
    }
}

/**
 * 억부(抑扶) 기본 구현 — 일간을 돕는 세력(비겁·인성) vs 빼는 세력(식상·재성·관성)의 비율.
 *
 * 가중치는 [weights](기본 [EokbuWeights.DEFAULT]), 지장간 분야표는 [hiddenStems](기본 [StandardHiddenStemTable]).
 * 둘 다 생성자로 주입해 '전략 교체' 없이도 '같은 억부의 파라미터 튜닝'이 가능하다.
 */
public class EokbuSinStrengthStrategy
    @JvmOverloads
    constructor(
        public val weights: EokbuWeights = EokbuWeights.DEFAULT,
        public val hiddenStems: HiddenStemTable = StandardHiddenStemTable,
        public val sibiUnseong: SibiUnseongStrategy = EumPotaeStrategy,
    ) : SinStrengthStrategy {
        override fun evaluate(chart: SajuChart): SinStrength {
            val dayMaster = chart.dayMaster
            var support = 0.0
            var total = 0.0
            val groupScores = SipSeongGroup.entries.associateWith { 0.0 }.toMutableMap()
            // 세력 1점을 돕는 세력 합·전체 합·십성 묶음별 합에 동시 반영(한 곳에서 누적).
            val accumulate: (Double, Double, SipSeongGroup) -> Unit = { s, t, g ->
                support += s
                total += t
                groupScores[g] = groupScores.getValue(g) + t
            }

            for (pillar in chart.pillars()) {
                val pillarWeight = if (pillar.position == PillarPosition.MONTH) weights.month else 1.0
                // 일주의 천간(나 자신)은 세력 계산에서 제외.
                if (pillar.position != PillarPosition.DAY) {
                    add(SipSeong.of(dayMaster, pillar.gan), pillarWeight, accumulate)
                }
                // 지장간 본기·중기·여기를 차등 가중으로 반영(연속 ratio → 5단계 verdict 모두 도달 가능).
                // 통근 배수(rooting): 일간이 그 지지에서 강근/약근이면 적용(기본 1.0=중립). 방향성을 위해
                // 일간을 '돕는' 지장간(비겁·인성)에만 곱한다 — add() 내부에서 isSupport 게이트.
                val hidden = hiddenStems.of(pillar.ji)
                val rooting = rootingFactorOf(sibiUnseong.stageOf(dayMaster, pillar.ji))
                add(SipSeong.of(dayMaster, hidden.mainQi), pillarWeight * weights.mainQi, accumulate, rooting)
                hidden.midQi?.let {
                    add(SipSeong.of(dayMaster, it), pillarWeight * weights.midQi, accumulate, rooting)
                }
                hidden.residualQi?.let {
                    add(SipSeong.of(dayMaster, it), pillarWeight * weights.residualQi, accumulate, rooting)
                }
            }

            val ratio = if (total > 0.0) support / total else NEUTRAL
            return SinStrength(ratio, verdictOf(ratio), basisOf(support, total, ratio), groupScores.toMap())
        }

        /** 산출 근거 — 돕는 세력 점수·전체 점수·가중 정책을 한 줄로(LLM 검증용). */
        private fun basisOf(
            support: Double,
            total: Double,
            ratio: Double,
        ): String =
            "돕는 세력(비겁·인성) ${"%.1f".format(support)} / 전체 ${"%.1f".format(total)} = " +
                "${"%.0f".format(ratio * 100)}% · 월령 ${fmtWeight(weights.month)}배·" +
                "지장간 정기${fmtWeight(weights.mainQi)}·중기${fmtWeight(weights.midQi)}·여기${fmtWeight(weights.residualQi)}" +
                " 가중 · 통근 $rootingPolicy"

        /** 통근 가중 정책 표시 — 셋 다 1.0이면 "중립", 아니면 강근·묘·기타 배수를 드러낸다(학파 의존). */
        private val rootingPolicy: String
            get() {
                if (weights.rootStrong == 1.0 && weights.rootWeak == 1.0 && weights.rootMid == 1.0) return "중립"
                return "강근×${fmtWeight(weights.rootStrong)}·묘×${fmtWeight(weights.rootWeak)}·" +
                    "기타×${fmtWeight(weights.rootMid)}"
            }

        /** 가중치 표시 — 정수면 정수로(2.0→"2"), 소수면 그대로(0.4→"0.4"). */
        private fun fmtWeight(w: Double): String =
            if (w == w.toLong().toDouble()) w.toLong().toString() else w.toString()

        /**
         * 십이운성 단계 → 통근 배수. 강근(장생·건록·제왕)=重, 묘=輕, 나머지=중간.
         * 기본 가중이 모두 1.0이라 중립 — 학파 의존이므로 [EokbuWeights] 튜닝으로만 차등이 켜진다.
         */
        private fun rootingFactorOf(stage: SibiUnseong): Double =
            when (stage) {
                SibiUnseong.JANGSAENG, SibiUnseong.GEOLLOK, SibiUnseong.JEWANG -> weights.rootStrong
                SibiUnseong.MYO -> weights.rootWeak
                else -> weights.rootMid
            }

        /**
         * 십성을 돕는 세력(비겁·인성)이면 support, 전체는 total, 묶음은 [SipSeong.group] 에 가산.
         * 통근 배수 [rooting] 은 방향성을 위해 **support(비겁·인성)에만** 곱한다 — 재·관·식은 base weight.
         * (기본 1.0이면 비트 동일 — 천간 호출은 rooting 미전달로 기존 동작 보존.)
         */
        private inline fun add(
            sipSeong: SipSeong,
            weight: Double,
            accumulate: (Double, Double, SipSeongGroup) -> Unit,
            rooting: Double = 1.0,
        ) {
            val supporting = isSupport(sipSeong)
            val effective = if (supporting) weight * rooting else weight
            accumulate(if (supporting) effective else 0.0, effective, sipSeong.group)
        }

        // 일간을 돕는 세력 = 비겁(같은 오행) + 인성(나를 생함).
        private fun isSupport(sipSeong: SipSeong): Boolean =
            sipSeong.group == SipSeongGroup.BIGEOP || sipSeong.group == SipSeongGroup.INSEONG

        private fun verdictOf(ratio: Double): SinStrengthVerdict =
            when {
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
            public val DEFAULT: EokbuSinStrengthStrategy = EokbuSinStrengthStrategy()
        }
    }
