// 전략 변형 케이스 테이블에 케이스별 명리 주석을 인라인으로 단다(의도된 가독성 패턴).
@file:Suppress("ktlint:standard:discouraged-comment-location")

package io.github.jaeyeonling.saju.interpretation

import io.github.jaeyeonling.saju.domain.Cheongan
import io.github.jaeyeonling.saju.domain.Ganji
import io.github.jaeyeonling.saju.domain.HiddenStemTable
import io.github.jaeyeonling.saju.domain.Jiji
import io.github.jaeyeonling.saju.domain.JijiHiddenStems
import io.github.jaeyeonling.saju.domain.Ohaeng
import io.github.jaeyeonling.saju.domain.Pillar
import io.github.jaeyeonling.saju.domain.PillarPosition
import io.github.jaeyeonling.saju.domain.SajuChart
import io.kotest.assertions.withClue
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe

/**
 * 해석 대안 전략의 **정답 핀값**(golden) — "갈아끼울 것이 *올바르게* 있다"를 박제한다.
 * 정답 데이터셋이 없는 영역이므로 '의도된 출력'을 핀값으로 고정해, 임계·분기·가중을 조용히 바꾸면 깨지게 한다.
 */
class StrategyVariationTest : StringSpec({

    "양포태 — 음간은 같은 오행 양간의 장생지에서 순행한다 (핀값)" {
        // 을→갑(장생 해), 정→병(인), 기→무(인), 신→경(사), 계→임(신). 각 장생지에서 장생.
        withClue("을@해=장생(갑과 동일)") { YangPotaeStrategy.stageOf(Cheongan.EUL, Jiji.HAE) shouldBe SibiUnseong.JANGSAENG }
        withClue("정@인=장생(병과 동일)") { YangPotaeStrategy.stageOf(Cheongan.JEONG, Jiji.IN) shouldBe SibiUnseong.JANGSAENG }
        withClue(
            "기@인=장생(무와 동일·화토동법)",
        ) { YangPotaeStrategy.stageOf(Cheongan.GI, Jiji.IN) shouldBe SibiUnseong.JANGSAENG }
        withClue("신@사=장생(경과 동일)") { YangPotaeStrategy.stageOf(Cheongan.SIN, Jiji.SA) shouldBe SibiUnseong.JANGSAENG }
        withClue("계@신=장생(임과 동일)") { YangPotaeStrategy.stageOf(Cheongan.GYE, Jiji.SIN) shouldBe SibiUnseong.JANGSAENG }
    }

    "음포태와 양포태는 음간에서 갈리고 양간에서 같다 (핀값)" {
        // 을@해: 음포태=사(자기 장생 오에서 역행), 양포태=장생(갑 장생 해에서 순행).
        EumPotaeStrategy.stageOf(Cheongan.EUL, Jiji.HAE) shouldBe SibiUnseong.SA
        YangPotaeStrategy.stageOf(Cheongan.EUL, Jiji.HAE) shouldBe SibiUnseong.JANGSAENG
        // 갑(양간)은 두 포태법이 같다.
        YangPotaeStrategy.stageOf(Cheongan.GAP, Jiji.HAE) shouldBe
            EumPotaeStrategy.stageOf(Cheongan.GAP, Jiji.HAE)
    }

    "조후 용신 — 한(寒)계열은 화, 난조(暖燥)계열은 수 (핀값)" {
        withClue("자월(겨울)→화") { johu(Jiji.JA) shouldBe Ohaeng.HWA }
        withClue("인월(봄·한)→화") { johu(Jiji.IN) shouldBe Ohaeng.HWA }
        withClue("오월(여름)→수") { johu(Jiji.O) shouldBe Ohaeng.SU }
        withClue("유월(가을)→수") { johu(Jiji.YU) shouldBe Ohaeng.SU }
    }

    "합성 용신 — 기후 극단(여름·겨울)은 조후, 중간계절(봄·가을)은 억부 (핀값)" {
        // 분기 축이 세력(verdict)이 아니라 월령 기후 — '억부 우선, 조후 보완'의 인코딩.
        fun method(monthJi: Jiji): YongsinMethod {
            val chart = chartWithMonth(monthJi)
            return CompositeYongsinStrategy().derive(chart, EokbuSinStrengthStrategy.DEFAULT.evaluate(chart)).method
        }
        withClue("자월(겨울)→조후") { method(Jiji.JA) shouldBe YongsinMethod.JOHU }
        withClue("오월(여름)→조후") { method(Jiji.O) shouldBe YongsinMethod.JOHU }
        withClue("인월(봄)→억부") { method(Jiji.IN) shouldBe YongsinMethod.EOKBU }
        withClue("유월(가을)→억부 (申酉 일괄 水 회피)") { method(Jiji.YU) shouldBe YongsinMethod.EOKBU }
    }

    "억부 분기 — 신강/신약 × 과다 세력별 용신 (손계산 앵커)" {
        // 일간 정(丁·火): 비겁=화, 식상=토, 재성=금, 관성=수, 인성=목.
        val chart = fireDayChart()

        fun yong(
            verdict: SinStrengthVerdict,
            groups: Map<SipSeongGroup, Double>,
        ): Ohaeng = EokbuYongsinStrategy.derive(chart, SinStrength(0.5, verdict, "", groups)).yongsin

        withClue("신강 비겁과다 → 관성(수)") {
            val g = mapOf(SipSeongGroup.BIGEOP to 5.0, SipSeongGroup.INSEONG to 1.0)
            yong(SinStrengthVerdict.SIN_GANG, g) shouldBe Ohaeng.SU
        }
        withClue("신강 인성과다 → 재성(금)") {
            val g = mapOf(SipSeongGroup.BIGEOP to 1.0, SipSeongGroup.INSEONG to 5.0)
            yong(SinStrengthVerdict.SIN_GANG, g) shouldBe Ohaeng.GEUM
        }
        withClue("신강 비겁·인성 균형 → 식상(토)") {
            val g = mapOf(SipSeongGroup.BIGEOP to 3.0, SipSeongGroup.INSEONG to 3.0)
            yong(SinStrengthVerdict.SIN_GANG, g) shouldBe Ohaeng.TO
        }
        withClue("신약 관성과다 → 인성(목)") {
            yong(
                SinStrengthVerdict.SIN_YAK,
                mapOf(SipSeongGroup.GWANSEONG to 5.0, SipSeongGroup.JAESEONG to 1.0, SipSeongGroup.SIKSANG to 1.0),
            ) shouldBe Ohaeng.MOK
        }
        withClue("신약 재성과다 → 비겁(화)") {
            yong(
                SinStrengthVerdict.SIN_YAK,
                mapOf(SipSeongGroup.JAESEONG to 5.0, SipSeongGroup.GWANSEONG to 1.0, SipSeongGroup.SIKSANG to 1.0),
            ) shouldBe Ohaeng.HWA
        }
        withClue("신약 식상과다 → 인성(목)") {
            yong(
                SinStrengthVerdict.SIN_YAK,
                mapOf(SipSeongGroup.SIKSANG to 5.0, SipSeongGroup.GWANSEONG to 1.0, SipSeongGroup.JAESEONG to 1.0),
            ) shouldBe Ohaeng.MOK
        }
    }

    "투출 격국과 자평 격국은 투출 여부로 갈린다 (핀값)" {
        // 월지 인(寅, 지장간 갑병무). 천간에 병만 투출(본기 갑·여기 무는 없음). 일간 갑.
        val chart =
            SajuChart(
                year = Pillar(PillarPosition.YEAR, Ganji(Cheongan.BYEONG, Jiji.JA)),
                month = Pillar(PillarPosition.MONTH, Ganji(Cheongan.BYEONG, Jiji.IN)),
                day = Pillar(PillarPosition.DAY, Ganji(Cheongan.GAP, Jiji.JA)),
                hour = Pillar(PillarPosition.HOUR, Ganji(Cheongan.BYEONG, Jiji.IN)),
            )
        withClue("본기 갑→비견→건록") { JapyeongGyeokgukStrategy.DEFAULT.classify(chart).type shouldBe GyeokgukType.GEOLLOK }
        withClue("투출 병→식신") { TuchulGyeokgukStrategy.DEFAULT.classify(chart).type shouldBe GyeokgukType.SIKSIN }
    }

    "투출 격국 — 비겁 투출은 격으로 삼지 않는다 (양인격 버그 회귀)" {
        // 갑 일간, 진월(지장간 무 편재·을 겁재·계 편인). 천간에 을(겁재)만 투출, 무·계 미투출.
        // 버그(수정 전): 을 겁재를 채택해 '양인격'. 정설: 비겁 투출 무시 → 본기 무=편재격.
        val chart =
            SajuChart(
                year = Pillar(PillarPosition.YEAR, Ganji(Cheongan.EUL, Jiji.MYO)), // 을묘 — 을(겁재) 투출
                month = Pillar(PillarPosition.MONTH, Ganji(Cheongan.BYEONG, Jiji.JIN)), // 병진 — 진월
                day = Pillar(PillarPosition.DAY, Ganji(Cheongan.GAP, Jiji.JA)), // 갑 일간
                hour = Pillar(PillarPosition.HOUR, Ganji(Cheongan.IM, Jiji.SIN)), // 임신
            )
        withClue("비겁(을) 투출 무시 → 본기 무=편재격") {
            TuchulGyeokgukStrategy.DEFAULT.classify(chart).type shouldBe GyeokgukType.PYEONJAE
        }
    }

    "억부 가중치를 바꾸면 지원율이 달라진다" {
        val chart = sampleChart()
        val default = EokbuSinStrengthStrategy.DEFAULT.evaluate(chart).supportRatio
        val noHidden =
            EokbuSinStrengthStrategy(
                EokbuWeights.DEFAULT.copy(mainQi = 0.0, midQi = 0.0, residualQi = 0.0),
            ).evaluate(chart).supportRatio
        withClue("지장간 가중을 끄면 지원율이 달라져야") { noHidden shouldNotBe default }
    }

    "withHiddenStems 는 커스텀 테이블을 신강신약·격국에 함께 전파한다" {
        // 모든 지지의 지장간을 갑(甲) 하나로 바꾼 커스텀 테이블 — 표준과 결과가 달라야 '실제 주입'이 입증된다.
        val custom = HiddenStemTable { JijiHiddenStems(Cheongan.GAP, null, null) }
        val ctx = InterpretationContext.DEFAULT.withHiddenStems(custom)
        val chart = sampleChart()

        // 두 전략 모두 커스텀 테이블을 봐 표준 DEFAULT와 달라야 한다(한쪽만 주입되는 버그를 잡는다).
        withClue("신강신약이 커스텀 테이블을 봐야") {
            ctx.sinStrength.evaluate(chart) shouldNotBe InterpretationContext.DEFAULT.sinStrength.evaluate(chart)
        }
        withClue("격국이 커스텀 테이블을 봐야") {
            ctx.gyeokguk.classify(chart) shouldNotBe InterpretationContext.DEFAULT.gyeokguk.classify(chart)
        }
    }

    "withHiddenStems 는 다른 전략 교체와 합성된다 (덮어쓰지 않음)" {
        val custom = HiddenStemTable { JijiHiddenStems(Cheongan.GAP, null, null) }
        // 십이운성을 양포태로 바꾼 뒤 테이블을 주입해도 양포태가 보존돼야(copy 합성).
        val composed =
            InterpretationContext.DEFAULT
                .copy(sibiUnseong = YangPotaeStrategy)
                .withHiddenStems(custom)
        withClue("withHiddenStems 가 다른 전략 교체를 덮으면 안 된다") { composed.sibiUnseong shouldBe YangPotaeStrategy }
    }

    "withHiddenStems 는 커스텀 억부 가중치를 보존한다 (테이블만 교체)" {
        val custom = HiddenStemTable { JijiHiddenStems(Cheongan.GAP, null, null) }
        val tunedWeights = EokbuWeights.DEFAULT.copy(month = 5.0)
        val ctx =
            InterpretationContext.DEFAULT
                .copy(sinStrength = EokbuSinStrengthStrategy(tunedWeights))
                .withHiddenStems(custom)
        val sin = ctx.sinStrength as EokbuSinStrengthStrategy
        withClue("커스텀 가중치가 보존돼야(테이블만 교체, weights 폐기 금지)") { sin.weights shouldBe tunedWeights }
    }

    "Interpretation_of 파사드가 비-DEFAULT 컨텍스트를 실제로 사용한다" {
        val chart = sampleChart()
        val default = Interpretation.of(chart)
        val custom =
            Interpretation.of(
                chart,
                InterpretationContext.DEFAULT.copy(yongsin = JohuYongsinStrategy),
            )
        // DEFAULT는 억부, 갈아끼우면 조후 — 파사드가 ctx.yongsin 을 실제로 호출함을 증명(미사용이면 둘이 같음).
        default.yongsin.method shouldBe YongsinMethod.EOKBU
        custom.yongsin.method shouldBe YongsinMethod.JOHU
        withClue("비-DEFAULT ctx 는 다른 리포트를 내야") { custom shouldNotBe default }
    }

    "대안 전략은 모두 결정론적이다" {
        val chart = sampleChart()
        val strength = EokbuSinStrengthStrategy.DEFAULT.evaluate(chart)
        YangPotaeStrategy.stageOf(Cheongan.EUL, Jiji.JA) shouldBe
            YangPotaeStrategy.stageOf(Cheongan.EUL, Jiji.JA)
        CompositeYongsinStrategy().derive(chart, strength) shouldBe
            CompositeYongsinStrategy().derive(chart, strength)
        StandardHapChungStrategy.detect(chart.stems(), chart.branches()) shouldBe
            StandardHapChungStrategy.detect(chart.stems(), chart.branches())
    }
})

