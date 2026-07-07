package io.github.jaeyeonling.saju.group

import io.github.jaeyeonling.saju.derivation.Daeun
import io.github.jaeyeonling.saju.derivation.Seun
import io.github.jaeyeonling.saju.domain.SajuChart
import io.github.jaeyeonling.saju.interpretation.Interpretation
import io.github.jaeyeonling.saju.interpretation.InterpretationReport

/**
 * 성별 — [io.github.jaeyeonling.saju.domain.Gender] 로 승격됐다(단일 도메인 개념).
 * 이 별칭은 기존 `group.Gender` 참조의 소스 호환을 위해 남긴다. 여기 멤버 필드로서의 성별은
 * 표시·면책용이고, 대운 방향(양남음녀 순행)은 호출자가 [GroupMember.daeun] 생성 시 이미 반영한다.
 */
public typealias Gender = io.github.jaeyeonling.saju.domain.Gender

/**
 * 그룹 합성 입력 단위 — 멤버 1명의 사주판·해석 + 시간(대운/세운) 정보 묶음.
 *
 * 개인 단위 계산은 모두 [chart]/[report]/[daeun]/[seun] 에 담겨 들어온다([io.github.jaeyeonling.saju] 의
 * `KoreanSaju`·`Interpretation`·`Saju` 로 호출자가 생성). 그룹 합성은 이 재료를 **합칠 뿐 재계산하지 않는다**.
 *
 * - [chart]: 8글자·일간 — 멤버간 합충·십성 카운트·오행 분포의 원천.
 * - [report]: 개인 해석 — 표면 오행 분포([InterpretationReport.ohaeng])를 재사용한다.
 * - [daeun]: 대운 목록 — 타임라인의 현재 대운/전환기 산출.
 * - [seun]: 그 해 세운 간지 — 공통 세운 + 멤버별 세운 십성.
 *
 * 생성은 [of] 팩토리만 공개한다(주 생성자 internal) — [report] 가 [chart] 에서 파생됨을 보장해
 * 두 필드가 다른 사주를 가리키는 조용한 불일치를 차단한다.
 */
public data class GroupMember internal constructor(
    public val id: String,
    public val alias: String,
    public val gender: Gender,
    public val birthYear: Int,
    public val chart: SajuChart,
    public val report: InterpretationReport,
    public val daeun: List<Daeun>,
    public val seun: Seun? = null,
) {
    public companion object {
        /**
         * 사주판으로부터 [report] 를 계산해 [GroupMember] 를 만든다(편의 팩토리).
         * 해석 학파는 [context] 의 [GroupContext.interpretation] 을 따른다.
         */
        @JvmStatic
        @JvmOverloads
        public fun of(
            id: String,
            alias: String,
            gender: Gender,
            birthYear: Int,
            chart: SajuChart,
            daeun: List<Daeun>,
            seun: Seun? = null,
            context: GroupContext = GroupContext.DEFAULT,
        ): GroupMember =
            GroupMember(
                id = id,
                alias = alias,
                gender = gender,
                birthYear = birthYear,
                chart = chart,
                report = Interpretation.of(chart, context.interpretation),
                daeun = daeun,
                seun = seun,
            )
    }
}
