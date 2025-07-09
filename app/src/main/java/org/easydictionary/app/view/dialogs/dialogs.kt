package org.easydictionary.app.view.dialogs

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.easydictionary.app.R
import org.easydictionary.app.view.widget.global.LightColors
import org.easydictionary.app.view.widget.global.TextDimen

@Composable
private fun BaseAlertDialog(
    onDismissRequest: (() -> Unit)? = null,
    onConfirmation: (() -> Unit)? = null,
    title: String,
    message: String,
    titleTextColor: Color,
    okButtonText: String = stringResource(R.string.ok),
    cancelButtonText: String? = stringResource(R.string.cancel),
    icon: ImageVector?,
    iconTintColor: Color = LightColors.Main
) {
    val isDark = isSystemInDarkTheme()
    val messageColor =
        if (isDark)
            LightColors.Text_Secondary
        else
            LightColors.Text_Secondary
    val okButtonTextColor =
        if (isDark)
            LightColors.Main
        else
            LightColors.Main
    val cancelButtonTextColor =
        if (isDark)
            LightColors.Main_Light
        else
            LightColors.Main_Light
    AlertDialog(
        icon = icon?.let {
            {
                Icon(
                    imageVector = it,
                    contentDescription = null,
                    tint = iconTintColor
                )
            }
        },
        iconContentColor = iconTintColor,
        title = {
            Text(
                text = title,
                color = titleTextColor,
                fontSize = TextDimen.TextFieldText,
                style = MaterialTheme.typography.titleMedium
            )
        },
        text = {
            Text(
                text = message,
                color = messageColor,
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
                    color = okButtonTextColor,
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
                        color = cancelButtonTextColor,
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
    val titleTextColor =
        if (isSystemInDarkTheme())
            LightColors.Error
        else
            LightColors.Error
    BaseAlertDialog(
        onDismissRequest = onDismissRequest,
        onConfirmation = onConfirmation,
        title = stringResource(R.string.error),
        message = message,
        titleTextColor = titleTextColor,
        cancelButtonText = null,
        icon = Icons.Default.ErrorOutline,
        iconTintColor = titleTextColor
    )
}

@Composable
fun InfoAlertDialog(
    onDismissRequest: (() -> Unit)? = null,
    onConfirmation: (() -> Unit)? = null,
    message: String
) {
    val titleTextColor =
        if (isSystemInDarkTheme())
            LightColors.Text_Main
        else
            LightColors.Text_Main
    BaseAlertDialog(
        onDismissRequest = onDismissRequest,
        onConfirmation = onConfirmation,
        title = stringResource(R.string.info),
        message = message,
        titleTextColor = titleTextColor,
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
    val titleTextColor =
        if (isSystemInDarkTheme())
            LightColors.Text_Main
        else
            LightColors.Text_Main
    BaseAlertDialog(
        onDismissRequest = onDismissRequest,
        onConfirmation = onConfirmation,
        title = title,
        message = message,
        titleTextColor = titleTextColor,
        cancelButtonText = cancelButtonText,
        okButtonText = okButtonText,
        icon = icon
    )
}