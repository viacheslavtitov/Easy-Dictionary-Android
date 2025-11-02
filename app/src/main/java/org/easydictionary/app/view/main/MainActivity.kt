package org.easydictionary.app.view.main

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.zIndex
import androidx.core.view.WindowCompat
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.dialog
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import coil.ImageLoader
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.serialization.json.Json
import org.easydictionary.app.data.remote.errors.GlobalErrorEvent
import org.easydictionary.app.domain.models.dictionary.DictionaryDetailShort
import org.easydictionary.app.domain.models.language.LangType
import org.easydictionary.app.domain.models.navigation.AppNavigation
import org.easydictionary.app.domain.models.translation.ComposedTranslation
import org.easydictionary.app.domain.models.word.WordDetail
import org.easydictionary.app.domain.viewmodels.main.SharedMainContract
import org.easydictionary.app.domain.viewmodels.main.SharedMainViewModel
import org.easydictionary.app.view.dictionary.AddOrEditDictionaryScreen
import org.easydictionary.app.view.dictionary.DictionariesScreen
import org.easydictionary.app.view.ext.clearStack
import org.easydictionary.app.view.home.HomeScreen
import org.easydictionary.app.view.indicators.LoadingIndicatorCircle
import org.easydictionary.app.view.language.AddNewLanguageDialogScreen
import org.easydictionary.app.view.language.BUNDLE_NEW_LANGUAGE
import org.easydictionary.app.view.language.SelectLanguageScreen
import org.easydictionary.app.view.register.SignUpScreen
import org.easydictionary.app.view.signin.SignInScreen
import org.easydictionary.app.view.splash.SplashScreen
import org.easydictionary.app.view.widget.global.EasyDictionaryTheme
import org.easydictionary.app.view.word.AddOrEditWordScreen
import org.easydictionary.app.view.word.translation.AddNewCategoryDialogScreen
import org.easydictionary.app.view.word.translation.AddOrEditWordTranslationScreen
import org.easydictionary.app.view.word.translation.BUNDLE_NEW_CATEGORY
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    companion object {
        private val TAG = MainActivity::class.simpleName
    }

    private val sharedMainContract: SharedMainContract by viewModels<SharedMainViewModel>()
    lateinit var navController: NavHostController
        private set

    @Inject
    lateinit var imageLoader: ImageLoader

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContent {
            navController = rememberNavController()
            LaunchedEffect(Unit) {
                sharedMainContract.authEvents.collect { event ->
                    Log.d(TAG, "Got global auth event $event")
                    when (event) {
                        GlobalErrorEvent.Unauthorized -> {
                            navController.currentBackStackEntryFlow
                                .first()
                            navController.navigate(AppNavigation.SignInScreen.route) {
                                clearStack()
                            }
                        }
                    }
                }
            }
            EasyDictionaryTheme {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
//                        .padding(WindowInsets.systemBars.asPaddingValues())
                ) {
                    AppNavHost(navController)
                    val loadingState by sharedMainContract.loadingUIState
                    if (loadingState) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(alpha = 0.3f))
                                .zIndex(1f), // always in the top
                            contentAlignment = Alignment.Center
                        ) {
                            LoadingIndicatorCircle()
                        }
                    }
                }
            }
        }
    }

    @Composable
    fun AppNavHost(navController: NavHostController) {
        NavHost(
            navController = navController,
            startDestination = AppNavigation.SplashScreen.route
        ) {
            composable(route = AppNavigation.SplashScreen.route) {
                SplashScreen(navController)
            }
            composable(route = AppNavigation.SignInScreen.route) {
                SignInScreen(navController, sharedMainContract = sharedMainContract)
            }
            composable(route = AppNavigation.SignUpScreen.route) {
                SignUpScreen(navController, sharedMainContract = sharedMainContract)
            }
            composable(route = AppNavigation.HomeScreen.route) { backStackEntry ->
                HomeScreen(backStackEntry, navController, sharedMainContract = sharedMainContract)
            }
            composable(route = AppNavigation.DictionariesScreen.route) { backStackEntry ->
                DictionariesScreen(backStackEntry, navController, sharedMainContract = sharedMainContract)
            }
            composable(route = AppNavigation.AddUserDictionaryScreen.route) { backStackEntry ->
                AddOrEditDictionaryScreen(
                    backStackEntry,
                    navController,
                    sharedMainContract = sharedMainContract
                )
            }
            composable(
                route = AppNavigation.EditDictionaryScreen.route,
                arguments = listOf(navArgument("dictionary") { type = NavType.StringType })
            ) { backStackEntry ->
                val dictJson = backStackEntry.arguments?.getString("dictionary") ?: ""
                val dictionary = Json.decodeFromString<DictionaryDetailShort>(dictJson)
                AddOrEditDictionaryScreen(
                    backStackEntry,
                    navController,
                    sharedMainContract = sharedMainContract,
                    editDictionary = dictionary
                )
            }
            composable(
                route = AppNavigation.EditDictionaryWordScreen.route,
                arguments = listOf(
                    navArgument("word") { type = NavType.StringType },
                    navArgument("dictionary") { type = NavType.StringType })
            ) { backStackEntry ->
                val wordJson = backStackEntry.arguments?.getString("word") ?: ""
                val dictionaryJson = backStackEntry.arguments?.getString("dictionary") ?: ""
                val word = Json.decodeFromString<WordDetail>(wordJson)
                val dictionary = Json.decodeFromString<DictionaryDetailShort>(dictionaryJson)
                AddOrEditWordScreen(
                    backStackEntry,
                    navController,
                    sharedMainContract = sharedMainContract,
                    dictionary = dictionary,
                    wordDetail = word
                )
            }
            composable(
                AppNavigation.LanguagesScreen.route,
                arguments = listOf(navArgument("langType") { type = NavType.IntType })
            ) { backStackEntry ->
                val langType = backStackEntry.arguments?.getInt("langType")
                LangType.fromInt(langType)?.let {
                    SelectLanguageScreen(
                        backStackEntry,
                        navController,
                        sharedMainContract = sharedMainContract,
                        langType = it,
                        imageLoader = imageLoader
                    )
                }
            }
            dialog(AppNavigation.AddNewLanguageScreen.route) { backStackEntry ->
                AddNewLanguageDialogScreen(
                    onDismiss = {
                        navController.popBackStack()
                    },
                    onConfirm = { value ->
                        navController.previousBackStackEntry
                            ?.savedStateHandle
                            ?.set(BUNDLE_NEW_LANGUAGE, value)
                        navController.popBackStack()
                    }
                )
            }
            composable(
                route = AppNavigation.AddDictionaryWordScreen.route,
                arguments = listOf(navArgument("dictionary") { type = NavType.StringType })
            ) { backStackEntry ->
                val dictJson = backStackEntry.arguments?.getString("dictionary") ?: ""
                val dictionary = Json.decodeFromString<DictionaryDetailShort>(dictJson)
                AddOrEditWordScreen(
                    backStackEntry,
                    navController,
                    sharedMainContract = sharedMainContract,
                    dictionary = dictionary
                )
            }
            composable(
                route = AppNavigation.AddDictionaryWordTranslationsScreen.route,
                arguments = listOf(navArgument("dictionaryId") { type = NavType.IntType })
            ) { backStackEntry ->
                val dictionaryId = backStackEntry.arguments?.getInt("dictionaryId") ?: -1
                AddOrEditWordTranslationScreen(
                    backStackEntry,
                    navController,
                    dictionaryId = dictionaryId,
                    sharedMainContract = sharedMainContract
                )
            }
            composable(
                route = AppNavigation.EditDictionaryWordTranslationsScreen.route,
                arguments = listOf(
                    navArgument("dictionaryId") { type = NavType.IntType },
                    navArgument("translation") { type = NavType.StringType })
            ) { backStackEntry ->
                val dictJson = backStackEntry.arguments?.getString("translation") ?: ""
                val dictionaryId = backStackEntry.arguments?.getInt("dictionaryId") ?: -1
                val translation = Json.decodeFromString<ComposedTranslation>(dictJson)
                AddOrEditWordTranslationScreen(
                    backStackEntry,
                    navController,
                    sharedMainContract = sharedMainContract,
                    dictionaryId = dictionaryId,
                    editTranslation = translation
                )
            }
            dialog(AppNavigation.AddNewCategoryScreen.route) { backStackEntry ->
                AddNewCategoryDialogScreen(
                    onDismiss = {
                        navController.popBackStack()
                    },
                    onConfirm = { value ->
                        navController.previousBackStackEntry
                            ?.savedStateHandle
                            ?.set(BUNDLE_NEW_CATEGORY, value)
                        navController.popBackStack()
                    }
                )
            }
        }
    }
}