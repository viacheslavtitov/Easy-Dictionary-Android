package org.easydictionary.app.view.buttons

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.easydictionary.app.view.widget.global.ButtonDimen

@Composable
fun ButtonPrimary(
    title: String,
    modifier: Modifier = Modifier
        .fillMaxWidth()
        .padding(0.dp),
    enabled: Boolean,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    Button(
        modifier = modifier,
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