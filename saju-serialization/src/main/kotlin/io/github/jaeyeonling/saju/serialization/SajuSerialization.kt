// Java 소비자에게 파일명 유래 `SajuSerializationKt` 대신 안정적인 `SajuSerialization` 을 노출한다.
// 파일을 rename 해도 이 JvmName 만 유지하면 JVM 바이너리 호환이 깨지지 않는다.
@file:JvmName("SajuSerialization")

package io.github.jaeyeonling.saju.serialization

import io.github.jaeyeonling.saju.derivation.Daeun
import io.github.jaeyeonling.saju.domain.Cheongan
import io.github.jaeyeonling.saju.domain.Ganji
import io.github.jaeyeonling.saju.domain.Gender
import io.github.jaeyeonling.saju.domain.JijiHiddenStems
import io.github.jaeyeonling.saju.domain.Pillar
import io.github.jaeyeonling.saju.domain.SajuChart
import io.github.jaeyeonling.saju.interpretation.GyeokgukResult
import io.github.jaeyeonling.saju.interpretation.HapChungRelation
import io.github.jaeyeonling.saju.interpretation.InterpretationReport
import io.github.jaeyeonling.saju.interpretation.PillarSipSeong
import io.github.jaeyeonling.saju.interpretation.SinStrength
import io.github.jaeyeonling.saju.interpretation.YongsinResult
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/*
 * 사주 도메인 → JSON 직렬화 진입점.
 *
 * 도메인/해석 타입에 toDto()/toJson() 확장을 더해 REST 응답·로깅·캐싱 키로 바로 쓸 수 있게 한다.
 * 역방향(JSON→도메인)은 DTO 까지만 제공한다 — 사주판은 생성 불변식이 있어 입력(생년월일시)에서
 * 재계산하는 것이 자연스럽기 때문이다.
 */

/** 사람이 읽기 좋은 들여쓰기 JSON. 기본값도 출력해 응답 스키마가 안정적이다. */
public val sajuJson: Json =
    Json {
        prettyPrint = true
        encodeDefaults = true
    }

public fun Ganji.toDto(): GanjiDto =
    GanjiDto(
        name = koreanName,
        hanja = hanja,
        gan = gan.koreanName,
        ji = ji.koreanName,
        index = index,
    )

public fun Cheongan.toDto(): CheonganDto =
    CheonganDto(
        name = koreanName,
        hanja = hanja,
        ohaeng = ohaeng.koreanName,
        eumyang = eumyang.koreanName,
    )

public fun Pillar.toDto(): PillarDto = PillarDto(position = position.name, ganji = ganji.toDto())

public fun SajuChart.toDto(): SajuChartDto =
    SajuChartDto(
        year = year.toDto(),
        month = month.toDto(),
        day = day.toDto(),
        hour = hour.toDto(),
        dayMaster = dayMaster.toDto(),
    )

/** 사주판을 JSON 문자열로. */
public fun SajuChart.toJson(): String = sajuJson.encodeToString(toDto())

public fun Daeun.toDto(): DaeunDto = DaeunDto(startAge = startAge, ganji = ganji.toDto())

/** 대운 시퀀스를 성별과 함께 DTO 로 — 성별이 방향(순행·역행)을 정했음을 JSON 에 드러낸다. */
public fun List<Daeun>.toDaeunSeriesDto(gender: Gender): DaeunSeriesDto =
    DaeunSeriesDto(
        gender = gender.name,
        genderKorean = gender.koreanName,
        daeun = map { it.toDto() },
    )

/** 대운 시퀀스를 성별과 함께 JSON 문자열로. */
public fun List<Daeun>.toDaeunJson(gender: Gender): String = sajuJson.encodeToString(toDaeunSeriesDto(gender))

public fun SinStrength.toDto(): SinStrengthDto =
    SinStrengthDto(
        verdict = verdict.name,
        verdictKorean = verdict.koreanName,
        supportRatio = supportRatio,
        basis = basis,
        groupScores = groupScores.entries.associate { (group, score) -> group.koreanName to score },
        contributions = contributions.map { it.toDto() },
        supportScore = supportScore,
        totalScore = totalScore,
    )

public fun YongsinResult.toDto(): YongsinDto =
    YongsinDto(
        ohaeng = yongsin.koreanName,
        method = method.name,
        methodKorean = method.koreanName,
        basis = basis,
        decisionPath = decisionPath,
    )

public fun GyeokgukResult.toDto(): GyeokgukDto =
    GyeokgukDto(
        type = type.name,
        typeKorean = type.koreanName,
        basis = basis,
    )

public fun HapChungRelation.toDto(): HapChungDto =
    when (this) {
        is HapChungRelation.CheonganHap ->
            HapChungDto("천간합", listOf(a.koreanName, b.koreanName), transformsTo?.koreanName)
        is HapChungRelation.CheonganChung -> HapChungDto("천간충", listOf(a.koreanName, b.koreanName))
        is HapChungRelation.JijiYukhap -> HapChungDto("육합", listOf(a.koreanName, b.koreanName))
        is HapChungRelation.JijiYukchung -> HapChungDto("육충", listOf(a.koreanName, b.koreanName))
        is HapChungRelation.JijiYukhae -> HapChungDto("육해", listOf(a.koreanName, b.koreanName))
        is HapChungRelation.JijiSamhap ->
            HapChungDto("삼합", members.map { it.koreanName }, transformsTo.koreanName)
        is HapChungRelation.JijiBanghap ->
            HapChungDto("방합", members.map { it.koreanName }, transformsTo.koreanName)
    }

public fun InterpretationReport.toDto(): InterpretationReportDto =
    InterpretationReportDto(
        strength = strength.toDto(),
        yongsin = yongsin.toDto(),
        gyeokguk = gyeokguk.toDto(),
        gongmang = listOf(gongmang.first.koreanName, gongmang.second.koreanName),
        hapChung = hapChung.map { it.toDto() },
        ohaengCounts = ohaeng.counts.entries.associate { (ohaeng, count) -> ohaeng.koreanName to count },
        dominantOhaeng = ohaeng.dominant().koreanName,
        sibiUnseong = sibiUnseong.entries.associate { (position, stage) -> position.name to stage.koreanName },
        sipSeong = sipSeong.entries.associate { (position, ps) -> position.name to ps.toDto() },
        hiddenStems = hiddenStems.entries.associate { (position, hidden) -> position.name to hidden.toDto() },
        sinSal = sinSal.entries.associate { (position, list) -> position.name to list.map { it.koreanName } },
        ohaengWeightedCounts =
            ohaengWeighted.counts.entries.associate { (ohaeng, count) -> ohaeng.koreanName to count },
    )

public fun PillarSipSeong.toDto(): PillarSipSeongDto =
    PillarSipSeongDto(
        stem = stem?.koreanName,
        branchMain = branchMain.koreanName,
        branchMid = branchMid?.koreanName,
        branchResidual = branchResidual?.koreanName,
    )

public fun JijiHiddenStems.toDto(): HiddenStemsDto =
    HiddenStemsDto(
        mainQi = mainQi.koreanName,
        midQi = midQi?.koreanName,
        residualQi = residualQi?.koreanName,
    )

/** 해석 리포트를 JSON 문자열로. */
public fun InterpretationReport.toJson(): String = sajuJson.encodeToString(toDto())
