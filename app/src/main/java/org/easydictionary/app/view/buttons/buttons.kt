package org.easydictionary.app.view.buttons

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.keyframes
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import org.easydictionary.app.view.widget.global.ButtonDimen
import kotlin.math.roundToInt

@Composable
fun ButtonPrimary(
    title: String,
    modifier: Modifier = Modifier,
    enabled: Boolean,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    Button(
        modifier = modifier.fillMaxWidth()
            .padding(0.dp),
        contentPadding = PaddingValues(16.dp),
        shape = RoundedCornerShape(12.dp),
        onClick = onClick,
        enabled = enabled,
        interactionSource = interactionSource
    ) {
        Text(text = title, fontSize = ButtonDimen.ButtonText)
    }
}

@Composable
fun ButtonSecondary(
    title: String,
    modifier: Modifier = Modifier
        .fillMaxWidth()
        .padding(0.dp),
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    OutlinedButton(
        modifier = modifier,
        contentPadding = PaddingValues(16.dp),
        shape = RoundedCornerShape(12.dp),
        onClick = onClick,
        interactionSource = interactionSource
    ) {
        Text(text = title, fontSize = ButtonDimen.ButtonText)
    }
}

@Composable
fun ButtonFilledTonalSecondary(
    title: String,
    modifier: Modifier = Modifier
        .fillMaxWidth()
        .padding(0.dp),
    shakeTrigger: State<Int>,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val offsetX = remember { Animatable(0f) }
    LaunchedEffect(shakeTrigger.value) {
        if(shakeTrigger.value > 0) {
            // shake anim
            offsetX.animateTo(
                targetValue = 0f,
                animationSpec = keyframes {
                    durationMillis = 1500
                    -16f at 50
                    16f at 100
                    -12f at 150
                    12f at 200
                    -8f at 250
                    8f at 300
                    -4f at 350
                    4f at 400
                    0f at 450
                }
            )
        }
    }
    FilledTonalButton(
        modifier = modifier.offset { IntOffset(offsetX.value.roundToInt(), 0) },
        contentPadding = PaddingValues(16.dp),
        shape = RoundedCornerShape(12.dp),
        onClick = onClick,
        enabled = enabled,
        interactionSource = interactionSource
    ) {
        Text(text = title, fontSize = ButtonDimen.ButtonText)
    }
}