package org.easydictionary.app.view.texts

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.easydictionary.app.view.widget.global.LightColors
import org.easydictionary.app.view.widget.global.TextDimen

@Composable
fun TextFieldLabel(
    label: String,
    modifier: Modifier = Modifier
        .wrapContentSize()
        .padding(horizontal = 6.dp)
) {
    val isDark = isSystemInDarkTheme()
    val textColor =
        if (isDark)
            LightColors.Text_Main
        else
            LightColors.Text_Main
    Text(
        text = label,
        modifier = modifier,
        fontSize = TextDimen.TextFieldText,
        style = MaterialTheme.typography.titleMedium,
        color = textColor
    )
}