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
 */
public data class YongsinResult(
    public val yongsin: Ohaeng,
    public val method: YongsinMethod,
    public val basis: String = "",
)

/** 용신 도출 전략. 억부·조후·병약 등 방법이 다중이라 전략화한다. */
public interface YongsinStrategy {
    public fun derive(
        chart: SajuChart,
        strength: SinStrength,
    ): YongsinResult
}

/**
 * 억부(抑扶) 용신 — 신강이면 일간 기운을 빼는 오행(식상=내가 생하는 것), 신약이면 돕는 오행(인성=나를 생하는 것).
 *
 * 단순화한 기본 구현(디자인 결정). 실제 억부는 가장 약한 길신을 고르는 등 더 정교하다.
 */
public object EokbuYongsinStrategy : YongsinStrategy {
    override fun derive(
        chart: SajuChart,
        strength: SinStrength,
    ): YongsinResult {
        val dayOhaeng = chart.dayMaster.ohaeng
        val pct = "%.0f".format(strength.supportRatio * 100)
        val (yongsin, basis) =
            if (strength.verdict.isStrong) {
                val y = dayOhaeng.generates() // 신강 → 설기(식상)
                y to "${strength.verdict.koreanName}($pct%) · 일간 ${label(dayOhaeng)} 설기 → 식상 ${label(y)}"
            } else {
                val y = dayOhaeng.generatedBy() // 신약 → 생조(인성)
                y to "${strength.verdict.koreanName}($pct%) · 일간 ${label(dayOhaeng)} 생조 → 인성 ${label(y)}"
            }
        return YongsinResult(yongsin, YongsinMethod.EOKBU, basis)
    }
}

/** 오행 표시 라벨 — "목(木)". */
private fun label(ohaeng: Ohaeng): String = "${ohaeng.koreanName}(${ohaeng.hanja})"

/**
 * 조후(調候) 용신 — 신강신약과 무관하게 월령(계절)의 치우친 한난조습을 중화한다.
 *
 * 단순 한난 이분: 한(寒) 계열(봄·겨울 寅卯辰亥子丑)은 火로 데우고, 난조(暖燥) 계열(여름·가을)은 水로 식힌다.
 * (한목향양·금수상관 등 통념의 골격만 취한 단순화 — 일간별 정밀 조후표는 후속)
 * ※ 申酉(가을)→水 일괄 처리는 금수상관 火喜 등과 어긋날 수 있는 contestable 영역(정설 정답 아님).
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
        return YongsinResult(yongsin, YongsinMethod.JOHU, basis)
    }
}

/**
 * 합성(合成) 용신 — 여러 용신법을 우선순위로 절충한다(한국 실무는 억부+조후 병용이 주류).
 *
 * **세력**이 극단(극신강·극신약)이면 [primary](억부)가 급하고, 중화 근처면 [secondary](조후)로 보완한다.
 * 우선순위(어느 법을 먼저 보느냐)가 곧 학파 선택이다.
 *
 * ※ 정설 조후위급은 '기후 극단'을 분기 기준으로 삼지만, 이 구현은 verdict(세력 극단)로 근사한 것이다
 *   — 세력 축과 기후 축은 직교하므로(자월 사주라도 세력은 중화일 수 있음) 명리 정설의 충실한 인코딩은 아니다.
 */
public class CompositeYongsinStrategy
    @JvmOverloads
    constructor(
        private val primary: YongsinStrategy = EokbuYongsinStrategy,
        private val secondary: YongsinStrategy = JohuYongsinStrategy,
    ) : YongsinStrategy {
        override fun derive(
            chart: SajuChart,
            strength: SinStrength,
        ): YongsinResult =
            if (strength.verdict == SinStrengthVerdict.GEUKSIN_GANG ||
                strength.verdict == SinStrengthVerdict.GEUKSIN_YAK
            ) {
                primary.derive(chart, strength)
            } else {
                secondary.derive(chart, strength)
            }
    }
