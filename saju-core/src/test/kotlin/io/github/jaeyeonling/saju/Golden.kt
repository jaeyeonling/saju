package io.github.jaeyeonling.saju

/**
 * 동결된 골든 벡터(CSV) 로더.
 *
 * 검증된 사주 정답값을 `src/test/resources/golden/` 에 박제해 두고, 골든 회귀 테스트가
 * 외부 의존 없이 자체 엔진을 그 값과 대조한다. 각 CSV 는 첫 줄이 헤더, 이후 콤마 구분 데이터다.
 */
object Golden {
    /** `golden/<name>` 을 읽어 헤더를 제외한 각 행을 컬럼 리스트로 반환한다. */
    fun rows(name: String): List<List<String>> {
        val stream =
            Golden::class.java.getResourceAsStream("/golden/$name")
                ?: error("골든 리소스 없음: /golden/$name")
        return stream.bufferedReader().readLines()
            .asSequence()
            .filter { it.isNotBlank() }
            .drop(1) // 헤더
            .map { line -> line.split(",") }
            .toList()
    }
}
