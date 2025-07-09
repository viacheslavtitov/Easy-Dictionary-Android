package org.easydictionary.app.view.inputs

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.easydictionary.app.R
import org.easydictionary.app.view.widget.global.LightColors
import org.easydictionary.app.view.widget.global.TextDimen

@Preview
@Composable
fun TextFieldPrimary(
    defaultValue: String,
    onValueChange: (String) -> Unit,
    label: String,
    required: Boolean = false,
    errorMessage: String? = null,
    modifier: Modifier = Modifier
        .fillMaxWidth()
        .padding(6.dp),
) {
    var value by remember { mutableStateOf(defaultValue) }
    val isValid = remember(value) {
        value.isNotEmpty()
    }

    val errorMessage = when {
        value.isEmpty() -> null
        required && value.isEmpty() -> errorMessage
        else -> null
    }
    OutlinedTextField(
        onValueChange = { newValue ->
            value = newValue
            onValueChange(value)
        },
        value = value,
        label = { OutlinedTextFieldLabel(label) },
        textStyle = TextStyle(
            fontSize = TextDimen.TextFieldText
        ),
        modifier = modifier,
        isError = required && !isValid && value.isNotEmpty(),
        supportingText = {
            errorMessage?.let {
                Text(text = errorMessage, fontSize = TextDimen.TextFieldError)
            }
        },
        trailingIcon = {
            if (value.isNotEmpty()) {
                IconButton(onClick = { value = "" }) {
                    Icon(imageVector = Icons.Default.Clear, contentDescription = "Clear text")
                }
            }
        },
    )
}

@Preview
@Composable
fun EmailTextField(
    defaultValue: String,
    onValueChange: (String) -> Unit,
    onValidationChanged: (Boolean) -> Unit,
    label: String
) {
    var email by remember { mutableStateOf(defaultValue) }
    var isValid by remember { mutableStateOf(false) }
    LaunchedEffect(email) {
        val newValid = android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
        if (newValid != isValid) {
            isValid = newValid
            onValidationChanged(newValid)
        }
    }

    val errorMessage = when {
        email.isEmpty() -> null
        !android.util.Patterns.EMAIL_ADDRESS.matcher(email)
            .matches() -> stringResource(R.string.error_email_format_failed)

        else -> null
    }
    OutlinedTextField(
        onValueChange = { newValue ->
            email = newValue
            onValueChange(newValue)
        },
        value = email,
        label = { OutlinedTextFieldLabel(label) },
        textStyle = TextStyle(
            fontSize = TextDimen.TextFieldText
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(6.dp),
        singleLine = true,
        isError = !isValid && email.isNotEmpty(),
        supportingText = {
            if (errorMessage != null) {
                Text(text = errorMessage, fontSize = TextDimen.TextFieldError)
            }
        },
    )
}

@Preview
@Composable
fun PasswordTextField(
    defaultValue: String,
    onValueChange: (String) -> Unit,
    onValidationChanged: (Boolean) -> Unit,
    label: String
) {
    var showPassword by remember { mutableStateOf(false) }
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()
    val isDark = isSystemInDarkTheme()
    var text by remember { mutableStateOf(defaultValue) }
    var isValid by remember { mutableStateOf(false) }
    LaunchedEffect(text) {
        val newValid = text.length >= 8 &&
                text.any { it.isUpperCase() } &&
                text.any { it.isLowerCase() }
        if (newValid != isValid) {
            isValid = newValid
            onValidationChanged(newValid)
        }
    }

    val errorMessage = when {
        text.isEmpty() -> null
        text.length < 8 -> stringResource(R.string.error_password_length)
        !text.any { it.isUpperCase() } -> stringResource(R.string.error_password_upper_letter)
        !text.any { it.isLowerCase() } -> stringResource(R.string.error_password_lower_letter)
        else -> null
    }
    val iconTintColor = if (isFocused) {
        if (isDark)
            LightColors.Main
        else
            LightColors.Main
    } else {
        if (isDark)
            LightColors.Outlined
        else
            LightColors.Outlined
    }
    OutlinedTextField(
        onValueChange = { newValue ->
            text = newValue
            onValueChange(newValue)
        },
        value = text,
        label = { OutlinedTextFieldLabel(label) },
        textStyle = TextStyle(
            fontSize = TextDimen.TextFieldText
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(6.dp),
        singleLine = true,
        interactionSource = interactionSource,
        trailingIcon = {
            val image = if (showPassword)
                Icons.Default.Visibility
            else
                Icons.Default.VisibilityOff
            IconButton(onClick = { showPassword = !showPassword }) {
                Icon(imageVector = image, tint = iconTintColor, contentDescription = "Toggle password visibility")
            }
        },
        visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
        isError = !isValid && text.isNotBlank(),
        supportingText = {
            if (errorMessage != null) {
                Text(text = errorMessage, fontSize = TextDimen.TextFieldError)
            }
        }
    )
}

@Composable
private fun OutlinedTextFieldLabel(label: String) {
    Text(
        text = label,
        modifier = Modifier
            .wrapContentSize()
            .padding(horizontal = 6.dp),
        fontSize = TextDimen.TextFieldLabel,
        style = MaterialTheme.typography.titleSmall
    )
}