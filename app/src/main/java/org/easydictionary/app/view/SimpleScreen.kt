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
import org.easydictionary.app.view.inputs.EmailTextField
import org.easydictionary.app.view.inputs.PasswordTextField

@Composable
fun SimpleScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center
    ) {
        PasswordTextField("test", {value -> {}}, "Password")
        Spacer(modifier = Modifier.height(12.dp))
        EmailTextField("", {value -> {}}, "Email")
    }
}
