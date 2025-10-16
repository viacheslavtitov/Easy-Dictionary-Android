package org.easydictionary.app.view.word

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import org.easydictionary.app.R
import org.easydictionary.app.domain.models.dictionary.DictionaryDetailShort
import org.easydictionary.app.domain.models.navigation.AppNavigation
import org.easydictionary.app.domain.models.translation.ComposedTranslation
import org.easydictionary.app.domain.models.translation.TranslationNotCreated
import org.easydictionary.app.domain.models.word.WordDetail
import org.easydictionary.app.domain.viewmodels.main.SharedMainViewModel
import org.easydictionary.app.domain.viewmodels.user.dictionary.translations.AddTranslationVariantViewModel
import org.easydictionary.app.domain.viewmodels.user.dictionary.words.add.AddDictionaryWordViewModel
import org.easydictionary.app.view.dialogs.ButtonsAlertDialog
import org.easydictionary.app.view.dialogs.ErrorAlertDialog
import org.easydictionary.app.view.dividers.Divider
import org.easydictionary.app.view.inputs.TextFieldPhonetic
import org.easydictionary.app.view.inputs.TextFieldPrimary
import org.easydictionary.app.view.texts.SecondaryTextFieldLabel
import org.easydictionary.app.view.texts.TextFieldLabel
import org.easydictionary.app.view.topbars.TitleTopBar
import org.easydictionary.app.view.widget.global.TextDimen
import org.easydictionary.app.view.widget.global.categoryLight
import org.easydictionary.app.view.widget.global.getCurrentColorScheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddOrEditWordScreen(
    backStackEntry: NavBackStackEntry,
    navController: NavController,
    viewModel: AddDictionaryWordViewModel = hiltViewModel(backStackEntry),
    sharedMainViewModel: SharedMainViewModel,
    dictionary: DictionaryDetailShort? = null,
    wordDetail: WordDetail? = null
) {
    var showError by remember { mutableStateOf("") }
    var showDeleteDialog by remember { mutableStateOf(false) }
    val loadingProgress by viewModel.loadingDataUI.collectAsState()
    val translations by viewModel.translations.collectAsState()
    val wordTypes by viewModel.wordTypes.collectAsState()
    val phonetics by viewModel.phonetics.collectAsState()
    var wordValue by rememberSaveable { mutableStateOf(wordDetail?.original ?: "") }
    var phonetic by rememberSaveable { mutableStateOf(wordDetail?.phonetic ?: "") }
    var selectedWordType by rememberSaveable { mutableStateOf(wordDetail?.type) }
    LaunchedEffect(Unit) {
        launch {
            viewModel.errorMessage.collect { msg ->
                showError = msg
            }
        }
        launch {
            viewModel.wordCreated.collect { created ->
                if (created) {
                    navController.previousBackStackEntry
                        ?.savedStateHandle
                        ?.set(AddDictionaryWordViewModel.BUNDLE_NEED_UPDATE_WORDS, true)
                    navController.popBackStack()
                }
            }
        }
        launch {
            viewModel.wordDeleted.collect { deleted ->
                if (deleted) {
                    navController.previousBackStackEntry
                        ?.savedStateHandle
                        ?.set(AddDictionaryWordViewModel.BUNDLE_NEED_UPDATE_WORDS, true)
                    navController.popBackStack()
                }
            }
        }
        viewModel.loadWordTypes()
        viewModel.setDictionary(dictionary)
        viewModel.setWord(wordDetail)
    }
    fun onPhoneticsChanged(symbol: String) {
        phonetic = symbol
    }

    val translationExistErrorMessage = stringResource(R.string.error_translation_exist)
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
    if (showDeleteDialog) {
        ButtonsAlertDialog(
            onConfirmation = {
                viewModel.delete()
                showDeleteDialog = false
            },
            onDismissRequest = {
                showDeleteDialog = false
            },
            message = stringResource(R.string.are_you_sure),
            title = stringResource(R.string.delete_word_title),
            icon = Icons.Default.Info
        )
    }
    LaunchedEffect(backStackEntry) {
        launch {
            backStackEntry.savedStateHandle.getStateFlow<String?>(
                AddTranslationVariantViewModel.BUNDLE_NEW_TRANSLATION, null
            ).filterNotNull()
                .collect { json ->
                    val translation: TranslationNotCreated = Json.decodeFromString(json)
                    if (translations.find { it.translate == translation.translate } != null) {
                        viewModel.displayError(translationExistErrorMessage)
                    } else {
                        viewModel.addTranslation(translation)
                    }
                    backStackEntry.savedStateHandle.remove<String>(AddTranslationVariantViewModel.BUNDLE_NEW_TRANSLATION)
                }
        }
    }
    val onEditTranslation: (ComposedTranslation) -> Unit = { item ->
        navController.navigate(
            AppNavigation.EditDictionaryWordTranslationsScreen.createRoute(
                item,
                dictionary!!.id
            )
        )
    }
    val onDeleteTranslation: (ComposedTranslation) -> Unit = { item ->
        viewModel.deleteTranslation(item)
    }
    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(
                text = { Text(stringResource(R.string.add_translation)) },
                icon = { Icon(Icons.Filled.Add, contentDescription = "Add Translation") },
                onClick = {
                    navController.navigate(
                        AppNavigation.AddDictionaryWordTranslationsScreen.createRoute(
                            dictionary!!.id
                        )
                    )
                }
            )
        },
        topBar = {
            TitleTopBar(
                title = if (viewModel.isEditMode()) stringResource(R.string.edit_word) else stringResource(
                    R.string.add_word
                ),
                actions = {
                    IconButton(onClick = {
                        if (!viewModel.isEditMode()) {
                            viewModel.createWord(
                                original = wordValue,
                                phonetic = phonetic,
                                type = selectedWordType
                            )
                        } else {

                        }
                    }) {
                        Icon(
                            imageVector = Icons.Filled.Save,
                            contentDescription = "Save"
                        )
                    }
                    if (viewModel.isEditMode()) {
                        IconButton(onClick = {
                            showDeleteDialog = true
                        }) {
                            Icon(
                                imageVector = Icons.Filled.Delete,
                                contentDescription = "Delete"
                            )
                        }
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
                .background(backgroundColor)
        ) {
            Column(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .fillMaxSize()
            ) {
                TextFieldPrimary(
                    defaultValue = wordValue,
                    onValueChange = { value -> wordValue = value },
                    required = true,
                    label = stringResource(R.string.add_word),
                    supportingText = stringResource(R.string.tap_your_word),
                    errorMessage = stringResource(R.string.field_required)
                )
                Spacer(modifier = Modifier.height(6.dp))
                TextFieldPhonetic(
                    symbols = phonetics.map { it.symbol },
                    defaultValue = phonetic,
                    onValueChange = ::onPhoneticsChanged
                )
                Spacer(modifier = Modifier.height(6.dp))
                WordTypesDropDown(
                    items = wordTypes,
                    selectedItem = selectedWordType,
                    onItemSelected = {
                        selectedWordType = it
                    })
                TextFieldLabel(
                    label = stringResource(R.string.translation_variants),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                )
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                ) {
                    items(
                        items = translations,
                        key = { "${it.id}-${it.translate}" }
                    ) { item ->
                        TranslationListItem(item, onEditTranslation, onDeleteTranslation)
                    }
                }
            }
        }
    }
}

