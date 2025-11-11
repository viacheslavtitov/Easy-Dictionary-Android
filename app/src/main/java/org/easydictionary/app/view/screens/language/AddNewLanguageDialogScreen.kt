package org.easydictionary.app.view.screens.language

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import org.easydictionary.app.R
import org.easydictionary.app.view.dialogs.InputAlertDialog

const val BUNDLE_NEW_LANGUAGE = "BUNDLE_NEW_LANGUAGE"

@Composable
fun AddNewLanguageDialogScreen(onDismiss: () -> Unit, onConfirm: (String) -> Unit) {
    var text by remember { mutableStateOf("") }
    InputAlertDialog(
        title = stringResource(R.string.add_language),
        defaultValue = "",
        onValueChange = { newValue ->
            text = newValue
        },
        onDismissRequest = onDismiss,
        onConfirmation = {
            onConfirm(text)
        },
        label = stringResource(R.string.add_your_own_language),
        okButtonText = stringResource(R.string.add)
    )
}