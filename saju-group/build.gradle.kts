plugins {
    id("saju.pure-domain") // 그룹 합성도 순수(java.time-free) — 입력 사주는 이미 시간 보정 완료
    id("saju.publish") // io.github.jaeyeonling:saju-group
}

// 다수 사주를 합쳐 그룹 차원(오행 균형·십성 역할·멤버간 합충·세운/대운)을 산출한다.
// 개인 단위 계산은 전부 interpretation/core 에 위임하고, 여기서는 '여럿을 합쳤을 때만 드러나는' 합성만 한다.
dependencies {
    api(project(":saju-interpretation"))
}
