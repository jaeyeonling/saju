package io.github.jaeyeonling.saju.lunar

/**
 * 음력 기준 프레임 — 삭·중기를 어느 타임존 자정으로 날짜에 귀속하느냐.
 *
 * 한국 KASI와 중국 농력은 이 1시간 차로 윤달이 갈릴 수 있다(예: 2017년 KASI 윤5월 vs 중국 윤6월).
 * 한국 사주는 [KOREA] 가 기본이다.
 */
public enum class CalendarBasis(public val utOffsetHours: Double) {
    /** 한국 천문연구원(KASI) 기준 — KST(UTC+9) 자정. */
    KOREA(9.0),

    /** 중국 농력 기준 — 베이징(UTC+8) 자정. tyme4j 와 정렬됨. */
    CHINA(8.0),
}
