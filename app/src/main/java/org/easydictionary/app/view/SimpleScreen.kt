package org.easydictionary.app.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.easydictionary.app.view.buttons.ButtonPrimary
import org.easydictionary.app.view.buttons.ButtonSecondary
import org.easydictionary.app.view.dialogs.ButtonsAlertDialog
import org.easydictionary.app.view.dialogs.ErrorAlertDialog
import org.easydictionary.app.view.dialogs.InfoAlertDialog
import org.easydictionary.app.view.indicators.LoadingIndicatorCircle
import org.easydictionary.app.view.inputs.EmailTextField
import org.easydictionary.app.view.inputs.PasswordTextField
import org.easydictionary.app.view.inputs.TextFieldPrimary

@Composable
fun SimpleScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center
    ) {
        var showTwoButtonsDialog by remember { mutableStateOf(false) }
        var showErrorDialog by remember { mutableStateOf(false) }
        var showInfoDialog by remember { mutableStateOf(false) }
        EmailTextField("", { value -> {} }, { isValid -> {} }, "Email")
        Spacer(modifier = Modifier.height(6.dp))
        PasswordTextField("", { value -> {} }, { isValid -> {} }, "Password")
        Spacer(modifier = Modifier.height(6.dp))
        TextFieldPrimary(
            defaultValue = "",
            { value -> {} },
            label = "Text field",
            required = true,
            errorMessage = "Test"
        )
        Spacer(modifier = Modifier.height(12.dp))
        ButtonPrimary("Two button alert", enabled = true) {
            showTwoButtonsDialog = true
        }
        if (showTwoButtonsDialog) {
            ButtonsAlertDialog(
                onDismissRequest = {
                    showTwoButtonsDialog = false
                },
                onConfirmation = {
                    showTwoButtonsDialog = false
                },
                title = "Test",
                message = "Test two buttons alert"
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
        ButtonSecondary("Info alert") { showInfoDialog = true }
        if (showInfoDialog) {
            InfoAlertDialog(
                onDismissRequest = {
                    showInfoDialog = false
                },
                onConfirmation = {
                    showInfoDialog = false
                },
                message = "Test info alert"
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
        ButtonSecondary("Error alert") { showErrorDialog = true }
        if (showErrorDialog) {
            ErrorAlertDialog(
                onDismissRequest = {
                    showErrorDialog = false
                },
                onConfirmation = {
                    showErrorDialog = false
                },
                message = "Error alert"
            )
        }
    }
}
