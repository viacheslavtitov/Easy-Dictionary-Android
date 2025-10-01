package org.easydictionary.app.view.inputs

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.KeyboardAlt
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.easydictionary.app.R
import org.easydictionary.app.view.widget.global.TextDimen
import org.easydictionary.app.view.widget.phonetic.PhoneticsView

@Composable
fun TextFieldPrimary(
    defaultValue: String,
    onValueChange: (String) -> Unit,
    label: String,
    required: Boolean = false,
    errorMessage: String? = null,
    supportingText: String? = null,
    singleLine: Boolean = true,
    modifier: Modifier = Modifier
        .fillMaxWidth()
        .padding(6.dp),
) {
    var value by remember { mutableStateOf(defaultValue) }
    val isValid = remember(value) {
        value.isNotEmpty()
    }
    LaunchedEffect(defaultValue) {
        value = defaultValue
    }

    val errorMessage = when {
        !required && supportingText?.isNotEmpty() == true -> supportingText
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
        singleLine = singleLine,
        modifier = modifier,
        isError = required && !isValid && value.isNotEmpty(),
        supportingText = {
            errorMessage?.let {
                Text(text = errorMessage, fontSize = TextDimen.TextFieldError)
            }
        },
        trailingIcon = {
            if (value.isNotEmpty()) {
                IconButton(onClick = {
                    value = ""
                    onValueChange(value)
                }) {
                    Icon(imageVector = Icons.Default.Clear, contentDescription = "Clear text")
                }
            }
        },
    )
}

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

@Composable
fun PasswordTextField(
    defaultValue: String,
    onValueChange: (String) -> Unit,
    onValidationChanged: (Boolean) -> Unit,
    isRelationValidationError: State<Boolean> = mutableStateOf(false),
    otherErrorMessage: String? = null,
    label: String
) {
    var showPassword by remember { mutableStateOf(false) }
    val interactionSource = remember { MutableInteractionSource() }
    var text by remember { mutableStateOf(defaultValue) }
    var isValid by remember { mutableStateOf(false) }
    LaunchedEffect(text, isRelationValidationError) {
        val relationValidationValid =
            if (otherErrorMessage == null) true else if (isRelationValidationError.value) false else true
        val newValid = text.length >= 8 &&
                text.any { it.isUpperCase() } &&
                text.any { it.isLowerCase() } && relationValidationValid
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
        isRelationValidationError.value && otherErrorMessage != null -> otherErrorMessage
        else -> null
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
                Icon(imageVector = image, contentDescription = "Toggle password visibility")
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TextFieldPhonetic(
    symbols: List<String>,
    defaultValue: String,
    onValueChange: (String) -> Unit,
) {
    val keyboard = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    var value by rememberSaveable { mutableStateOf(defaultValue) }
    var showSheet by rememberSaveable { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    LaunchedEffect(defaultValue) {
        value = defaultValue
    }
    fun openSheet() {
        focusManager.clearFocus(force = true)
        keyboard?.hide()
        showSheet = true
        scope.launch { sheetState.show() }
    }
    OutlinedTextField(
        onValueChange = { newValue ->
            value = newValue
            onValueChange(value)
        },
        value = value,
        label = { OutlinedTextFieldLabel(stringResource(R.string.phonetic)) },
        textStyle = TextStyle(
            fontSize = TextDimen.TextFieldText,
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(6.dp),
        supportingText = {
            Text(
                text = stringResource(R.string.tap_to_add_phonetics),
                fontSize = TextDimen.TextFieldLabel
            )
        },
        trailingIcon = {
            IconButton(onClick = ::openSheet) {
                Icon(Icons.Default.KeyboardAlt, contentDescription = null)
            }
        },
    )
    fun onPhoneticsChanged(symbol: String) {
        value += symbol
        onValueChange(value)
    }
    if (showSheet) {
        ModalBottomSheet(
            onDismissRequest = { showSheet = false },
            sheetState = sheetState,
        ) {
            PhoneticsView(
                defaultValue = value,
                symbols = symbols,
                onInsert = ::onPhoneticsChanged,
                onClose = { showSheet = false }
            )
        }
    }
}