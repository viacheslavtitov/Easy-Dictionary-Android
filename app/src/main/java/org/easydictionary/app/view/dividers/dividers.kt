package org.easydictionary.app.view.dividers

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun Divider(
    modifier: Modifier = Modifier.fillMaxWidth(),
    height: Dp = 1.dp
) {
    HorizontalDivider(
        modifier = modifier,
        thickness = height
    )
}