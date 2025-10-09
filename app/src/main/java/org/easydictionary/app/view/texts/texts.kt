package org.easydictionary.app.view.texts

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.easydictionary.app.view.widget.global.TextDimen
import org.easydictionary.app.view.widget.global.getCurrentColorScheme
import org.easydictionary.app.view.widget.global.notoSansBold
import org.easydictionary.app.view.widget.global.notoSansMedium

@Composable
fun TextFieldLabel(
    label: String,
    modifier: Modifier
) {
    Text(
        text = label,
        modifier = modifier,
        fontSize = TextDimen.TextFieldText,
        style = MaterialTheme.typography.titleMedium
    )
}
@Composable
fun SecondaryTextFieldLabel(
    label: String,
    modifier: Modifier
) {
    Text(
        text = label,
        color = getCurrentColorScheme().inverseSurface,
        modifier = modifier,
        fontSize = TextDimen.TextFieldText,
        style = MaterialTheme.typography.titleMedium
    )
}

@Composable
fun Secondary2TextFieldLabel(
    label: String,
    modifier: Modifier
) {
    Text(
        text = label,
        color = getCurrentColorScheme().secondary,
        modifier = modifier,
        fontSize = TextDimen.TextFieldText,
        style = MaterialTheme.typography.titleMedium,
        fontFamily = notoSansMedium
    )
}