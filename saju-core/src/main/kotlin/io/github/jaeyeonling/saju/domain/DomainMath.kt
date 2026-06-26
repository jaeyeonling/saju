package io.github.jaeyeonling.saju.domain

/** 음수에도 양의 나머지를 주는 floor modulo (KMP 친화 — Math.floorMod 대신 자체 구현). */
internal fun floorMod(value: Int, modulus: Int): Int = ((value % modulus) + modulus) % modulus
