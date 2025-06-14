
package com.example.horner_ostateczny

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.horner_ostateczny.ui.theme.Horner_OstatecznyTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Horner_OstatecznyTheme {
                HornerMain()
            }
        }
    }
}

data class Coefficient(
    var number: Double,
    var power: Int
)

@Composable
fun HornerMain() {
    val coefficientsListInput = remember {
        mutableStateListOf<Coefficient>()
    }

    val coefficientsList = remember {
        mutableStateListOf<Coefficient>()
    }

    var showTableAndText by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var selectedCoefficient by remember { mutableStateOf<Coefficient?>(null) }
    var numberText by remember { mutableStateOf("") }
    var powerText by remember { mutableStateOf("") }
    var showDz by remember { mutableStateOf(false) }
    var showCoefficients by remember { mutableStateOf(false) }
    var dividerText by remember { mutableStateOf("") }
    var divider by remember { mutableStateOf(1.0) }
    var resultText by remember { mutableStateOf<String?>(null) }


    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(16.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        ShowText(coefficientsList, divider)

        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            items(coefficientsList) { coefficient ->
                BoxCoefficient(
                    classCoe = coefficient,
                    onDelete = {
                        coefficientsListInput.remove(coefficient)
                        calculateCoefficients(coefficientsListInput, coefficientsList)
                    },
                    onClick = { coeff ->
                        selectedCoefficient = coeff
                        numberText = coeff.number.toString()
                        powerText = coeff.power.toString()
                        showEditDialog = true
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))


        Button(
            onClick = {
                if (showDz == false)
                    showTableAndText = true

                if (showTableAndText)
                    if (coefficientsListInput.isNotEmpty()) {
                        val (quotient, remainder) = hornerCalculate(coefficientsListInput, divider)
                        val remainderStr = if (remainder % 1 == 0.0) remainder.toInt()
                            .toString() else remainder.toString()
                    } else {
                        resultText = "Brak współczynników do obliczenia"
                    }
            },
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(bottom = 8.dp)
        ) {
            Text("oblicz")
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(onClick = { showCoefficients = true }) {
                Text("Dodaj składnik")
            }
            Button(onClick = { showDz = true }) {
                Text("Dodaj dzielnik")
            }
        }


        if (showTableAndText) {
            HornerTable(
                coefficients = coefficientsList
                    .sortedByDescending { it.power }
                    .map { it.number },
                divider = divider
            )
            Button(onClick = {
                showTableAndText = false
                resultText = null
            }) {
                Text("Zamknij")
            }
        }

        if (resultText != null) {
            Text(
                text = resultText!!,
                modifier = Modifier.padding(top = 16.dp),
                style = MaterialTheme.typography.bodyLarge.copy(color = Color.Black)
            )
        }
    }

    if (showCoefficients) {
        AlertDialog(
            onDismissRequest = { showCoefficients = false },
            title = { Text("Dodaj składnik") },
            text = {
                Column(modifier = Modifier.padding(16.dp)) {
                    TextField(
                        value = numberText,
                        onValueChange = { numberText = it },
                        label = { Text("Wpisz liczbę") }
                    )
                    TextField(
                        value = powerText,
                        onValueChange = { powerText = it },
                        label = { Text("Podaj potęgę") }
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    val number = numberText.toDoubleOrNull()
                    val power = powerText.toIntOrNull()

                    if (number != null && power != null) {
                        coefficientsListInput.add(Coefficient(number, power))
                        coefficientsListInput.sortedByDescending { it.power }
                        calculateCoefficients(coefficientsListInput, coefficientsList)
                        numberText = ""
                        powerText = ""
                        showCoefficients = false
                    }
                }) {
                    Text("OK")
                }
            }
        )
    }

    if (showDz) {
        AlertDialog(
            onDismissRequest = { showDz = false },
            title = { Text("Podaj dzielnik") },
            text = {
                TextField(
                    value = dividerText,
                    onValueChange = { dividerText = it },
                    label = { Text("Podaj dzielnik") }
                )
            },
            confirmButton = {
                Button(onClick = {
                    val divid = dividerText.toDoubleOrNull()
                    if (divid != null) {
                        divider = divid
                        showDz = false
                    }
                }) {
                    Text("OK")
                }
            }
        )
    }

    if (showEditDialog) {
        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = { Text("Edytuj składnik") },
            text = {
                Column(modifier = Modifier.padding(16.dp)) {
                    TextField(
                        value = numberText,
                        onValueChange = { numberText = it },
                        label = { Text("Wpisz liczbę") }
                    )
                    TextField(
                        value = powerText,
                        onValueChange = { powerText = it },
                        label = { Text("Podaj potęgę") }
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    val number = numberText.toDoubleOrNull()
                    val power = powerText.toIntOrNull()

                    if (number != null && power != null) {
                        coefficientsListInput.remove(selectedCoefficient)
                        coefficientsListInput.add(Coefficient(number, power))
                        calculateCoefficients(coefficientsListInput, coefficientsList)
                        numberText = ""
                        powerText = ""
                        showEditDialog = false
                    }
                }) {
                    Text("OK")
                }
            }
        )
    }
}

