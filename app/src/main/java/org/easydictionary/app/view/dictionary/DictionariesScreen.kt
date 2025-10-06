package org.easydictionary.app.view.dictionary

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import org.easydictionary.app.R
import org.easydictionary.app.domain.models.dictionary.DictionaryDetailShort
import org.easydictionary.app.domain.models.navigation.AppNavigation
import org.easydictionary.app.domain.viewmodels.main.SharedMainViewModel
import org.easydictionary.app.domain.viewmodels.user.dictionary.UserDictionaryViewModel
import org.easydictionary.app.view.dialogs.ButtonsAlertDialog
import org.easydictionary.app.view.dialogs.ErrorAlertDialog
import org.easydictionary.app.view.dividers.Divider
import org.easydictionary.app.view.swipe.SwipeRevealItem
import org.easydictionary.app.view.widget.global.getCurrentColorScheme


@Composable
fun DictionariesScreen(
    navController: NavController,
    viewModel: UserDictionaryViewModel = hiltViewModel(),
    sharedMainViewModel: SharedMainViewModel
) {
    LaunchedEffect(Unit) {
        viewModel.loadDictionariesDetailShort()
    }
    var showErrorDialog by remember { mutableStateOf(false) }
    val dictionaries by viewModel.dictionariesDetailShort.collectAsState()
    val loadingProgress by viewModel.loadingDataUI.collectAsState()
    val errorMessage by viewModel.errorUI.collectAsState()
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
    val openItemId = remember { mutableStateOf<Int?>(null) }
    val onEdit: (Int) -> Unit = { itemId ->
        dictionaries.find { itemId == it.id }?.let { dictionary ->
            Log.d("DictionariesScreen", "Click on edit $itemId")
            navController.navigate(AppNavigation.EditDictionaryScreen.createRoute(dictionary))
        }
    }
    val deleteItemId = remember { mutableStateOf<Int?>(null) }
    deleteItemId.value?.let { itemId ->
        dictionaries.find { itemId == it.id }?.let { dictionary ->
            Log.d("DictionariesScreen", "Click on delete $itemId")
            ButtonsAlertDialog(
                onConfirmation = {
                    viewModel.deleteDictionary(dictionary)
                },
                onDismissRequest = {
                    deleteItemId.value = null
                },
                message = stringResource(R.string.are_you_sure),
                title = stringResource(R.string.delete_dictionary_title),
                icon = Icons.Default.Info
            )
        }
    }
    val onDelete: (Int) -> Unit = { itemId ->
        deleteItemId.value = itemId
    }
    val onClick: (DictionaryDetailShort) -> Unit = { dictionary ->
        Log.d("DictionariesScreen", "Click on ${dictionary.id}")
        navController.navigate(AppNavigation.EditDictionaryScreen.createRoute(dictionary))
    }
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() },
                onClick = {
                    openItemId.value = null
                }
            )
    ) {
        items(
            items = dictionaries,
            key = { it.id }
        ) { item ->
            DictionaryListItem(item, openItemId, onClick, onEdit, onDelete)
        }
    }
}

@Composable
private fun DictionaryListItem(
    dictionary: DictionaryDetailShort,
    openItemId: MutableState<Int?>,
    onClick: (DictionaryDetailShort) -> Unit,
    onEdit: (Int) -> Unit,
    onDelete: (Int) -> Unit
) {
    val backgroundColor = getCurrentColorScheme().primaryContainer
    val dialect = if (dictionary.dialect?.isNotEmpty() == true) " (${dictionary.dialect})" else ""
    val titleText = "${dictionary.langFrom?.name} - ${dictionary.langTo?.name}$dialect"
    SwipeRevealItem(
        modifier = Modifier
            .fillMaxWidth(),
        menuWidth = 160.dp,
        content = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(backgroundColor)
                    .padding(16.dp)
                    .clickable{
                        onClick(dictionary)
                    },
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = titleText,
                    fontSize = 18.sp
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.words_count, dictionary.wordsCount),
                        fontSize = 14.sp
                    )
                    Text(
                        text = stringResource(R.string.quiz_count, dictionary.quizCount),
                        fontSize = 14.sp
                    )
                    Text(
                        text = stringResource(R.string.tags_count, dictionary.wordTagsCount),
                        fontSize = 14.sp
                    )
                }
            }
            Divider()
        },
        itemId = dictionary.id,
        isOpen = openItemId.value == dictionary.id,
        onCloseRequest = { openItemId.value = null },
        onOpen = { openItemId.value = dictionary.id },
        menuContent = {
            MenuDictionaryItem(openItemId, onEdit, onDelete)
        }
    )
}

@Composable
private fun MenuDictionaryItem(
    openItemId: MutableState<Int?>,
    onEdit: (Int) -> Unit,
    onDelete: (Int) -> Unit
) {
    val deleteRequest = remember { mutableStateOf<Int?>(null) }
    LaunchedEffect(deleteRequest.value) {
        deleteRequest.value?.let {
            onDelete(it)
            deleteRequest.value = null
        }
    }
    val backgroundColor = getCurrentColorScheme().tertiaryContainer
    val tintIcon = getCurrentColorScheme().primary
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .background(backgroundColor)
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier
                .width(70.dp)
                .fillMaxHeight()
                .clickable {
                    openItemId.value?.let {
                        onEdit(it)
                    }
                    openItemId.value = null
                },
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(Icons.Default.Edit, contentDescription = "Edit", tint = tintIcon)
            Text(
                modifier = Modifier.padding(top = 9.dp),
                text = stringResource(R.string.edit),
                fontSize = 14.sp
            )
        }
        Column(
            modifier = Modifier
                .width(70.dp)
                .fillMaxHeight()
                .clickable {
                    openItemId.value?.let {
                        deleteRequest.value = it
                    }
                    openItemId.value = null
                },
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = tintIcon)
            Text(
                modifier = Modifier.padding(top = 9.dp),
                text = stringResource(R.string.delete),
                fontSize = 14.sp
            )
        }
    }
}