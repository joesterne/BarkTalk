package com.example.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Material 3 Window Size Classes for responsive handheld, foldable, and tablet layouts.
 */
enum class BarkWindowSizeClass(val label: String) {
    COMPACT("Compact"),
    MEDIUM("Tablet • Medium"),
    EXPANDED("Tablet • Expanded")
}

/**
 * Computed container-based layout metrics derived dynamically from [BoxWithConstraints].
 */
@Immutable
data class BarkAdaptiveLayoutInfo(
    val widthDp: Dp,
    val heightDp: Dp,
    val windowSizeClass: BarkWindowSizeClass,
    val useNavigationRail: Boolean,
    val useTwoPaneLayout: Boolean,
    val gridColumns: Int,
    val horizontalPadding: Dp,
    val contentMaxWidth: Dp
) {
    val isTabletOrExpanded: Boolean
        get() = windowSizeClass != BarkWindowSizeClass.COMPACT

    companion object {
        fun fromDimensions(widthDp: Dp, heightDp: Dp): BarkAdaptiveLayoutInfo {
            val sizeClass = when {
                widthDp < 600.dp -> BarkWindowSizeClass.COMPACT
                widthDp < 840.dp -> BarkWindowSizeClass.MEDIUM
                else -> BarkWindowSizeClass.EXPANDED
            }
            return when (sizeClass) {
                BarkWindowSizeClass.COMPACT -> BarkAdaptiveLayoutInfo(
                    widthDp = widthDp,
                    heightDp = heightDp,
                    windowSizeClass = BarkWindowSizeClass.COMPACT,
                    useNavigationRail = false,
                    useTwoPaneLayout = false,
                    gridColumns = 2,
                    horizontalPadding = 16.dp,
                    contentMaxWidth = 600.dp
                )
                BarkWindowSizeClass.MEDIUM -> BarkAdaptiveLayoutInfo(
                    widthDp = widthDp,
                    heightDp = heightDp,
                    windowSizeClass = BarkWindowSizeClass.MEDIUM,
                    useNavigationRail = true,
                    useTwoPaneLayout = widthDp >= 700.dp,
                    gridColumns = 3,
                    horizontalPadding = 24.dp,
                    contentMaxWidth = 960.dp
                )
                BarkWindowSizeClass.EXPANDED -> BarkAdaptiveLayoutInfo(
                    widthDp = widthDp,
                    heightDp = heightDp,
                    windowSizeClass = BarkWindowSizeClass.EXPANDED,
                    useNavigationRail = true,
                    useTwoPaneLayout = true,
                    gridColumns = 4,
                    horizontalPadding = 28.dp,
                    contentMaxWidth = 1280.dp
                )
            }
        }
    }
}

val LocalAdaptiveLayoutInfo = compositionLocalOf {
    BarkAdaptiveLayoutInfo.fromDimensions(360.dp, 800.dp)
}

/**
 * Observes the available container bounds via [BoxWithConstraints] and provides
 * [LocalAdaptiveLayoutInfo] to all child composables so the UI dynamically resizes
 * across phones, split-screen windows, foldables, and tablets.
 */
@Composable
fun ProvideAdaptiveLayout(
    modifier: Modifier = Modifier,
    content: @Composable (BarkAdaptiveLayoutInfo) -> Unit
) {
    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .testTag("adaptive_root_container")
    ) {
        val currentWidth = maxWidth
        val currentHeight = maxHeight
        val adaptiveInfo = remember(currentWidth, currentHeight) {
            BarkAdaptiveLayoutInfo.fromDimensions(currentWidth, currentHeight)
        }
        CompositionLocalProvider(LocalAdaptiveLayoutInfo provides adaptiveInfo) {
            content(adaptiveInfo)
        }
    }
}

/**
 * Centers content within a readable maximum width on large screens while remaining
 * full-width on compact handheld screens.
 */
@Composable
fun AdaptiveContentContainer(
    modifier: Modifier = Modifier,
    maxWidth: Dp = LocalAdaptiveLayoutInfo.current.contentMaxWidth,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = maxWidth)
                .fillMaxHeight()
        ) {
            content()
        }
    }
}

/**
 * Canonical Two-Pane layout helper that renders [primaryPane] and [supportingPane]
 * side-by-side when in tablet/expanded mode, or falls back to [compactContent] on phones.
 */
@Composable
fun AdaptiveTwoPaneLayout(
    useTwoPane: Boolean,
    primaryWeight: Float = 0.52f,
    supportingWeight: Float = 0.48f,
    paneSpacing: Dp = 20.dp,
    primaryPane: @Composable () -> Unit,
    supportingPane: @Composable () -> Unit,
    compactContent: @Composable () -> Unit,
    modifier: Modifier = Modifier
) {
    if (useTwoPane) {
        Row(
            modifier = modifier
                .fillMaxSize()
                .testTag("adaptive_two_pane_row"),
            horizontalArrangement = Arrangement.spacedBy(paneSpacing)
        ) {
            Box(
                modifier = Modifier
                    .weight(primaryWeight)
                    .fillMaxHeight()
                    .testTag("adaptive_primary_pane")
            ) {
                primaryPane()
            }
            Box(
                modifier = Modifier
                    .weight(supportingWeight)
                    .fillMaxHeight()
                    .testTag("adaptive_supporting_pane")
            ) {
                supportingPane()
            }
        }
    } else {
        Box(
            modifier = modifier
                .fillMaxSize()
                .testTag("adaptive_single_pane_box")
        ) {
            compactContent()
        }
    }
}
