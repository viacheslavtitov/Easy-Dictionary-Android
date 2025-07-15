package org.easydictionary.app.view.dividers

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.easydictionary.app.view.widget.global.LightColors

@Composable
fun Divider(
    modifier: Modifier = Modifier.fillMaxWidth(),
    height: Dp = 1.dp,
    color: Color = if (isSystemInDarkTheme())
        LightColors.Divider
    else
        LightColors.Divider
) {
    HorizontalDivider(
        modifier = modifier,
        thickness = height,
        color = color
    )
}