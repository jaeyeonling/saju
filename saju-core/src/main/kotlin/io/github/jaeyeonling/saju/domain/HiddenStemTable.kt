package io.github.jaeyeonling.saju.domain

/**
 * 지장간(地藏干) 분야표 — 지지가 품은 천간을 어느 판본으로 보느냐.
 *
 * **사령**(司令): 절기 경과 일수에 따라 그 달에 당령(當令)하는 기가 여기→중기→본기 순으로 바뀐다는 학설.
 * 기본 [StandardHiddenStemTable] 은 사령을 반영하지 않고 항상 본기를 대표로 둔다(경과일 무관).
 * 판본·사령 반영 방식이 유파마다 달라 추상화한다.
 *
 * 신강신약·격국·투출 판정이 모두 이 테이블에 의존하므로, 한 곳을 갈아끼우면 해석 전반이 일관되게 바뀐다.
 */
public fun interface HiddenStemTable {
    /** 지지의 지장간(본기/중기/여기). */
    public fun of(jiji: Jiji): JijiHiddenStems
}

/** 표준 지장간표 — 기존 [JijiHiddenStems.of] 와 동일한 일반본(사령 미반영). */
public object StandardHiddenStemTable : HiddenStemTable {
    override fun of(jiji: Jiji): JijiHiddenStems = JijiHiddenStems.of(jiji)
}
