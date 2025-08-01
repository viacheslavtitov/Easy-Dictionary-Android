package org.easydictionary.app.view.dialogs

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import org.easydictionary.app.R
import org.easydictionary.app.view.inputs.TextFieldPrimary
import org.easydictionary.app.view.widget.global.TextDimen

@Composable
private fun BaseAlertDialog(
    onDismissRequest: (() -> Unit)? = null,
    onConfirmation: (() -> Unit)? = null,
    title: String,
    message: String,
    okButtonText: String = stringResource(R.string.ok),
    cancelButtonText: String? = stringResource(R.string.cancel),
    icon: ImageVector?,
) {
    AlertDialog(
        icon = icon?.let {
            {
                Icon(
                    imageVector = it,
                    contentDescription = null,
                )
            }
        },
        title = {
            Text(
                text = title,
                fontSize = TextDimen.TextFieldText,
                style = MaterialTheme.typography.titleMedium
            )
        },
        text = {
            Text(
                text = message,
                fontSize = TextDimen.TextFieldLabel,
                style = MaterialTheme.typography.titleMedium
            )
        },
        onDismissRequest = {
            onDismissRequest?.invoke()
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirmation?.invoke()
                }
            ) {
                Text(
                    text = okButtonText.uppercase(),
                    fontSize = TextDimen.TextFieldLabel
                )
            }
        },
        dismissButton = {
            cancelButtonText?.let {
                TextButton(
                    onClick = {
                        onDismissRequest?.invoke()
                    }
                ) {
                    Text(
                        text = cancelButtonText.uppercase(),
                        fontSize = TextDimen.TextFieldLabel
                    )
                }
            }
        }
    )
}

@Composable
fun ErrorAlertDialog(
    onDismissRequest: (() -> Unit)? = null,
    onConfirmation: (() -> Unit)? = null,
    message: String,
) {
    BaseAlertDialog(
        onDismissRequest = onDismissRequest,
        onConfirmation = onConfirmation,
        title = stringResource(R.string.error),
        message = message,
        cancelButtonText = null,
        icon = Icons.Default.ErrorOutline,
    )
}

@Composable
fun InfoAlertDialog(
    onDismissRequest: (() -> Unit)? = null,
    onConfirmation: (() -> Unit)? = null,
    message: String
) {
    BaseAlertDialog(
        onDismissRequest = onDismissRequest,
        onConfirmation = onConfirmation,
        title = stringResource(R.string.info),
        message = message,
        cancelButtonText = null,
        icon = Icons.Default.Info,
    )
}

@Composable
fun ButtonsAlertDialog(
    onDismissRequest: (() -> Unit)? = null,
    onConfirmation: (() -> Unit)? = null,
    title: String,
    message: String,
    okButtonText: String = stringResource(R.string.ok),
    cancelButtonText: String? = stringResource(R.string.cancel),
    icon: ImageVector? = null,
) {
    BaseAlertDialog(
        onDismissRequest = onDismissRequest,
        onConfirmation = onConfirmation,
        title = title,
        message = message,
        cancelButtonText = cancelButtonText,
        okButtonText = okButtonText,
        icon = icon
    )
}

@Composable
fun InputAlertDialog(
    onDismissRequest: (() -> Unit)? = null,
    onConfirmation: (() -> Unit)? = null,
    title: String,
    defaultValue: String,
    onValueChange: (String) -> Unit,
    label: String,
    singleLine: Boolean = true,
    okButtonText: String = stringResource(R.string.ok),
    cancelButtonText: String? = stringResource(R.string.cancel),
    icon: ImageVector? = null,
) {
    AlertDialog(
        icon = icon?.let {
            {
                Icon(
                    imageVector = it,
                    contentDescription = null,
                )
            }
        },
        title = {
            Text(
                text = title,
                fontSize = TextDimen.TextFieldText,
                style = MaterialTheme.typography.titleMedium
            )
        },
        text = {
            TextFieldPrimary(
                defaultValue = defaultValue,
                onValueChange = onValueChange,
                label = label,
                singleLine = singleLine
            )
        },
        onDismissRequest = {
            onDismissRequest?.invoke()
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirmation?.invoke()
                }
            ) {
                Text(
                    text = okButtonText.uppercase(),
                    fontSize = TextDimen.TextFieldLabel
                )
            }
        },
        dismissButton = {
            cancelButtonText?.let {
                TextButton(
                    onClick = {
                        onDismissRequest?.invoke()
                    }
                ) {
                    Text(
                        text = cancelButtonText.uppercase(),
                        fontSize = TextDimen.TextFieldLabel
                    )
                }
            }
        }
    )
}