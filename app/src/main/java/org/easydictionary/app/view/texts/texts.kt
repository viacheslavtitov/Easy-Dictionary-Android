package org.easydictionary.app.view.texts

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import org.easydictionary.app.view.widget.global.TextDimen
import org.easydictionary.app.view.widget.global.getCurrentColorScheme
import org.easydictionary.app.view.widget.global.notoSansMedium

@Composable
fun TextFieldLabel(
    label: String,
    modifier: Modifier,
    singleLine: Boolean = false
) {
    val maxLines = if (singleLine) 1 else Int.MAX_VALUE
    val overflow = if (singleLine) TextOverflow.Ellipsis else TextOverflow.Clip
    Text(
        text = label,
        modifier = modifier,
        fontSize = TextDimen.TextFieldText,
        maxLines = maxLines,
        overflow = overflow,
        style = MaterialTheme.typography.titleMedium
    )
}

@Composable
fun SecondaryTextFieldLabel(
    label: String,
    modifier: Modifier,
    singleLine: Boolean = false
) {
    val maxLines = if (singleLine) 1 else Int.MAX_VALUE
    val overflow = if (singleLine) TextOverflow.Ellipsis else TextOverflow.Clip
    Text(
        text = label,
        color = getCurrentColorScheme().inverseSurface,
        modifier = modifier,
        maxLines = maxLines,
        overflow = overflow,
        fontSize = TextDimen.Secondary,
        style = MaterialTheme.typography.titleMedium
    )
}

@Composable
fun Secondary2TextFieldLabel(
    label: String,
    modifier: Modifier,
    singleLine: Boolean = false
) {
    val maxLines = if (singleLine) 1 else Int.MAX_VALUE
    val overflow = if (singleLine) TextOverflow.Ellipsis else TextOverflow.Clip
    Text(
        text = label,
        color = getCurrentColorScheme().secondary,
        modifier = modifier,
        maxLines = maxLines,
        overflow = overflow,
        fontSize = TextDimen.TextFieldText,
        style = MaterialTheme.typography.titleMedium,
        fontFamily = notoSansMedium
    )
}