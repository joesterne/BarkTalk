package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.RecordedBark
import com.example.data.TranslationLog
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.roundToInt

/**
 * Represents a single day's aggregated bark translation telemetry in the 7-day window.
 */
data class DailyBarkFrequencyBucket(
    val dayIndex: Int,
    val dayStartMillis: Long,
    val dayEndMillis: Long,
    val dayShortLabel: String,
    val dateLabel: String,
    val totalCount: Int,
    val highUrgencyCount: Int,
    val standardCount: Int,
    val averageDb: Int,
    val dominantVocalization: String,
    val isToday: Boolean
)

/**
 * Internal lightweight record for unified aggregation across [TranslationLog] and [RecordedBark].
 */
private data class BarkEventSnapshot(
    val timestamp: Long,
    val urgencyLevel: String,
    val intensityDb: Int,
    val vocalizationType: String
)

/**
 * Pure domain calculator that aggregates bark translations over the last 7 calendar days.
 */
object WeeklyBarkFrequencyCalculator {

    private const val DEDUP_WINDOW_MS = 2_000L

    fun computeLast7DaysFromLogs(
        logs: List<TranslationLog>,
        referenceTimeMillis: Long = System.currentTimeMillis()
    ): List<DailyBarkFrequencyBucket> {
        val snapshots = logs.map { log ->
            BarkEventSnapshot(
                timestamp = log.timestamp,
                urgencyLevel = log.urgencyLevel,
                intensityDb = log.intensityDb,
                vocalizationType = log.vocalizationType
            )
        }
        return bucketSnapshots(snapshots, referenceTimeMillis)
    }

    fun computeLast7DaysFromRecordings(
        recordings: List<RecordedBark>,
        referenceTimeMillis: Long = System.currentTimeMillis()
    ): List<DailyBarkFrequencyBucket> {
        val snapshots = recordings.map { bark ->
            BarkEventSnapshot(
                timestamp = bark.timestamp,
                urgencyLevel = bark.urgencyLevel,
                intensityDb = bark.peakDb,
                vocalizationType = bark.vocalizationType
            )
        }
        return bucketSnapshots(snapshots, referenceTimeMillis)
    }

    /**
     * Combines both [TranslationLog] and [RecordedBark] lists, deduplicating events recorded
     * within [DEDUP_WINDOW_MS] of each other so simultaneous saves are counted once.
     */
    fun computeLast7DaysCombined(
        logs: List<TranslationLog>,
        recordings: List<RecordedBark>,
        referenceTimeMillis: Long = System.currentTimeMillis()
    ): List<DailyBarkFrequencyBucket> {
        val combined = mutableListOf<BarkEventSnapshot>()
        logs.forEach { log ->
            combined.add(
                BarkEventSnapshot(
                    timestamp = log.timestamp,
                    urgencyLevel = log.urgencyLevel,
                    intensityDb = log.intensityDb,
                    vocalizationType = log.vocalizationType
                )
            )
        }
        recordings.forEach { bark ->
            val alreadyPresent = combined.any { existing ->
                kotlin.math.abs(existing.timestamp - bark.timestamp) <= DEDUP_WINDOW_MS &&
                    existing.vocalizationType == bark.vocalizationType
            }
            if (!alreadyPresent) {
                combined.add(
                    BarkEventSnapshot(
                        timestamp = bark.timestamp,
                        urgencyLevel = bark.urgencyLevel,
                        intensityDb = bark.peakDb,
                        vocalizationType = bark.vocalizationType
                    )
                )
            }
        }
        return bucketSnapshots(combined, referenceTimeMillis)
    }

