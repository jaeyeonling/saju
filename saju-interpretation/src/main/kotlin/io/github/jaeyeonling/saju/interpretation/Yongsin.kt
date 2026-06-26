package io.github.jaeyeonling.saju.interpretation

import io.github.jaeyeonling.saju.domain.Ohaeng
import io.github.jaeyeonling.saju.domain.SajuChart

/** 용신 도출 결과. */
public data class YongsinResult(
    public val yongsin: Ohaeng,
    public val method: String,
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
        return YongsinResult(yongsin, "억부")
    }
}
