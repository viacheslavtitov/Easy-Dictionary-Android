package org.easydictionary.app.view.word.translation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LibraryAdd
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import org.easydictionary.app.R
import org.easydictionary.app.domain.models.category.Category
import org.easydictionary.app.domain.models.navigation.AppNavigation
import org.easydictionary.app.domain.models.translation.ComposedTranslation
import org.easydictionary.app.domain.models.translation.TranslationNotCreated
import org.easydictionary.app.domain.viewmodels.main.SharedMainContract
import org.easydictionary.app.domain.viewmodels.main.SharedMainViewModel
import org.easydictionary.app.domain.viewmodels.user.dictionary.translations.AddTranslationVariantViewModel
import org.easydictionary.app.domain.viewmodels.user.dictionary.words.add.AddDictionaryWordViewModel
import org.easydictionary.app.view.dialogs.ButtonsAlertDialog
import org.easydictionary.app.view.dialogs.ErrorAlertDialog
import org.easydictionary.app.view.inputs.TextFieldPrimary
import org.easydictionary.app.view.texts.TextFieldLabel
import org.easydictionary.app.view.topbars.TitleTopBar
import org.easydictionary.app.view.widget.global.getCurrentColorScheme

@Composable
fun AddOrEditWordTranslationScreen(
    backStackEntry: NavBackStackEntry,
    navController: NavController,
    viewModel: AddTranslationVariantViewModel = hiltViewModel(backStackEntry),
    sharedMainContract: SharedMainContract = hiltViewModel<SharedMainViewModel>(),
    dictionaryId: Int,
    editTranslation: ComposedTranslation? = null
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    val loadingProgress by viewModel.loadingDataUI.collectAsState()
    var showError by remember { mutableStateOf("") }
    val categories by viewModel.categories.collectAsStateWithLifecycle()
    var translationValue by remember { mutableStateOf(editTranslation?.translate ?: "") }
    var descriptionValue by remember { mutableStateOf(editTranslation?.description ?: "") }
    var selectedCategory by remember { mutableStateOf<Category?>(editTranslation?.category) }
    sharedMainContract.loading(loadingProgress)
    LaunchedEffect(backStackEntry) {
        launch {
            backStackEntry.savedStateHandle.getStateFlow<String?>(
                BUNDLE_NEW_CATEGORY, null
            ).filterNotNull().collect { newLanguage ->
                viewModel.createCategory(newLanguage)
            }
        }
    }
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
            title = stringResource(R.string.delete_translation_title),
            icon = Icons.Default.Info
        )
    }
    LaunchedEffect(dictionaryId) {
        viewModel.setDictionaryId(dictionaryId)
        viewModel.setEditModel(editTranslation)
        viewModel.loadCategories()
    }
    LaunchedEffect(Unit) {
        launch {
            viewModel.translationUpdated.collect { entity ->
                navController.previousBackStackEntry
                    ?.savedStateHandle
                    ?.set(
                        AddTranslationVariantViewModel.BUNDLE_NEED_UPDATE_TRANSLATION,
                        entity.toJson()
                    )
                navController.popBackStack()
            }
        }
        launch {
            viewModel.errorMessage.collect { msg ->
                showError = msg
            }
        }
        launch {
            viewModel.translationDeleted.collect { deleted ->
                if (deleted) {
                    navController.previousBackStackEntry
                        ?.savedStateHandle
                        ?.set(
                            AddTranslationVariantViewModel.BUNDLE_NEED_DELETE_TRANSLATION,
                            editTranslation?.toJson()
                        )
                    navController.popBackStack()
                }
            }
        }
    }
    Scaffold(
        topBar = {
            TitleTopBar(
                title = if (viewModel.isEditMode()) stringResource(R.string.edit_translation) else stringResource(
                    R.string.add_translation
                ),
                actions = {
                    IconButton(onClick = {
                        if (!viewModel.isEditMode() && translationValue.isNotEmpty()) {
                            val translation = TranslationNotCreated(
                                translate = translationValue,
                                description = descriptionValue,
                                category = selectedCategory
                            )
                            navController.previousBackStackEntry
                                ?.savedStateHandle
                                ?.set(
                                    AddTranslationVariantViewModel.BUNDLE_NEW_TRANSLATION,
                                    translation.toJson()
                                )
                            navController.popBackStack()
                        } else {
                            viewModel.editTranslation(
                                translate = translationValue,
                                description = descriptionValue,
                                category = selectedCategory
                            )
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
                .verticalScroll(rememberScrollState())
                .background(backgroundColor)
        ) {
            Column(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .fillMaxSize()
            ) {
                TextFieldPrimary(
                    defaultValue = translationValue,
                    onValueChange = { value -> translationValue = value },
                    required = true,
                    label = stringResource(R.string.tap_your_translation),
                    errorMessage = stringResource(R.string.field_required)
                )
                TextFieldPrimary(
                    defaultValue = descriptionValue,
                    onValueChange = { value -> descriptionValue = value },
                    required = false,
                    label = stringResource(R.string.tap_to_add_example),
                    supportingText = stringResource(R.string.optional)
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(6.dp)
                        .clickable {
                            navController.navigate(AppNavigation.AddNewCategoryScreen.route)
                        },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextFieldLabel(
                        label = stringResource(R.string.select_category_optional),
                        modifier = Modifier.weight(1f)
                    )
                    Icon(
                        imageVector = Icons.Filled.LibraryAdd,
                        contentDescription = "Add category"
                    )
                }
                CategoryDropDown(
                    items = categories,
                    selectedItem = selectedCategory,
                    onItemSelected = {
                        selectedCategory = it
                    })
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryDropDown(
    items: List<Category>,
    selectedItem: Category?,
    onItemSelected: (Category) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = Modifier.fillMaxWidth()
    ) {
        TextField(
            value = selectedItem?.name ?: stringResource(R.string.select_category),
            onValueChange = {},
            readOnly = true,
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.fillMaxWidth()
        ) {
            items.forEach { item ->
                DropdownMenuItem(
                    text = { Text(item.name, modifier = Modifier.fillMaxWidth()) },
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
