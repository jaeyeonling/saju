package io.github.jaeyeonling.saju.interpretation

import io.github.jaeyeonling.saju.domain.Jiji
import io.github.jaeyeonling.saju.domain.Ohaeng
import io.github.jaeyeonling.saju.domain.SajuChart

/**
 * 용신 도출법 — 결과에 어느 법으로 뽑았는지 타입으로 담는다(문자열 아님).
 *
 * Method used to derive the Yongsin (用神, the "Useful God" — the favorable element/agent that balances the chart):
 * EOKBU = suppress-or-support balancing, JOHU = seasonal climate adjustment.
 */
public enum class YongsinMethod(
    /** 한글 이름(억부·조후). */
    public val koreanName: String,
    /** 한자(抑扶·調候). */
    public val hanja: String,
) {
    EOKBU("억부", "抑扶"),
    JOHU("조후", "調候"),
}

/**
 * 용신 도출 결과.
 *
 * [basis] 는 산출 근거(왜 이 오행인가). 용신은 LLM 이 가장 자신 있게 틀리는 지점이라,
 * 근거를 함께 노출해 모델이 결론만 보고 추론을 지어내는 걸 막는다([GyeokgukResult] 의 basis 와 같은 역할).
 *
 * [decisionPath] 는 분기 트리의 통과 경로다(trace) — 한 문장으로 접힌 [basis] 와 달리
 * 판단 단계(강약 판정 → 과다 세력 비교 → 결론)를 낱개로 보존해, 시각화가 의사결정
 * 트리에서 경로를 하이라이트할 수 있게 한다. 기본값이 있어 스키마 안정.
 */
public data class YongsinResult(
    public val yongsin: Ohaeng,
    public val method: YongsinMethod,
    public val basis: String = "",
    public val decisionPath: List<String> = emptyList(),
)

/** 용신 도출 전략. 억부·조후·병약 등 방법이 다중이라 전략화한다. */
public interface YongsinStrategy {
    public fun derive(
        chart: SajuChart,
        strength: SinStrength,
    ): YongsinResult
}

/**
 * 억부(抑扶) 용신 — 신강이면 과한 세력을 누르고, 신약이면 부족을 메우는 오행. **무엇이 과한가**로 분기한다.
 *
 * [심화 p219] 억부 분기표:
 * - 신강: 비겁과다→관성(극제) / 인성과다→재성(재극인) / 균형→식상(설기)
 * - 신약·중화: 관성과다→인성(설기+생조) / 재성과다→비겁(재 극) / 식상과다→인성(제어)
 *
 * 입력은 [SinStrength.groupScores](십성 5묶음 세력). 정설 정답 없는 영역이라 학파차가 있고, [basis] 로 근거를 노출한다.
 * (groupScores 가 비면 균형/생조로 폴백 — 단순 구현과 동일 동작)
 */
public object EokbuYongsinStrategy : YongsinStrategy {
    override fun derive(
        chart: SajuChart,
        strength: SinStrength,
    ): YongsinResult {
        val day = chart.dayMaster.ohaeng
        // 일간 기준 오행 역할 (상생상극 산술로 도출).
        val bigeop = day // 비겁: 같은 오행
        val siksang = day.generates() // 식상: 내가 생함
        val jaeseong = day.controls() // 재성: 내가 극함
        val gwanseong = day.controlledBy() // 관성: 나를 극함
        val inseong = day.generatedBy() // 인성: 나를 생함

        fun score(group: SipSeongGroup): Double = strength.groupScores[group] ?: 0.0
        val pct = "%.0f".format(strength.supportRatio * 100)

        // (용신, 한 줄 근거, 분기 단계) — 분기 단계는 decisionPath 로 보존한다(트리 하이라이트용).
        val (yongsin, reason, branchStep) =
            if (strength.verdict.isStrong) {
                val bg = score(SipSeongGroup.BIGEOP)
                val ins = score(SipSeongGroup.INSEONG)
                when {
                    bg > ins ->
                        Triple(
                            gwanseong,
                            "비겁과다(비겁 ${f(bg)} > 인성 ${f(ins)}) → 관성 ${label(gwanseong)} 극제",
                            "비겁 ${f(bg)} > 인성 ${f(ins)} → 비겁과다",
                        )
                    ins > bg ->
                        Triple(
                            jaeseong,
                            "인성과다(인성 ${f(ins)} > 비겁 ${f(bg)}) → 재성 ${label(jaeseong)} 재극인",
                            "인성 ${f(ins)} > 비겁 ${f(bg)} → 인성과다",
                        )
                    else ->
                        Triple(
                            siksang,
                            "비겁·인성 균형 → 식상 ${label(siksang)} 설기",
                            "비겁 ${f(bg)} = 인성 ${f(ins)} → 균형",
                        )
                }
            } else {
                val gw = score(SipSeongGroup.GWANSEONG)
                val ja = score(SipSeongGroup.JAESEONG)
                val sik = score(SipSeongGroup.SIKSANG)
                val maxDrain = maxOf(gw, ja, sik)
                when {
                    maxDrain == 0.0 ->
                        Triple(
                            inseong,
                            "일간 ${label(day)} 생조 → 인성 ${label(inseong)}",
                            "빼는 세력 없음 → 생조",
                        )
                    gw == maxDrain ->
                        Triple(
                            inseong,
                            "관성과다(관성 ${f(gw)}) → 인성 ${label(inseong)} 설기·생조",
                            "관성 ${f(gw)} 최대 → 관성과다",
                        )
                    ja == maxDrain ->
                        Triple(
                            bigeop,
                            "재성과다(재성 ${f(ja)}) → 비겁 ${label(bigeop)} 재극",
                            "재성 ${f(ja)} 최대 → 재성과다",
                        )
                    else ->
                        Triple(
                            inseong,
                            "식상과다(식상 ${f(sik)}) → 인성 ${label(inseong)} 제어",
                            "식상 ${f(sik)} 최대 → 식상과다",
                        )
                }
            }
        return YongsinResult(
            yongsin = yongsin,
            method = YongsinMethod.EOKBU,
            basis = "${strength.verdict.koreanName}($pct%) · $reason",
            decisionPath =
                listOf(
                    "${strength.verdict.koreanName}($pct%) → ${if (strength.verdict.isStrong) "억(抑)" else "부(扶)"}",
                    branchStep,
                    "용신 ${label(yongsin)}",
                ),
        )
    }
}