private fun johu(monthJi: Jiji): Ohaeng {
    val chart = chartWithMonth(monthJi)
    return JohuYongsinStrategy.derive(chart, EokbuSinStrengthStrategy.DEFAULT.evaluate(chart)).yongsin
}

// 월지의 음양(parity)에 맞는 천간을 짝지어 유효 간지로 만든다(갑=양지, 을=음지).
private fun chartWithMonth(monthJi: Jiji): SajuChart =
    SajuChart(
        year = Pillar(PillarPosition.YEAR, Ganji.fromIndex(0)),
        month = Pillar(PillarPosition.MONTH, Ganji(Cheongan.fromIndex(monthJi.ordinal % 2), monthJi)),
        day = Pillar(PillarPosition.DAY, Ganji(Cheongan.GAP, Jiji.JA)),
        hour = Pillar(PillarPosition.HOUR, Ganji.fromIndex(0)),
    )

// 일간을 정(丁·火)으로 고정 — 억부 분기는 dayMaster 오행 + 주입한 groupScores 로만 결정되므로 나머지 기둥은 무관.
private fun fireDayChart(): SajuChart =
    SajuChart(
        year = Pillar(PillarPosition.YEAR, Ganji.fromIndex(0)),
        month = Pillar(PillarPosition.MONTH, Ganji.fromIndex(0)),
        day = Pillar(PillarPosition.DAY, Ganji(Cheongan.JEONG, Jiji.MYO)), // 정묘 — 일간 정(火)
        hour = Pillar(PillarPosition.HOUR, Ganji.fromIndex(0)),
    )

private fun sampleChart(): SajuChart =
    SajuChart(
        year = Pillar(PillarPosition.YEAR, Ganji.fromIndex(0)),
        month = Pillar(PillarPosition.MONTH, Ganji.fromIndex(20)),
        day = Pillar(PillarPosition.DAY, Ganji.fromIndex(40)),
        hour = Pillar(PillarPosition.HOUR, Ganji.fromIndex(15)),
    )
