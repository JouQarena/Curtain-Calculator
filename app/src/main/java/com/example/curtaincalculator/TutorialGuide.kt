package com.example.curtaincalculator

import android.content.Context
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

private const val TUTORIAL_PREFERENCES = "calculator_tutorial_preferences"
const val WINDOW_TUTORIAL_KEY = "window_calculator_tutorial_v2_completed"
const val RINGS_TUTORIAL_KEY = "rings_calculator_tutorial_v2_completed"

enum class GuideIllustration {
    WINDOW_MEASUREMENT,
    ROD_MEASUREMENT,
    DEDUCTION,
    WINDOW_RESULT,
    CURTAIN_MEASUREMENT,
    ODD_DIVISIONS,
    RING_RESULT,
    HISTORY_FOLDERS
}

data class TutorialStep(
    val targetId: String,
    val title: String,
    val description: String,
    val illustration: GuideIllustration
)

val windowTutorialSteps = listOf(
    TutorialStep(
        targetId = "window_width",
        title = "أدخل عرض الشباك",
        description = "قِس عرض الشباك بالسنتيمتر، ثم اكتب المقاس في هذا الحقل.",
        illustration = GuideIllustration.WINDOW_MEASUREMENT
    ),
    TutorialStep(
        targetId = "rod_width",
        title = "أدخل عرض الماسورة",
        description = "اكتب العرض الكامل للماسورة بالسنتيمتر كما قمت بقياسه.",
        illustration = GuideIllustration.ROD_MEASUREMENT
    ),
    TutorialStep(
        targetId = "rod_deduction",
        title = "حدد الخصم من الماسورة",
        description = "القيمة الافتراضية 10 سم. يمكنك استبدالها بصفر أو بأي قيمة موجبة، بما في ذلك الكسور.",
        illustration = GuideIllustration.DEDUCTION
    ),
    TutorialStep(
        targetId = "window_calculate",
        title = "احسب المسافة",
        description = "اضغط «احسب» أو اضغط علامة صح (✓) في الكيبورد مباشرةً لإظهار النتيجة. زر «مسح» يعيد الحقول إلى حالتها الافتراضية.",
        illustration = GuideIllustration.WINDOW_RESULT
    ),
    TutorialStep(
        targetId = "history_button",
        title = "سجل الحسابات اليومي",
        description = "يفتح هذا الزر صفحة سجل الحسابات مقسمة إلى فولدرات مستطيلة حسب اليوم، ويمكنك حذف حسبة مفردة أو فولدر يوم كامل.",
        illustration = GuideIllustration.HISTORY_FOLDERS
    )
)

val ringsTutorialSteps = listOf(
    TutorialStep(
        targetId = "curtain_width",
        title = "أدخل عرض الستارة",
        description = "اكتب عرض الستارة بالسنتيمتر. سيُخصم 10 سم تلقائيًا للحصول على المقاس الفعلي.",
        illustration = GuideIllustration.CURTAIN_MEASUREMENT
    ),
    TutorialStep(
        targetId = "rings_calculate",
        title = "احسب من الزر أو الكيبورد",
        description = "اضغط «احسب» أو اضغط علامة صح (✓) في الكيبورد مباشرةً دون الحاجة للضغط على الزر. يبحث التطبيق عن التقسيمات التي تجعل المسافة بين الحلقات من 15 إلى 18 سم.",
        illustration = GuideIllustration.ODD_DIVISIONS
    ),
    TutorialStep(
        targetId = "rings_calculate",
        title = "اقرأ النتيجة والكبسات في المتر",
        description = "تعرض كل نتيجة عدد الكبسات في كل متر (عدد الحلقات ÷ عرض الستارة بالمتر)، وعدد الحلقات الفعلي، ثم المسافة بين الحلقات.",
        illustration = GuideIllustration.RING_RESULT
    ),
    TutorialStep(
        targetId = "history_button",
        title = "سجل الحسابات اليومي",
        description = "يفتح هذا الزر صفحة سجل الحسابات مقسمة إلى فولدرات مستطيلة بعرض الشاشة حسب اليوم، مع إمكانية حذف حسبة أو فولدر كامل.",
        illustration = GuideIllustration.HISTORY_FOLDERS
    )
)

/**
 * Returns true until the user explicitly confirms the guide with the checkmark.
 * A temporary close only changes in-memory state, so the guide appears next time
 * the calculator section is opened.
 */
