package com.example.curtaincalculator

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.foundation.isSystemInDarkTheme
import android.os.Build
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.CompositionLocalProvider
import kotlin.math.abs

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CurtainCalculatorTheme {
                // فرض اتجاه RTL للتطبيق كله
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        CurtainCalculatorScreen()
                    }
                }
            }
        }
    }
}

@Composable
fun CurtainCalculatorTheme(content: @Composable () -> Unit) {
    val darkTheme = isSystemInDarkTheme()
    val context = LocalContext.current
    val colorScheme = when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        else -> lightColorScheme()
    }
    MaterialTheme(colorScheme = colorScheme, content = content)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CurtainCalculatorScreen() {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("حاسبة الستائر", fontSize = 22.sp) },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            WindowCalculatorCard()
            PleatCalculatorCard()
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

/* ============================================================
   الحاسبة الأولى: قياس الشباك
   ============================================================ */
@Composable
fun WindowCalculatorCard() {
    var windowWidth by remember { mutableStateOf("") }
    var rodWidth by remember { mutableStateOf("") }
    var result by remember { mutableStateOf<String?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                "حاسبة قياس الشباك",
                style = MaterialTheme.typography.headlineSmall,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = windowWidth,
                onValueChange = { windowWidth = it.filter { ch -> ch.isDigit() || ch == '.' } },
                label = { Text("عرض الشباك (سم)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = rodWidth,
                onValueChange = { rodWidth = it.filter { ch -> ch.isDigit() || ch == '.' } },
                label = { Text("عرض البارة (سم)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        errorMessage = null
                        result = null
                        val w = windowWidth.toDoubleOrNull()
                        val r = rodWidth.toDoubleOrNull()
                        if (w == null || r == null || w <= 0 || r <= 0) {
                            errorMessage = "يرجى إدخال أرقام موجبة صحيحة."
                        } else {
                            val res = (w - (r - 10)) / 2.0
                            result = "المسافة من كل جانب: ${"%.2f".format(res)} سم"
                        }
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("احسب")
                }

                OutlinedButton(
                    onClick = {
                        windowWidth = ""
                        rodWidth = ""
                        result = null
                        errorMessage = null
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("مسح")
                }
            }

            errorMessage?.let {
                Text(
                    it,
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            result?.let {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        it,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
        }
    }
}

/* ============================================================
   الحاسبة الثانية: حاسبة طيات الستارة
   ============================================================ */
data class PleatResult(val oddNumber: Int, val spacing: Double, val closestTarget: Double)

@Composable
fun PleatCalculatorCard() {
    var curtainWidth by remember { mutableStateOf("") }
    var results by remember { mutableStateOf<List<PleatResult>>(emptyList()) }
    var noResult by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                "حاسبة طيات الستارة",
                style = MaterialTheme.typography.headlineSmall,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = curtainWidth,
                onValueChange = { curtainWidth = it.filter { ch -> ch.isDigit() || ch == '.' } },
                label = { Text("عرض الستارة (سم)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        errorMessage = null
                        results = emptyList()
                        noResult = false
                        val w = curtainWidth.toDoubleOrNull()
                        if (w == null || w <= 0) {
                            errorMessage = "يرجى إدخال رقم موجب صحيح."
                        } else {
                            val computed = calculatePleats(w)
                            if (computed.isEmpty()) noResult = true
                            else results = computed
                        }
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("احسب")
                }

                OutlinedButton(
                    onClick = {
                        curtainWidth = ""
                        results = emptyList()
                        noResult = false
                        errorMessage = null
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("مسح")
                }
            }

            errorMessage?.let {
                Text(
                    it,
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            if (noResult) {
                Text(
                    "لا يوجد عدد فردي مناسب.",
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            if (results.isNotEmpty()) {
                Text(
                    "النتائج المحتملة",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )

                results.forEach { res ->
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.tertiaryContainer
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                "العدد الفردي: ${res.oddNumber}",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                "المسافة بين الطيات: ${"%.2f".format(res.spacing)} سم",
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * تبحث في كل الأرقام الفردية وتقسم عرض الستارة عليها،
 * وترجع كل النتائج القريبة من 15 أو 17 أو 18 سم.
 * يعتبر الرقم قريبًا إذا كان الفرق أقل من أو يساوي 1 سم.
 */
fun calculatePleats(curtainWidth: Double): List<PleatResult> {
    val targets = listOf(15.0, 17.0, 18.0)
    val tolerance = 1.0
    val results = mutableListOf<PleatResult>()

    // نبدأ من 3 لأن أقل عدد فردي منطقي للطيات هو 3
    var odd = 3
    while (odd <= curtainWidth.toInt()) {
        val spacing = curtainWidth / odd
        // نبحث عن أقرب هدف
        val closest = targets.minByOrNull { abs(it - spacing) } ?: 0.0
        val diff = abs(closest - spacing)
        if (diff <= tolerance) {
            results.add(PleatResult(odd, spacing, closest))
        }
        // لو المسافة أصبحت أصغر بكثير من أصغر هدف نوقف
        if (spacing < 14.0) break
        odd += 2
    }

    // نرتب النتائج بالأقرب لهدفها أولًا
    return results.sortedBy { abs(it.closestTarget - it.spacing) }
}