private fun calculateCoefficients(
    coefficientsListInput: SnapshotStateList<Coefficient>,
    coefficientsList: SnapshotStateList<Coefficient>
) {
    val maxPower = coefficientsListInput.maxOfOrNull { it.power } ?: 0
    val fullList = (maxPower downTo 0).map { power ->
        coefficientsListInput.find { it.power == power } ?: Coefficient(0.0, power)
    }
    coefficientsList.clear()
    coefficientsList.addAll(fullList)
}

fun hornerCalculate(
    coefficientsList: List<Coefficient>,
    divider: Double
): Pair<List<Coefficient>, Double> {
    if (coefficientsList.isEmpty()) return Pair(emptyList(), 0.0)

    val maxPower = coefficientsList.maxOf { it.power }
    val fullCoeffs = DoubleArray(maxPower + 1) { 0.0 }

    for (coef in coefficientsList) {
        fullCoeffs[maxPower - coef.power] = coef.number
    }

    if (fullCoeffs.isEmpty() || fullCoeffs.size < 2) return Pair(emptyList(), 0.0)

    val n = fullCoeffs.size
    val r = -divider

    val result = MutableList(n - 1) { 0.0 }
    result[0] = fullCoeffs[0]

    for (i in 1 until n - 1) {
        result[i] = result[i - 1] * r + fullCoeffs[i]
    }

    val remainder = result.last() * r + fullCoeffs.last()

    val resultCoefficients = result.mapIndexed { index, value ->
        Coefficient(number = value, power = maxPower - 1 - index)
    }

    return Pair(resultCoefficients, remainder)
}

@Composable
fun ShowText(list: List<Coefficient>, x: Double) {
    if (list.isEmpty()) {
        Text(
            text = "Brak współczynników",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray
        )
        return
    }

    val sortedList = list.sortedByDescending { it.power }

    val numberPower = buildString {
        sortedList.forEachIndexed { index, coefficient ->
            val numStr = if (coefficient.number % 1 == 0.0) {
                coefficient.number.toInt().toString()
            } else {
                coefficient.number.toString()
            }

            val formatted = when (coefficient.power) {
                0 -> numStr
                1 -> "${numStr}x"
                else -> "${numStr}x^${coefficient.power}"
            }

            if (index > 0) {
                if (coefficient.number >= 0) append(" + ") else append(" ")
            }

            append(formatted)
        }
    }

    val formattedX = if (x % 1 == 0.0) x.toInt() else x
    val dividerText = if (x >= 0) "x + $formattedX" else "x$formattedX"

    Text(
        text = "($numberPower) : ($dividerText)",
        style = MaterialTheme.typography.bodyLarge
    )
}

@Composable
fun BoxCoefficient(
    classCoe: Coefficient,
    onDelete: () -> Unit,
    onClick: (Coefficient) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .background(
                color = Color(0xFFE3F2FD),
                shape = RoundedCornerShape(12.dp)
            )
            .border(
                width = 1.dp,
                color = Color(0xFF90CAF9),
                shape = RoundedCornerShape(12.dp)
            )
            .shadow(4.dp, shape = RoundedCornerShape(12.dp))
            .padding(16.dp)
            .clickable { onClick(classCoe) }
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column {
                Text(
                    text = "Liczba: ${
                        if (classCoe.number % 1 == 0.0) {
                            classCoe.number.toInt()
                        } else {
                            classCoe.number
                        }
                    }",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0D47A1)
                    )
                )
                Text(
                    text = "Potęga: ${classCoe.power}",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = Color(0xFF1565C0)
                    )
                )
            }
            Button(
                onClick = onDelete,
                colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
            ) {
                Text("Usuń", color = Color.White)
            }
        }
    }
}