@Composable
fun rememberTutorialVisibility(tutorialKey: String): Pair<Boolean, (Boolean) -> Unit> {
    val context = LocalContext.current
    val preferences = remember(context) {
        context.getSharedPreferences(TUTORIAL_PREFERENCES, Context.MODE_PRIVATE)
    }
    var visible by remember(tutorialKey) {
        mutableStateOf(!preferences.getBoolean(tutorialKey, false))
    }

    val updateVisibility: (Boolean) -> Unit = { completed ->
        if (completed) {
            preferences.edit().putBoolean(tutorialKey, true).apply()
        }
        visible = false
    }
    return visible to updateVisibility
}

@Composable
fun TutorialGuideOverlay(
    steps: List<TutorialStep>,
    targetBoundsInRoot: Map<String, Rect>,
    onTemporarilyDismiss: () -> Unit,
    onComplete: () -> Unit,
    modifier: Modifier = Modifier
) {
    var currentStep by remember(steps) { mutableIntStateOf(0) }
    var overlayOrigin by remember { mutableStateOf(Offset.Zero) }
    var overlaySize by remember { mutableStateOf(IntSize.Zero) }
    var guideCardBoundsInRoot by remember { mutableStateOf<Rect?>(null) }

    BackHandler(onBack = onTemporarilyDismiss)

    val step = steps[currentStep]
    val targetInOverlay = targetBoundsInRoot[step.targetId]?.relativeTo(overlayOrigin)
    val guideCardInOverlay = guideCardBoundsInRoot?.relativeTo(overlayOrigin)
    val placeCardAtBottom = targetInOverlay?.center?.y
        ?.let { it < overlaySize.height * 0.52f } ?: true

    val overlayInteraction = remember { MutableInteractionSource() }
    val dimColor = Color.Black.copy(alpha = 0.68f)
    val highlightColor = MaterialTheme.colorScheme.primary

    Box(
        modifier = modifier
            .fillMaxSize()
            .onSizeChanged { overlaySize = it }
            .onGloballyPositioned { overlayOrigin = it.boundsInRoot().topLeft }
            // Consume taps outside the guide so fields underneath cannot change.
            .clickable(
                interactionSource = overlayInteraction,
                indication = null,
                onClick = {}
            )
            .semantics {
                contentDescription = "دليل استخدام الحاسبة، الخطوة ${currentStep + 1} من ${steps.size}"
            }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val spotlightPadding = 8.dp.toPx()
            val spotlight = targetInOverlay?.expandedBy(spotlightPadding)?.clampedTo(size)

            if (spotlight == null) {
                drawRect(dimColor)
            } else {
                // Four dimmed rectangles leave the real control visible in the middle.
                drawRect(
                    color = dimColor,
                    topLeft = Offset.Zero,
                    size = Size(size.width, spotlight.top)
                )
                drawRect(
                    color = dimColor,
                    topLeft = Offset(0f, spotlight.top),
                    size = Size(spotlight.left, spotlight.height)
                )
                drawRect(
                    color = dimColor,
                    topLeft = Offset(spotlight.right, spotlight.top),
                    size = Size(size.width - spotlight.right, spotlight.height)
                )
                drawRect(
                    color = dimColor,
                    topLeft = Offset(0f, spotlight.bottom),
                    size = Size(size.width, size.height - spotlight.bottom)
                )
                drawRoundRect(
                    color = Color.White.copy(alpha = 0.95f),
                    topLeft = spotlight.topLeft,
                    size = spotlight.size,
                    cornerRadius = CornerRadius(14.dp.toPx()),
                    style = Stroke(width = 5.dp.toPx())
                )
                drawRoundRect(
                    color = highlightColor,
                    topLeft = spotlight.topLeft,
                    size = spotlight.size,
                    cornerRadius = CornerRadius(14.dp.toPx()),
                    style = Stroke(width = 2.5.dp.toPx())
                )

                guideCardInOverlay?.let { card ->
                    if (!card.overlaps(spotlight)) {
                        val cardIsBelowTarget = card.center.y > spotlight.center.y
                        val start = if (cardIsBelowTarget) {
                            Offset(card.center.x, card.top)
                        } else {
                            Offset(card.center.x, card.bottom)
                        }
                        val end = if (cardIsBelowTarget) {
                            Offset(spotlight.center.x, spotlight.bottom)
                        } else {
                            Offset(spotlight.center.x, spotlight.top)
                        }
                        drawGuideArrow(
                            start = start,
                            end = end,
                            color = highlightColor,
                            outlineColor = Color.White
                        )
                    }
                }
            }
        }

        Card(
            modifier = Modifier
                .align(if (placeCardAtBottom) Alignment.BottomCenter else Alignment.TopCenter)
                .padding(horizontal = 16.dp, vertical = 18.dp)
                .fillMaxWidth()
                .widthIn(max = 540.dp)
                .onGloballyPositioned { guideCardBoundsInRoot = it.boundsInRoot() }
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = {}
                ),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "الخطوة ${currentStep + 1} من ${steps.size}",
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = step.title,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    IconButton(
                        onClick = onTemporarilyDismiss,
                        modifier = Modifier.semantics {
                            contentDescription = "إغلاق الدليل مؤقتًا"
                        }
                    ) {
                        Text("×", fontSize = 28.sp)
                    }
                }

                Text(
                    text = step.description,
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Start,
                    modifier = Modifier.fillMaxWidth()
                )

                GuideIllustration(step.illustration)

                Divider(color = MaterialTheme.colorScheme.outlineVariant)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = onComplete,
                            modifier = Modifier
                                .size(48.dp)
                                .semantics {
                                    contentDescription = "فهمت، لا تعرض هذا الدليل مرة أخرى"
                                },
                            shape = CircleShape,
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text("✓", fontSize = 24.sp, fontWeight = FontWeight.Bold)
                        }
                        Text(
                            text = "فهمت",
                            style = MaterialTheme.typography.labelLarge
                        )
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (currentStep > 0) {
                            TextButton(onClick = { currentStep-- }) {
                                Text("السابق")
                            }
                        }
                        if (currentStep < steps.lastIndex) {
                            Button(onClick = { currentStep++ }) {
                                Text("التالي")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun GuideIllustration(illustration: GuideIllustration) {
    val primary = MaterialTheme.colorScheme.primary
    val secondary = MaterialTheme.colorScheme.tertiary
    val lineColor = MaterialTheme.colorScheme.onSurfaceVariant

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.38f)
    ) {
        when (illustration) {
            GuideIllustration.WINDOW_MEASUREMENT,
            GuideIllustration.CURTAIN_MEASUREMENT -> {
                Column(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Canvas(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                    ) {
                        val frameLeft = size.width * 0.18f
                        val frameRight = size.width * 0.82f
                        val frameTop = size.height * 0.08f
                        val frameBottom = size.height * 0.58f
                        drawRoundRect(
                            color = lineColor,
                            topLeft = Offset(frameLeft, frameTop),
                            size = Size(frameRight - frameLeft, frameBottom - frameTop),
                            cornerRadius = CornerRadius(5.dp.toPx()),
                            style = Stroke(width = 2.dp.toPx())
                        )
                        val y = size.height * 0.82f
                        drawDoubleArrow(
                            start = Offset(frameLeft, y),
                            end = Offset(frameRight, y),
                            color = primary,
                            strokeWidth = 2.5.dp.toPx()
                        )
                    }
                    Text(
                        if (illustration == GuideIllustration.WINDOW_MEASUREMENT) "قياس عرض الشباك"
                        else "قياس عرض الستارة",
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }

            GuideIllustration.ROD_MEASUREMENT -> {
                Column(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Canvas(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(45.dp)
                    ) {
                        val start = Offset(size.width * 0.14f, size.height * 0.38f)
                        val end = Offset(size.width * 0.86f, size.height * 0.38f)
                        drawLine(
                            color = lineColor,
                            start = start,
                            end = end,
                            strokeWidth = 8.dp.toPx(),
                            cap = StrokeCap.Round
                        )
                        val measurementY = size.height * 0.82f
                        drawDoubleArrow(
                            start = Offset(start.x, measurementY),
                            end = Offset(end.x, measurementY),
                            color = primary,
                            strokeWidth = 2.5.dp.toPx()
                        )
                    }
                    Text("قياس عرض الماسورة بالكامل", style = MaterialTheme.typography.labelMedium)
                }
            }

            GuideIllustration.DEDUCTION -> {
                Row(
                    modifier = Modifier.padding(12.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("10 سم", color = primary, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                    Text("  ←  ", fontSize = 20.sp)
                    Text("قيمة افتراضية قابلة للتعديل", style = MaterialTheme.typography.bodyMedium)
                }
            }

            GuideIllustration.WINDOW_RESULT -> {
                Column(
                    modifier = Modifier
                        .padding(12.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "(عرض الشباك − (عرض الماسورة − الخصم)) ÷ 2",
                        textAlign = TextAlign.Center,
                        color = primary,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "احسب من الزر أو علامة (✓) في الكيبورد",
                        style = MaterialTheme.typography.labelMedium,
                        color = lineColor
                    )
                }
            }

            GuideIllustration.ODD_DIVISIONS -> {
                Column(
                    modifier = Modifier
                        .padding(10.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        listOf("احسب", "✓ الكيبورد").forEachIndexed { index, label ->
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (index == 1) primary else MaterialTheme.colorScheme.surface
                            ) {
                                Text(
                                    text = label,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                    color = if (index == 1) MaterialTheme.colorScheme.onPrimary else lineColor,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            if (index == 0) Spacer(modifier = Modifier.width(8.dp))
                        }
                    }
                }
            }

            GuideIllustration.RING_RESULT -> {
                Column(
                    modifier = Modifier.padding(10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Canvas(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(34.dp)
                    ) {
                        val count = 6
                        val startX = size.width * 0.18f
                        val spacing = size.width * 0.64f / (count - 1)
                        drawLine(
                            color = lineColor.copy(alpha = 0.5f),
                            start = Offset(startX, size.height / 2),
                            end = Offset(startX + spacing * (count - 1), size.height / 2),
                            strokeWidth = 2.dp.toPx()
                        )
                        repeat(count) { index ->
                            drawCircle(
                                color = if (index % 2 == 0) primary else secondary,
                                radius = 6.dp.toPx(),
                                center = Offset(startX + spacing * index, size.height / 2)
                            )
                        }
                    }
                    Text(
                        text = "عدد الكبسات في كل متر = عدد الحلقات ÷ العرض بالمتر",
                        color = primary,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center
                    )
                }
            }

            GuideIllustration.HISTORY_FOLDERS -> {
                Column(
                    modifier = Modifier
                        .padding(10.dp)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surface
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "📁 فولدر اليوم (بعرض الشاشة)",
                                color = primary,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                text = "حذف",
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Text(
                        text = "حذف حسبة واحدة أو فولدر يوم كامل",
                        style = MaterialTheme.typography.labelMedium,
                        color = lineColor
                    )
                }
            }
        }
    }
}

private fun Rect.relativeTo(origin: Offset): Rect = Rect(
    left = left - origin.x,
    top = top - origin.y,
    right = right - origin.x,
    bottom = bottom - origin.y
)

private fun Rect.expandedBy(padding: Float): Rect = Rect(
    left = left - padding,
    top = top - padding,
    right = right + padding,
    bottom = bottom + padding
)

private fun Rect.clampedTo(size: Size): Rect = Rect(
    left = left.coerceIn(0f, size.width),
    top = top.coerceIn(0f, size.height),
    right = right.coerceIn(0f, size.width),
    bottom = bottom.coerceIn(0f, size.height)
)

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawGuideArrow(
    start: Offset,
    end: Offset,
    color: Color,
    outlineColor: Color
) {
    val outlineWidth = 7.dp.toPx()
    val lineWidth = 4.dp.toPx()
    drawLine(outlineColor, start, end, outlineWidth, StrokeCap.Round)
    drawLine(color, start, end, lineWidth, StrokeCap.Round)

    val angle = atan2(end.y - start.y, end.x - start.x)
    val arrowLength = 15.dp.toPx()
    val leftWing = Offset(
        x = end.x - arrowLength * cos(angle - 0.55f),
        y = end.y - arrowLength * sin(angle - 0.55f)
    )
    val rightWing = Offset(
        x = end.x - arrowLength * cos(angle + 0.55f),
        y = end.y - arrowLength * sin(angle + 0.55f)
    )
    val head = Path().apply {
        moveTo(end.x, end.y)
        lineTo(leftWing.x, leftWing.y)
        lineTo(rightWing.x, rightWing.y)
        close()
    }
    drawPath(head, outlineColor)
    val innerHead = Path().apply {
        val innerLength = arrowLength - 3.dp.toPx()
        val innerLeft = Offset(
            x = end.x - innerLength * cos(angle - 0.55f),
            y = end.y - innerLength * sin(angle - 0.55f)
        )
        val innerRight = Offset(
            x = end.x - innerLength * cos(angle + 0.55f),
            y = end.y - innerLength * sin(angle + 0.55f)
        )
        moveTo(end.x, end.y)
        lineTo(innerLeft.x, innerLeft.y)
        lineTo(innerRight.x, innerRight.y)
        close()
    }
    drawPath(innerHead, color)
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawDoubleArrow(
    start: Offset,
    end: Offset,
    color: Color,
    strokeWidth: Float
) {
    drawLine(color, start, end, strokeWidth, StrokeCap.Round)
    val wing = 7.dp.toPx()
    drawLine(color, start, Offset(start.x + wing, start.y - wing / 2), strokeWidth, StrokeCap.Round)
    drawLine(color, start, Offset(start.x + wing, start.y + wing / 2), strokeWidth, StrokeCap.Round)
    drawLine(color, end, Offset(end.x - wing, end.y - wing / 2), strokeWidth, StrokeCap.Round)
    drawLine(color, end, Offset(end.x - wing, end.y + wing / 2), strokeWidth, StrokeCap.Round)
}
