package io.github.jaeyeonling.saju;

import io.github.jaeyeonling.saju.domain.Cheongan;
import io.github.jaeyeonling.saju.domain.GanZhi;
import io.github.jaeyeonling.saju.domain.SajuChart;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/** Java 소비자가 공개 API를 자연스럽게 호출할 수 있는지 검증한다(@JvmStatic/@JvmField/@JvmOverloads). */
class JavaInteropTest {

    @Test
    void facadeIsCallableFromJava() {
        // @JvmStatic + @JvmOverloads — zishiPolicy 생략 가능해야 한다.
        SajuChart chart = Saju.fromLocalDateTime(1990, 3, 15, 7, 0, 9.0);
        assertNotNull(chart);
        Cheongan dayMaster = chart.getDayMaster();
        assertNotNull(dayMaster);
        assertEquals(4, chart.pillars().size());
    }

    @Test
    void ganZhiFactoryAndFieldAreStatic() {
        // @JvmStatic factory
        GanZhi gapja = GanZhi.fromIndex(0);
        assertEquals(Cheongan.GAB, gapja.getGan());

        // @JvmField 정적 상수
        List<GanZhi> all = GanZhi.ALL;
        assertEquals(60, all.size());

        // 인스턴스 메서드
        GanZhi next = gapja.next(1);
        assertEquals(1, next.getIndex());
    }

    @Test
    void enumPropertiesAccessibleFromJava() {
        Cheongan gab = Cheongan.GAB;
        assertNotNull(gab.getOhaeng());
        assertNotNull(gab.getEumyang());
        // 천간합 갑→기
        assertEquals(Cheongan.GI, gab.combinePartner());
    }
}
