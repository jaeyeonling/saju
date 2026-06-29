package io.github.jaeyeonling.saju.interpretation

import io.github.jaeyeonling.saju.domain.Jiji
import io.github.jaeyeonling.saju.domain.Ohaeng
import io.github.jaeyeonling.saju.domain.SajuChart

/** 용신 도출법 — 결과에 어느 법으로 뽑았는지 타입으로 담는다(문자열 아님). */
public enum class YongsinMethod(public val koreanName: String) {
    BUEOK("억부"),
    JOHU("조후"),
}

/** 용신 도출 결과. */
public data class YongsinResult(
    public val yongsin: Ohaeng,
    public val method: YongsinMethod,
)

/** 용신 도출 전략. 억부·조후·병약 등 방법이 다중이라 전략화한다. */
public interface YongsinStrategy {
    public fun derive(chart: SajuChart, strength: SinStrength): YongsinResult
}

/**
 * 억부(抑扶) 용신 — 신강이면 일간 기운을 빼는 오행(식상=내가 생하는 것), 신약이면 돕는 오행(인성=나를 생하는 것).
 *
 * 단순화한 기본 구현(디자인 결정). 실제 억부는 가장 약한 길신을 고르는 등 더 정교하다.
 */
public object BueokYongsinStrategy : YongsinStrategy {
    override fun derive(chart: SajuChart, strength: SinStrength): YongsinResult {
        val dayOhaeng = chart.dayMaster.ohaeng
        val yongsin = if (strength.verdict.isStrong) {
            dayOhaeng.generates() // 신강 → 설기(식상)
        } else {
            dayOhaeng.generatedBy() // 신약 → 생조(인성)
        }
        return YongsinResult(yongsin, YongsinMethod.BUEOK)
    }
}

/**
 * 조후(調候) 용신 — 신강신약과 무관하게 월령(계절)의 치우친 한난조습을 중화한다.
 *
 * 단순 한난 이분: 한(寒) 계열(봄·겨울 寅卯辰亥子丑)은 火로 데우고, 난조(暖燥) 계열(여름·가을)은 水로 식힌다.
 * (한목향양·금수상관 등 통념의 골격만 취한 단순화 — 일간별 정밀 조후표는 후속)
 * ※ 申酉(가을)→水 일괄 처리는 금수상관 火喜 등과 어긋날 수 있는 contestable 영역(정설 정답 아님).
 */
public object JohuYongsinStrategy : YongsinStrategy {
    override fun derive(chart: SajuChart, strength: SinStrength): YongsinResult {
        val yongsin = when (chart.month.ji) {
            Jiji.IN, Jiji.MYO, Jiji.JIN, Jiji.HAE, Jiji.JA, Jiji.CHUK -> Ohaeng.HWA // 한(寒) → 火
            else -> Ohaeng.SU // 난조(暖燥) → 水
        }
        return YongsinResult(yongsin, YongsinMethod.JOHU)
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
        private val primary: YongsinStrategy = BueokYongsinStrategy,
        private val secondary: YongsinStrategy = JohuYongsinStrategy,
    ) : YongsinStrategy {
        override fun derive(chart: SajuChart, strength: SinStrength): YongsinResult =
            if (strength.verdict == SinStrengthVerdict.GEUKSIN_GANG ||
                strength.verdict == SinStrengthVerdict.GEUKSIN_YAK
            ) {
                primary.derive(chart, strength)
            } else {
                secondary.derive(chart, strength)
            }
    }
