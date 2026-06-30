// 룩업 배열에 케이스별 명리 주석을 인라인으로 단다(StrategyTest/SibiUnseong 과 동일한 가독성 패턴).
@file:Suppress("ktlint:standard:discouraged-comment-location")

package io.github.jaeyeonling.saju.interpretation

import io.github.jaeyeonling.saju.domain.Cheongan
import io.github.jaeyeonling.saju.domain.Jiji
import io.github.jaeyeonling.saju.domain.PillarPosition
import io.github.jaeyeonling.saju.domain.SajuChart

/** 신살을 판정하는 기준 축 — 일간(천간)에서 보느냐, 일지의 삼합국에서 보느냐. */
public enum class SinSalBasis {
    DAY_STEM, // 일간 기준 — 천을귀인·양인살·문창귀인
    DAY_BRANCH, // 일지 삼합 기준 — 도화살·역마살·화개살
}

/**
 * 신살(神殺) — 사주 지지에 깃드는 길흉 표식. 대중 사주 상담의 기대치가 높은 영역이다.
 *
 * 6종을 구현한다(살 3 + 귀인 3). 판정 기준([basis])에 따라 일간 또는 일지 삼합국을 본다.
 * 십이운성([SibiUnseong])과 마찬가지로 일간/일지 → 지지 lookup 이라, 유파 분기가 핵심이 아닌 한
 * 전략 인터페이스 없이 순수 함수 + 박제 룩업표로 둔다(골든 회귀로 동결).
 *
 * The Sinsal — auspicious/inauspicious markers attached to the chart's branches.
 */
public enum class SinSal(
    /** 한글 이름(도화살·역마살·…). */
    public val koreanName: String,
    /** 한자(桃花殺·驛馬殺·…). */
    public val hanja: String,
    /** 판정 기준 축. */
    public val basis: SinSalBasis,
) {
    DOHWA("도화살", "桃花殺", SinSalBasis.DAY_BRANCH), // 매력·이성·인기. 일지 삼합의 왕지(子午卯酉).
    YEOKMA("역마살", "驛馬殺", SinSalBasis.DAY_BRANCH), // 이동·이주·변동. 일지 삼합 생지의 충(寅申巳亥).
    HWAGAE("화개살", "華蓋殺", SinSalBasis.DAY_BRANCH), // 고독·예술·학문·종교. 일지 삼합의 고지(辰戌丑未).
    CHEONEUL("천을귀인", "天乙貴人", SinSalBasis.DAY_STEM), // 최고 길신 — 귀인의 도움·위기 모면.
    YANGIN("양인살", "羊刃殺", SinSalBasis.DAY_STEM), // 강렬한 칼날 기운 — 양간의 제왕지(겁재의 양면).
    MUNCHANG("문창귀인", "文昌貴人", SinSalBasis.DAY_STEM), // 학문·글재주·총명.
}

// === 룩업표 (Jiji ordinal 子0…亥11, Cheongan ordinal 甲0…癸9) ===
//
// 삼합국 그룹 = Jiji.ordinal % 4 — 0=수국(申子辰) · 1=금국(巳酉丑) · 2=화국(寅午戌) · 3=목국(亥卯未).
// (자0·진4·신8 → 모두 %4=0 으로 수국. "순서가 곧 규칙"인 이 코드베이스의 산술 패턴을 따른다.)
private const val SAMHAP_GROUPS = 4
private const val NONE = -1

// 일지 삼합국별 신살 글자(그룹 0·1·2·3 순). 골든(sinsal_branch.csv)으로 동결.
private val DOHWA_BY_GROUP = intArrayOf(9, 6, 3, 0) // 도화=왕지: 수국→酉, 금국→午, 화국→卯, 목국→子
private val YEOKMA_BY_GROUP = intArrayOf(2, 11, 8, 5) // 역마=생지의 충: 수국→寅, 금국→亥, 화국→申, 목국→巳
private val HWAGAE_BY_GROUP = intArrayOf(4, 1, 10, 7) // 화개=고지: 수국→辰, 금국→丑, 화국→戌, 목국→未

