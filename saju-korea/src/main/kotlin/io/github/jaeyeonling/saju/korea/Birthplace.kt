package io.github.jaeyeonling.saju.korea

/**
 * 출생지 경도 프리셋 — 진태양시 경도보정에 쓰인다.
 *
 * 경도보정 = (표준 자오선 − 출생지 경도) × 4분. 한국은 동경 135° 표준시를 쓰는데
 * 실제 한반도는 약 동경 124~131°라 시계가 진태양보다 앞선다(서울 ≈ 32분).
 */
public enum class Birthplace(public val longitudeDeg: Double) {
    SEOUL(126.9780), // 서울
    BUSAN(129.0756), // 부산
    DAEGU(128.6014), // 대구
    INCHEON(126.7052), // 인천
    GWANGJU(126.8526), // 광주
    DAEJEON(127.3845), // 대전
    ULSAN(129.3114), // 울산
    JEJU(126.5312), // 제주
    PYONGYANG(125.7625), // 평양
    ;

    public companion object {
        /** 임의 경도(도)를 그대로 쓰는 헬퍼 — 프리셋에 없는 출생지용. */
        @JvmStatic
        public fun ofLongitude(longitudeDeg: Double): Double = longitudeDeg
    }
}
