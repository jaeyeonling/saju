package io.github.jaeyeonling.saju.serialization

import kotlinx.serialization.Serializable

/*
 * 직렬화용 DTO 묶음 — 도메인 타입을 JSON 친화 형태로 평탄화한다.
 *
 * 설계 원칙: 기계 분기용 영문 enum 이름(verdict)과 사람용 한글 라벨(verdictKorean)을 함께 담고,
 * Kotlin 고유 타입(Pair·sealed·enum-key Map)은 List·문자열-key Map 으로 풀어 Jackson/JS 어디서나
 * 깔끔하게 읽히도록 한다. 도메인 모델 자체는 직렬화 애너테이션에서 자유롭다(core 의존성 0 보존).
 */

/** 60갑자 한 쌍 — name="경오", hanja="庚午", gan/ji 는 한글, index 는 0..59. */
@Serializable
public data class GanjiDto(
    public val name: String,
    public val hanja: String,
    public val gan: String,
    public val ji: String,
    public val index: Int,
)

/** 천간(주로 일간 표현용) — 오행·음양 라벨 포함. */
@Serializable
public data class CheonganDto(
    public val name: String,
    public val hanja: String,
    public val ohaeng: String,
    public val eumyang: String,
)

/** 한 기둥(위치 + 간지). position 은 YEAR|MONTH|DAY|HOUR. */
@Serializable
public data class PillarDto(
    public val position: String,
    public val ganji: GanjiDto,
)

/** 사주판 8글자 + 일간. */
@Serializable
public data class SajuChartDto(
    public val year: PillarDto,
    public val month: PillarDto,
    public val day: PillarDto,
    public val hour: PillarDto,
    public val dayMaster: CheonganDto,
)

/**
 * 신강신약 — verdict(영문 enum)·verdictKorean·supportRatio(0~1, 0.45~0.55=중화)·basis(산출 근거).
 * groupScores 는 십성 5묶음(비겁·식상·재성·관성·인성)별 세력 점수 — 억부 용신 분기의 입력.
 */
@Serializable
public data class SinStrengthDto(
    public val verdict: String,
    public val verdictKorean: String,
    public val supportRatio: Double,
    public val basis: String = "",
    public val groupScores: Map<String, Double> = emptyMap(),
)

/** 용신 — ohaeng(한글)·method(영문 enum)·methodKorean·basis(왜 이 오행인가). */
@Serializable
public data class YongsinDto(
    public val ohaeng: String,
    public val method: String,
    public val methodKorean: String,
    public val basis: String = "",
)

/** 격국 — type(영문 enum)·typeKorean·basis(근거 설명). */
@Serializable
public data class GyeokgukDto(
    public val type: String,
    public val typeKorean: String,
    public val basis: String,
)

/** 합충 한 건 — sealed 계열을 kind(천간합/육합/육충/육해/삼합)로 평탄화. transformsTo 는 천간합·삼합만 채워진다. */
@Serializable
public data class HapChungDto(
    public val kind: String,
    public val members: List<String>,
    public val transformsTo: String? = null,
)

/** 한 기둥의 십성 — stem(천간 십성, 일주는 null=나) + 지장간 본/중/여 십성(한글). 없는 지장간은 null. */
@Serializable
public data class PillarSipSeongDto(
    public val stem: String?,
    public val branchMain: String,
    public val branchMid: String? = null,
    public val branchResidual: String? = null,
)

/** 한 지지의 지장간 — 본기/중기/여기(한글). 없으면 null. */
@Serializable
public data class HiddenStemsDto(
    public val mainQi: String,
    public val midQi: String? = null,
    public val residualQi: String? = null,
)

/**
 * 해석 리포트 전체. gongmang=["신","유"], sibiUnseong={"YEAR":"건록",…}, ohaengCounts={"목":2,…}.
 *
 * sipSeong/hiddenStems/sinSal/ohaengWeightedCounts 는 네 기둥(YEAR|MONTH|DAY|HOUR) 기준 부가 정보로,
 * 기본값(빈 맵)을 둬 스키마를 깨지 않으면서 추가됐다(육친·신살·숨은 오행 — LLM 해석 레이어용).
 */
@Serializable
public data class InterpretationReportDto(
    public val strength: SinStrengthDto,
    public val yongsin: YongsinDto,
    public val gyeokguk: GyeokgukDto,
    public val gongmang: List<String>,
    public val hapChung: List<HapChungDto>,
    public val ohaengCounts: Map<String, Int>,
    public val dominantOhaeng: String,
    public val sibiUnseong: Map<String, String>,
    public val sipSeong: Map<String, PillarSipSeongDto> = emptyMap(),
    public val hiddenStems: Map<String, HiddenStemsDto> = emptyMap(),
    public val sinSal: Map<String, List<String>> = emptyMap(),
    public val ohaengWeightedCounts: Map<String, Int> = emptyMap(),
)
