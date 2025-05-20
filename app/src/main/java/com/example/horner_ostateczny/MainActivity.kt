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
    val coefficientsList = remember {
        mutableStateListOf(
            Coefficient(59.0, 4),
            Coefficient(3.0, 4),
            Coefficient(3.0, 4),
            Coefficient(.0, 3),
            Coefficient(-2.0, 2),
            Coefficient(1.0, 1),
            Coefficient(-5.0, 0)
        )
    }

    var showTableAndText by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var selectedCoefficient by remember { mutableStateOf<Coefficient?>(null) }
    var numberText by remember { mutableStateOf("") }
    var powerText by remember { mutableStateOf("") }
    var showDz by remember { mutableStateOf(false) }
    var showCoefficients by remember { mutableStateOf(false) }
    var dividerText by remember { mutableStateOf("") }
    var divider by remember { mutableStateOf(2.0) }
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
                        coefficientsList.remove(coefficient)
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
                    if (coefficientsList.isNotEmpty()) {
                        val (quotient, remainder) = hornerCalculate(coefficientsList, divider)
                        val quotientStr = formatPolynomialFromCoefficients(quotient)
                        val remainderStr = if (remainder % 1 == 0.0) remainder.toInt()
                            .toString() else remainder.toString()
                        resultText = "Wynik:\nIloraz: $quotientStr\nReszta: $remainderStr"
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
                resultText = null // <-- to dodaj
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
                        coefficientsList.add(Coefficient(number, power))
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
                        coefficientsList.remove(selectedCoefficient)
                        coefficientsList.add(Coefficient(number, power))
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

fun formatPolynomialFromCoefficients(coefficients: List<Coefficient>): String {
    val parts = mutableListOf<String>()

    for ((_, coef) in coefficients.sortedByDescending { it.power }.withIndex()) {
        if (coef.number == 0.0) continue

        val num = coef.number
        val numStr = when {
            num == 1.0 && coef.power != 0 -> ""
            num == -1.0 && coef.power != 0 -> "-"
            num % 1 == 0.0 -> num.toInt().toString()
            else -> num.toString()
        }

        val part = when (coef.power) {
            0 -> numStr
            1 -> "${numStr}x"
            else -> "${numStr}x^${coef.power}"
        }

        if (parts.isNotEmpty() && num > 0) {
            parts.add("+ $part")
        } else {
            parts.add(part)
        }
    }

    return if (parts.isEmpty()) "0" else parts.joinToString(" ")
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
    val full = coefficients
    val r = -divider

    val mulRow = MutableList<Double?>(n) { null }
    val sumRow = MutableList<Double>(n) { 0.0 }

    sumRow[0] = full[0]
    for (i in 1 until n) {
        mulRow[i] = sumRow[i - 1] * r
        sumRow[i] = mulRow[i]!! + full[i]
    }

    Column(modifier = modifier.padding(4.dp)) {
        // Nagłówki
        Row(modifier = Modifier.fillMaxWidth()) {
            Box(
                Modifier
                    .weight(1f)
                    .border(1.dp, Color.Black)
                    .padding(horizontal = 2.dp, vertical = 2.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "Dzielnik", fontSize = 6.sp)
            }
            full.forEach { coef ->
                Box(
                    Modifier
                        .weight(1f)
                        .border(1.dp, Color.Black)
                        .padding(horizontal = 2.dp, vertical = 2.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (coef % 1.0 == 0.0) coef.toInt().toString() else coef.toString(),
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
                    .padding(horizontal = 2.dp, vertical = 2.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (r % 1.0 == 0.0) r.toInt().toString() else r.toString(),
                    fontSize = 9.sp
                )
            }
            mulRow.forEach { m ->
                Box(
                    Modifier
                        .weight(1f)
                        .border(1.dp, Color.Black)
                        .padding(horizontal = 2.dp, vertical = 2.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = m?.let {
                            if (it % 1.0 == 0.0) it.toInt().toString() else String.format(
                                "%.2f",
                                it
                            )
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
                    .padding(horizontal = 2.dp, vertical = 2.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("", fontSize = 9.sp)
            }
            sumRow.forEach { s ->
                Box(
                    Modifier
                        .weight(1f)
                        .border(1.dp, Color.Black)
                        .padding(horizontal = 2.dp, vertical = 2.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (s % 1.0 == 0.0) s.toInt().toString()
                        else String.format("%.2f", s),
                        fontSize = 9.sp
                    )
                }
            }
        }
    }
}

