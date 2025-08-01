package org.easydictionary.app.view.dictionary

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import org.easydictionary.app.R
import org.easydictionary.app.domain.models.language.LangType
import org.easydictionary.app.domain.models.language.Language
import org.easydictionary.app.domain.models.navigation.AppNavigation
import org.easydictionary.app.domain.viewmodels.main.SharedMainViewModel
import org.easydictionary.app.domain.viewmodels.user.dictionary.add.AddUserDictionaryViewModel
import org.easydictionary.app.domain.viewmodels.user.dictionary.add.DictionaryValidationException
import org.easydictionary.app.domain.viewmodels.user.dictionary.add.languages.LanguagesViewModel
import org.easydictionary.app.view.buttons.ButtonFilledTonalSecondary
import org.easydictionary.app.view.dialogs.ErrorAlertDialog
import org.easydictionary.app.view.inputs.TextFieldPrimary
import org.easydictionary.app.view.topbars.TitleTopBar
import org.easydictionary.app.view.widget.global.getCurrentColorScheme

@Composable
fun AddOrEditDictionaryScreen(
    backStackEntry: NavBackStackEntry,
    navController: NavController,
    viewModel: AddUserDictionaryViewModel = hiltViewModel(backStackEntry),
    sharedMainViewModel: SharedMainViewModel
) {
    var showErrorDialog by remember { mutableStateOf(false) }
    val loadingProgress by viewModel.loadingDataUI.collectAsState()
    val errorMessage by viewModel.errorUI.collectAsState()
    val selectedLanguageFrom by viewModel.selectedLanguageFrom.collectAsState()
    val selectedLanguageTo by viewModel.selectedLanguageTo.collectAsState()
    LaunchedEffect(errorMessage) {
        showErrorDialog = errorMessage.isNotEmpty()
    }
    sharedMainViewModel.loading(loadingProgress)
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
    LaunchedEffect(backStackEntry) {
        launch {
            backStackEntry.savedStateHandle.getStateFlow<String?>(
                LanguagesViewModel.BUNDLE_SELECTED_LANGUAGE_FROM, null
            ).filterNotNull().collect { json ->
                viewModel.setLanguage(LangType.FROM, json)
            }
        }
        launch {
            backStackEntry.savedStateHandle.getStateFlow<String?>(
                LanguagesViewModel.BUNDLE_SELECTED_LANGUAGE_TO, null
            ).filterNotNull().collect { json ->
                viewModel.setLanguage(LangType.TO, json)
            }
        }
    }
    LaunchedEffect(Unit) {
        viewModel.dictionaryCreated.collect { created ->
            if (created) {
                navController.popBackStack()
            }
        }
    }
    val languageFrom = remember { mutableStateOf<Language?>(null) }
    val shakeLanguageFrom = remember { mutableIntStateOf(0) }
    val shakeLanguageTo = remember { mutableIntStateOf(0) }
    val languageTo = remember { mutableStateOf<Language?>(null) }
    val dialect = remember { mutableStateOf<String?>(null) }
    Scaffold(
        topBar = {
            TitleTopBar(
                title = stringResource(R.string.add_dictionary),
                actions = {
                    IconButton(onClick = {
                        try {
                            viewModel.createDictionary(dialect.value)
                        } catch (exLanguageFrom: DictionaryValidationException.LanguageFromException) {
                            Log.e(
                                "AddOrEditDictionaryScreen",
                                "Failed to edit or create dictionary",
                                exLanguageFrom
                            )
                            shakeLanguageFrom.intValue += 1
                        } catch (exLanguageTo: DictionaryValidationException.LanguageToException) {
                            Log.e(
                                "AddOrEditDictionaryScreen",
                                "Failed to edit or create dictionary",
                                exLanguageTo
                            )
                            shakeLanguageTo.intValue += 1
                        }
                    }) {
                        Icon(
                            imageVector = Icons.Filled.Save,
                            contentDescription = "Save"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        val backgroundColor = getCurrentColorScheme().surface
        Column(
            modifier = Modifier
                .padding(
                    top = innerPadding.calculateTopPadding()
                )
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .background(backgroundColor)
        ) {
            Column(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .fillMaxSize()
            ) {
                ButtonFilledTonalSecondary(
                    title = getLanguageButtonTitle(
                        isEditMode = viewModel.isEditMode(),
                        langType = LangType.FROM,
                        selectedLanguage = selectedLanguageFrom,
                        existLanguage = languageFrom
                    ), onClick = {
                        navController.navigate(AppNavigation.LanguagesScreen.createRoute(langType = LangType.FROM))
                    }, shakeTrigger = shakeLanguageFrom
                )
                Spacer(modifier = Modifier.height(12.dp))
                ButtonFilledTonalSecondary(
                    title = getLanguageButtonTitle(
                        isEditMode = viewModel.isEditMode(),
                        langType = LangType.TO,
                        selectedLanguage = selectedLanguageTo,
                        existLanguage = languageTo
                    ), onClick = {
                        navController.navigate(AppNavigation.LanguagesScreen.createRoute(langType = LangType.TO))
                    }, shakeTrigger = shakeLanguageTo
                )
                Spacer(modifier = Modifier.height(12.dp))
                TextFieldPrimary(
                    defaultValue = "",
                    onValueChange = { newValue ->
                        {
                            dialect.value = newValue
                        }
                    },
                    label = stringResource(R.string.dialect),
                    supportingText = stringResource(R.string.optional)
                )
            }
        }
    }
}

@Composable
private fun getLanguageButtonTitle(
    isEditMode: Boolean,
    langType: LangType,
    selectedLanguage: Language? = null,
    existLanguage: State<Language?>
): String {
    if (!isEditMode) {
        return when (langType) {
            LangType.FROM -> selectedLanguage?.name
                ?: stringResource(R.string.select_language_from)

            LangType.TO -> selectedLanguage?.name ?: stringResource(R.string.select_language_to)
        }
    } else {
        return if (existLanguage.value != null) existLanguage.value!!.name else {
            return when (langType) {
                LangType.FROM -> stringResource(R.string.select_language_from)
                LangType.TO -> stringResource(R.string.select_language_to)
            }
        }
    }
}