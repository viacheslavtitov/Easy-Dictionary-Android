package org.easydictionary.app.view.swipe

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.rememberSplineBasedDecay
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.AnchoredDraggableState
import androidx.compose.foundation.gestures.DraggableAnchors
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.anchoredDraggable
import androidx.compose.foundation.gestures.animateTo
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

enum class DragAnchors {
    Opened,
    Closed
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SwipeRevealItem(
    modifier: Modifier = Modifier,
    menuWidth: Dp = 200.dp,
    itemId: Int? = null,
    isOpen: Boolean = false,
    onCloseRequest: () -> Unit,
    onOpen: () -> Unit,
    content: @Composable () -> Unit,
    menuContent: @Composable RowScope.() -> Unit
) {
    val density = LocalDensity.current
//    val menuWidthPx = remember { with(density) { menuWidth.toPx() } }
    val anchors = with(density) {
        DraggableAnchors {
            DragAnchors.Closed at 0f
            DragAnchors.Opened at (menuWidth - (menuWidth * 2)).toPx()
        }
    }
    val decayAnimationSpec = rememberSplineBasedDecay<Float>()

    val state = remember {
        AnchoredDraggableState(
            initialValue = DragAnchors.Closed,
            anchors = anchors,
            positionalThreshold = { totalDistance: Float -> totalDistance * 0.5f },
            velocityThreshold = { with(density) { 100.dp.toPx() } },
            confirmValueChange = { newValue -> true },
            snapAnimationSpec = spring<Float>(stiffness = Spring.StiffnessMediumLow),
            decayAnimationSpec = decayAnimationSpec
        )
    }
    LaunchedEffect(isOpen) {
        if (!isOpen) {
            state.animateTo(DragAnchors.Closed)
        }
    }
    LaunchedEffect(state.currentValue) {
        if (state.currentValue == DragAnchors.Opened) {
            onOpen()
        }
    }
    LaunchedEffect(isOpen) {
        if (!isOpen && state.currentValue == DragAnchors.Opened) {
            state.animateTo(DragAnchors.Closed)
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .matchParentSize(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
            content = menuContent
        )
        Box(
            modifier = modifier
                .offset { IntOffset(state.requireOffset().roundToInt(), 0) }
                .anchoredDraggable(
                    state = state,
                    orientation = Orientation.Horizontal
                )
        ) {
            content()
        }
    }
}
