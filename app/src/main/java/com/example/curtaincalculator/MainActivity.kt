package com.example.curtaincalculator

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import kotlin.math.cos
import kotlin.math.sin

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

/* ============================================================
   ألوان التطبيق المخصصة (متناسقة مع أيقونة التطبيق)
   ============================================================ */
private val LightCurtainColorScheme = lightColorScheme(
    primary = Color(0xFF283593),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFE0E5FF),
    onPrimaryContainer = Color(0xFF101847),
    secondary = Color(0xFF00695C),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFD2F5EE),
    onSecondaryContainer = Color(0xFF003830),
    tertiary = Color(0xFFB45309),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFEDF1FF),
    onTertiaryContainer = Color(0xFF162058),
    background = Color(0xFFF4F6FC),
    onBackground = Color(0xFF171B2C),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF171B2C),
    surfaceVariant = Color(0xFFE9ECF8),
    onSurfaceVariant = Color(0xFF434965),
    outline = Color(0xFF757C9B),
    error = Color(0xFFC62828),
    onError = Color(0xFFFFFFFF)
)

private val DarkCurtainColorScheme = darkColorScheme(
    primary = Color(0xFFAEC0FF),
    onPrimary = Color(0xFF142061),
    primaryContainer = Color(0xFF23307A),
    onPrimaryContainer = Color(0xFFE0E6FF),
    secondary = Color(0xFF64FFDA),
    onSecondary = Color(0xFF003830),
    secondaryContainer = Color(0xFF005046),
    onSecondaryContainer = Color(0xFFB2FEF0),
    tertiary = Color(0xFFFFCA7A),
    onTertiary = Color(0xFF472A00),
    tertiaryContainer = Color(0xFF222B59),
    onTertiaryContainer = Color(0xFFE3E8FF),
    background = Color(0xFF0E1220),
    onBackground = Color(0xFFE4E7F5),
    surface = Color(0xFF171C2E),
    onSurface = Color(0xFFE4E7F5),
    surfaceVariant = Color(0xFF232A42),
    onSurfaceVariant = Color(0xFFC1C6DF),
    outline = Color(0xFF8B91AD),
    error = Color(0xFFFF8A80),
    onError = Color(0xFF601410)
)

@Composable
fun CurtainCalculatorTheme(content: @Composable () -> Unit) {
    val darkTheme = isSystemInDarkTheme()
    val colorScheme = if (darkTheme) DarkCurtainColorScheme else LightCurtainColorScheme
    MaterialTheme(colorScheme = colorScheme, content = content)
}

