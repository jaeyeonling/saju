package io.github.jaeyeonling.saju.group

import io.github.jaeyeonling.saju.domain.Ohaeng
import java.math.RoundingMode

/**
 * 그룹 오행 균형 합성(STAGE3a). 입력은 각 멤버의 **표면** 오행 분포([InterpretationReport.ohaeng] = 8글자).
 * 지장간 가중(ohaengWeighted)이 아니라 표면 분포를 쓴다 — 합계가 항상 8이라 그룹 비율 해석이 일관적이다.
 */
internal object OhaengSynthesis {
    fun analyze(
        members: List<GroupMember>,
        context: GroupContext,
    ): OhaengBalance {
        val groupVector = Ohaeng.entries.associateWith { oh -> members.sumOf { it.report.ohaeng.count(oh) } }
        val total = groupVector.values.sum().takeIf { it > 0 } ?: 1
        val normalized = groupVector.mapValues { (_, count) -> round3(count.toDouble() / total) }
        val expected = 1.0 / Ohaeng.entries.size

        val deficient =
            Ohaeng.entries.filter { oh ->
                groupVector.getValue(oh) == 0 || normalized.getValue(oh) < expected * context.deficitFactor
            }
        val excessive = Ohaeng.entries.filter { oh -> normalized.getValue(oh) > expected * context.excessFactor }
        val brokenChains =
            Ohaeng.entries.mapNotNull { oh ->
                val source = oh.generatedBy()
                if (groupVector.getValue(oh) > 0 && groupVector.getValue(source) == 0) BrokenChain(source, oh) else null
            }
        val dominantByMember = members.associate { it.id to it.report.ohaeng.dominant() }

        return OhaengBalance(groupVector, normalized, deficient, excessive, brokenChains, dominantByMember)
    }

    /** 소수 3자리 반올림 — Python `round(x, 3)`와 동일(십진 HALF_EVEN). 정규화 표시·임계 비교 동치. */
    private fun round3(value: Double): Double = value.toBigDecimal().setScale(SCALE, RoundingMode.HALF_EVEN).toDouble()

    private const val SCALE = 3
}
