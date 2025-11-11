package org.easydictionary.app.view.screens.dictionary

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.Alignment
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
import org.easydictionary.app.view.dividers.Divider
import org.easydictionary.app.view.inputs.TextFieldPrimary
import org.easydictionary.app.view.texts.Secondary2TextFieldLabel
import org.easydictionary.app.view.texts.TextFieldLabel
import org.easydictionary.app.view.topbars.SearchTopBar
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
    sharedMainContract: SharedMainContract = androidx.hilt.navigation.compose.hiltViewModel<SharedMainViewModel>(),
    editDictionary: DictionaryDetailShort? = null
) {
    val ui by contract.state.collectAsStateWithLifecycle()
    sharedMainContract.loading(ui.isLoading)
    var showError by rememberSaveable { mutableStateOf("") }
    var showDeleteDialog by rememberSaveable { mutableStateOf(false) }
    var isSearching by rememberSaveable { mutableStateOf(false) }
    val listState = rememberLazyListState()
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    var showPhonetics by rememberSaveable { mutableStateOf(true) }

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
    val shouldLoadMore by remember {
        derivedStateOf {
            val layoutInfo = listState.layoutInfo
            val total = layoutInfo.totalItemsCount
            val lastVisible = layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            lastVisible >= total - 1 - 5
        }
    }
    LaunchedEffect(listState, ui.isLoading) {
        snapshotFlow { shouldLoadMore }
            .distinctUntilChanged()
            .filter { it && !ui.isLoading }
            .collect {
                if (ui.query.isNullOrEmpty()) {
                    contract.loadWords()
                } else {
                    contract.onQueryChanged(ui.query, true)
                }
            }
    }
    LaunchedEffect(Unit) {
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
    contract.setEditMode(editDictionary)
    sharedMainContract.loading(ui.isLoading)

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
                    contract.loadWords()
                }
            }
        }
    }
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
    val title =
        if (!contract.isEditMode()) stringResource(R.string.add_dictionary) else stringResource(
            R.string.edit_dictionary
        )
    Scaffold(
        floatingActionButton = {
            if (contract.isEditMode()) {
                ExtendedFloatingActionButton(
                    text = { Text(stringResource(R.string.add_words)) },
                    icon = { Icon(Icons.Filled.Add, contentDescription = "Add") },
                    onClick = {
                        editDictionary?.let { dictionary ->
                            navController.navigate(
                                AppNavigation.AddDictionaryWordScreen.createRoute(dictionary)
                            )
                        }
                    }
                )
            }
        },
        topBar = {
            if (contract.isEditMode()) {
                SearchTopBar(
                    title = title,
                    placeHolderText = stringResource(R.string.words_search_hint),
                    query = ui.query ?: "",
                    onQueryChange = { contract.onQueryChanged(it, false) },
                    isSearching = isSearching,
                    onSearchToggle = {
                        isSearching = true

                    },
                    onClearQuery = {
                        contract.onQueryChanged(null, false)
                        isSearching = false
                    },
                    actions = {
                        ToolBarActions(
                            contract.isEditMode(),
                            onCreateDictionary,
                            onUpdateDictionary,
                            onDeleteDictionary
                        )
                    },
                    scrollBehavior = scrollBehavior
                )
            } else {
                TitleTopBar(
                    title = title,
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
                .nestedScroll(scrollBehavior.nestedScrollConnection)
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
                if (contract.isEditMode()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                            .clickable {
                                showPhonetics = !showPhonetics
                            }, verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextFieldLabel(
                            stringResource(R.string.show_phonetics),
                            Modifier
                                .fillMaxWidth()
                                .weight(1.0.toFloat())
                        )
                        Checkbox(
                            checked = showPhonetics,
                            onCheckedChange = { showPhonetics = it }
                        )
                    }
                }
            }
            items(
                items = ui.words,
                key = { it.original + it.id }
            ) { item ->
                WordListItem(item, showPhonetics, onSelectWord)
            }
        }
    }
}

@Composable
private fun WordListItem(
    word: WordDetail,
    showPhonetics: Boolean,
    onSelect: (WordDetail) -> Unit
) {
    val backgroundColor = getCurrentColorScheme().secondaryContainer
    var translations = ""
    word.translations.forEachIndexed { index, item ->
        translations += if (index == word.translations.size - 1) {
            " ${item.translate}"
        } else {
            " ${item.translate},"
        }
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(backgroundColor)
            .clickable {
                onSelect(word)
            },
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically
        ) {
//            if (BuildConfig.DEBUG) {
//                TextFieldLabel("${word.id} ${word.original}", Modifier.wrapContentSize())
//            } else {
            TextFieldLabel(word.original, Modifier.wrapContentSize())
//            }
            if (word.phonetic?.isNotEmpty() == true && showPhonetics) {
                Secondary2TextFieldLabel(
                    label = " - [${word.phonetic}]",
                    modifier = Modifier.wrapContentWidth()
                )
                Secondary2TextFieldLabel(
                    label = " $translations",
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            } else {
                Secondary2TextFieldLabel(
                    label = " - $translations",
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        }
    }
    Divider()
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

@Composable
private fun ToolBarActions(
    isEditMode: Boolean,
    onCreateDictionaryClick: () -> Unit,
    onUpdateDictionaryClick: () -> Unit,
    onDeleteDictionaryClick: () -> Unit,
) {
    IconButton(onClick = {
        if (!isEditMode) {
            onCreateDictionaryClick()
        } else {
            onUpdateDictionaryClick()
        }
    }) {
        Icon(
            imageVector = Icons.Filled.Save,
            contentDescription = "Save"
        )
    }
    if (isEditMode) {
        IconButton(onClick = {
            onDeleteDictionaryClick()
        }) {
            Icon(
                imageVector = Icons.Filled.Delete,
                contentDescription = "Delete"
            )
        }
    }
}