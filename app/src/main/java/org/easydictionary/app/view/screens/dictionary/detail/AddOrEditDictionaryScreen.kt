package org.easydictionary.app.view.screens.dictionary.detail

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import org.easydictionary.app.R
import org.easydictionary.app.domain.models.dictionary.DictionaryDetailShort
import org.easydictionary.app.domain.models.language.LangType
import org.easydictionary.app.domain.models.language.Language
import org.easydictionary.app.domain.models.navigation.AppNavigation
import org.easydictionary.app.domain.models.word.WordDetail
import org.easydictionary.app.domain.viewmodels.main.SharedMainContract
import org.easydictionary.app.domain.viewmodels.main.SharedMainViewModel
import org.easydictionary.app.domain.viewmodels.user.dictionary.UserDictionaryViewModel
import org.easydictionary.app.domain.viewmodels.user.dictionary.add.AddOrEditUserDictionaryContract
import org.easydictionary.app.domain.viewmodels.user.dictionary.add.AddOrEditUserDictionaryEffect
import org.easydictionary.app.domain.viewmodels.user.dictionary.add.AddUserDictionaryViewModel
import org.easydictionary.app.domain.viewmodels.user.dictionary.add.DictionaryValidationException
import org.easydictionary.app.domain.viewmodels.user.dictionary.add.languages.LanguagesViewModel
import org.easydictionary.app.domain.viewmodels.user.dictionary.words.add.AddDictionaryWordViewModel
import org.easydictionary.app.view.buttons.ButtonFilledTonalSecondary
import org.easydictionary.app.view.dialogs.ButtonsAlertDialog
import org.easydictionary.app.view.dialogs.ErrorAlertDialog
import org.easydictionary.app.view.inputs.TextFieldPrimary
import org.easydictionary.app.view.pickers.DateRangePickerDialog
import org.easydictionary.app.view.topbars.FilterableSearchTopBar
import org.easydictionary.app.view.topbars.TitleTopBar
import org.easydictionary.app.view.widget.global.getCurrentColorScheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddOrEditDictionaryScreen(
    backStackEntry: NavBackStackEntry,
    navController: NavController,
    contract: AddOrEditUserDictionaryContract = hiltViewModel<AddUserDictionaryViewModel>(
        backStackEntry,
        "AddOrEditDictionaryScreen"
    ),
    sharedMainContract: SharedMainContract = hiltViewModel<SharedMainViewModel>(),
    editDictionary: DictionaryDetailShort? = null
) {
    val ui by contract.state.collectAsStateWithLifecycle()
    var showError by rememberSaveable { mutableStateOf("") }
    var showDateRangePicker by remember { mutableStateOf(false) }
    var showDeleteDialog by rememberSaveable { mutableStateOf(false) }
    val isSearchingState = rememberSaveable { mutableStateOf(false) }
    val isSearching by isSearchingState
    val listState = rememberLazyListState()
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
    if (showDeleteDialog) {
        ButtonsAlertDialog(
            onConfirmation = {
                contract.deleteDictionary()
                showDeleteDialog = false
            },
            onDismissRequest = {
                showDeleteDialog = false
            },
            message = stringResource(R.string.are_you_sure),
            title = stringResource(R.string.delete_dictionary_title),
            icon = Icons.Default.Info
        )
    }
    if (showDateRangePicker) {
        DateRangePickerDialog(
            initialStart = contract.getFilterDateFromInMillis(),
            initialEnd = contract.getFilterDateToInMillis(),
            formatter = contract.getDateRangeFormatter(),
            onDismiss = { showDateRangePicker = false },
            onDateRangeSelected = { start, end ->
                contract.onFilterDateRangeFromChanged(start)
                contract.onFilterDateRangeToChanged(end)
            }
        )
    }
    val shouldLoadMore by remember {
        derivedStateOf {
            val layoutInfo = listState.layoutInfo
            val total = layoutInfo.totalItemsCount
            val lastVisible = layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            lastVisible >= total - 1 - 5
        }
    }
    val isDateRangeFilled by remember {
        derivedStateOf {
            ui.filter.dateFrom?.isNotEmpty() == true && ui.filter.dateTo?.isNotEmpty() == true
        }
    }
    LaunchedEffect(listState, ui.isLoading) {
        snapshotFlow { shouldLoadMore }
            .distinctUntilChanged()
            .filter { it && !ui.isLoading }
            .collect {
                contract.loadWords(ui.filter.query)
            }
    }
    LaunchedEffect(Unit) {
        contract.setEditMode(editDictionary)
        launch {
            contract.effects.collect { eff ->
                when (eff) {
                    AddOrEditUserDictionaryEffect.DictionaryCreated -> {
                        navController.previousBackStackEntry
                            ?.savedStateHandle
                            ?.set(UserDictionaryViewModel.BUNDLE_NEED_UPDATE_DICTIONARIES, true)
                        navController.popBackStack()
                    }

                    is AddOrEditUserDictionaryEffect.ShowError -> {
                        showError = eff.message
                    }
                }
            }
        }
    }

    LaunchedEffect(backStackEntry) {
        launch {
            backStackEntry.savedStateHandle.getStateFlow<String?>(
                LanguagesViewModel.BUNDLE_SELECTED_LANGUAGE_FROM, null
            ).filterNotNull().collect { json ->
                contract.setLanguage(LangType.FROM, json)
            }
        }
        launch {
            backStackEntry.savedStateHandle.getStateFlow<String?>(
                LanguagesViewModel.BUNDLE_SELECTED_LANGUAGE_TO, null
            ).filterNotNull().collect { json ->
                contract.setLanguage(LangType.TO, json)
            }
        }
        launch {
            backStackEntry.savedStateHandle.getStateFlow<Boolean>(
                AddDictionaryWordViewModel.BUNDLE_NEED_UPDATE_WORDS, false
            ).filterNotNull().collect { shouldUpdate ->
                if (shouldUpdate) {
                    contract.loadWords(ui.filter.query)
                }
            }
        }
    }
    sharedMainContract.loading(ui.isLoading)
    val shakeLanguageFrom = remember { mutableIntStateOf(0) }
    val shakeLanguageTo = remember { mutableIntStateOf(0) }
    val dialect = rememberSaveable { mutableStateOf<String?>(null) }
    val onSelectWord: (WordDetail) -> Unit = { item ->
        editDictionary?.let { dict ->
            navController.navigate(
                AppNavigation.EditDictionaryWordScreen.createRoute(dict, item)
            )
        }
    }
    val onCreateDictionary: () -> Unit = {
        try {
            contract.createDictionary()
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
    }
    val onUpdateDictionary: () -> Unit = {
        contract.updateDictionary()
    }
    val onDeleteDictionary: () -> Unit = {
        showDeleteDialog = true
    }
    val onSearchSubmit: (String) -> Unit = { query ->
        contract.loadWords(query = query)
    }
    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing,
        floatingActionButton = {
            if (contract.isEditMode()) {
                AnimatedVisibility(visible = !isSearching) {
                    ExtendedFloatingActionButton(
                        text = { Text(stringResource(R.string.add_words)) },
                        icon = { Icon(Icons.Filled.Add, contentDescription = "Add") },
                        onClick = {
                            contract.getDictionary()?.let { dictionary ->
                                navController.navigate(
                                    AppNavigation.AddDictionaryWordScreen.createRoute(dictionary)
                                )
                            }
                        }
                    )
                }
            }
        },
        topBar = {
            if (contract.isEditMode()) {
                FilterableSearchTopBar(
                    isSearchingEnabled = isSearchingState,
                    onSearchSubmit = onSearchSubmit,
                    actions = {
                        ToolBarActions(
                            contract.isEditMode(),
                            onCreateDictionary,
                            onUpdateDictionary,
                            onDeleteDictionary
                        )
                    },
                    onClearClicked = {
                        contract.onFilterClearClicked()
                    },
                    onDateRangeClicked = {
                        showDateRangePicker = true
                    },
                    isDateRangeFilled = isDateRangeFilled,
                    expandedContent = {
                        FilterContent(
                            tags = ui.filter.tags,
                            categories = ui.filter.categories,
                            types = ui.filter.wordTypes,
                            onTagClicked = {
                                contract.onFilterTagClicked(it)
                            },
                            onTypeClicked = {
                                contract.onFilterWordTypeClicked(it)
                            },
                            onCategoryClicked = {
                                contract.onFilterCategoryClicked(it)
                            }
                        )
                    }
                )
            } else {
                TitleTopBar(
                    title = stringResource(R.string.add_dictionary),
                    actions = {
                        ToolBarActions(
                            contract.isEditMode(),
                            onCreateDictionary,
                            onUpdateDictionary,
                            onDeleteDictionary
                        )
                    }
                )
            }
        }
    ) { innerPadding ->
        val backgroundColor = getCurrentColorScheme().surface
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    bottom = innerPadding.calculateBottomPadding(),
                    top = innerPadding.calculateTopPadding()
                )
                .background(backgroundColor)
        ) {
            item {
                Column(
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .wrapContentSize()
                ) {
                    ButtonFilledTonalSecondary(
                        enabled = !contract.isEditMode(),
                        title = getLanguageButtonTitle(
                            langType = LangType.FROM,
                            selectedLanguage = ui.selectedLanguageFrom
                        ), onClick = {
                            navController.navigate(
                                AppNavigation.LanguagesScreen.createRoute(
                                    langType = LangType.FROM
                                )
                            )
                        }, shakeTrigger = shakeLanguageFrom
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    ButtonFilledTonalSecondary(
                        enabled = !contract.isEditMode(),
                        title = getLanguageButtonTitle(
                            langType = LangType.TO,
                            selectedLanguage = ui.selectedLanguageTo
                        ), onClick = {
                            navController.navigate(
                                AppNavigation.LanguagesScreen.createRoute(
                                    langType = LangType.TO
                                )
                            )
                        }, shakeTrigger = shakeLanguageTo
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    TextFieldPrimary(
                        defaultValue = ui.dialect ?: "",
                        onValueChange = { newValue ->
                            dialect.value = newValue
                        },
                        label = stringResource(R.string.dialect),
                        supportingText = stringResource(R.string.optional)
                    )
                }
            }
            items(
                items = ui.words,
                key = { it.original + it.id }
            ) { item ->
                WordListItem(item, onSelectWord)
            }
        }
    }
}

@Composable
private fun getLanguageButtonTitle(
    langType: LangType,
    selectedLanguage: Language? = null,
): String {
    return when (langType) {
        LangType.FROM -> selectedLanguage?.name
            ?: stringResource(R.string.select_language_from)

        LangType.TO -> selectedLanguage?.name ?: stringResource(R.string.select_language_to)
    }
}