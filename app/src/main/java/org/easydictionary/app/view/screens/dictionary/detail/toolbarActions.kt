package org.easydictionary.app.view.screens.dictionary.detail

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable

@Composable
fun ToolBarActions(
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