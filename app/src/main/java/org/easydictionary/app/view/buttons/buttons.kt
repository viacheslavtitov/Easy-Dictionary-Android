package org.easydictionary.app.view.buttons

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.isSystemInDarkTheme
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
import org.easydictionary.app.view.widget.global.ButtonDimen
import org.easydictionary.app.view.widget.global.Colors
import org.easydictionary.app.view.widget.global.LightColors

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
    val isDark = isSystemInDarkTheme()

    val backgroundColor = if (isPressed) {
        if (isDark)
            LightColors.Main_Dark
        else
            LightColors.Main_Dark
    } else {
        if (isDark)
            LightColors.Main
        else
            LightColors.Main
    }
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
        Text(text = title, color = Colors.White, fontSize = ButtonDimen.ButtonText)
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
    val isDark = isSystemInDarkTheme()

    val strokeColor = if (isPressed) {
        if (isDark)
            BorderStroke(
                width = 0.dp,
                color = Color.Transparent,
            )
        else
            BorderStroke(
                width = 0.dp,
                color = Color.Transparent,
            )
    } else {
        if (isDark)
            BorderStroke(
                width = 2.dp,
                color = LightColors.Main_Light,
            )
        else
            BorderStroke(
                width = 2.dp,
                color = LightColors.Main_Light,
            )
    }
    val backgroundColor = if (isPressed) {
        if (isDark)
            LightColors.Main_Light
        else
            LightColors.Main_Light
    } else {
        if (isDark)
            Color.Transparent
        else
            Color.Transparent
    }
    val textColor = if (isPressed) {
        if (isDark)
            Colors.White
        else
            Colors.White
    } else {
        if (isDark)
            LightColors.Text_Secondary
        else
            LightColors.Text_Secondary
    }
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
        Text(text = title, color = textColor, fontSize = ButtonDimen.ButtonText)
    }
}