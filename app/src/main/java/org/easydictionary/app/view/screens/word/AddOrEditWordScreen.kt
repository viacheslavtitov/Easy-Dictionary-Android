package org.easydictionary.app.view.screens.word

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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
import org.easydictionary.app.domain.models.word.WordTag
import org.easydictionary.app.domain.viewmodels.main.SharedMainContract
import org.easydictionary.app.domain.viewmodels.main.SharedMainViewModel
import org.easydictionary.app.domain.viewmodels.user.dictionary.translations.AddTranslationVariantViewModel
import org.easydictionary.app.domain.viewmodels.user.dictionary.words.add.AddDictionaryWordContract
import org.easydictionary.app.domain.viewmodels.user.dictionary.words.add.AddDictionaryWordEffect
import org.easydictionary.app.domain.viewmodels.user.dictionary.words.add.AddDictionaryWordValidationException
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
    contract: AddDictionaryWordContract = hiltViewModel<AddDictionaryWordViewModel>(
        backStackEntry,
        "AddOrEditWordScreen"
    ),
    sharedMainContract: SharedMainContract = hiltViewModel<SharedMainViewModel>(),
    dictionary: DictionaryDetailShort? = null,
    wordDetail: WordDetail? = null
) {
    val logTag = "AddOrEditWordScreen"
    val ui by contract.state.collectAsStateWithLifecycle()
    val tagsSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)
    var showTagsBottomSheet by remember { mutableStateOf(false) }
    val validationOriginalShakeFieldAnim = remember { mutableIntStateOf(0) }
    val keyboard = LocalSoftwareKeyboardController.current
    var showError by remember { mutableStateOf("") }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showEmptyTagError by remember { mutableStateOf(false) }
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
                contract.deleteWord()
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
    sharedMainContract.loading(ui.isLoading)
    LaunchedEffect(Unit) {
        launch {
            contract.effects.collect { eff ->
                when (eff) {
                    AddDictionaryWordEffect.WordUpdated, AddDictionaryWordEffect.WordCreated -> {
                        navController.previousBackStackEntry
                            ?.savedStateHandle
                            ?.set(AddDictionaryWordViewModel.BUNDLE_NEED_UPDATE_WORDS, true)
                        navController.popBackStack()
                    }

                    AddDictionaryWordEffect.WordDeleted -> {
                        navController.previousBackStackEntry
                            ?.savedStateHandle
                            ?.set(AddDictionaryWordViewModel.BUNDLE_NEED_UPDATE_WORDS, true)
                        navController.popBackStack()
                    }

                    AddDictionaryWordEffect.TagCreated -> {

                    }

                    is AddDictionaryWordEffect.ShowError -> {
                        showError = eff.message
                    }
                }
            }
        }
        contract.setDictionary(dictionary)
        contract.setWord(wordDetail)
    }
    fun onPhoneticsChanged(symbol: String) {
        contract.onPhoneticChanged(symbol)
    }

    val translationExistErrorMessage = stringResource(R.string.error_translation_exist)
    val translationsEmptyErrorMessage = stringResource(R.string.error_translations_empty)

    LaunchedEffect(backStackEntry) {
        launch {
            backStackEntry.savedStateHandle.getStateFlow<String?>(
                AddTranslationVariantViewModel.BUNDLE_NEW_TRANSLATION, null
            ).filterNotNull()
                .collect { json ->
                    val translation: TranslationNotCreated = Json.decodeFromString(json)
                    Log.d(logTag, "received new translation ${translation.translate}")
                    if (ui.translations.find { it.translate == translation.translate } != null) {
                        showError = translationExistErrorMessage
                    } else {
                        contract.addTranslation(translation)
                    }
                    backStackEntry.savedStateHandle.remove<String>(AddTranslationVariantViewModel.BUNDLE_NEW_TRANSLATION)
                }
        }
        launch {
            backStackEntry.savedStateHandle.getStateFlow<String?>(
                AddTranslationVariantViewModel.BUNDLE_NEED_UPDATE_TRANSLATION, null
            ).filterNotNull()
                .collect { json ->
                    val translation: ComposedTranslation = Json.decodeFromString(json)
                    Log.d(
                        logTag,
                        "received translation to update ${translation.translate} by id ${translation.id}"
                    )
                    contract.updateTranslation(translation)
                    backStackEntry.savedStateHandle.remove<String>(AddTranslationVariantViewModel.BUNDLE_NEED_UPDATE_TRANSLATION)
                }
        }
        launch {
            backStackEntry.savedStateHandle.getStateFlow<String?>(
                AddTranslationVariantViewModel.BUNDLE_NEED_DELETE_TRANSLATION, null
            ).filterNotNull()
                .collect { json ->
                    val translation: ComposedTranslation = Json.decodeFromString(json)
                    Log.d(logTag, "received translation to delete ${translation.translate}")
                    contract.deleteTranslation(translation)
                    backStackEntry.savedStateHandle.remove<String>(AddTranslationVariantViewModel.BUNDLE_NEED_DELETE_TRANSLATION)
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
        contract.deleteTranslation(item)
    }
    val navigateToAddTranslation: () -> Unit = {
        navController.navigate(
            AppNavigation.AddDictionaryWordTranslationsScreen.createRoute(
                dictionary!!.id
            )
        )
    }
    val navigateToAddTag: () -> Unit = {
        showTagsBottomSheet = true
    }
    val onTagClicked: (WordTag) -> Unit = { tag ->
        contract.onTagSelected(tag, !tag.selected)
    }
    Scaffold(
        floatingActionButton = {
            FabMenu(
                navigateToAddTranslation,
                navigateToAddTag
            )
        },
        topBar = {
            TitleTopBar(
                title = if (contract.isEditMode()) stringResource(R.string.edit_word) else stringResource(
                    R.string.add_word
                ),
                actions = {
                    IconButton(onClick = {
                        keyboard?.hide()
                        try {
                            if (!contract.isEditMode()) {
                                contract.createWord()
                            } else {
                                contract.updateWord()
                            }
                        } catch (ex: AddDictionaryWordValidationException.OriginalFieldException) {
                            Log.e(logTag, "Failed validation", ex)
                            validationOriginalShakeFieldAnim.intValue += 1
                        } catch (ex: AddDictionaryWordValidationException.TranslationEmptyException) {
                            Log.e(logTag, "Failed validation", ex)
                            showError = translationsEmptyErrorMessage
                        }
                    }) {
                        Icon(
                            imageVector = Icons.Filled.Save,
                            contentDescription = "Save"
                        )
                    }
                    if (contract.isEditMode()) {
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
            if (showTagsBottomSheet) {
                ModalBottomSheet(
                    onDismissRequest = {
                        showTagsBottomSheet = false
                        showEmptyTagError = false
                    },
                    sheetState = tagsSheetState,
                    contentColor = backgroundColor,
                    containerColor = backgroundColor
                ) {
                    BoxWithConstraints(Modifier.fillMaxWidth()) {
                        val minH = maxHeight / 2
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = minH),
                            content = {
                                TagsBottomSheetModal(
                                    items = ui.tags,
                                    newTagFieldErrorShow = showEmptyTagError,
                                    onAddNewClicked = {
                                        try {
                                            keyboard?.hide()
                                            showEmptyTagError = false
                                            contract.createNewTag()
                                        } catch (ex: AddDictionaryWordValidationException.TagEmptyException) {
                                            Log.e(logTag, "Failed validation", ex)
                                            showEmptyTagError = true
                                        }
                                    },
                                    onNewValueChanged = { tag ->
                                        showEmptyTagError = false
                                        contract.onNewTagChanged(tag)
                                    },
                                    onTagClicked = onTagClicked
                                )
                            }
                        )
                    }
                }
            }
            Column(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .fillMaxSize()
            ) {
                TextFieldPrimary(
                    defaultValue = ui.original ?: "",
                    onValueChange = { value -> contract.onOriginalChanged(value) },
                    required = true,
                    label = stringResource(R.string.add_word),
                    supportingText = stringResource(R.string.tap_your_word),
                    errorMessage = stringResource(R.string.field_required),
                    shakeTrigger = validationOriginalShakeFieldAnim
                )
                Spacer(modifier = Modifier.height(6.dp))
                TextFieldPhonetic(
                    symbols = ui.phonetics.map { it.symbol },
                    defaultValue = ui.phonetic ?: "",
                    onValueChange = ::onPhoneticsChanged
                )
                Spacer(modifier = Modifier.height(6.dp))
                WordTypesDropDown(
                    items = ui.wordTypes,
                    selectedItem = ui.wordType,
                    onItemSelected = {
                        contract.onTypeChanged(it)
                    })
                if (ui.tags.any { it.selected }) {
                    TextFieldLabel(
                        label = stringResource(R.string.tags),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp)
                    )
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ui.tags.filter { it.selected }.forEach { item ->
                            TagChip(
                                tag = item,
                                onTagClicked = onTagClicked
                            )
                        }
                    }
                }
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
                        items = ui.translations,
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
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable, true)
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