/* ============================================================
   الشاشة الرئيسية مع نظام التابات وسجل منفصل لكل قسم
   ============================================================ */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
    val context = LocalContext.current
    val tabs = listOf("الشباك", "الحلقات")
    var selectedTab by remember { mutableIntStateOf(0) }
    var activeHistoryType by remember { mutableStateOf<CalculatorType?>(null) }
    var historyEntries by remember { mutableStateOf(loadHistoryEntries(context)) }
    var manualTutorialTrigger by remember { mutableIntStateOf(0) }

    val recordCalculation: (CalculationHistoryEntry) -> Unit = { newEntry ->
        val updated = addHistoryEntry(historyEntries, newEntry)
        historyEntries = updated
        saveHistoryEntries(context, updated)
    }

    activeHistoryType?.let { sectionType ->
        CalculationHistoryScreen(
            calculatorType = sectionType,
            entries = historyEntries,
            onBack = { activeHistoryType = null },
            onDeleteEntry = { entryId ->
                val updated = deleteHistoryEntry(historyEntries, entryId)
                historyEntries = updated
                saveHistoryEntries(context, updated)
            },
            onDeleteDayGroup = { dateKey ->
                val updated = deleteHistoryDayGroup(historyEntries, sectionType, dateKey)
                historyEntries = updated
                saveHistoryEntries(context, updated)
            }
        )
        return
    }

    val windowHistoryCount = remember(historyEntries) {
        historyEntries.count { it.calculatorType == CalculatorType.WINDOW }
    }
    val ringsHistoryCount = remember(historyEntries) {
        historyEntries.count { it.calculatorType == CalculatorType.RINGS }
    }

    Scaffold(
        topBar = {
            Surface(
                color = MaterialTheme.colorScheme.primary,
                shadowElevation = 6.dp
            ) {
                Column {
                    CenterAlignedTopAppBar(
                        title = {
                            Text(
                                "حاسبة الستائر",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        },
                        actions = {
                            Surface(
                                onClick = { manualTutorialTrigger++ },
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.16f),
                                modifier = Modifier
                                    .padding(end = 12.dp)
                                    .size(36.dp)
                                    .semantics {
                                        contentDescription = "عرض دليل الاستخدام"
                                    }
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = "؟",
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimary
                                    )
                                }
                            }
                        },
                        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                            containerColor = Color.Transparent
                        )
                    )
                    TabRow(
                        selectedTabIndex = selectedTab,
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ) {
                        tabs.forEachIndexed { index, title ->
                            val isSelected = selectedTab == index
                            Tab(
                                selected = isSelected,
                                onClick = { selectedTab = index },
                                text = {
                                    Text(
                                        title,
                                        fontSize = 16.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) {
                                            MaterialTheme.colorScheme.onPrimary
                                        } else {
                                            MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.72f)
                                        }
                                    )
                                }
                            )
                        }
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
                0 -> WindowCalculatorScreen(
                    historyCount = windowHistoryCount,
                    manualTutorialTrigger = manualTutorialTrigger,
                    onOpenHistory = { activeHistoryType = CalculatorType.WINDOW },
                    onSaveHistory = recordCalculation
                )
                1 -> RingsCalculatorScreen(
                    historyCount = ringsHistoryCount,
                    manualTutorialTrigger = manualTutorialTrigger,
                    onOpenHistory = { activeHistoryType = CalculatorType.RINGS },
                    onSaveHistory = recordCalculation
                )
            }
        }
    }
}

/* ============================================================
   الحاسبة الأولى: قياس الشباك
   ============================================================ */
