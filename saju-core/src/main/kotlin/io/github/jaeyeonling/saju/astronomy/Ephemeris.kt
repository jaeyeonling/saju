package io.github.jaeyeonling.saju.astronomy

/**
 * 공개 천문 facade — 상위 레이어(saju-korea 등)가 쓰는 천문 값의 진입점.
 *
 * 내부 천문 객체들은 `internal` 로 캡슐화하고, 외부에 필요한 최소 표면만 여기서 공개한다.
 */
public object Ephemeris {
    /**
     * 균시차(분). **진태양시 = 평균태양시 + 반환값** (NOAA 관습). 한국 진태양시 보정에 쓰인다.
     *
     * @param utJd 율리우스일(UT 기준). 내부에서 ΔT 로 TT 변환 후 계산한다.
     */
    @JvmStatic
    public fun equationOfTimeMinutes(utJd: Double): Double {
        val ttJd = toTT(utJd)
        return EquationOfTime.minutes(ttJd)
    }
}