    private fun bucketSnapshots(
        snapshots: List<BarkEventSnapshot>,
        referenceTimeMillis: Long
    ): List<DailyBarkFrequencyBucket> {
        val dayNameFormat = SimpleDateFormat("EEE", Locale.getDefault())
        val dateFormat = SimpleDateFormat("MMM d", Locale.getDefault())

        // Normalize reference time to start of today (00:00:00.000) in local calendar
        val todayStartCal = Calendar.getInstance().apply {
            timeInMillis = referenceTimeMillis
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        // Build 7 buckets from 6 days ago (index 0) to Today (index 6)
        return (6 downTo 0).mapIndexed { index, daysAgo ->
            val dayStartCal = (todayStartCal.clone() as Calendar).apply {
                add(Calendar.DAY_OF_YEAR, -daysAgo)
            }
            val dayStart = dayStartCal.timeInMillis
            val dayEndCal = (dayStartCal.clone() as Calendar).apply {
                add(Calendar.DAY_OF_YEAR, 1)
            }
            val dayEnd = dayEndCal.timeInMillis

            val dayEvents = snapshots.filter { it.timestamp in dayStart until dayEnd }
            val total = dayEvents.size
            val highUrgency = dayEvents.count {
                it.urgencyLevel.equals("HIGH", ignoreCase = true) ||
                    it.urgencyLevel.equals("CRITICAL", ignoreCase = true)
            }
            val standard = (total - highUrgency).coerceAtLeast(0)
            val avgDb = if (dayEvents.isNotEmpty()) {
                dayEvents.map { it.intensityDb }.average().roundToInt()
            } else {
                0
            }
            val topVocalization = dayEvents
                .groupingBy { it.vocalizationType }
                .eachCount()
                .maxByOrNull { it.value }
                ?.key ?: "No barks recorded"

            val dateObj = Date(dayStart)
            DailyBarkFrequencyBucket(
                dayIndex = index,
                dayStartMillis = dayStart,
                dayEndMillis = dayEnd,
                dayShortLabel = dayNameFormat.format(dateObj),
                dateLabel = dateFormat.format(dateObj),
                totalCount = total,
                highUrgencyCount = highUrgency,
                standardCount = standard,
                averageDb = avgDb,
                dominantVocalization = topVocalization,
                isToday = daysAgo == 0
            )
        }
    }
}

/**
 * A Recharts / D3-inspired interactive 7-day bar chart component built with Jetpack Compose Canvas.
 * Displays Cartesian dashed gridlines, Y-axis tick labels, stacked standard vs. high-urgency
 * frequency bars, a 7-day daily average reference line, and an interactive tooltip callout.
 */
@Composable
fun WeeklyBarkFrequencyChart(
    buckets: List<DailyBarkFrequencyBucket>,
    modifier: Modifier = Modifier,
    title: String = "7-Day Bark Translation Frequency"
) {
    val safeBuckets = remember(buckets) {
        if (buckets.size == 7) buckets
        else WeeklyBarkFrequencyCalculator.computeLast7DaysFromLogs(emptyList())
    }

    // Default selection to Today (index 6) or the most recent day with activity
    var selectedDayIndex by remember(safeBuckets) {
        val lastActive = safeBuckets.indexOfLast { it.totalCount > 0 }
        mutableIntStateOf(if (lastActive >= 0) lastActive else safeBuckets.lastIndex)
    }

    val selectedBucket = safeBuckets.getOrElse(selectedDayIndex) { safeBuckets.last() }

    val totalWeekBarks = remember(safeBuckets) { safeBuckets.sumOf { it.totalCount } }
    val totalHighUrgency = remember(safeBuckets) { safeBuckets.sumOf { it.highUrgencyCount } }
    val peakDayBarks = remember(safeBuckets) { safeBuckets.maxOfOrNull { it.totalCount } ?: 0 }
    val avgDailyBarks = remember(safeBuckets) { totalWeekBarks / 7f }

    // Nice integer ceiling for the Y-axis scale (minimum 4 so gridlines are always well-proportioned)
    val yAxisMax = remember(peakDayBarks) {
        val rawMax = max(4, peakDayBarks + 1)
        if (rawMax % 2 == 0) rawMax else rawMax + 1
    }

    // Animated bar height fractions for smooth D3/Recharts-like transitions
    val animatedFractions = safeBuckets.map { bucket ->
        val targetFraction = (bucket.totalCount.toFloat() / yAxisMax.toFloat()).coerceIn(0f, 1f)
        animateFloatAsState(
            targetValue = targetFraction,
            animationSpec = tween(durationMillis = 550, easing = FastOutSlowInEasing),
            label = "bar_fraction_${bucket.dayIndex}"
        ).value
    }

    val primaryBarColor = MaterialTheme.colorScheme.primary
    val primaryBarContainer = MaterialTheme.colorScheme.primaryContainer
    val urgentBarColor = MaterialTheme.colorScheme.tertiary
    val avgLineColor = MaterialTheme.colorScheme.secondary
    val gridLineColor = MaterialTheme.colorScheme.outlineVariant
    val labelTextColor = MaterialTheme.colorScheme.onSurfaceVariant
    val activeColumnHighlight = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
    val selectedBarBorderColor = MaterialTheme.colorScheme.onSurface

    val textMeasurer = rememberTextMeasurer()

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("weekly_bark_frequency_chart"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // 1. Header Row with Title & Summary KPI Pills
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.BarChart,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }

                    Column {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Last 7 days • Tap any bar to inspect daily telemetry",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.65f),
                    modifier = Modifier.testTag("weekly_chart_total_badge")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.TrendingUp,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "$totalWeekBarks Barks",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            // 2. Recharts-style Legend & Quick Summary Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ChartLegendDot(
                        color = primaryBarColor,
                        label = "Standard (${totalWeekBarks - totalHighUrgency})"
                    )
                    ChartLegendDot(
                        color = urgentBarColor,
                        label = "High Urgency ($totalHighUrgency)"
                    )
                }