/** 오행 표시 라벨 — "목(木)". */
private fun label(ohaeng: Ohaeng): String = "${ohaeng.koreanName}(${ohaeng.hanja})"

/** 세력 점수 표시 — "4.2". */
private fun f(score: Double): String = "%.1f".format(score)

/**
 * 조후(調候) 용신 — 신강신약과 무관하게 월령(계절)의 치우친 한난조습을 중화한다.
 *
 * 단순 한난 이분: 한(寒) 계열(봄·겨울 寅卯辰亥子丑)은 火로 데우고, 난조(暖燥) 계열(여름·가을)은 水로 식힌다.
 * (한목향양·금수상관 등 통념의 골격만 취한 단순화 — 일간별 정밀 조후표(궁통보감 120)는 미도입)
 *
 * ※ 중간계절(봄 寅卯辰·가을 申酉戌)의 조후는 약하다 — [CompositeYongsinStrategy] 는 기후 극단(여름·겨울)에서만
 *   조후를 우선하고 그 외엔 억부를 쓴다. 申酉(가을)→水 일괄 단정은 그 자체로 contestable 하나(금수상관 火喜 등),
 *   합성 전략 경로에서는 가을 사주가 억부로 가므로 영향이 제한된다. [기초 p277] 조후 우선순위는 학파 의존(참고).
 */
public object JohuYongsinStrategy : YongsinStrategy {
    override fun derive(
        chart: SajuChart,
        strength: SinStrength,
    ): YongsinResult {
        val (yongsin, season) =
            when (chart.month.ji) {
                Jiji.IN, Jiji.MYO, Jiji.JIN, Jiji.HAE, Jiji.JA, Jiji.CHUK -> Ohaeng.HWA to "한(寒)" // → 火
                else -> Ohaeng.SU to "난조(暖燥)" // → 水
            }
        val ji = chart.month.ji
        val basis = "월지 ${ji.koreanName}(${ji.hanja})=$season 계절 → 조후로 ${label(yongsin)}"
        return YongsinResult(
            yongsin = yongsin,
            method = YongsinMethod.JOHU,
            basis = basis,
            decisionPath =
                listOf(
                    "월지 ${ji.koreanName}(${ji.hanja}) → $season 계절",
                    "용신 ${label(yongsin)}",
                ),
        )
    }
}

/**
 * 합성(合成) 용신 — **억부 우선, 조후 보완** [격국 p391].
 *
 * 월령이 기후 극단(여름 巳午未·겨울 亥子丑)이면 한난조습 균형이 급하므로 [joho](조후)를 우선하고,
 * 그 외(봄 寅卯辰·가을 申酉戌)는 [eokbu](억부)로 세력을 본다. 이로써 "가을 금월을 일괄 水로 보는"
 * 조후의 과적용을 피한다 — 분기 축이 세력(verdict)이 아니라 **기후(월령)** 라야 조후의 본래 정의와 맞다.
 *
 * ※ 억부 vs 조후 우선순위는 학파마다 다르다(저자도 단정 회피, [기초 p277]) — 기후 극단 기준은 한 입장의 인코딩이다.
 */
public class CompositeYongsinStrategy
    @JvmOverloads
    constructor(
        private val eokbu: YongsinStrategy = EokbuYongsinStrategy,
        private val joho: YongsinStrategy = JohuYongsinStrategy,
    ) : YongsinStrategy {
        override fun derive(
            chart: SajuChart,
            strength: SinStrength,
        ): YongsinResult =
            if (chart.month.ji in CLIMATE_EXTREME_BRANCHES) {
                val result = joho.derive(chart, strength)
                result.copy(decisionPath = listOf("월령 기후 극단(여름·겨울) → 조후 우선") + result.decisionPath)
            } else {
                val result = eokbu.derive(chart, strength)
                result.copy(decisionPath = listOf("월령 기후 극단 아님 → 억부") + result.decisionPath)
            }

        private companion object {
            // 기후 극단 월령 — 여름(巳午未) 화왕·겨울(亥子丑) 수왕. 조후가 억부보다 급한 구간.
            private val CLIMATE_EXTREME_BRANCHES =
                setOf(Jiji.SA, Jiji.O, Jiji.MI, Jiji.HAE, Jiji.JA, Jiji.CHUK)
        }
    }
