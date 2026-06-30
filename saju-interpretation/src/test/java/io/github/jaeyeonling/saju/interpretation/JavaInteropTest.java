package io.github.jaeyeonling.saju.interpretation;

import io.github.jaeyeonling.saju.domain.Ganji;
import io.github.jaeyeonling.saju.domain.Pillar;
import io.github.jaeyeonling.saju.domain.PillarPosition;
import io.github.jaeyeonling.saju.domain.SajuChart;
import io.github.jaeyeonling.saju.domain.StandardHiddenStemTable;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/** Java 소비자가 해석 파사드·전략(object/class 혼재)을 자연스럽게 호출하는지 검증. */
class JavaInteropTest {

    private SajuChart sampleChart() {
        return new SajuChart(
                new Pillar(PillarPosition.YEAR, Ganji.fromIndex(0)),
                new Pillar(PillarPosition.MONTH, Ganji.fromIndex(20)),
                new Pillar(PillarPosition.DAY, Ganji.fromIndex(40)),
                new Pillar(PillarPosition.HOUR, Ganji.fromIndex(15)));
    }

    @Test
    void facadeCallableFromJava() {
        InterpretationReport report = Interpretation.of(sampleChart()); // @JvmStatic @JvmOverloads
        assertNotNull(report);
        // enum 결과 — 문자열 블롭이 아니라 타입이라 Java 가 분기 가능.
        assertEquals("억부", report.getYongsin().getMethod().getKoreanName());
        assertNotNull(report.getGyeokguk().getType());
    }

    @Test
    void strategiesCallableFromJava() {
        // object → INSTANCE, class → DEFAULT 혼재 호출형 검증.
        SibiUnseongStrategy eum = EumPotaeStrategy.INSTANCE;
        SinStrengthStrategy sin = EokbuSinStrengthStrategy.DEFAULT;
        assertNotNull(eum);
        assertNotNull(sin);

        // withHiddenStems 인스턴스 메서드 — DEFAULT 에 표준 테이블 일관 주입(copy 합성).
        InterpretationContext ctx = InterpretationContext.DEFAULT.withHiddenStems(StandardHiddenStemTable.INSTANCE);
        assertNotNull(Interpretation.of(sampleChart(), ctx));
    }
}
