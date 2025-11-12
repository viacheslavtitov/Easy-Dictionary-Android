package org.easydictionary.app.view.screens.signup

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import org.easydictionary.app.R
import org.easydictionary.app.domain.models.navigation.AppNavigation
import org.easydictionary.app.domain.viewmodels.main.SharedMainContract
import org.easydictionary.app.domain.viewmodels.main.SharedMainViewModel
import org.easydictionary.app.domain.viewmodels.signup.SignUpViewModel
import org.easydictionary.app.view.buttons.ButtonPrimary
import org.easydictionary.app.view.dialogs.ErrorAlertDialog
import org.easydictionary.app.view.ext.clearStack
import org.easydictionary.app.view.inputs.TextFieldPrimary

@Composable
fun SignUpScreen(
    navController: NavController,
    viewModel: SignUpViewModel = hiltViewModel(),
    sharedMainContract: SharedMainContract = hiltViewModel<SharedMainViewModel>()
) {
    var showError by remember { mutableStateOf("") }
    val loadingProgress by viewModel.loadingDataUI.collectAsState()
    LaunchedEffect(Unit) {
        launch {
            viewModel.signedUpSuccess.collect { success ->
                if (success) {
                    navController.navigate(AppNavigation.SignInScreen.route) {
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
    sharedMainContract.loading(loadingProgress)
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
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        var firstName by remember { mutableStateOf("") }
        var lastName by remember { mutableStateOf("") }
        var email by remember { mutableStateOf("") }
        var password by remember { mutableStateOf("") }
        var confirmPassword by remember { mutableStateOf("") }
        var errorMessage by remember { mutableStateOf("") }
        var isEmailValid by remember { mutableStateOf(false) }
        var isPasswordValid by remember { mutableStateOf(false) }
        var isConfirmPasswordValid by remember { mutableStateOf(false) }
        val isPasswordsMatched by remember(isConfirmPasswordValid, isPasswordValid) {
            derivedStateOf { isConfirmPasswordValid && isPasswordValid && (confirmPassword == password) }
        }
        val isFirstNameValid = remember(firstName) {
            firstName.isNotEmpty()
        }
        val isLastNameValid = remember(lastName) {
            lastName.isNotEmpty()
        }
        val isFormValid by remember(
            isEmailValid,
            isPasswordValid,
            isPasswordsMatched,
            isFirstNameValid,
            isLastNameValid
        ) {
            derivedStateOf { isEmailValid && isPasswordValid && isPasswordsMatched && isFirstNameValid && isLastNameValid }
        }
        val isMismatchError by remember(confirmPassword, password) {
            derivedStateOf { confirmPassword.isNotEmpty() && password != confirmPassword }
        }
        var showErrorDialog by remember { mutableStateOf(false) }

        TextFieldPrimary(
            defaultValue = "",
            onValueChange = { value -> firstName = value },
            required = true,
            label = stringResource(R.string.first_name)
        )
        Spacer(modifier = Modifier.height(6.dp))
        TextFieldPrimary(
            defaultValue = "",
            onValueChange = { value -> lastName = value },
            required = true,
            label = stringResource(R.string.last_name)
        )
        Spacer(modifier = Modifier.height(6.dp))
//        EmailTextField(
//            "",
//            { value -> email = value },
//            onValidationChanged = { isValid ->
//                isEmailValid = isValid
//            },
//            stringResource(R.string.email)
//        )
//        Spacer(modifier = Modifier.height(6.dp))
//        PasswordTextField(
//            "",
//            { value -> password = value },
//            onValidationChanged = { isValid -> isPasswordValid = isValid },
//            label = stringResource(R.string.password)
//        )
//        Spacer(modifier = Modifier.height(6.dp))
//        PasswordTextField(
//            "",
//            { value -> confirmPassword = value },
//            onValidationChanged = { isValid -> isConfirmPasswordValid = isValid },
//            label = stringResource(R.string.confirm_password),
//            isRelationValidationError = derivedStateOf { isMismatchError },
//            otherErrorMessage = stringResource(R.string.error_passwords_not_match)
//        )
        Spacer(modifier = Modifier.height(6.dp))
        ButtonPrimary(title = stringResource(R.string.sign_up), enabled = isFormValid) {
            viewModel.signUp(email, password, firstName, lastName, "email", "")
        }
    }
}