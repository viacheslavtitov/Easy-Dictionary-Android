package org.easydictionary.app.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.easydictionary.app.view.buttons.ButtonPrimary
import org.easydictionary.app.view.buttons.ButtonSecondary
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
        EmailTextField("", {value -> {}}, "Email")
        Spacer(modifier = Modifier.height(6.dp))
        PasswordTextField("", {value -> {}}, "Password")
        Spacer(modifier = Modifier.height(6.dp))
        TextFieldPrimary(defaultValue = "", {value -> {}}, label = "Text field", required = true, errorMessage = "Test")
        Spacer(modifier = Modifier.height(12.dp))
        ButtonPrimary("Log in") { }
        Spacer(modifier = Modifier.height(12.dp))
        ButtonSecondary("Cancel") { }
    }
}