// 일간별 신살 글자(甲0…癸9 순). 골든(sinsal_daystem.csv)으로 동결.
private val CHEONEUL_BY_STEM = // 천을귀인 2지지 — 甲戊庚→丑未, 乙己→子申, 丙丁→亥酉, 辛→寅午, 壬癸→卯巳
    arrayOf(
        intArrayOf(1, 7), // 甲 → 丑未
        intArrayOf(0, 8), // 乙 → 子申
        intArrayOf(11, 9), // 丙 → 亥酉
        intArrayOf(11, 9), // 丁 → 亥酉
        intArrayOf(1, 7), // 戊 → 丑未
        intArrayOf(0, 8), // 己 → 子申
        intArrayOf(1, 7), // 庚 → 丑未
        intArrayOf(2, 6), // 辛 → 寅午
        intArrayOf(3, 5), // 壬 → 卯巳
        intArrayOf(3, 5), // 癸 → 卯巳
    )
private val YANGIN_BY_STEM = // 양인 — 양간(甲丙戊庚壬)의 제왕지. 음간은 없음(NONE).
    intArrayOf(3, NONE, 6, NONE, 6, NONE, 9, NONE, 0, NONE) // 甲→卯, 丙戊→午, 庚→酉, 壬→子
private val MUNCHANG_BY_STEM = // 문창귀인 — 甲巳 乙午 丙戊申 丁己酉 庚亥 辛子 壬寅 癸卯
    intArrayOf(5, 6, 8, 9, 8, 9, 11, 0, 2, 3)

private fun samhapGroup(dayBranch: Jiji): Int = dayBranch.ordinal % SAMHAP_GROUPS

// 검증·계산 공용 룩업 (internal: 같은 모듈의 골든 테스트가 차트 생성 없이 직접 대조).
internal fun dohwaTarget(dayBranch: Jiji): Jiji = Jiji.entries[DOHWA_BY_GROUP[samhapGroup(dayBranch)]]

internal fun yeokmaTarget(dayBranch: Jiji): Jiji = Jiji.entries[YEOKMA_BY_GROUP[samhapGroup(dayBranch)]]

internal fun hwagaeTarget(dayBranch: Jiji): Jiji = Jiji.entries[HWAGAE_BY_GROUP[samhapGroup(dayBranch)]]

internal fun cheoneulTargets(dayStem: Cheongan): List<Jiji> = CHEONEUL_BY_STEM[dayStem.ordinal].map { Jiji.entries[it] }

internal fun yanginTarget(dayStem: Cheongan): Jiji? =
    YANGIN_BY_STEM[dayStem.ordinal].takeIf { it != NONE }?.let { Jiji.entries[it] }

internal fun munchangTarget(dayStem: Cheongan): Jiji = Jiji.entries[MUNCHANG_BY_STEM[dayStem.ordinal]]

/**
 * 신살 판정기 — 일간·일지를 기준으로 네 기둥 지지에 깃든 신살을 찾는다.
 *
 * 도화/역마/화개는 **일지(日支)** 삼합국 기준, 천을/양인/문창은 **일간(日干)** 기준이다.
 * 각 기둥의 신살 목록은 [SinSal] 선언 순서로 정렬돼 결정적이다.
 */
public object SinSalFinder {
    /** 네 기둥(연·월·일·시) 각각에 깃든 신살. 없으면 빈 리스트. */
    @JvmStatic
    public fun find(chart: SajuChart): Map<PillarPosition, List<SinSal>> {
        val dayStem = chart.dayMaster
        val dayBranch = chart.day.ji
        val dohwa = dohwaTarget(dayBranch)
        val yeokma = yeokmaTarget(dayBranch)
        val hwagae = hwagaeTarget(dayBranch)
        val cheoneul = cheoneulTargets(dayStem)
        val yangin = yanginTarget(dayStem)
        val munchang = munchangTarget(dayStem)
        return chart.pillars().associate { pillar ->
            val ji = pillar.ji
            pillar.position to
                buildList {
                    if (ji == dohwa) add(SinSal.DOHWA)
                    if (ji == yeokma) add(SinSal.YEOKMA)
                    if (ji == hwagae) add(SinSal.HWAGAE)
                    if (ji in cheoneul) add(SinSal.CHEONEUL)
                    if (ji == yangin) add(SinSal.YANGIN)
                    if (ji == munchang) add(SinSal.MUNCHANG)
                }
        }
    }
}
