package io.github.jaeyeonling.saju.korea;

import io.github.jaeyeonling.saju.derivation.SajuConfig;
import io.github.jaeyeonling.saju.domain.SajuChart;
import io.github.jaeyeonling.saju.domain.ZishiPolicy;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/** Java 소비자가 korea 진입점·설정 묶음을 자연스럽게 호출하는지 검증(@JvmStatic/@JvmField/@JvmOverloads). */
class JavaInteropTest {

    @Test
    void koreanSajuCallableFromJava() {
        // @JvmStatic + @JvmOverloads — longitudeDeg/config 생략 가능.
        SajuChart chart = KoreanSaju.fromCivilTime(1990, 3, 15, 7, 0);
        assertNotNull(chart);

        // KoreanSajuConfig 합성 + @JvmOverloads 생성자(앞에서부터 일부만 지정).
        // 진태양시 보정(서울 ≈ -41분)이 23:30 을 자시 밖으로 밀지 않도록 무보정(NONE)으로 자시 학파만 비교.
        KoreanSajuConfig jeongCfg = new KoreanSajuConfig(SajuConfig.DEFAULT, TrueSolarTimePolicy.NONE);
        KoreanSajuConfig yajaCfg = new KoreanSajuConfig(new SajuConfig(ZishiPolicy.YAJASI), TrueSolarTimePolicy.NONE);
        double seoul = Birthplace.SEOUL.getLongitudeDeg();
        SajuChart jeong = KoreanSaju.fromCivilTime(1990, 3, 15, 23, 30, seoul, jeongCfg);
        SajuChart yaja = KoreanSaju.fromCivilTime(1990, 3, 15, 23, 30, seoul, yajaCfg);
        // 23:30 정자시 vs 야자시 일주가 갈려야.
        assertNotEquals(jeong.getDay().getGanji(), yaja.getDay().getGanji());

        // @JvmField DEFAULT + enum.
        assertEquals(TrueSolarTimePolicy.FULL, KoreanSajuConfig.DEFAULT.getTrueSolarTime());
    }
}
