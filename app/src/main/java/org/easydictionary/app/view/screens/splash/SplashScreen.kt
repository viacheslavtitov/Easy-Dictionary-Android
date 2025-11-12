package org.easydictionary.app.view.screens.splash

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import org.easydictionary.app.domain.models.navigation.AppNavigation
import org.easydictionary.app.domain.viewmodels.splash.SplashViewModel
import org.easydictionary.app.view.ext.clearStack
import org.easydictionary.app.view.indicators.LoadingIndicatorCircle

@Composable
fun SplashScreen(navController: NavController, viewModel: SplashViewModel = hiltViewModel()) {
    Box(
        modifier = Modifier
            .fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        LoadingIndicatorCircle()
    }
    if(viewModel.isUserSignedIn()) {
        navController.navigate(AppNavigation.HomeScreen.route) {
            clearStack()
        }
    } else {
        navController.navigate(AppNavigation.SignInScreen.route) {
            clearStack()
        }
    }
}