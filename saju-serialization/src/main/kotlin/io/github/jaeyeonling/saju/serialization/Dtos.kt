package io.github.jaeyeonling.saju.serialization

import kotlinx.serialization.Serializable

/**
 * 직렬화용 DTO 묶음 — 도메인 타입을 JSON 친화 형태로 평탄화한다.
 *
 * 설계 원칙: 기계 분기용 영문 enum 이름(`verdict`)과 사람용 한글 라벨(`verdictKorean`)을 함께 담고,
 * Kotlin 고유 타입(Pair·sealed·enum-key Map)은 List·문자열-key Map 으로 풀어 Jackson/JS 어디서나
 * 깔끔하게 읽히도록 한다. 도메인 모델 자체는 직렬화 애너테이션에서 자유롭다(core 의존성 0 보존).
 */

/** 60갑자 한 쌍. */
@Serializable
public data class GanZhiDto(
    public val name: String, // 한글 "경오"
    public val hanja: String, // "庚午"
    public val gan: String, // 천간 한글 "경"
    public val ji: String, // 지지 한글 "오"
    public val index: Int, // 0..59
)

/** 천간(주로 일간 표현용) — 오행·음양 라벨 포함. */
@Serializable
public data class CheonganDto(
    public val name: String, // "기"
    public val hanja: String, // "己"
    public val ohaeng: String, // "토"
    public val eumyang: String, // "음"
)

/** 한 기둥(위치 + 간지). */
@Serializable
public data class PillarDto(
    public val position: String, // "YEAR"|"MONTH"|"DAY"|"HOUR"
    public val ganZhi: GanZhiDto,
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

/** 신강신약. */
@Serializable
public data class SinStrengthDto(
    public val verdict: String, // "JUNGHWA"
    public val verdictKorean: String, // "중화"
    public val supportRatio: Double, // 0~1 (0.45~0.55=중화)
)

/** 용신. */
@Serializable
public data class YongsinDto(
    public val ohaeng: String, // "화"
    public val method: String, // "BUEOK"
    public val methodKorean: String, // "억부"
)

/** 격국. */
@Serializable
public data class GyeokgukDto(
    public val type: String, // "PYEONGWAN"
    public val typeKorean: String, // "편관격"
    public val basis: String,
)

/** 합충 한 건 — sealed 계열을 kind 로 평탄화. */
@Serializable
public data class HapChungDto(
    public val kind: String, // "천간합"|"육합"|"육충"|"육해"|"삼합"
    public val members: List<String>, // 관여 글자(한글)
    public val transformsTo: String? = null, // 합화 오행(천간합·삼합만)
)

/** 해석 리포트 전체. */
@Serializable
public data class InterpretationReportDto(
    public val strength: SinStrengthDto,
    public val yongsin: YongsinDto,
    public val gyeokguk: GyeokgukDto,
    public val gongmang: List<String>, // ["신","유"]
    public val hapChung: List<HapChungDto>,
    public val ohaengCounts: Map<String, Int>, // {"목":2,"화":1,...}
    public val dominantOhaeng: String, // 최다 오행 한글
    public val sibiUnseong: Map<String, String>, // {"YEAR":"건록",...}
)
