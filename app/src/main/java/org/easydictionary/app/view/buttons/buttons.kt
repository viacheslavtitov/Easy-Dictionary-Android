package org.easydictionary.app.view.buttons

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Preview
@Composable
fun ButtonPrimary(
    title: String,
    modifier: Modifier = Modifier
        .fillMaxWidth()
        .padding(0.dp),
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val backgroundColor = if (isPressed) Color.LightGray else Color.Gray
    Button(
        modifier = modifier,
        contentPadding = PaddingValues(16.dp),
        shape = RoundedCornerShape(12.dp),
        onClick = onClick,
        interactionSource = interactionSource,
        colors = ButtonDefaults.buttonColors(
            containerColor = backgroundColor
        ),
    ) {
        Text(text = title, color = Color.White, fontSize = 18.sp)
    }
}

@Preview
@Composable
fun ButtonSecondary(
    title: String,
    modifier: Modifier = Modifier
        .fillMaxWidth()
        .padding(0.dp),
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val strokeColor = if (isPressed) {
        BorderStroke(
            width = 2.dp,
            color = Color.Black,
        )
    } else {
        BorderStroke(
            width = 2.dp,
            color = Color.Gray,
        )
    }
    val backgroundColor = if (isPressed) Color.LightGray else Color.Transparent
    val textColor = if (isPressed) Color.White else Color.Gray
    OutlinedButton(
        modifier = modifier,
        contentPadding = PaddingValues(16.dp),
        shape = RoundedCornerShape(12.dp),
        onClick = onClick,
        interactionSource = interactionSource,
        colors = ButtonDefaults.buttonColors(
            containerColor = backgroundColor
        ),
        border = strokeColor
    ) {
        Text(text = title, color = textColor, fontSize = 18.sp)
    }
}