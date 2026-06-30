package io.github.jaeyeonling.saju.group

import io.github.jaeyeonling.saju.derivation.Daeun
import io.github.jaeyeonling.saju.derivation.Seun
import io.github.jaeyeonling.saju.domain.Cheongan
import io.github.jaeyeonling.saju.domain.Ganji
import io.github.jaeyeonling.saju.domain.Jiji
import io.github.jaeyeonling.saju.domain.Pillar
import io.github.jaeyeonling.saju.domain.PillarPosition
import io.github.jaeyeonling.saju.domain.SajuChart
import io.github.jaeyeonling.saju.interpretation.Interpretation

/** 네 기둥(천간 to 지지)으로 사주판 구성. 음양 불일치 조합은 [Ganji] 가 생성 시 거부한다. */
internal fun chartOf(
    year: Pair<Cheongan, Jiji>,
    month: Pair<Cheongan, Jiji>,
    day: Pair<Cheongan, Jiji>,
    hour: Pair<Cheongan, Jiji>,
): SajuChart =
    SajuChart(
        year = Pillar(PillarPosition.YEAR, Ganji(year.first, year.second)),
        month = Pillar(PillarPosition.MONTH, Ganji(month.first, month.second)),
        day = Pillar(PillarPosition.DAY, Ganji(day.first, day.second)),
        hour = Pillar(PillarPosition.HOUR, Ganji(hour.first, hour.second)),
    )

/** 네 기둥 동일한 사주판(합충/오행 분포를 단순 제어하는 테스트용). */
internal fun uniformChart(
    gan: Cheongan,
    ji: Jiji,
): SajuChart = chartOf(gan to ji, gan to ji, gan to ji, gan to ji)

internal fun memberOf(
    id: String,
    chart: SajuChart,
    alias: String = id,
    birthYear: Int = 0,
    daeun: List<Daeun> = emptyList(),
    seun: Seun? = null,
): GroupMember =
    GroupMember(
        id = id,
        alias = alias,
        gender = Gender.MALE,
        birthYear = birthYear,
        chart = chart,
        report = Interpretation.of(chart),
        daeun = daeun,
        seun = seun,
    )
