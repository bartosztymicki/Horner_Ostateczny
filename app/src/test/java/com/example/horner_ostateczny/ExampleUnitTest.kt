fun main() {
    // Przykład wielomianu: 3x^4 − 2x^2 + x − 5
    val terms = listOf(
        Term(1, 1),
        Term(-5, 0),
        Term(-2, 2),
        Term(0, 3),
        Term(3, 4)
    )

    val sorted = terms.sortedByDescending { it.power }
    val coefficients = sorted.map { it.coefficient }
    val r = -2  // dzielimy przez (x + 2)

    val (quotient, remainder) = hornerDivision(coefficients, r)

    println("Quotient coefficients: $quotient")
    println("Remainder: $remainder")
}

data class Term(val coefficient: Int, val power: Int)

fun hornerDivision(coefficients: List<Int>, r: Int): Pair<List<Int>, Int> {
    if (coefficients.isEmpty()) throw IllegalArgumentException("Lista współczynników nie może być pusta")
    if (coefficients.size == 1) return Pair(emptyList(), coefficients[0])

    val result = MutableList(coefficients.size - 1) { 0 }
    result[0] = coefficients[0]

    for (i in 1 until coefficients.size - 1) {
        result[i] = result[i - 1] * r + coefficients[i]
    }

    val remainder = result.last() * r + coefficients.last()
    return Pair(result, remainder)
}
