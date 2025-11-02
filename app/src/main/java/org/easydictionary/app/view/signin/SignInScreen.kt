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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import org.easydictionary.app.R
import org.easydictionary.app.domain.models.navigation.AppNavigation
import org.easydictionary.app.domain.viewmodels.auth.SignInContract
import org.easydictionary.app.domain.viewmodels.auth.SignInEffect
import org.easydictionary.app.domain.viewmodels.auth.SignInViewModel
import org.easydictionary.app.domain.viewmodels.main.SharedMainContract
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
    sharedMainContract: SharedMainContract = hiltViewModel<SharedMainViewModel>(),
    contract: SignInContract = hiltViewModel<SignInViewModel>()
) {
    val ui = contract.state.collectAsState().value
    var showError by remember { mutableStateOf("") }
    LaunchedEffect(Unit) {
        launch {
            contract.effects.collect { eff ->
                when (eff) {
                    SignInEffect.NavigateHome ->
                        navController.navigate(AppNavigation.HomeScreen.route) { clearStack() }

                    is SignInEffect.ShowError -> {
                        showError = eff.message
                    }

                    SignInEffect.NavigateSignUp -> navController.navigate(AppNavigation.SignUpScreen.route)
                }
            }
        }
    }
    sharedMainContract.loading(ui.isLoading)
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
        EmailTextField(
            "",
            { value ->
                contract.onEmailChanged(value)
            },
            onValidationChanged = { isValid ->

            },
            stringResource(R.string.email)
        )
        Spacer(modifier = Modifier.height(6.dp))
        PasswordTextField(
            "",
            { value -> contract.onPasswordChanged(value) },
            onValidationChanged = { isValid -> },
            label = stringResource(R.string.password)
        )
        Spacer(modifier = Modifier.height(6.dp))
        ButtonPrimary(title = stringResource(R.string.log_in), enabled = ui.isFormValid, modifier = Modifier.testTag("btn-login")) {
            contract.onSubmit()
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
    }
}