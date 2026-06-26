package io.github.jaeyeonling.saju.interpretation

import io.github.jaeyeonling.saju.domain.Cheongan
import io.github.jaeyeonling.saju.domain.Jiji

/**
 * 십이운성(十二運星) — 일간이 지지를 만났을 때의 에너지 12단계(인생 그래프).
 * 순서는 tyme4j Terrain 과 동일(장생·목욕·관대·건록·제왕·쇠·병·사·묘·절·태·양).
 */
public enum class SibiUnseong {
    JANGSAENG, // 장생 長生
    MOGYOK, // 목욕 沐浴
    GWANDAE, // 관대 冠帶
    GEOLLOK, // 건록 建祿(臨官)
    JEWANG, // 제왕 帝旺
    SOE, // 쇠 衰
    BYEONG, // 병 病
    SA, // 사 死
    MYO, // 묘 墓
    JEOL, // 절 絕
    TAE, // 태 胎
    YANG, // 양 養
}

/** 십이운성 도출 전략 — 양포태/음포태 논쟁을 추상화한다. */
public interface SibiUnseongStrategy {
    public fun stageOf(dayMaster: Cheongan, branch: Jiji): SibiUnseong
}

/**
 * 음포태(陰胞胎) — 통설. 양간은 순행, 음간은 역행하며 12단계를 돈다.
 * (화토동법: 무토=병화, 기토=정정의 장생지를 공유)
 */
public object EumPotaeStrategy : SibiUnseongStrategy {
    override fun stageOf(dayMaster: Cheongan, branch: Jiji): SibiUnseong {
        val jangsaengBranch = JANGSAENG_BRANCH[dayMaster.ordinal]
        val stageIndex = if (dayMaster.eumyang.isYang) {
            mod(branch.ordinal - jangsaengBranch, STAGES) // 순행
        } else {
            mod(jangsaengBranch - branch.ordinal, STAGES) // 역행
        }
        return SibiUnseong.entries[stageIndex]
    }

    // 각 천간의 장생(長生) 지지 ordinal. 갑=해, 을=오, 병·무=인, 정·기=유, 경=사, 신=자, 임=신, 계=묘.
    private val JANGSAENG_BRANCH = intArrayOf(11, 6, 2, 9, 2, 9, 5, 0, 8, 3)

    private fun mod(value: Int, modulus: Int): Int = ((value % modulus) + modulus) % modulus
    private const val STAGES = 12
}
