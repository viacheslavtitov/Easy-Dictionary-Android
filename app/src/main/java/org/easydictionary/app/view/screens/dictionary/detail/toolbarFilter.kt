package org.easydictionary.app.view.screens.dictionary.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.easydictionary.app.R
import org.easydictionary.app.domain.models.category.CategoryDictionary
import org.easydictionary.app.domain.models.dictionary.WordTypeSelectableItem
import org.easydictionary.app.domain.models.word.WordTag
import org.easydictionary.app.view.dividers.Divider
import org.easydictionary.app.view.screens.word.TagChip
import org.easydictionary.app.view.texts.SecondaryTextFieldLabel

@Composable
fun FilterContent(
    tags: List<WordTag>,
    onTagClicked: (WordTag) -> Unit,
    categories: List<CategoryDictionary>,
    onCategoryClicked: (CategoryDictionary) -> Unit,
    types: List<WordTypeSelectableItem>,
    onTypeClicked: (WordTypeSelectableItem) -> Unit
) {
    Column(Modifier.fillMaxWidth().padding(vertical = 8.dp, horizontal = 16.dp)) {
        SecondaryTextFieldLabel(
            label = stringResource(R.string.tags),
            modifier = Modifier.fillMaxWidth()
        )
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(tags) { item ->
                TagChip(
                    tag = item,
                    onTagClicked = onTagClicked
                )
            }
        }
        Divider()
        SecondaryTextFieldLabel(
            label = stringResource(R.string.categories),
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
        )
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(categories) { item ->
                FilterChip(
                    onClick = { onCategoryClicked(item) },
                    label = { Text(item.name) },
                    selected = item.selected
                )
            }
        }
        Divider()
        SecondaryTextFieldLabel(
            label = stringResource(R.string.types),
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
        )
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(types) { item ->
                FilterChip(
                    onClick = { onTypeClicked(item) },
                    label = { Text(item.name) },
                    selected = item.selected
                )
            }
        }
        Divider()
        SecondaryTextFieldLabel(
            label = stringResource(R.string.history),
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
        )
    }
}