@Composable
private fun TranslationListItem(
    translation: ComposedTranslation,
    onEdit: (ComposedTranslation) -> Unit,
    onDelete: (ComposedTranslation) -> Unit
) {
    val backgroundColor = getCurrentColorScheme().outlineVariant
    val categoryColor = if (isSystemInDarkTheme()) categoryLight else categoryLight
    Row(
        modifier = Modifier
            .background(backgroundColor)
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
                .weight(1f),
            verticalArrangement = Arrangement.Center
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                var translateText = ""
                if (translation.category != null) {
                    Text(
                        text = translation.category.name,
                        modifier = Modifier.wrapContentSize(),
                        fontSize = TextDimen.TextFieldText,
                        color = categoryColor
                    )
                    translateText += " - "
                }
                translateText += translation.translate
                TextFieldLabel(
                    modifier = Modifier.fillMaxWidth(),
                    label = translateText
                )
            }
            if (translation.description?.isNotEmpty() == true) {
                SecondaryTextFieldLabel(
                    modifier = Modifier.fillMaxWidth(),
                    label = translation.description
                )
            }
        }
        Column(
            modifier = Modifier
                .padding(horizontal = 9.dp)
                .clickable {
                    onEdit(translation)
                },
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Filled.Edit,
                tint = getCurrentColorScheme().primary,
                contentDescription = "Edit"
            )
        }
        Column(
            modifier = Modifier
                .padding(horizontal = 9.dp)
                .clickable {
                    onDelete(translation)
                },
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Filled.Delete,
                tint = getCurrentColorScheme().error,
                contentDescription = "Delete"
            )
        }
    }
    Divider()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WordTypesDropDown(
    items: List<String>,
    selectedItem: String?,
    onItemSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = Modifier.fillMaxWidth()
    ) {
        TextField(
            value = selectedItem ?: stringResource(R.string.select_word_type),
            onValueChange = {},
            readOnly = true,
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
            modifier = Modifier
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable, false)
                .fillMaxWidth()
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.fillMaxWidth()
        ) {
            items.forEach { item ->
                DropdownMenuItem(
                    text = { Text(item, modifier = Modifier.fillMaxWidth()) },
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        onItemSelected(item)
                        expanded = false
                    }
                )
            }
        }
    }
}