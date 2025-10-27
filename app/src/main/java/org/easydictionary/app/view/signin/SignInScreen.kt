package org.easydictionary.app.view.signin

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import org.easydictionary.app.R
import org.easydictionary.app.domain.models.navigation.AppNavigation
import org.easydictionary.app.domain.viewmodels.auth.SignInViewModel
import org.easydictionary.app.domain.viewmodels.main.SharedMainViewModel
import org.easydictionary.app.view.buttons.ButtonPrimary
import org.easydictionary.app.view.dialogs.ErrorAlertDialog
import org.easydictionary.app.view.ext.clearStack
import org.easydictionary.app.view.inputs.EmailTextField
import org.easydictionary.app.view.inputs.PasswordTextField
import org.easydictionary.app.view.texts.TextFieldLabel

@Composable
fun SignInScreen(
    navController: NavController,
    viewModel: SignInViewModel = hiltViewModel(),
    sharedMainViewModel: SharedMainViewModel
) {
    var showError by remember { mutableStateOf("") }
    val loadingProgress by viewModel.loadingDataUI.collectAsState()
    LaunchedEffect(Unit) {
        launch {
            viewModel.signedInSuccess.collect { success ->
                if (success) {
                    navController.navigate(AppNavigation.HomeScreen.route) {
                        clearStack()
                    }
                }
            }
        }
        launch {
            viewModel.errorMessage.collect { msg ->
                showError = msg
            }
        }
    }
    sharedMainViewModel.loading(loadingProgress)
    if (showError.isNotEmpty()) {
        ErrorAlertDialog(
            onDismissRequest = {
                showError = ""
            },
            onConfirmation = {
                showError = ""
            },
            message = showError
        )
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        var email by remember { mutableStateOf("") }
        var password by remember { mutableStateOf("") }
        var errorMessage by remember { mutableStateOf("") }
        var isEmailValid by remember { mutableStateOf(false) }
        var isPasswordValid by remember { mutableStateOf(false) }
        val isFormValid by remember(isEmailValid, isPasswordValid) {
            derivedStateOf { isEmailValid && isPasswordValid }
        }
        var showErrorDialog by remember { mutableStateOf(false) }

        EmailTextField(
            "",
            { value -> email = value },
            onValidationChanged = { isValid ->
                isEmailValid = isValid
            },
            stringResource(R.string.email)
        )
        Spacer(modifier = Modifier.height(6.dp))
        PasswordTextField(
            "",
            { value -> password = value },
            onValidationChanged = { isValid -> isPasswordValid = isValid },
            label = stringResource(R.string.password)
        )
        Spacer(modifier = Modifier.height(6.dp))
        ButtonPrimary(title = stringResource(R.string.log_in), enabled = isFormValid) {
            viewModel.signIn(email, password, "email", "")
        }
        Spacer(modifier = Modifier.height(6.dp))
        TextFieldLabel(
            label = stringResource(R.string.or), modifier = Modifier
                .wrapContentSize()
                .padding(horizontal = 6.dp)
        )
        Spacer(modifier = Modifier.height(6.dp))
        ButtonPrimary(title = stringResource(R.string.sign_up), enabled = true) {
            navController.navigate(AppNavigation.SignUpScreen.route)
        }
        if (showErrorDialog) {
            ErrorAlertDialog(
                onDismissRequest = {
                    showErrorDialog = false
                },
                onConfirmation = {
                    showErrorDialog = false
                },
                message = errorMessage
            )
        }
    }
}