package com.example.curtaincalculator

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CurtainCalculatorTheme {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        MainScreen()
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

/* ============================================================
   الشاشة الرئيسية مع نظام التابات
   ============================================================ */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
    val tabs = listOf("الشباك", "الحلقات")
    var selectedTab by remember { mutableIntStateOf(0) }

    Scaffold(
        topBar = {
            Column {
                CenterAlignedTopAppBar(
                    title = { Text("حاسبة الستائر", fontSize = 22.sp) },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                )
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            text = {
                                Text(
                                    title,
                                    fontSize = 16.sp
                                )
                            }
                        )
                    }
                }
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when (selectedTab) {
                0 -> WindowCalculatorScreen()
                1 -> RingsCalculatorScreen()
            }
        }
    }
}

/* ============================================================
   الحاسبة الأولى: قياس الشباك
   ============================================================ */
@Composable
fun WindowCalculatorScreen() {
    var windowWidth by remember { mutableStateOf("") }
    var rodWidth by remember { mutableStateOf("") }
    var result by remember { mutableStateOf<String?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
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
                    label = { Text("عرض الماسورة (سم)") },
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
}

/* ============================================================
   الحاسبة الثانية: حاسبة عدد الحلقات
   ============================================================ */
data class RingResult(val ringsCount: Int, val spacing: Double)

@Composable
fun RingsCalculatorScreen() {
    var curtainWidth by remember { mutableStateOf("") }
    var results by remember { mutableStateOf<List<RingResult>>(emptyList()) }
    var noResult by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var effectiveWidth by remember { mutableStateOf<Double?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    "حاسبة عدد الحلقات",
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
                            effectiveWidth = null
                            val w = curtainWidth.toDoubleOrNull()
                            if (w == null || w <= 10) {
                                errorMessage = "يرجى إدخال رقم أكبر من 10."
                            } else {
                                val actual = w - 10.0
                                effectiveWidth = actual
                                val computed = calculateRings(actual)
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
                            effectiveWidth = null
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

                effectiveWidth?.let {
                    Text(
                        "المقاس الفعلي بعد خصم 10 سم: ${"%.2f".format(it)} سم",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                if (noResult) {
                    Text(
                        "لا يوجد عدد حلقات مناسب.",
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
                                    "عدد الحلقات: ${res.ringsCount}",
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Text(
                                    "المسافة بين الحلقات: ${"%.2f".format(res.spacing)} سم",
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * تبحث في كل الأعداد الفردية وتقسم عرض الستارة عليها،
 * وترجع كل النتائج التي تقع في المدى من 15 إلى 18 سم (شامل الطرفين).
 * المدخل هنا هو "المقاس الفعلي" (عرض الستارة - 10).
 */
fun calculateRings(effectiveWidth: Double): List<RingResult> {
    val minSpacing = 15.0
    val maxSpacing = 18.0
    val results = mutableListOf<RingResult>()

    // نبدأ من 3 لأن أقل عدد فردي منطقي للحلقات هو 3
    var odd = 3
    while (odd <= effectiveWidth.toInt()) {
        val spacing = effectiveWidth / odd
        if (spacing in minSpacing..maxSpacing) {
            results.add(RingResult(odd, spacing))
        }
        // لو المسافة أصغر من الحد الأدنى، لا داعي لإكمال البحث
        if (spacing < minSpacing) break
        odd += 2
    }

    // نرتب النتائج بالأقل عددًا (الأكبر مسافة) أولاً
    return results.sortedBy { it.ringsCount }
}
