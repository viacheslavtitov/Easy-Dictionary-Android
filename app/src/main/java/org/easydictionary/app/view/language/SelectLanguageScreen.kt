package org.easydictionary.app.view.language

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import coil.ImageLoader
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import org.easydictionary.app.R
import org.easydictionary.app.domain.models.language.LangType
import org.easydictionary.app.domain.models.language.Language
import org.easydictionary.app.domain.models.navigation.AppNavigation
import org.easydictionary.app.domain.viewmodels.main.SharedMainViewModel
import org.easydictionary.app.domain.viewmodels.user.dictionary.add.languages.LanguagesViewModel
import org.easydictionary.app.view.dialogs.ErrorAlertDialog
import org.easydictionary.app.view.dividers.Divider
import org.easydictionary.app.view.topbars.SearchTopBar
import org.easydictionary.app.view.widget.global.getCurrentColorScheme

@Composable
fun SelectLanguageScreen(
    backStackEntry: NavBackStackEntry,
    navController: NavController,
    languagesViewModel: LanguagesViewModel = hiltViewModel(),
    sharedMainViewModel: SharedMainViewModel,
    langType: LangType,
    imageLoader: ImageLoader
) {
    val languages by languagesViewModel.filteredLanguages.collectAsState()
    var showErrorDialog by remember { mutableStateOf(false) }
    val loadingProgress by languagesViewModel.loadingDataUI.collectAsState()
    val errorMessage by languagesViewModel.errorUI.collectAsState()
    LaunchedEffect(errorMessage) {
        showErrorDialog = errorMessage.isNotEmpty()
    }
    LaunchedEffect(backStackEntry) {
        launch {
            backStackEntry.savedStateHandle.getStateFlow<String?>(
                BUNDLE_NEW_LANGUAGE, null
            ).filterNotNull().collect { newLanguage ->
                languagesViewModel.addNewLanguage(newLanguage)
            }
        }
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
    val onSelect: (Language) -> Unit = { item ->
        Log.d("SelectLanguageScreen", "Click on language ${item.name} for type ${langType.name}")
        when (langType) {
            LangType.FROM -> {
                navController.previousBackStackEntry
                    ?.savedStateHandle
                    ?.set(LanguagesViewModel.BUNDLE_SELECTED_LANGUAGE_FROM, item.toJson())
            }

            LangType.TO -> {
                navController.previousBackStackEntry
                    ?.savedStateHandle
                    ?.set(LanguagesViewModel.BUNDLE_SELECTED_LANGUAGE_TO, item.toJson())
            }
        }
        navController.popBackStack()
    }
    val toolBarTitle =
        if (langType == LangType.FROM) stringResource(R.string.select_language_from) else stringResource(
            R.string.select_language_to
        )
    LaunchedEffect(Unit) {
        languagesViewModel.loadLanguages()
    }
    var query by remember { mutableStateOf("") }
    var isSearching by remember { mutableStateOf(false) }
    LaunchedEffect(query) {
        languagesViewModel.searchQuery.value = query
    }
    Scaffold(
        topBar = {
            SearchTopBar(
                title = toolBarTitle,
                placeHolderText = stringResource(R.string.language_search_hint),
                query = query,
                onQueryChange = { query = it },
                isSearching = isSearching,
                onSearchToggle = { isSearching = true },
                onClearQuery = {
                    query = ""
                    isSearching = false
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                text = { Text(stringResource(R.string.or_add_your_own_language)) },
                icon = { Icon(Icons.Filled.Add, contentDescription = "Add") },
                onClick = {
                    navController.navigate(AppNavigation.AddNewLanguageScreen.route)
                }
            )
        },
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
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                items(
                    items = languages,
                    key = { it.code + it.id }
                ) { item ->
                    LanguageListItem(item, imageLoader, onSelect)
                }
            }
        }
    }
}

@Composable
private fun LanguageListItem(
    language: Language,
    imageLoader: ImageLoader,
    onSelect: (Language) -> Unit
) {
    val backgroundColor = getCurrentColorScheme().primaryContainer
    val context = LocalContext.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(backgroundColor)
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    onSelect(language)
                },
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically
        ) {
//            AsyncImage(
//                model = ImageRequest.Builder(context)
//                    .data(language.flags?.svg)
//                    .crossfade(true)
//                    .build(),
//                contentDescription = null,
//                imageLoader = imageLoader,
//                modifier = Modifier.size(64.dp)
//            )
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = language.name,
                    fontSize = 18.sp
                )
            }
        }
    }
    Divider()
}