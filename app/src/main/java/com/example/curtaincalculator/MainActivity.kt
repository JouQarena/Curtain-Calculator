package com.example.curtaincalculator

import android.content.Context
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
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
   الشاشة الرئيسية مع نظام التابات وسجل الحسابات
   ============================================================ */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
    val context = LocalContext.current
    val tabs = listOf("الشباك", "الحلقات")
    var selectedTab by remember { mutableIntStateOf(0) }
    var showHistoryScreen by remember { mutableStateOf(false) }
    var historyEntries by remember { mutableStateOf(loadHistoryEntries(context)) }

    val recordCalculation: (CalculationHistoryEntry) -> Unit = { newEntry ->
        val updated = addHistoryEntry(historyEntries, newEntry)
        historyEntries = updated
        saveHistoryEntries(context, updated)
    }

    if (showHistoryScreen) {
        CalculationHistoryScreen(
            entries = historyEntries,
            onBack = { showHistoryScreen = false },
            onDeleteEntry = { entryId ->
                val updated = deleteHistoryEntry(historyEntries, entryId)
                historyEntries = updated
                saveHistoryEntries(context, updated)
            },
            onDeleteFolder = { dateKey ->
                val updated = deleteHistoryFolder(historyEntries, dateKey)
                historyEntries = updated
                saveHistoryEntries(context, updated)
            }
        )
        return
    }

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
                0 -> WindowCalculatorScreen(
                    onOpenHistory = { showHistoryScreen = true },
                    onSaveHistory = recordCalculation
                )
                1 -> RingsCalculatorScreen(
                    onOpenHistory = { showHistoryScreen = true },
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
    val (showTutorial, updateTutorialVisibility) =
        rememberTutorialVisibility(WINDOW_TUTORIAL_KEY)

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
                    inputsSummary = "عرض الشباك: ${"%.2f".format(w)} سم | عرض الماسورة: ${"%.2f".format(r)} سم | الخصم: ${"%.2f".format(deduction)} سم",
                    resultSummary = formattedResult
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
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = performWindowCalculation,
                            modifier = Modifier
                                .weight(1f)
                                .onGloballyPositioned {
                                    tutorialTargets["window_calculate"] = it.boundsInRoot()
                                }
                        ) {
                            Text("احسب")
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

            Spacer(modifier = Modifier.height(72.dp))
        }

        HistoryFloatingButton(
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
    val (showTutorial, updateTutorialVisibility) =
        rememberTutorialVisibility(RINGS_TUTORIAL_KEY)

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
                val summaryLines = computed.joinToString("\n") { res ->
                    val presses = res.pressesPerMeter(w)
                    "• الكبسات في كل متر: ${"%.2f".format(presses)} | عدد الحلقات: ${res.actualRingsCount} | المسافة: ${"%.2f".format(res.spacing)} سم"
                }
                onSaveHistory(
                    createHistoryEntry(
                        calculatorType = CalculatorType.RINGS,
                        inputsSummary = "عرض الستارة: ${"%.2f".format(w)} سم (المقاس الفعلي: ${"%.2f".format(actual)} سم)",
                        resultSummary = summaryLines
                    )
                )
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
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = performRingsCalculation,
                            modifier = Modifier
                                .weight(1f)
                                .onGloballyPositioned {
                                    tutorialTargets["rings_calculate"] = it.boundsInRoot()
                                }
                        ) {
                            Text("احسب")
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

                        val widthForPresses = calculatedCurtainWidth ?: 0.0
                        results.forEach { res ->
                            val pressesPerMeter = res.pressesPerMeter(widthForPresses)
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
                                        "عدد الكبسات في كل متر: ${"%.2f".format(pressesPerMeter)}",
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                    Text(
                                        "عدد الحلقات: ${res.actualRingsCount} حلقات",
                                        style = MaterialTheme.typography.titleMedium,
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

            Spacer(modifier = Modifier.height(72.dp))
        }

        HistoryFloatingButton(
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
   الزر العائم وصفحة سجل الحسابات (الفولدرات اليومية)
   ============================================================ */
@Composable
fun HistoryFloatingButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    FloatingActionButton(
        onClick = onClick,
        shape = CircleShape,
        containerColor = MaterialTheme.colorScheme.inverseSurface,
        contentColor = MaterialTheme.colorScheme.inverseOnSurface,
        modifier = modifier
            .size(56.dp)
            .semantics {
                contentDescription = "سجل الحسابات"
            }
    ) {
        val iconColor = MaterialTheme.colorScheme.inverseOnSurface
        Canvas(modifier = Modifier.size(26.dp)) {
            val radius = size.minDimension * 0.42f
            val center = Offset(size.width / 2f, size.height / 2f)
            drawCircle(
                color = iconColor,
                radius = radius,
                center = center,
                style = Stroke(width = 2.4.dp.toPx())
            )
            // عقارب الساعة
            drawLine(
                color = iconColor,
                start = center,
                end = Offset(center.x, center.y - radius * 0.55f),
                strokeWidth = 2.4.dp.toPx(),
                cap = StrokeCap.Round
            )
            drawLine(
                color = iconColor,
                start = center,
                end = Offset(center.x + radius * 0.45f, center.y + radius * 0.2f),
                strokeWidth = 2.4.dp.toPx(),
                cap = StrokeCap.Round
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalculationHistoryScreen(
    entries: List<CalculationHistoryEntry>,
    onBack: () -> Unit,
    onDeleteEntry: (String) -> Unit,
    onDeleteFolder: (String) -> Unit
) {
    BackHandler(onBack = onBack)

    val folders = remember(entries) { groupHistoryByDay(entries) }
    val expandedFolders = remember { mutableStateMapOf<String, Boolean>() }

    // افتراضياً نفتح أحدث فولدر يومي لسهولة المراجعة
    LaunchedEffect(folders.firstOrNull()?.dateKey) {
        val newestDate = folders.firstOrNull()?.dateKey
        if (newestDate != null && !expandedFolders.containsKey(newestDate)) {
            expandedFolders[newestDate] = true
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("سجل الحسابات", fontSize = 22.sp) },
                navigationIcon = {
                    TextButton(onClick = onBack) {
                        Text(
                            "رجوع",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { padding ->
        if (folders.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "لا توجد حسابات محفوظة في السجل حتى الآن.",
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
                folders.forEach { folder ->
                    val isExpanded = expandedFolders[folder.dateKey] ?: false

                    // الفولدر اليومي: مستطيل بعرض الشاشة بالكامل
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                expandedFolders[folder.dateKey] = !isExpanded
                            },
                        shape = RoundedCornerShape(10.dp),
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
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                FolderIcon(
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(26.dp)
                                )
                                Column {
                                    Text(
                                        text = "فولدر يوم ${folder.dateKey}",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "عدد الحسابات: ${folder.entries.size}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                OutlinedButton(
                                    onClick = { onDeleteFolder(folder.dateKey) },
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        contentColor = MaterialTheme.colorScheme.error
                                    )
                                ) {
                                    Text("حذف الفولدر", fontSize = 13.sp)
                                }
                                Text(
                                    text = if (isExpanded) "▲" else "▼",
                                    fontSize = 16.sp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }

                    // الحسابات داخل الفولدر المفتوح
                    if (isExpanded) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 8.dp, end = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            folder.entries.forEach { entry ->
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                                    )
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(12.dp),
                                        verticalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                text = "${entry.calculatorType.displayTitle} • ${entry.timeLabel}",
                                                style = MaterialTheme.typography.labelLarge,
                                                color = MaterialTheme.colorScheme.primary,
                                                fontWeight = FontWeight.Bold
                                            )
                                            TextButton(
                                                onClick = { onDeleteEntry(entry.id) },
                                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                                colors = ButtonDefaults.textButtonColors(
                                                    contentColor = MaterialTheme.colorScheme.error
                                                )
                                            ) {
                                                Text("حذف الحسبة", fontSize = 13.sp)
                                            }
                                        }

                                        Text(
                                            text = entry.inputsSummary,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.SemiBold
                                        )

                                        Text(
                                            text = entry.resultSummary,
                                            style = MaterialTheme.typography.bodyMedium
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
private fun FolderIcon(
    color: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val path = Path().apply {
            moveTo(w * 0.08f, h * 0.22f)
            lineTo(w * 0.42f, h * 0.22f)
            lineTo(w * 0.52f, h * 0.34f)
            lineTo(w * 0.92f, h * 0.34f)
            lineTo(w * 0.92f, h * 0.82f)
            lineTo(w * 0.08f, h * 0.82f)
            close()
        }
        drawPath(path = path, color = color)
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
    val inputContainerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.52f)
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
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = inputContainerColor,
            unfocusedContainerColor = inputContainerColor,
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
            focusedLabelColor = MaterialTheme.colorScheme.primary,
            unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
        ),
        modifier = modifier
    )
}

/* ============================================================
   دوال الحساب وإدارة سجل الحسابات
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

enum class CalculatorType(val displayTitle: String) {
    WINDOW("حاسبة قياس الشباك"),
    RINGS("حاسبة عدد الحلقات")
}

data class CalculationHistoryEntry(
    val id: String,
    val dateKey: String,
    val timeLabel: String,
    val calculatorType: CalculatorType,
    val inputsSummary: String,
    val resultSummary: String,
    val timestampMillis: Long
)

data class DailyHistoryFolder(
    val dateKey: String,
    val entries: List<CalculationHistoryEntry>
)

fun createHistoryEntry(
    calculatorType: CalculatorType,
    inputsSummary: String,
    resultSummary: String,
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
        inputsSummary = inputsSummary,
        resultSummary = resultSummary,
        timestampMillis = nowMillis
    )
}

fun addHistoryEntry(
    existing: List<CalculationHistoryEntry>,
    newEntry: CalculationHistoryEntry
): List<CalculationHistoryEntry> = listOf(newEntry) + existing

fun groupHistoryByDay(entries: List<CalculationHistoryEntry>): List<DailyHistoryFolder> =
    entries
        .groupBy { it.dateKey }
        .map { (dateKey, dayEntries) ->
            DailyHistoryFolder(
                dateKey = dateKey,
                entries = dayEntries.sortedByDescending { it.timestampMillis }
            )
        }
        .sortedByDescending { it.dateKey }

fun deleteHistoryEntry(
    entries: List<CalculationHistoryEntry>,
    entryId: String
): List<CalculationHistoryEntry> = entries.filterNot { it.id == entryId }

fun deleteHistoryFolder(
    entries: List<CalculationHistoryEntry>,
    dateKey: String
): List<CalculationHistoryEntry> = entries.filterNot { it.dateKey == dateKey }

private const val HISTORY_PREFS_NAME = "curtain_calculation_history_prefs"
private const val HISTORY_ENTRIES_KEY = "history_entries_v1"

internal fun serializeHistoryEntries(entries: List<CalculationHistoryEntry>): String =
    entries.joinToString("\n") { entry ->
        listOf(
            escapeHistoryField(entry.id),
            escapeHistoryField(entry.dateKey),
            escapeHistoryField(entry.timeLabel),
            escapeHistoryField(entry.calculatorType.name),
            escapeHistoryField(entry.inputsSummary),
            escapeHistoryField(entry.resultSummary),
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
            if (parts.size < 7) return@mapNotNull null
            val type = runCatching { CalculatorType.valueOf(unescapeHistoryField(parts[3])) }
                .getOrNull() ?: return@mapNotNull null
            val timestamp = parts[6].toLongOrNull() ?: 0L
            CalculationHistoryEntry(
                id = unescapeHistoryField(parts[0]),
                dateKey = unescapeHistoryField(parts[1]),
                timeLabel = unescapeHistoryField(parts[2]),
                calculatorType = type,
                inputsSummary = unescapeHistoryField(parts[4]),
                resultSummary = unescapeHistoryField(parts[5]),
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