@Composable
fun HornerTable(
    coefficients: List<Double>,
    divider: Double,
    modifier: Modifier = Modifier
) {
    if (coefficients.isEmpty()) return

    val n = coefficients.size
    val r = -divider

    val mulRow = MutableList<Double?>(n) { null }
    val sumRow = MutableList<Double>(n) { 0.0 }

    // Start: pierwszy współczynnik kopiujemy bez zmian
    sumRow[0] = coefficients[0]

    // Horner: kolejno mnożymy i dodajemy współczynnik
    for (i in 1 until n) {
        mulRow[i] = sumRow[i - 1] * r
        sumRow[i] = mulRow[i]!! + coefficients[i]
    }

    Column(modifier = modifier.padding(4.dp)) {
        // Nagłówki: Dzielnik i współczynniki
        Row(modifier = Modifier.fillMaxWidth()) {
            Box(
                Modifier
                    .weight(1f)
                    .border(1.dp, Color.Black)
                    .padding(2.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "Dzielnik", fontSize = 8.sp)
            }
            coefficients.forEach { coef ->
                Box(
                    Modifier
                        .weight(1f)
                        .border(1.dp, Color.Black)
                        .padding(2.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (coef % 1.0 == 0.0) coef.toInt().toString() else String.format("%.2f", coef),
                        fontSize = 9.sp
                    )
                }
            }
        }

        // Wiersz mnożenia
        Row(modifier = Modifier.fillMaxWidth()) {
            Box(
                Modifier
                    .weight(1f)
                    .border(1.dp, Color.Black)
                    .padding(2.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (r % 1.0 == 0.0) r.toInt().toString() else String.format("%.2f", r),
                    fontSize = 9.sp
                )
            }
            mulRow.forEach { m ->
                Box(
                    Modifier
                        .weight(1f)
                        .border(1.dp, Color.Black)
                        .padding(2.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = m?.let {
                            if (it % 1.0 == 0.0) it.toInt().toString() else String.format("%.2f", it)
                        } ?: "",
                        fontSize = 9.sp
                    )
                }
            }
        }

        Spacer(
            Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(Color.Black)
        )

        // Wiersz sum
        Row(modifier = Modifier.fillMaxWidth()) {
            Box(
                Modifier
                    .weight(1f)
                    .border(1.dp, Color.Black)
                    .padding(2.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("", fontSize = 9.sp)
            }
            sumRow.forEach { s ->
                Box(
                    Modifier
                        .weight(1f)
                        .border(1.dp, Color.Black)
                        .padding(2.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (s % 1.0 == 0.0) s.toInt().toString() else String.format("%.2f", s),
                        fontSize = 9.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Wyliczenie wyniku w postaci wielomianu
        val quotient = sumRow.dropLast(1)
        val remainder = sumRow.last()
        val degree = coefficients.size - 2

        val resultPolynomial = buildString {
            quotient.forEachIndexed { index, coef ->
                if (coef == 0.0) return@forEachIndexed
                val currentPower = degree - index
                val absCoef = kotlin.math.abs(coef)
                val formattedCoef = if (absCoef % 1.0 == 0.0) absCoef.toInt().toString() else String.format("%.2f", absCoef)

                val sign = when {
                    index == 0 -> if (coef < 0) "-" else ""
                    coef > 0 -> " + "
                    else -> " - "
                }

                val term = when (currentPower) {
                    0 -> formattedCoef
                    1 -> "${formattedCoef}x"
                    else -> "${formattedCoef}x^$currentPower"
                }

                append(sign).append(term)
            }
        }

        Text(
            text = "Wielomian: $resultPolynomial",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Black
        )

        Text(
            text = "Reszta: ${if (remainder % 1.0 == 0.0) remainder.toInt() else String.format("%.2f", remainder)}",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Black
        )
    }
}


fun prepareCoefficientsForHorner(coefficientsList: List<Coefficient>): List<Double> {
    if (coefficientsList.isEmpty()) return emptyList()

    // Grupujemy po potędze i sumujemy współczynniki
    val grouped = coefficientsList.groupBy { it.power }
        .mapValues { entry -> entry.value.sumOf { it.number } }

    val maxPower = grouped.keys.maxOrNull() ?: 0

    // Tworzymy listę od max potęgi do 0 z zerami tam gdzie brak składnika
    return (maxPower downTo 0).map { power ->
        grouped[power] ?: 0.0
    }
}