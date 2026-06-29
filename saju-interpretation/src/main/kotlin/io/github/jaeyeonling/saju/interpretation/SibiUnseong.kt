package io.github.jaeyeonling.saju.interpretation

import io.github.jaeyeonling.saju.domain.Cheongan
import io.github.jaeyeonling.saju.domain.Jiji

/**
 * 십이운성(十二運星) — 일간이 지지를 만났을 때의 에너지 12단계(인생 그래프).
 * 순서는 표준 십이운성 순서와 동일(장생·목욕·관대·건록·제왕·쇠·병·사·묘·절·태·양).
 */
public enum class SibiUnseong(
    /** 한글 이름(장생·목욕·…). */
    public val koreanName: String,
    /** 한자(長生·沐浴·…). */
    public val hanja: String,
) {
    JANGSAENG("장생", "長生"), // 태어남. 기운이 막 솟는 시작.
    MOGYOK("목욕", "沐浴"), // 갓 씻김. 불안정·시행착오.
    GWANDAE("관대", "冠帶"), // 갓을 씀(성년). 사회 진출 채비.
    GEOLLOK("건록", "建祿"), // (臨官) 벼슬에 오름. 자립·전성기 진입.
    JEWANG("제왕", "帝旺"), // 기운의 정점.
    SOE("쇠", "衰"), // 정점을 지나 기울기 시작.
    BYEONG("병", "病"), // 쇠하여 병듦.
    SA("사", "死"), // 기운이 다함.
    MYO("묘", "墓"), // 무덤. 갈무리·저장.
    JEOL("절", "絕"), // 끊김. 기운의 바닥.
    TAE("태", "胎"), // 새 생명을 품음. 다시 잉태.
    YANG("양", "養"), // 뱃속에서 자람. 출생 준비.
}

/** 십이운성 도출 전략 — 양포태/음포태 논쟁을 추상화한다. */
public interface SibiUnseongStrategy {
    public fun stageOf(
        dayMaster: Cheongan,
        branch: Jiji,
    ): SibiUnseong
}

// 각 천간의 장생(長生) 지지 ordinal — 갑=해,을=오,병·무=인,정·기=유,경=사,신=자,임=신,계=묘.
// 화토동법(火土同法): 토(무·기)는 화(병·정)와 장생지를 공유한다는 통설. 그래서 무=병, 기=정과 같은 값이다.
private val HWATO_JANGSAENG_BRANCH = intArrayOf(11, 6, 2, 9, 2, 9, 5, 0, 8, 3)
private const val SIBIUNSEONG_STAGES = 12

/** 12단계 순환 보정. */
private fun cyclicStage(offset: Int): Int = ((offset % SIBIUNSEONG_STAGES) + SIBIUNSEONG_STAGES) % SIBIUNSEONG_STAGES

/**
 * 음포태(陰胞胎) — 통설. 양간은 순행, 음간은 역행하며 12단계를 돈다.
 * (화토동법: 무토=병화, 기토=정화의 장생지를 공유)
 */
public object EumPotaeStrategy : SibiUnseongStrategy {
    override fun stageOf(
        dayMaster: Cheongan,
        branch: Jiji,
    ): SibiUnseong {
        val jangsaengBranch = HWATO_JANGSAENG_BRANCH[dayMaster.ordinal]
        val stageIndex =
            if (dayMaster.eumyang.isYang) {
                cyclicStage(branch.ordinal - jangsaengBranch) // 순행
            } else {
                cyclicStage(jangsaengBranch - branch.ordinal) // 역행
            }
        return SibiUnseong.entries[stageIndex]
    }
}

/**
 * 양포태(陽胞胎) — 음간 역행을 부정하고, 음간을 **같은 오행의 양간과 동일시**하는 유파.
 *
 * 을(乙)은 갑(甲)과, 정(丁)은 병(丙)과 같은 장생지에서 순행한다. 예: 을@해 = 장생(갑과 동일).
 * 음포태가 음간 자신의 장생지에서 역행하는 것과 달리, 여기서는 음간의 장생지 자체가 양간 것으로 바뀐다.
 */
public object YangPotaeStrategy : SibiUnseongStrategy {
    override fun stageOf(
        dayMaster: Cheongan,
        branch: Jiji,
    ): SibiUnseong {
        // 음간(ordinal 홀수)은 같은 오행 양간(ordinal 짝수)으로 환원: 을1→갑0, 정3→병2, 기5→무4 …
        val yangStem = dayMaster.ordinal - dayMaster.ordinal % 2
        return SibiUnseong.entries[cyclicStage(branch.ordinal - HWATO_JANGSAENG_BRANCH[yangStem])]
    }
}
