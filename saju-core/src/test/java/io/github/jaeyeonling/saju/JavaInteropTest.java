package io.github.jaeyeonling.saju;

import io.github.jaeyeonling.saju.astronomy.CalendarDate;
import io.github.jaeyeonling.saju.derivation.SajuConfig;
import io.github.jaeyeonling.saju.domain.Cheongan;
import io.github.jaeyeonling.saju.domain.Ganji;
import io.github.jaeyeonling.saju.domain.SajuChart;
import io.github.jaeyeonling.saju.domain.ZishiPolicy;
import io.github.jaeyeonling.saju.lunar.CalendarBasis;
import io.github.jaeyeonling.saju.lunar.LunarDate;
import io.github.jaeyeonling.saju.lunar.LunarConverter;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
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
    void sajuConfigIsCallableFromJava() {
        // @JvmField DEFAULT — 정적 필드로 노출.
        SajuChart withDefault = Saju.fromLocalDateTime(1990, 3, 15, 7, 0, 9.0, SajuConfig.DEFAULT);
        assertNotNull(withDefault);

        // @JvmOverloads constructor — 앞에서부터 일부 정책만 지정(나머지 기본값).
        SajuConfig yajasi = new SajuConfig(ZishiPolicy.YAJASI);
        SajuChart jeong = Saju.fromLocalDateTime(1990, 3, 15, 23, 30, 9.0, SajuConfig.DEFAULT);
        SajuChart ya = Saju.fromLocalDateTime(1990, 3, 15, 23, 30, 9.0, yajasi);
        // 같은 23:30 입력에 정자시(기본)와 야자시는 일주가 달라야.
        assertNotEquals(jeong.getDay().getGanji(), ya.getDay().getGanji());
    }

    @Test
    void ganjiFactoryAndFieldAreStatic() {
        // @JvmStatic factory
        Ganji gapja = Ganji.fromIndex(0);
        assertEquals(Cheongan.GAP, gapja.getGan());

        // @JvmField 정적 상수
        List<Ganji> all = Ganji.ALL;
        assertEquals(60, all.size());

        // 인스턴스 메서드
        Ganji next = gapja.next(1);
        assertEquals(1, next.getIndex());
    }

    @Test
    void enumPropertiesAccessibleFromJava() {
        Cheongan gab = Cheongan.GAP;
        assertNotNull(gab.getOhaeng());
        assertNotNull(gab.getEumyang());
        // 천간합 갑→기
        assertEquals(Cheongan.GI, gab.combinePartner());
    }

    @Test
    void lunarConversionCallableFromJava() {
        // @JvmStatic + @JvmOverloads — basis 지정/생략 모두 가능. @JvmField 는 필드로 노출.
        CalendarDate solar = LunarConverter.toSolar(2023, 1, 1, false, CalendarBasis.KOREA);
        assertEquals(2023, solar.year);
        assertEquals(1, solar.month);

        LunarDate lunar = LunarConverter.toLunar(2023, 1, 22); // basis 생략(@JvmOverloads)
        assertEquals(1, lunar.month);
        assertEquals(false, lunar.isLeapMonth);
    }
}
