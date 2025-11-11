package org.easydictionary.app.view.screens.word.translation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import org.easydictionary.app.R
import org.easydictionary.app.view.dialogs.InputAlertDialog

const val BUNDLE_NEW_CATEGORY = "BUNDLE_NEW_CATEGORY"

@Composable
fun AddNewCategoryDialogScreen(onDismiss: () -> Unit, onConfirm: (String) -> Unit) {
    var text by remember { mutableStateOf("") }
    InputAlertDialog(
        title = stringResource(R.string.add_category),
        defaultValue = "",
        onValueChange = { newValue ->
            text = newValue
        },
        onDismissRequest = onDismiss,
        onConfirmation = {
            onConfirm(text)
        },
        label = stringResource(R.string.category_name),
        okButtonText = stringResource(R.string.add)
    )
}