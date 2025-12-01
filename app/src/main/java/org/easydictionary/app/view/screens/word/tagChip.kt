package org.easydictionary.app.view.screens.word

import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import org.easydictionary.app.domain.models.word.WordTag

@Composable
fun TagChip(
    tag: WordTag,
    onTagClicked: (WordTag) -> Unit = {}
) {
    FilterChip(
        onClick = { onTagClicked(tag) },
        label = { Text(tag.name) },
        selected = tag.selected
    )
}