package org.easydictionary.app.test.view.signin

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.navigation.compose.ComposeNavigator
import androidx.navigation.compose.DialogNavigator
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.testing.TestNavHostController
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.cash.turbine.test
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withTimeout
import org.easydictionary.app.domain.models.navigation.AppNavigation
import org.easydictionary.app.domain.viewmodels.auth.SignInEffect
import org.easydictionary.app.test.view.FakeSharedMainContract
import org.easydictionary.app.view.screens.signin.SignInScreen
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class SignInScreenTest {

    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Before
    fun setup() {
        hiltRule.inject()
    }

    @Test
    fun entering_valid_email_and_password_enables_login_and_calls_viewModel() = runTest{
        val fakeVm = FakeSignInContract()
        lateinit var nav: TestNavHostController

        composeRule.setContent {
            val ctx = LocalContext.current
            nav = TestNavHostController(ctx).apply {
                navigatorProvider.addNavigator(ComposeNavigator())
                navigatorProvider.addNavigator(DialogNavigator())
            }
            NavHost(nav, startDestination = AppNavigation.SignInScreen.route) {
                composable(AppNavigation.SignInScreen.route) {
                    SignInScreen(
                    navController = nav,
                    contract = fakeVm,
                    sharedMainContract = FakeSharedMainContract()
                ) }
                composable(AppNavigation.HomeScreen.route) { Box(Modifier.testTag(AppNavigation.HomeScreen.route)) }
            }
        }
        composeRule.onNodeWithText("Email").performTextInput("test1@example.com")
        composeRule.onNodeWithText("Password").performTextInput("Qwerty123")

        val job = launch {
            fakeVm.effects.test {
                val item = awaitItem()
                assert(item == SignInEffect.NavigateHome)
                cancelAndIgnoreRemainingEvents()
            }
        }
        composeRule.onNodeWithTag("btn-login").assertIsEnabled().performClick()
        withTimeout(3_000) { job.join() }
    }

    @Test
    fun entering_not_valid_email_and_password() {
        val fakeVm = FakeSignInContract()
        lateinit var nav: TestNavHostController

        composeRule.setContent {
            val ctx = LocalContext.current
            nav = TestNavHostController(ctx).apply {
                navigatorProvider.addNavigator(ComposeNavigator())
                navigatorProvider.addNavigator(DialogNavigator())
            }
            NavHost(nav, startDestination = AppNavigation.SignInScreen.route) {
                composable(AppNavigation.SignInScreen.route) {
                    SignInScreen(
                        navController = nav,
                        contract = fakeVm,
                        sharedMainContract = FakeSharedMainContract()
                    ) }
                composable(AppNavigation.HomeScreen.route) { Box(Modifier.testTag(AppNavigation.HomeScreen.route)) }
            }
        }

        composeRule.onNodeWithText("Email").performTextInput("test1@example.")
        composeRule.onNodeWithText("Password").performTextInput("qwerty123")

        composeRule.onNodeWithText("Log in").assertIsNotEnabled()
    }

//    @Test
//    fun sign_up_perform_click() = runTest {
//        val fakeVm = FakeSignInContract()
//        lateinit var nav: TestNavHostController
//
//        composeRule.activity.setContent {
//            val ctx = LocalContext.current
//            nav = TestNavHostController(ctx).apply {
//                navigatorProvider.addNavigator(ComposeNavigator())
//                navigatorProvider.addNavigator(DialogNavigator())
//            }
//            NavHost(nav, startDestination = AppNavigation.SignInScreen.route) {
//                composable(AppNavigation.SignInScreen.route) {
//                    SignInScreen(
//                        navController = nav,
//                        contract = fakeVm,
//                        sharedMainContract = FakeSharedMainContract()
//                    ) }
//                composable(AppNavigation.HomeScreen.route) { Box(Modifier.testTag(AppNavigation.HomeScreen.route)) }
//            }
//        }
//        val job = launch {
//            fakeVm.effects.test {
//                composeRule.onNodeWithText("Sign up").performClick()
//                assert(awaitItem() == SignInEffect.NavigateHome)
//                cancelAndIgnoreRemainingEvents()
//            }
//        }
//        withTimeout(3_000) { job.join() }
//    }

    @Test
    fun shows_error_dialog_when_errorMessage_emitted() {
        val fakeVm = FakeSignInContract()
        lateinit var nav: TestNavHostController

        composeRule.setContent {
            val ctx = LocalContext.current
            nav = TestNavHostController(ctx).apply {
                navigatorProvider.addNavigator(ComposeNavigator())
                navigatorProvider.addNavigator(DialogNavigator())
            }
            NavHost(nav, startDestination = AppNavigation.SignInScreen.route) {
                composable(AppNavigation.SignInScreen.route) {
                    SignInScreen(
                        navController = nav,
                        contract = fakeVm,
                        sharedMainContract = FakeSharedMainContract()
                    ) }
                composable(AppNavigation.HomeScreen.route) { Box(Modifier.testTag(AppNavigation.HomeScreen.route)) }
            }
        }

        fakeVm.emitError("Some error")

        composeRule.onNodeWithText("Some error").assertIsDisplayed()
    }
}