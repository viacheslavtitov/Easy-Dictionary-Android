package org.easydictionary.app.view.indicators

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Preview
@Composable
fun LoadingIndicatorCircle(
    modifier: Modifier = Modifier
        .fillMaxSize(fraction = 0.3f)
) {
    CircularProgressIndicator(
        modifier = modifier,
        strokeWidth = 8.dp,
    )
}