package com.example.horner_ostateczny


fun main() {
    val coefficientsListInput = listOf(
        Term(4, 4),
        Term(2, 2),
    )
    // printing the input for clarity
    println("Input coefficients: $coefficientsListInput")

    val coefficientsList = recalculateList(coefficientsListInput)
    println("Coefficients list (with zeros): $coefficientsList")
}

private fun recalculateList(coefficientsListInput: List<Term>): List<Term> {
    val sorted = coefficientsListInput.sortedByDescending { it.power }

    // printing the sorted coefficients for clarity
    println("Sorted coefficients: $sorted")

    val maxPower = coefficientsListInput.maxOf { it.power }
    val coefficientsList = (maxPower downTo 0).map { power ->
        coefficientsListInput.find { it.power == power }
            ?: Term(0, power)
    }
    return coefficientsList
}

data class Term(val coefficient: Int, val power: Int)