                Text(
                    text = "Avg ${String.format(Locale.US, "%.1f", avgDailyBarks)}/day • Peak $peakDayBarks",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.testTag("weekly_chart_avg_badge")
                )
            }

            // 3. D3 / Recharts-Inspired Bar Chart Canvas
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(176.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.28f))
                    .padding(horizontal = 8.dp, vertical = 12.dp)
            ) {
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(152.dp)
                        .semantics {
                            contentDescription =
                                "Bar chart showing bark translation frequency over the last 7 days. Total $totalWeekBarks barks."
                        }
                        .pointerInput(safeBuckets) {
                            detectTapGestures { tapOffset ->
                                val leftAxisPadding = 28.dp.toPx()
                                val plotWidth = (size.width - leftAxisPadding).coerceAtLeast(1f)
                                if (tapOffset.x >= leftAxisPadding) {
                                    val slotWidth = plotWidth / 7f
                                    val tappedIdx =
                                        ((tapOffset.x - leftAxisPadding) / slotWidth).toInt()
                                            .coerceIn(0, 6)
                                    selectedDayIndex = tappedIdx
                                }
                            }
                        }
                        .testTag("weekly_bark_canvas")
                ) {
                    val leftAxisWidth = 28.dp.toPx()
                    val topPadding = 16.dp.toPx()
                    val bottomPadding = 8.dp.toPx()
                    val plotWidth = (size.width - leftAxisWidth).coerceAtLeast(1f)
                    val plotHeight = (size.height - topPadding - bottomPadding).coerceAtLeast(1f)
                    val baselineY = topPadding + plotHeight

                    val dashEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 6f), 0f)
                    val avgDashEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 8f), 0f)

                    // Draw 3 horizontal Cartesian gridlines (0%, 50%, 100% of yAxisMax)
                    val gridSteps = listOf(0, yAxisMax / 2, yAxisMax)
                    gridSteps.forEach { stepValue ->
                        val fraction = stepValue.toFloat() / yAxisMax.toFloat()
                        val y = baselineY - (fraction * plotHeight)

                        drawLine(
                            color = gridLineColor,
                            start = Offset(leftAxisWidth, y),
                            end = Offset(size.width, y),
                            strokeWidth = if (stepValue == 0) 1.5.dp.toPx() else 1.dp.toPx(),
                            pathEffect = if (stepValue == 0) null else dashEffect
                        )

                        // Y-axis tick label
                        val tickLayout = textMeasurer.measure(
                            text = stepValue.toString(),
                            style = TextStyle(
                                color = labelTextColor,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Medium
                            )
                        )
                        drawText(
                            textLayoutResult = tickLayout,
                            topLeft = Offset(
                                x = (leftAxisWidth - tickLayout.size.width - 6.dp.toPx()).coerceAtLeast(0f),
                                y = (y - tickLayout.size.height / 2f).coerceIn(0f, size.height - tickLayout.size.height)
                            )
                        )
                    }

                    val slotWidth = plotWidth / 7f
                    val barWidth = (slotWidth * 0.56f).coerceIn(12.dp.toPx(), 34.dp.toPx())
                    val cornerRadius = CornerRadius(8.dp.toPx(), 8.dp.toPx())

                    // Draw each day's column highlight & stacked frequency bar
                    safeBuckets.forEachIndexed { idx, bucket ->
                        val slotLeft = leftAxisWidth + idx * slotWidth
                        val barLeft = slotLeft + (slotWidth - barWidth) / 2f

                        // Recharts-style active hover/selection band
                        if (idx == selectedDayIndex) {
                            drawRoundRect(
                                color = activeColumnHighlight,
                                topLeft = Offset(slotLeft + 2.dp.toPx(), topPadding - 8.dp.toPx()),
                                size = Size(slotWidth - 4.dp.toPx(), plotHeight + 12.dp.toPx()),
                                cornerRadius = CornerRadius(10.dp.toPx(), 10.dp.toPx())
                            )
                        }

                        val animFraction = animatedFractions[idx]
                        val totalBarHeight = if (bucket.totalCount > 0) {
                            (animFraction * plotHeight).coerceAtLeast(8.dp.toPx())
                        } else {
                            4.dp.toPx()
                        }
                        val barTop = baselineY - totalBarHeight

                        if (bucket.totalCount == 0) {
                            // Subtle baseline stub for zero-count days
                            drawRoundRect(
                                color = gridLineColor.copy(alpha = 0.6f),
                                topLeft = Offset(barLeft, baselineY - 4.dp.toPx()),
                                size = Size(barWidth, 4.dp.toPx()),
                                cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx())
                            )
                        } else {
                            // Primary bar gradient
                            drawRoundRect(
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        primaryBarColor,
                                        primaryBarColor.copy(alpha = 0.78f)
                                    ),
                                    startY = barTop,
                                    endY = baselineY
                                ),
                                topLeft = Offset(barLeft, barTop),
                                size = Size(barWidth, totalBarHeight),
                                cornerRadius = cornerRadius
                            )

                            // Stacked high-urgency segment at top of the bar if highUrgencyCount > 0
                            if (bucket.highUrgencyCount > 0) {
                                val urgentRatio =
                                    (bucket.highUrgencyCount.toFloat() / bucket.totalCount.toFloat())
                                        .coerceIn(0f, 1f)
                                val urgentHeight = (totalBarHeight * urgentRatio).coerceAtLeast(6.dp.toPx())
                                drawRoundRect(
                                    color = urgentBarColor,
                                    topLeft = Offset(barLeft, barTop),
                                    size = Size(barWidth, urgentHeight),
                                    cornerRadius = cornerRadius
                                )
                            }

                            // Active selection outline ring around the selected bar
                            if (idx == selectedDayIndex) {
                                drawRoundRect(
                                    color = selectedBarBorderColor.copy(alpha = 0.55f),
                                    topLeft = Offset(barLeft, barTop),
                                    size = Size(barWidth, totalBarHeight),
                                    cornerRadius = cornerRadius,
                                    style = Stroke(width = 1.8.dp.toPx())
                                )
                            }

                            // Value label directly above bar
                            val countText = bucket.totalCount.toString()
                            val countLayout = textMeasurer.measure(
                                text = countText,
                                style = TextStyle(
                                    color = if (idx == selectedDayIndex) primaryBarColor else labelTextColor,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                            drawText(
                                textLayoutResult = countLayout,
                                topLeft = Offset(
                                    x = barLeft + (barWidth - countLayout.size.width) / 2f,
                                    y = (barTop - countLayout.size.height - 2.dp.toPx()).coerceAtLeast(0f)
                                )
                            )
                        }
                    }

                    // Draw 7-Day Average Reference Line across plot area if totalWeekBarks > 0
                    if (totalWeekBarks > 0) {
                        val avgFraction = (avgDailyBarks / yAxisMax.toFloat()).coerceIn(0f, 1f)
                        val avgY = baselineY - (avgFraction * plotHeight)
                        drawLine(
                            color = avgLineColor.copy(alpha = 0.85f),
                            start = Offset(leftAxisWidth, avgY),
                            end = Offset(size.width, avgY),
                            strokeWidth = 1.5.dp.toPx(),
                            pathEffect = avgDashEffect
                        )
                    }
                }
            }

            // 4. Interactive X-Axis Day Selector Row (Accessible 48dp touch targets)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("weekly_chart_x_axis_row"),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Spacer matching left Y-axis label width so pills align under bars
                Spacer(modifier = Modifier.width(24.dp))

                safeBuckets.forEachIndexed { idx, bucket ->
                    val isSelected = idx == selectedDayIndex
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (isSelected) {
                            MaterialTheme.colorScheme.primaryContainer
                        } else {
                            Color.Transparent
                        },
                        border = if (bucket.isToday && !isSelected) {
                            BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
                        } else {
                            null
                        },
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = 48.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { selectedDayIndex = idx }
                            .testTag("weekly_chart_day_${bucket.dayIndex}")
                    ) {
                        Column(
                            modifier = Modifier.padding(vertical = 6.dp, horizontal = 2.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = bucket.dayShortLabel,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (isSelected || bucket.isToday) FontWeight.ExtraBold else FontWeight.Medium,
                                color = if (isSelected) {
                                    MaterialTheme.colorScheme.onPrimaryContainer
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                },
                                maxLines = 1,
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = if (bucket.isToday) "Today" else "${bucket.totalCount}",
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 9.sp,
                                fontWeight = if (bucket.isToday) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                                },
                                maxLines = 1,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }

            // 5. Recharts-Style Selected Day Tooltip Callout Card
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = primaryBarContainer.copy(alpha = 0.38f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("weekly_chart_tooltip")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "${selectedBucket.dayShortLabel}, ${selectedBucket.dateLabel}" +
                                    if (selectedBucket.isToday) " (Today)" else "",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = "${selectedBucket.totalCount} ${if (selectedBucket.totalCount == 1) "Bark" else "Barks"}",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                        .testTag("weekly_chart_selected_day_count")
                                )
                            }
                        }

                        Text(
                            text = if (selectedBucket.totalCount > 0) {
                                "Standard: ${selectedBucket.standardCount} • High Urgency: ${selectedBucket.highUrgencyCount} • Avg ${selectedBucket.averageDb} dB"
                            } else {
                                "No bark translations recorded on ${selectedBucket.dateLabel}"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        if (selectedBucket.totalCount > 0) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.GraphicEq,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.secondary,
                                    modifier = Modifier.size(13.dp)
                                )
                                Text(
                                    text = "Top Pattern: ${selectedBucket.dominantVocalization}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.secondary,
                                    fontWeight = FontWeight.SemiBold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ChartLegendDot(
    color: Color,
    label: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(color)
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
