package org.easydictionary.app.view.signin

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import org.easydictionary.app.R
import org.easydictionary.app.domain.models.auth.Auth
import org.easydictionary.app.domain.models.navigation.AppNavigation
import org.easydictionary.app.domain.viewmodels.auth.SignInViewModel
import org.easydictionary.app.domain.viewmodels.main.SharedMainViewModel
import org.easydictionary.app.view.FetchDataState
import org.easydictionary.app.view.buttons.ButtonPrimary
import org.easydictionary.app.view.dialogs.ErrorAlertDialog
import org.easydictionary.app.view.ext.clearStack
import org.easydictionary.app.view.inputs.EmailTextField
import org.easydictionary.app.view.inputs.PasswordTextField
import org.easydictionary.app.view.texts.TextFieldLabel
import org.easydictionary.app.view.widget.global.TextDimen

@Composable
fun SignInScreen(
    navController: NavController,
    viewModel: SignInViewModel = hiltViewModel(),
    sharedMainViewModel: SharedMainViewModel
) {
    val scope = rememberCoroutineScope()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        var email  by remember { mutableStateOf("") }
        var password  by remember { mutableStateOf("") }
        var errorMessage  by remember { mutableStateOf("") }
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
            scope.launch {
                viewModel.signIn(email, password, "email", "").collect {
                    when (it) {
                        is FetchDataState.DataState<Auth> -> {
                            navController.navigate(AppNavigation.HomeScreen.route) {
                                clearStack()
                            }
                        }

                        is FetchDataState.ErrorStateString -> {
                            Log.e("SignInScreen", it.error)
                            errorMessage = it.error
                            showErrorDialog = true
                        }

                        is FetchDataState.ErrorState -> {
                            Log.e("SignInScreen", {it.exception.message}.toString())
                            errorMessage = it.exception.message.toString()
                            showErrorDialog = true
                        }

                        is FetchDataState.StartLoadingState -> {
                            sharedMainViewModel.loading(true)
                        }

                        is FetchDataState.FinishLoadingState -> {
                            sharedMainViewModel.loading(false)
                        }
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        TextFieldLabel(stringResource(R.string.or))
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