@Composable
fun WindowCalculatorScreen(
    historyCount: Int = 0,
    manualTutorialTrigger: Int = 0,
    onOpenHistory: () -> Unit = {},
    onSaveHistory: (CalculationHistoryEntry) -> Unit = {}
) {
    var windowWidth by remember { mutableStateOf("") }
    var rodWidth by remember { mutableStateOf("") }
    var rodDeduction by remember { mutableStateOf("10") }
    var result by remember { mutableStateOf<String?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val focusManager = LocalFocusManager.current
    val tutorialTargets = remember { mutableStateMapOf<String, Rect>() }
    val (showTutorial, updateTutorialVisibility, reopenTutorial) =
        rememberTutorialController(WINDOW_TUTORIAL_KEY)

    LaunchedEffect(manualTutorialTrigger) {
        if (manualTutorialTrigger > 0) reopenTutorial()
    }

    val performWindowCalculation = {
        focusManager.clearFocus()
        errorMessage = null
        result = null
        val w = windowWidth.toDoubleOrNull()
        val r = rodWidth.toDoubleOrNull()
        val deduction = rodDeduction.toDoubleOrNull()
        if (
            w == null || r == null || deduction == null ||
            w <= 0 || r <= 0 || deduction < 0
        ) {
            errorMessage =
                "يرجى إدخال عرضين موجبين، وخصم يساوي صفرًا أو أكثر."
        } else {
            val res = calculateRodDistance(w, r, deduction)
            val formattedResult = "المسافة من كل جانب: ${"%.2f".format(res)} سم"
            result = formattedResult
            onSaveHistory(
                createHistoryEntry(
                    calculatorType = CalculatorType.WINDOW,
                    measurementSummary = "الشباك: ${formatCleanNumber(w)} سم • الماسورة: ${formatCleanNumber(r)} سم\n$formattedResult"
                )
            )
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        "حاسبة قياس الشباك",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )

                    CalculatorNumberField(
                        value = windowWidth,
                        onValueChange = { windowWidth = it },
                        label = "عرض الشباك (سم)",
                        onDoneAction = performWindowCalculation,
                        modifier = Modifier
                            .fillMaxWidth()
                            .onGloballyPositioned {
                                tutorialTargets["window_width"] = it.boundsInRoot()
                            }
                    )

                    CalculatorNumberField(
                        value = rodWidth,
                        onValueChange = { rodWidth = it },
                        label = "عرض الماسورة (سم)",
                        onDoneAction = performWindowCalculation,
                        modifier = Modifier
                            .fillMaxWidth()
                            .onGloballyPositioned {
                                tutorialTargets["rod_width"] = it.boundsInRoot()
                            }
                    )

                    CalculatorNumberField(
                        value = rodDeduction,
                        onValueChange = { rodDeduction = it },
                        label = "الخصم من الماسورة (سم)",
                        onDoneAction = performWindowCalculation,
                        modifier = Modifier
                            .fillMaxWidth()
                            .onGloballyPositioned {
                                tutorialTargets["rod_deduction"] = it.boundsInRoot()
                            }
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = performWindowCalculation,
                            shape = RoundedCornerShape(14.dp),
                            contentPadding = PaddingValues(vertical = 12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .onGloballyPositioned {
                                    tutorialTargets["window_calculate"] = it.boundsInRoot()
                                }
                        ) {
                            Text("احسب", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = {
                                focusManager.clearFocus()
                                windowWidth = ""
                                rodWidth = ""
                                rodDeduction = "10"
                                result = null
                                errorMessage = null
                            },
                            shape = RoundedCornerShape(14.dp),
                            contentPadding = PaddingValues(vertical = 12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("مسح", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }

                    errorMessage?.let {
                        Text(
                            it,
                            color = MaterialTheme.colorScheme.error,
                            textAlign = TextAlign.Center,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    result?.let {
                        Card(
                            shape = RoundedCornerShape(14.dp),
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
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(84.dp))
        }

        HistoryFloatingButton(
            count = historyCount,
            onClick = onOpenHistory,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(20.dp)
                .onGloballyPositioned {
                    tutorialTargets["history_button"] = it.boundsInRoot()
                }
        )

        if (showTutorial) {
            TutorialGuideOverlay(
                steps = windowTutorialSteps,
                targetBoundsInRoot = tutorialTargets,
                onTemporarilyDismiss = { updateTutorialVisibility(false) },
                onComplete = { updateTutorialVisibility(true) }
            )
        }
    }
}

/* ============================================================
   الحاسبة الثانية: حاسبة عدد الحلقات
   ============================================================ */
data class RingResult(val ringsCount: Int, val spacing: Double) {
    // ringsCount remains the existing odd divisor; display one extra as the ring count.
    val actualRingsCount: Int
        get() = ringsCount + 1

    fun pressesPerMeter(curtainWidthCm: Double): Double =
        calculatePressesPerMeter(actualRingsCount, curtainWidthCm)
}

@Composable
fun RingsCalculatorScreen(
    historyCount: Int = 0,
    manualTutorialTrigger: Int = 0,
    onOpenHistory: () -> Unit = {},
    onSaveHistory: (CalculationHistoryEntry) -> Unit = {}
) {
    var curtainWidth by remember { mutableStateOf("") }
    var calculatedCurtainWidth by remember { mutableStateOf<Double?>(null) }
    var results by remember { mutableStateOf<List<RingResult>>(emptyList()) }
    var noResult by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var effectiveWidth by remember { mutableStateOf<Double?>(null) }
    val focusManager = LocalFocusManager.current
    val tutorialTargets = remember { mutableStateMapOf<String, Rect>() }
    val (showTutorial, updateTutorialVisibility, reopenTutorial) =
        rememberTutorialController(RINGS_TUTORIAL_KEY)

    LaunchedEffect(manualTutorialTrigger) {
        if (manualTutorialTrigger > 0) reopenTutorial()
    }

    val performRingsCalculation = {
        focusManager.clearFocus()
        errorMessage = null
        results = emptyList()
        noResult = false
        effectiveWidth = null
        calculatedCurtainWidth = null
        val w = curtainWidth.toDoubleOrNull()
        if (w == null || w <= 10) {
            errorMessage = "يرجى إدخال رقم أكبر من 10."
        } else {
            val actual = w - 10.0
            effectiveWidth = actual
            calculatedCurtainWidth = w
            val computed = calculateRings(actual)
            if (computed.isEmpty()) {
                noResult = true
            } else {
                results = computed
                val middleRing = selectMiddleRingResult(computed)
                if (middleRing != null) {
                    onSaveHistory(
                        createHistoryEntry(
                            calculatorType = CalculatorType.RINGS,
                            measurementSummary = formatRingsHistorySummary(
                                curtainWidthCm = w,
                                middleRingsCount = middleRing.actualRingsCount
                            )
                        )
                    )
                }
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        "حاسبة عدد الحلقات",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )

                    CalculatorNumberField(
                        value = curtainWidth,
                        onValueChange = { curtainWidth = it },
                        label = "عرض الستارة (سم)",
                        onDoneAction = performRingsCalculation,
                        modifier = Modifier
                            .fillMaxWidth()
                            .onGloballyPositioned {
                                tutorialTargets["curtain_width"] = it.boundsInRoot()
                            }
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = performRingsCalculation,
                            shape = RoundedCornerShape(14.dp),
                            contentPadding = PaddingValues(vertical = 12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .onGloballyPositioned {
                                    tutorialTargets["rings_calculate"] = it.boundsInRoot()
                                }
                        ) {
                            Text("احسب", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = {
                                focusManager.clearFocus()
                                curtainWidth = ""
                                calculatedCurtainWidth = null
                                results = emptyList()
                                noResult = false
                                errorMessage = null
                                effectiveWidth = null
                            },
                            shape = RoundedCornerShape(14.dp),
                            contentPadding = PaddingValues(vertical = 12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("مسح", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }

                    errorMessage?.let {
                        Text(
                            it,
                            color = MaterialTheme.colorScheme.error,
                            textAlign = TextAlign.Center,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    effectiveWidth?.let {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                "المقاس الفعلي بعد خصم 10 سم: ${"%.2f".format(it)} سم",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp, horizontal = 12.dp)
                            )
                        }
                    }

                    if (noResult) {
                        Text(
                            "لا يوجد عدد حلقات مناسب.",
                            color = MaterialTheme.colorScheme.error,
                            textAlign = TextAlign.Center,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    if (results.isNotEmpty()) {
                        Text(
                            "النتائج المحتملة",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )

                        val widthForPresses = calculatedCurtainWidth ?: 0.0
                        val middleResult = selectMiddleRingResult(results)

                        results.forEach { res ->
                            val isMiddle = res == middleResult && results.size > 1
                            val pressesPerMeter = res.pressesPerMeter(widthForPresses)
                            Card(
                                shape = RoundedCornerShape(16.dp),
                                border = if (isMiddle) {
                                    BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
                                } else {
                                    BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
                                },
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isMiddle) {
                                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.65f)
                                    } else {
                                        MaterialTheme.colorScheme.tertiaryContainer
                                    }
                                ),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    if (isMiddle) {
                                        Surface(
                                            shape = RoundedCornerShape(50),
                                            color = MaterialTheme.colorScheme.primary
                                        ) {
                                            Text(
                                                text = "الأوسط",
                                                color = MaterialTheme.colorScheme.onPrimary,
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                    Text(
                                        "عدد الكبسات في كل متر: ${"%.2f".format(pressesPerMeter)}",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        "عدد الحلقات: ${res.actualRingsCount} حلقات",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
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

            Spacer(modifier = Modifier.height(84.dp))
        }

        HistoryFloatingButton(
            count = historyCount,
            onClick = onOpenHistory,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(20.dp)
                .onGloballyPositioned {
                    tutorialTargets["history_button"] = it.boundsInRoot()
                }
        )

        if (showTutorial) {
            TutorialGuideOverlay(
                steps = ringsTutorialSteps,
                targetBoundsInRoot = tutorialTargets,
                onTemporarilyDismiss = { updateTutorialVisibility(false) },
                onComplete = { updateTutorialVisibility(true) }
            )
        }
    }
}

/* ============================================================
   تصميم زر السجل العائم وصفحة السجل الخاصة بكل قسم
   ============================================================ */
@Composable
fun HistoryFloatingButton(
    count: Int = 0,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(28.dp),
        color = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary,
        shadowElevation = 8.dp,
        border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.25f)),
        modifier = modifier.semantics {
            contentDescription = "سجل الحسابات"
        }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // دائرة داخلية للأيقونة تعطي شكلاً عصرياً مميزاً
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.18f)),
                contentAlignment = Alignment.Center
            ) {
                val iconColor = MaterialTheme.colorScheme.onPrimary
                Canvas(modifier = Modifier.size(20.dp)) {
                    val stroke = 2.2.dp.toPx()
                    val radius = size.minDimension * 0.42f
                    val center = Offset(size.width / 2f, size.height / 2f)

                    // قوس الساعة مع سهم الاسترجاع
                    drawArc(
                        color = iconColor,
                        startAngle = -35f,
                        sweepAngle = 295f,
                        useCenter = false,
                        topLeft = Offset(center.x - radius, center.y - radius),
                        size = Size(radius * 2, radius * 2),
                        style = Stroke(width = stroke, cap = StrokeCap.Round)
                    )

                    // رأس السهم أعلى القوس
                    val arrowAngleRad = Math.toRadians(-35.0)
                    val tipX = (center.x + radius * cos(arrowAngleRad)).toFloat()
                    val tipY = (center.y + radius * sin(arrowAngleRad)).toFloat()
                    val arrowHead = Path().apply {
                        moveTo(tipX, tipY)
                        lineTo(tipX - 5.dp.toPx(), tipY - 1.5.dp.toPx())
                        lineTo(tipX - 1.dp.toPx(), tipY - 5.5.dp.toPx())
                        close()
                    }
                    drawPath(arrowHead, color = iconColor)

                    // عقارب الساعة
                    drawLine(
                        color = iconColor,
                        start = center,
                        end = Offset(center.x, center.y - radius * 0.52f),
                        strokeWidth = stroke,
                        cap = StrokeCap.Round
                    )
                    drawLine(
                        color = iconColor,
                        start = center,
                        end = Offset(center.x + radius * 0.44f, center.y + radius * 0.22f),
                        strokeWidth = stroke,
                        cap = StrokeCap.Round
                    )
                }
            }

            Text(
                text = "السجل",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimary
            )

            if (count > 0) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                ) {
                    Text(
                        text = count.toString(),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalculationHistoryScreen(
    calculatorType: CalculatorType,
    entries: List<CalculationHistoryEntry>,
    onBack: () -> Unit,
    onDeleteEntry: (String) -> Unit,
    onDeleteDayGroup: (String) -> Unit
) {
    BackHandler(onBack = onBack)

    val dayGroups = remember(entries, calculatorType) {
        groupHistoryByDayForSection(entries, calculatorType)
    }
    // جميع الأيام تكون مقفولة تلقائياً عند فتح السجل
    val expandedDays = remember(calculatorType) { mutableStateMapOf<String, Boolean>() }

    Scaffold(
        topBar = {
            Surface(
                color = MaterialTheme.colorScheme.primary,
                shadowElevation = 4.dp
            ) {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            calculatorType.historyTitle,
                            fontSize = 21.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    },
                    navigationIcon = {
                        TextButton(onClick = onBack) {
                            Text(
                                "رجوع",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = Color.Transparent
                    )
                )
            }
        }
    ) { padding ->
        if (dayGroups.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "لا توجد حسابات محفوظة في هذا القسم حتى الآن.",
                    style = MaterialTheme.typography.titleMedium,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                dayGroups.forEach { dayGroup ->
                    val isExpanded = expandedDays[dayGroup.dateKey] == true

                    // مستطيل اليوم بعرض الشاشة بالكامل (مقفول تلقائياً، وعليه من الخارج اسم اليوم ورقم اليوم ورقم الشهر فقط)
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                expandedDays[dayGroup.dateKey] = !isExpanded
                            },
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = dayGroup.outsideLabel,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.weight(1f)
                            )

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                OutlinedButton(
                                    onClick = { onDeleteDayGroup(dayGroup.dateKey) },
                                    shape = RoundedCornerShape(10.dp),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.6f)),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        contentColor = MaterialTheme.colorScheme.error
                                    )
                                ) {
                                    Text("حذف", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                }
                                Text(
                                    text = if (isExpanded) "▲" else "▼",
                                    fontSize = 15.sp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }

                    // عند فتح اليوم: يعرض المقاس وأوسط عدد حلقات والوقت فقط (بدون تكرار التاريخ)
                    if (isExpanded) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 6.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            dayGroup.entries.forEach { entry ->
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    border = BorderStroke(
                                        1.dp,
                                        MaterialTheme.colorScheme.outline.copy(alpha = 0.22f)
                                    ),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.surface
                                    ),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 14.dp, vertical = 12.dp),
                                        verticalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Surface(
                                                shape = RoundedCornerShape(8.dp),
                                                color = MaterialTheme.colorScheme.surfaceVariant
                                            ) {
                                                Text(
                                                    text = "الوقت: ${entry.timeLabel}",
                                                    style = MaterialTheme.typography.labelLarge,
                                                    color = MaterialTheme.colorScheme.primary,
                                                    fontWeight = FontWeight.Bold,
                                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                                )
                                            }
                                            TextButton(
                                                onClick = { onDeleteEntry(entry.id) },
                                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                                colors = ButtonDefaults.textButtonColors(
                                                    contentColor = MaterialTheme.colorScheme.error
                                                )
                                            ) {
                                                Text("حذف", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }

                                        Text(
                                            text = entry.measurementSummary,
                                            style = MaterialTheme.typography.bodyLarge,
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CalculatorNumberField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    imeAction: ImeAction = ImeAction.Done,
    onDoneAction: (() -> Unit)? = null
) {
    val focusManager = LocalFocusManager.current
    val inputContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f)
    OutlinedTextField(
        value = value,
        onValueChange = { enteredValue ->
            onValueChange(enteredValue.filter { character ->
                character.isDigit() || character == '.'
            })
        },
        label = { Text(label) },
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Decimal,
            imeAction = imeAction
        ),
        keyboardActions = KeyboardActions(
            onDone = {
                focusManager.clearFocus()
                onDoneAction?.invoke()
            }
        ),
        singleLine = true,
        shape = RoundedCornerShape(14.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = inputContainerColor,
            unfocusedContainerColor = inputContainerColor,
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.6f),
            focusedLabelColor = MaterialTheme.colorScheme.primary,
            unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
        ),
        modifier = modifier
    )
}

/* ============================================================
   دوال الحساب وإدارة سجل الحسابات لكل قسم
   ============================================================ */

/** Keeps the existing rod formula while allowing its former fixed 10 cm to vary. */
fun calculateRodDistance(
    windowWidth: Double,
    rodWidth: Double,
    deduction: Double = 10.0
): Double = (windowWidth - (rodWidth - deduction)) / 2.0

/** يحسب عدد الكبسات في كل متر = عدد الحلقات الفعلي ÷ عرض الستارة بالمتر */
fun calculatePressesPerMeter(actualRingsCount: Int, curtainWidthCm: Double): Double {
    val curtainWidthMeters = curtainWidthCm / 100.0
    return if (curtainWidthMeters > 0.0) actualRingsCount / curtainWidthMeters else 0.0
}

/** يختار النتيجة الوسطى من قائمة نتائج الحلقات */
fun selectMiddleRingResult(results: List<RingResult>): RingResult? =
    if (results.isEmpty()) null else results[results.size / 2]

/** ينسق الرقم بدون أصفار عشرية زائدة لو كان رقماً صحيحاً */
fun formatCleanNumber(value: Double): String =
    if (value % 1.0 == 0.0) value.toLong().toString() else "%.2f".format(value)

/** ينسق تفاصيل سجل الحلقات: المقاس الذي كتبه المستخدم + أوسط عدد حلقات فقط */
fun formatRingsHistorySummary(curtainWidthCm: Double, middleRingsCount: Int): String =
    "مقاس الستارة: ${formatCleanNumber(curtainWidthCm)} سم  •  أوسط عدد حلقات: $middleRingsCount حلقة"

/**
 * تبحث في كل الأعداد الفردية وتقسم عرض الستارة عليها،
 * وترجع كل النتائج التي تقع في المدى من 15 إلى 18 سم (شامل الطرفين).
 * المدخل هنا هو "المقاس الفعلي" (عرض الستارة - 10).
 * تظل قيمة ringsCount هي رقم القسمة الفردي كما كانت دون تغيير.
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

enum class CalculatorType(val historyTitle: String) {
    WINDOW("سجل الشباك"),
    RINGS("سجل الحلقات")
}

data class CalculationHistoryEntry(
    val id: String,
    val dateKey: String,
    val timeLabel: String,
    val calculatorType: CalculatorType,
    val measurementSummary: String,
    val timestampMillis: Long
)

data class DailyHistoryGroup(
    val dateKey: String,
    val outsideLabel: String,
    val entries: List<CalculationHistoryEntry>
)

/**
 * يعرض اسم اليوم ورقم اليوم ورقم الشهر فقط على المستطيل من الخارج
 * مثال: "الثلاثاء 27 / 10"
 */
fun formatDayOutsideLabel(dateKey: String): String {
    val parser = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    val parsedDate = runCatching { parser.parse(dateKey) }.getOrNull() ?: return dateKey
    val dayName = SimpleDateFormat("EEEE", Locale("ar")).format(parsedDate)
    val dayAndMonth = SimpleDateFormat("d / M", Locale.US).format(parsedDate)
    return "$dayName $dayAndMonth"
}

fun createHistoryEntry(
    calculatorType: CalculatorType,
    measurementSummary: String,
    nowMillis: Long = System.currentTimeMillis()
): CalculationHistoryEntry {
    val date = Date(nowMillis)
    val dateFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    val timeFormatter = SimpleDateFormat("hh:mm a", Locale("ar"))
    return CalculationHistoryEntry(
        id = UUID.randomUUID().toString(),
        dateKey = dateFormatter.format(date),
        timeLabel = timeFormatter.format(date),
        calculatorType = calculatorType,
        measurementSummary = measurementSummary,
        timestampMillis = nowMillis
    )
}

fun addHistoryEntry(
    existing: List<CalculationHistoryEntry>,
    newEntry: CalculationHistoryEntry
): List<CalculationHistoryEntry> = listOf(newEntry) + existing

fun groupHistoryByDayForSection(
    entries: List<CalculationHistoryEntry>,
    calculatorType: CalculatorType
): List<DailyHistoryGroup> =
    entries
        .filter { it.calculatorType == calculatorType }
        .groupBy { it.dateKey }
        .map { (dateKey, dayEntries) ->
            DailyHistoryGroup(
                dateKey = dateKey,
                outsideLabel = formatDayOutsideLabel(dateKey),
                entries = dayEntries.sortedByDescending { it.timestampMillis }
            )
        }
        .sortedByDescending { it.dateKey }

fun deleteHistoryEntry(
    entries: List<CalculationHistoryEntry>,
    entryId: String
): List<CalculationHistoryEntry> = entries.filterNot { it.id == entryId }

fun deleteHistoryDayGroup(
    entries: List<CalculationHistoryEntry>,
    calculatorType: CalculatorType,
    dateKey: String
): List<CalculationHistoryEntry> =
    entries.filterNot { it.calculatorType == calculatorType && it.dateKey == dateKey }

private const val HISTORY_PREFS_NAME = "curtain_calculation_history_prefs"
private const val HISTORY_ENTRIES_KEY = "history_entries_v3"

internal fun serializeHistoryEntries(entries: List<CalculationHistoryEntry>): String =
    entries.joinToString("\n") { entry ->
        listOf(
            escapeHistoryField(entry.id),
            escapeHistoryField(entry.dateKey),
            escapeHistoryField(entry.timeLabel),
            escapeHistoryField(entry.calculatorType.name),
            escapeHistoryField(entry.measurementSummary),
            entry.timestampMillis.toString()
        ).joinToString("\t")
    }

internal fun deserializeHistoryEntries(serialized: String): List<CalculationHistoryEntry> {
    if (serialized.isBlank()) return emptyList()
    return serialized
        .lineSequence()
        .filter { it.isNotBlank() }
        .mapNotNull { line ->
            val parts = line.split("\t")
            if (parts.size < 6) return@mapNotNull null
            val type = runCatching { CalculatorType.valueOf(unescapeHistoryField(parts[3])) }
                .getOrNull() ?: return@mapNotNull null
            val timestamp = parts[5].toLongOrNull() ?: 0L
            CalculationHistoryEntry(
                id = unescapeHistoryField(parts[0]),
                dateKey = unescapeHistoryField(parts[1]),
                timeLabel = unescapeHistoryField(parts[2]),
                calculatorType = type,
                measurementSummary = unescapeHistoryField(parts[4]),
                timestampMillis = timestamp
            )
        }
        .toList()
}

private fun escapeHistoryField(value: String): String =
    value
        .replace("\\", "\\\\")
        .replace("\t", "\\t")
        .replace("\n", "\\n")
        .replace("\r", "\\r")

private fun unescapeHistoryField(value: String): String {
    val sb = StringBuilder(value.length)
    var index = 0
    while (index < value.length) {
        val ch = value[index]
        if (ch == '\\' && index + 1 < value.length) {
            when (val next = value[index + 1]) {
                '\\' -> sb.append('\\')
                't' -> sb.append('\t')
                'n' -> sb.append('\n')
                'r' -> sb.append('\r')
                else -> sb.append(next)
            }
            index += 2
        } else {
            sb.append(ch)
            index++
        }
    }
    return sb.toString()
}

private fun loadHistoryEntries(context: Context): List<CalculationHistoryEntry> {
    val prefs = context.getSharedPreferences(HISTORY_PREFS_NAME, Context.MODE_PRIVATE)
    val raw = prefs.getString(HISTORY_ENTRIES_KEY, "") ?: ""
    return deserializeHistoryEntries(raw)
}

private fun saveHistoryEntries(context: Context, entries: List<CalculationHistoryEntry>) {
    val prefs = context.getSharedPreferences(HISTORY_PREFS_NAME, Context.MODE_PRIVATE)
    prefs.edit().putString(HISTORY_ENTRIES_KEY, serializeHistoryEntries(entries)).apply()
}
